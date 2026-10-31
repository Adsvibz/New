import {
  AppThemeColor,
  Campaign,
  CampaignContact,
  CampaignFilterOptions,
  CampaignOverviewStats,
  CampaignProgress,
} from '../types/campaign';

export const APP_THEME_COLORS: AppThemeColor[] = [
  {
    id: 'sunset',
    hex: '#E2532B',
    rgb: '226, 83, 43',
    nameEn: 'Sunset Orange',
    nameAr: 'برتقالي غروب',
  },
  {
    id: 'royal',
    hex: '#2563EB',
    rgb: '37, 99, 235',
    nameEn: 'Royal Blue',
    nameAr: 'أزرق ملكي',
  },
  {
    id: 'emerald',
    hex: '#059669',
    rgb: '5, 150, 105',
    nameEn: 'Emerald',
    nameAr: 'أخضر زمردي',
  },
  {
    id: 'violet',
    hex: '#7C3AED',
    rgb: '124, 58, 237',
    nameEn: 'Violet',
    nameAr: 'بنفسجي',
  },
  {
    id: 'crimson',
    hex: '#E11D48',
    rgb: '225, 29, 72',
    nameEn: 'Crimson Rose',
    nameAr: 'وردي قرمزي',
  },
  {
    id: 'teal',
    hex: '#0D9488',
    rgb: '13, 148, 136',
    nameEn: 'Teal Ocean',
    nameAr: 'تركواز محيطي',
  },
  {
    id: 'indigo',
    hex: '#4F46E5',
    rgb: '79, 70, 229',
    nameEn: 'Indigo',
    nameAr: 'نيلي',
  },
  {
    id: 'amber',
    hex: '#D97706',
    rgb: '217, 119, 6',
    nameEn: 'Amber Gold',
    nameAr: 'كهرماني ذهبي',
  },
  {
    id: 'fuchsia',
    hex: '#C026D3',
    rgb: '192, 38, 211',
    nameEn: 'Fuchsia',
    nameAr: 'فوشيا',
  },
  {
    id: 'slate',
    hex: '#334155',
    rgb: '51, 65, 85',
    nameEn: 'Nordic Slate',
    nameAr: 'رمادي صخري',
  },
];

export function getThemeColorById(colorId?: string): AppThemeColor {
  if (!colorId) return APP_THEME_COLORS[0];
  return (
    APP_THEME_COLORS.find((c) => c.id === colorId || c.hex.toLowerCase() === colorId.toLowerCase()) ||
    APP_THEME_COLORS[0]
  );
}

/**
 * Computes exact CampaignProgress from a contact list
 */
export function computeCampaignProgress(contacts: CampaignContact[]): CampaignProgress {
  const total = contacts.length;
  let answered = 0;
  let noAnswer = 0;
  let failed = 0;
  let unverified = 0;
  let remaining = 0;

  for (const c of contacts) {
    if (c.status === 'answered') answered++;
    else if (c.status === 'no_answer') noAnswer++;
    else if (c.status === 'failed') failed++;
    else if (c.status === 'unverified') unverified++;
    else remaining++; // pending or calling
  }

  const called = answered + noAnswer + failed + unverified;
  const percentage = total > 0 ? Math.round((called / total) * 100) : 0;

  return {
    total,
    called,
    answered,
    noAnswer,
    failed,
    unverified,
    remaining,
    percentage,
  };
}

/**
 * Synchronizes a campaign's summary fields from its contacts array
 */
export function syncCampaignFromContacts(campaign: Campaign): Campaign {
  if (!campaign.contacts || campaign.contacts.length === 0) {
    return {
      ...campaign,
      totalContacts: 0,
      completedCalls: 0,
      pendingCalls: 0,
      answeredCalls: 0,
      noAnswerCalls: 0,
      failedCalls: 0,
      unverifiedCalls: 0,
      progressPercentage: 0,
      duplicateContactsFound: 0,
      status: campaign.status === 'running' ? 'running' : 'ready',
    };
  }

  const prog = computeCampaignProgress(campaign.contacts);
  const seen = new Set<string>();
  let dupes = 0;
  for (const c of campaign.contacts) {
    const norm = c.phone.replace(/[\s\-\(\)\.]/g, '');
    if (seen.has(norm)) dupes++;
    else seen.add(norm);
  }

  let nextStatus = campaign.status;
  if (campaign.status !== 'running') {
    if (prog.total > 0 && prog.remaining === 0) {
      nextStatus = 'completed';
    } else if (prog.called === 0) {
      nextStatus = 'ready';
    } else if (campaign.status === 'completed' || campaign.status === 'ready') {
      nextStatus = 'active';
    }
  }

  return {
    ...campaign,
    totalContacts: prog.total,
    completedCalls: prog.called,
    pendingCalls: prog.remaining,
    answeredCalls: prog.answered,
    noAnswerCalls: prog.noAnswer,
    failedCalls: prog.failed,
    unverifiedCalls: prog.unverified,
    progressPercentage: prog.percentage,
    duplicateContactsFound: dupes,
    status: nextStatus,
  };
}

