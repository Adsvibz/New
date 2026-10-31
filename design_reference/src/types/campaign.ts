export type CampaignStatus = 'ready' | 'active' | 'paused' | 'completed' | 'running';

export type ContactCallStatus =
  | 'pending'
  | 'calling'
  | 'answered'
  | 'no_answer'
  | 'failed'
  | 'unverified';

export type CallStage =
  | 'none'
  | 'potential'
  | 'not_interested'
  | 'call_back'
  | 'whatsapp'
  | 'voicemail';

export interface CampaignContact {
  id: string;
  name: string;
  phone: string; // Display / normalized number
  company?: string;
  status: ContactCallStatus;
  attempts: number;
  callDurationSeconds: number;
  calledAt: number | null; // Unix timestamp (ms)
  lastAttempt?: string;
  stage: CallStage;
  notes: string; // feedback
}

export interface CampaignProgress {
  total: number;
  called: number;
  answered: number;
  noAnswer: number;
  failed: number;
  unverified: number;
  remaining: number;
  percentage: number;
}

export interface Campaign {
  id: string;
  name: string;
  description?: string;
  simSlot: number; // -1 = Default SIM, 0 = SIM 1, 1 = SIM 2
  status: CampaignStatus;
  totalContacts: number;
  completedCalls: number;
  pendingCalls: number;
  answeredCalls: number;
  noAnswerCalls: number;
  failedCalls: number;
  unverifiedCalls: number;
  progressPercentage: number;
  createdAt: string;
  lastActivity: string;
  delayBetweenCalls: number;
  duplicateContactsFound: number;
  callerId?: string;
  contacts: CampaignContact[];
  tags: string[];
}

export interface CallHistoryItem {
  id: string;
  time: number; // Unix timestamp ms
  campaignId: string;
  campaignName: string;
  contactId: string;
  contactName: string;
  phoneNumber: string;
  status: ContactCallStatus;
  durationSeconds: number;
  attemptNumber: number;
  stage?: CallStage;
  feedback?: string;
}

export interface AppThemeColor {
  id: string;
  hex: string;
  rgb: string;
  nameEn: string;
  nameAr: string;
}

export interface AppSettings {
  // 11.1 General
  appearance: 'system' | 'light' | 'dark';
  dynamicColors: boolean;
  appColor: string;
  // 11.2 Calling Timing
  delayBetweenCalls: number; // 0-120s
  randomDelay: boolean;
  maxRandomDelay: number; // 1-300s
  countdownBeforeStart: number; // 0-30s
  endCallAfterSeconds: number; // 0-600s (0 disables)
  // 11.3 Campaign Behavior
  retryUnanswered: number; // 0-5
  campaignRounds: number; // 1-20
  shuffleContacts: boolean;
  skipPreviouslyAnswered: boolean;
  detectAnswersFromCallLog: boolean;
  // 11.4 Audio During Calls
  autoSpeakerAndMute: boolean;
  audioRoute: 'speaker' | 'earpiece' | 'headset';
  // 11.5 Call Results & Stages
  askForCallResult: boolean;
  waitForStageSeconds: number; // 0-60s
  // 11.6 Phone Numbers
  defaultCountryCode: string; // e.g. "20" or "1"
  // 11.7 Allowed Calling Hours
  restrictCallingHours: boolean;
  allowedHourFrom: number; // 0-23
  allowedHourTo: number; // 0-23
  // 11.8 Screen & Notifications
  keepScreenOn: boolean;
  vibrateOnCampaignEnd: boolean;
  // 11.9 Permissions (Simulated OS state for prototype)
  permissions: {
    calling: boolean;
    phoneState: boolean;
    callLog: boolean;
    endingCalls: boolean;
    phoneContacts: boolean;
  };
  // 11.10 Licensing
  licensingEnabled: boolean;
  licenseStatus: 'not_activated' | 'checking' | 'activated' | 'suspended' | 'error';
  activationCode: string;
}

export type SortField = 'lastActivity' | 'name' | 'progress' | 'contacts' | 'createdAt';
export type SortOrder = 'asc' | 'desc';

export interface CampaignFilterOptions {
  query: string;
  status: CampaignStatus | 'all';
  sortBy: SortField;
  sortOrder: SortOrder;
}

export interface CampaignOverviewStats {
  totalCampaigns: number;
  totalContacts: number;
  completedCalls: number;
  pendingCalls: number;
  overallProgress: number;
  readyCount: number;
  activeCount: number;
  completedCount: number;
  pausedCount: number;
  runningCount: number;
}
