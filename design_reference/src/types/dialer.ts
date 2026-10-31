import { CallStage, CampaignContact, ContactCallStatus } from './campaign';

export type SessionStatus =
  | 'idle'
  | 'ready'
  | 'starting'
  | 'running'
  | 'calling'
  | 'waiting'
  | 'awaiting_result'
  | 'paused'
  | 'finished'
  | 'stopped'
  | 'error';

export type CallPhase = 'idle' | 'dialing' | 'ringing' | 'connected';

export type CallOutcome = 'answered' | 'no_answer' | 'failed' | 'unverified' | 'skipped';

export interface CallLogRecord {
  id: string;
  contactId: string;
  name: string;
  phone: string;
  outcome: CallOutcome;
  durationSeconds: number;
  timestamp: string;
  stage?: CallStage;
  feedback?: string;
}

export interface SessionConfig {
  delayBetweenCalls: number;
  retryAttempts: number;
  audioRoute: 'speaker' | 'headset' | 'earpiece';
  randomizeDelay: boolean;
  maxCallDurationSeconds: number;
}

export interface LiveSessionState {
  campaignId: string;
  status: SessionStatus;
  callPhase: CallPhase;
  currentContact: CampaignContact | null;
  currentContactIndex: number;
  callDurationSeconds: number;
  delayCountdownSeconds: number;
  startCountdownSeconds: number;
  currentRound: number;
  totalRounds: number;
  awaitingStageContact: CampaignContact | null;
  outcomes: {
    answered: number;
    noAnswer: number;
    failed: number;
    unverified: number;
    skipped: number;
  };
  history: CallLogRecord[];
  errorMessage?: string;
}