/**
 * Calculates aggregated overview metrics for the campaign dashboard
 */
export function calculateOverviewStats(campaigns: Campaign[]): CampaignOverviewStats {
  const totalCampaigns = campaigns.length;
  let totalContacts = 0;
  let completedCalls = 0;
  let pendingCalls = 0;
  let readyCount = 0;
  let activeCount = 0;
  let completedCount = 0;
  let pausedCount = 0;
  let runningCount = 0;

  for (const c of campaigns) {
    totalContacts += c.totalContacts;
    completedCalls += c.completedCalls;
    pendingCalls += c.pendingCalls;

    if (c.status === 'ready') readyCount++;
    else if (c.status === 'active') activeCount++;
    else if (c.status === 'completed') completedCount++;
    else if (c.status === 'paused') pausedCount++;
    else if (c.status === 'running') runningCount++;
  }

  const overallProgress =
    totalContacts > 0 ? Math.round((completedCalls / totalContacts) * 100) : 0;

  return {
    totalCampaigns,
    totalContacts,
    completedCalls,
    pendingCalls,
    overallProgress,
    readyCount,
    activeCount,
    completedCount,
    pausedCount,
    runningCount,
  };
}

/**
 * Filters and sorts campaigns according to query, status, and sorting fields
 */
export function filterAndSortCampaigns(
  campaigns: Campaign[],
  options: CampaignFilterOptions
): Campaign[] {
  const query = options.query.trim().toLowerCase();

  const filtered = campaigns.filter((c) => {
    if (options.status !== 'all' && c.status !== options.status) {
      return false;
    }

    if (query) {
      const matchName = c.name.toLowerCase().includes(query);
      const matchDesc = c.description?.toLowerCase().includes(query) ?? false;
      const matchTag = c.tags.some((t) => t.toLowerCase().includes(query));
      if (!matchName && !matchDesc && !matchTag) {
        return false;
      }
    }

    return true;
  });

  return filtered.sort((a, b) => {
    let comparison = 0;
    switch (options.sortBy) {
      case 'name':
        comparison = a.name.localeCompare(b.name);
        break;
      case 'progress':
        comparison = a.progressPercentage - b.progressPercentage;
        break;
      case 'contacts':
        comparison = a.totalContacts - b.totalContacts;
        break;
      case 'createdAt':
        comparison = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
        break;
      case 'lastActivity':
      default:
        comparison = new Date(a.lastActivity).getTime() - new Date(b.lastActivity).getTime();
        break;
    }
    return options.sortOrder === 'asc' ? comparison : -comparison;
  });
}

/**
 * Removes duplicate phone numbers in a campaign and recalculates metrics
 */
export function removeDuplicateContacts(campaign: Campaign): {
  updatedCampaign: Campaign;
  removedCount: number;
} {
  const seenPhones = new Set<string>();
  const uniqueContacts: CampaignContact[] = [];
  let duplicates = 0;

  for (const contact of campaign.contacts) {
    const normalizedPhone = contact.phone.replace(/[\s\-\(\)\.]/g, '');
    if (seenPhones.has(normalizedPhone)) {
      duplicates++;
    } else {
      seenPhones.add(normalizedPhone);
      uniqueContacts.push(contact);
    }
  }

  const synced = syncCampaignFromContacts({
    ...campaign,
    contacts: uniqueContacts,
    lastActivity: new Date().toISOString(),
  });

  return { updatedCampaign: synced, removedCount: duplicates };
}

/**
 * Resets progress of a campaign to 0% and marks all contacts as pending
 */
