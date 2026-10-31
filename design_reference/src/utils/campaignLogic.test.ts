import test from 'node:test';
import assert from 'node:assert/strict';
import {
  APP_THEME_COLORS,
  getThemeColorById,
  calculateOverviewStats,
  filterAndSortCampaigns,
  removeDuplicateContacts,
  resetCampaignProgress,
  formatDuration,
  parseBulkContactLines,
  computeCampaignProgress,
} from './campaignLogic';
import { Campaign } from '../types/campaign';

const sampleCampaigns: Campaign[] = [
  {
    id: 'camp-1',
    name: 'Enterprise VIP Outbound Q4',
    simSlot: 0,
    status: 'active',
    totalContacts: 3,
    completedCalls: 1,
    pendingCalls: 2,
    answeredCalls: 1,
    noAnswerCalls: 0,
    failedCalls: 0,
    unverifiedCalls: 0,
    progressPercentage: 33,
    createdAt: '2026-10-01T08:00:00.000Z',
    lastActivity: '2026-10-28T09:00:00.000Z',
    delayBetweenCalls: 5,
    duplicateContactsFound: 1,
    tags: ['vip', 'enterprise'],
    contacts: [
      {
        id: '1',
        name: 'John Doe',
        phone: '+15551234567',
        status: 'answered',
        attempts: 1,
        callDurationSeconds: 120,
        calledAt: 1793205000000,
        stage: 'potential',
        notes: 'Interested in Q4 package',
      },
      {
        id: '2',
        name: 'Jane Smith',
        phone: '+15559876543',
        status: 'pending',
        attempts: 0,
        callDurationSeconds: 0,
        calledAt: null,
        stage: 'none',
        notes: '',
      },
      {
        id: '3',
        name: 'John Duplicate',
        phone: '+1 555-123-4567',
        status: 'pending',
        attempts: 0,
        callDurationSeconds: 0,
        calledAt: null,
        stage: 'none',
        notes: '',
      },
    ],
  },
  {
    id: 'camp-2',
    name: 'Healthcare Follow-up Wave 2',
    simSlot: 1,
    status: 'completed',
    totalContacts: 50,
    completedCalls: 50,
    pendingCalls: 0,
    answeredCalls: 42,
    noAnswerCalls: 5,
    failedCalls: 3,
    unverifiedCalls: 0,
    progressPercentage: 100,
    createdAt: '2026-10-10T10:00:00.000Z',
    lastActivity: '2026-10-25T14:30:00.000Z',
    delayBetweenCalls: 3,
    duplicateContactsFound: 0,
    tags: ['healthcare', 'followup'],
    contacts: [],
  },
  {
    id: 'camp-3',
    name: 'SaaS Renewal Reminders',
    simSlot: -1,
    status: 'paused',
    totalContacts: 80,
    completedCalls: 20,
    pendingCalls: 60,
    answeredCalls: 15,
    noAnswerCalls: 3,
    failedCalls: 2,
    unverifiedCalls: 0,
    progressPercentage: 25,
    createdAt: '2026-10-15T12:00:00.000Z',
    lastActivity: '2026-10-20T11:00:00.000Z',
    delayBetweenCalls: 10,
    duplicateContactsFound: 0,
    tags: ['renewals'],
    contacts: [],
  },
];

test('calculateOverviewStats correctly aggregates total metrics', () => {
  const stats = calculateOverviewStats(sampleCampaigns);
  assert.equal(stats.totalCampaigns, 3);
  assert.equal(stats.totalContacts, 133);
  assert.equal(stats.completedCalls, 71);
  assert.equal(stats.pendingCalls, 62);
  assert.equal(stats.activeCount, 1);
  assert.equal(stats.completedCount, 1);
  assert.equal(stats.pausedCount, 1);
});

test('filterAndSortCampaigns filters by search query and status', () => {
  const queryResult = filterAndSortCampaigns(sampleCampaigns, {
    query: 'health',
    status: 'all',
    sortBy: 'lastActivity',
    sortOrder: 'desc',
  });
  assert.equal(queryResult.length, 1);
  assert.equal(queryResult[0].id, 'camp-2');

  const activeResult = filterAndSortCampaigns(sampleCampaigns, {
    query: '',
    status: 'active',
    sortBy: 'lastActivity',
    sortOrder: 'desc',
  });
  assert.equal(activeResult.length, 1);
  assert.equal(activeResult[0].id, 'camp-1');
});

test('removeDuplicateContacts detects and removes duplicate phone numbers', () => {
  const { updatedCampaign, removedCount } = removeDuplicateContacts(sampleCampaigns[0]);
  assert.equal(removedCount, 1);
  assert.equal(updatedCampaign.contacts.length, 2);
  assert.equal(updatedCampaign.totalContacts, 2);
  assert.equal(updatedCampaign.duplicateContactsFound, 0);
});

test('resetCampaignProgress resets completed calls and marks contacts pending', () => {
  const reset = resetCampaignProgress(sampleCampaigns[0]);
  assert.equal(reset.completedCalls, 0);
  assert.equal(reset.progressPercentage, 0);
  assert.equal(reset.status, 'ready');
  assert.equal(reset.contacts.every((c) => c.status === 'pending'), true);
});

test('parseBulkContactLines parses valid name+number lines and catches invalid lines', () => {
  const raw = `
    Sarah Mitchell, +12025550142
    James Wilson, +12025550168
    +201001234567
    bad_line_123
  `;
  const result = parseBulkContactLines(raw, '20');
  assert.equal(result.validContacts.length, 3);
  assert.equal(result.validContacts[0].name, 'Sarah Mitchell');
  assert.equal(result.validContacts[0].phone, '+12025550142');
  assert.equal(result.invalidLines.length, 1);
});

test('computeCampaignProgress derives exact counts', () => {
  const prog = computeCampaignProgress(sampleCampaigns[0].contacts);
  assert.equal(prog.total, 3);
  assert.equal(prog.called, 1);
  assert.equal(prog.answered, 1);
  assert.equal(prog.remaining, 2);
  assert.equal(prog.percentage, 33);
});

test('formatDuration formats seconds cleanly', () => {
  assert.equal(formatDuration(45), '45s');
  assert.equal(formatDuration(90), '1m 30s');
  assert.equal(formatDuration(3600), '1h 0m');
});

test('APP_THEME_COLORS provides 10 distinct colors and getThemeColorById resolves accurately', () => {
  assert.equal(APP_THEME_COLORS.length, 10);
  const uniqueIds = new Set(APP_THEME_COLORS.map((c) => c.id));
  const uniqueHex = new Set(APP_THEME_COLORS.map((c) => c.hex));
  assert.equal(uniqueIds.size, 10);
  assert.equal(uniqueHex.size, 10);

  assert.equal(getThemeColorById('royal').hex, '#2563EB');
  assert.equal(getThemeColorById('emerald').hex, '#059669');
  assert.equal(getThemeColorById('unknown_color').hex, '#E2532B');
});
