import test from 'node:test';
import assert from 'node:assert/strict';
import {
  formatCallTimer,
  getQueuedContacts,
  getNextPendingContact,
  applyCallOutcomeToCampaign,
  removeContactFromCampaign,
  updateContactStageAndNotes,
} from './dialerLogic';
import { Campaign } from '../types/campaign';

const sampleCampaign: Campaign = {
  id: 'c-test',
  name: 'Test Campaign',
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
  createdAt: '2026-10-01T00:00:00.000Z',
  lastActivity: '2026-10-01T00:00:00.000Z',
  delayBetweenCalls: 4,
  duplicateContactsFound: 0,
  tags: ['test'],
  contacts: [
    {
      id: '1',
      name: 'Alice Smith',
      phone: '+15551112222',
      status: 'answered',
      attempts: 1,
      callDurationSeconds: 65,
      calledAt: 1793200000000,
      stage: 'potential',
      notes: 'Follow up',
    },
    {
      id: '2',
      name: 'Bob Jones',
      phone: '+15553334444',
      status: 'pending',
      attempts: 0,
      callDurationSeconds: 0,
      calledAt: null,
      stage: 'none',
      notes: '',
    },
    {
      id: '3',
      name: 'Carol White',
      phone: '+15555556666',
      status: 'pending',
      attempts: 0,
      callDurationSeconds: 0,
      calledAt: null,
      stage: 'none',
      notes: '',
    },
  ],
};

test('formatCallTimer formats seconds into mm:ss', () => {
  assert.equal(formatCallTimer(0), '00:00');
  assert.equal(formatCallTimer(5), '00:05');
  assert.equal(formatCallTimer(42), '00:42');
  assert.equal(formatCallTimer(60), '01:00');
  assert.equal(formatCallTimer(125), '02:05');
});

test('getQueuedContacts returns pending contacts excluding current caller', () => {
  const queued = getQueuedContacts(sampleCampaign.contacts, '2');
  assert.equal(queued.length, 1);
  assert.equal(queued[0].id, '3');
});

test('getNextPendingContact returns the first pending contact', () => {
  const next = getNextPendingContact(sampleCampaign.contacts);
  assert.ok(next);
  assert.equal(next?.id, '2');

  const afterBob = getNextPendingContact(sampleCampaign.contacts, '2');
  assert.ok(afterBob);
  assert.equal(afterBob?.id, '3');
});

test('applyCallOutcomeToCampaign marks contact answered and recalculates campaign counts', () => {
  const updated = applyCallOutcomeToCampaign(
    sampleCampaign,
    '2',
    'answered',
    90,
    'call_back',
    'Call back on Thursday'
  );
  assert.equal(updated.completedCalls, 2);
  assert.equal(updated.pendingCalls, 1);
  assert.equal(updated.answeredCalls, 2);
  assert.equal(updated.progressPercentage, 67);
  const bob = updated.contacts.find((c) => c.id === '2');
  assert.equal(bob?.status, 'answered');
  assert.equal(bob?.callDurationSeconds, 90);
  assert.equal(bob?.stage, 'call_back');
  assert.equal(bob?.notes, 'Call back on Thursday');
});

test('updateContactStageAndNotes updates stage and feedback', () => {
  const updated = updateContactStageAndNotes(sampleCampaign, '1', 'whatsapp', 'Sent brochure on WhatsApp');
  const alice = updated.contacts.find((c) => c.id === '1');
  assert.equal(alice?.stage, 'whatsapp');
  assert.equal(alice?.notes, 'Sent brochure on WhatsApp');
});

test('removeContactFromCampaign removes contact from queue and updates total', () => {
  const updated = removeContactFromCampaign(sampleCampaign, '3');
  assert.equal(updated.contacts.length, 2);
  assert.equal(updated.totalContacts, 2);
  assert.equal(updated.pendingCalls, 1);
});