export function resetCampaignProgress(campaign: Campaign): Campaign {
  const resetContacts: CampaignContact[] = campaign.contacts.map((c) => ({
    ...c,
    status: 'pending' as const,
    attempts: 0,
    callDurationSeconds: 0,
    calledAt: null,
    lastAttempt: undefined,
  }));

  return {
    ...campaign,
    contacts: resetContacts,
    completedCalls: 0,
    pendingCalls: resetContacts.length,
    answeredCalls: 0,
    noAnswerCalls: 0,
    failedCalls: 0,
    unverifiedCalls: 0,
    progressPercentage: 0,
    status: 'ready',
    lastActivity: new Date().toISOString(),
  };
}

/**
 * Parses multiline pasted numbers or CSV lines:
 * Supports formats like:
 *   Sarah Mitchell, +12025550142
 *   +201001234567
 */
export function parseBulkContactLines(
  rawText: string,
  defaultCountryCode = '1'
): {
  validContacts: Array<{ name: string; phone: string }>;
  invalidLines: string[];
} {
  const lines = rawText
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter(Boolean);

  const validContacts: Array<{ name: string; phone: string }> = [];
  const invalidLines: string[] = [];

  for (const line of lines) {
    // Skip CSV header row if present
    if (/^(name|contact|full\s*name)\s*[,;\t]/i.test(line)) {
      continue;
    }

    let name = '';
    let rawPhone = line;

    if (line.includes(',') || line.includes(';') || line.includes('\t')) {
      const parts = line.split(/[,;\t]/).map((p) => p.trim());
      if (parts.length >= 2) {
        // Check which part looks like a phone number
        const firstDigits = parts[0].replace(/\D/g, '');
        const secondDigits = parts[1].replace(/\D/g, '');
        if (secondDigits.length >= 7) {
          name = parts[0];
          rawPhone = parts[1];
        } else if (firstDigits.length >= 7) {
          rawPhone = parts[0];
          name = parts[1];
        } else {
          invalidLines.push(line);
          continue;
        }
      }
    }

    const digitsOnly = rawPhone.replace(/[^\d+]/g, '');
    const numericCount = digitsOnly.replace(/\D/g, '').length;

    if (numericCount < 7 || numericCount > 15) {
      invalidLines.push(line);
      continue;
    }

    let normalizedPhone = rawPhone.trim();
    if (!normalizedPhone.startsWith('+') && defaultCountryCode) {
      const cleanLocal = digitsOnly.replace(/^0+/, '');
      normalizedPhone = `+${defaultCountryCode.replace(/^\+/, '')}${cleanLocal}`;
    }

    validContacts.push({
      name: name || normalizedPhone,
      phone: normalizedPhone,
    });
  }

  return { validContacts, invalidLines };
}

export function formatSimLabel(simSlot: number, lang: 'en' | 'ar' = 'en'): string {
  if (simSlot === 0) return lang === 'ar' ? 'شريحة SIM 1' : 'SIM 1';
  if (simSlot === 1) return lang === 'ar' ? 'شريحة SIM 2' : 'SIM 2';
  return lang === 'ar' ? 'الشريحة الافتراضية' : 'Default SIM';
}

/**
 * Human-friendly relative time formatter
 */
export function formatTimeAgo(isoOrTimestamp: string | number | null | undefined): string {
  if (!isoOrTimestamp) return 'No calls yet';
  try {
    const date = new Date(isoOrTimestamp);
    const now = new Date();
    const diffSec = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffSec < 60) return 'Just now';
    const diffMin = Math.floor(diffSec / 60);
    if (diffMin < 60) return `${diffMin}m ago`;
    const diffHours = Math.floor(diffMin / 60);
    if (diffHours < 24) return `${diffHours}h ago`;
    const diffDays = Math.floor(diffHours / 24);
    if (diffDays < 7) return `${diffDays}d ago`;

    return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  } catch {
    return 'Recent';
  }
}

/**
 * Format duration in mm:ss or Xm Ys
 */
export function formatDuration(seconds: number): string {
  if (!seconds || seconds <= 0) return '0s';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  if (m === 0) return `${s}s`;
  if (m < 60) return `${m}m ${s > 0 ? `${s}s` : ''}`.trim();
  const h = Math.floor(m / 60);
  const remM = m % 60;
  return `${h}h ${remM}m`;
}
