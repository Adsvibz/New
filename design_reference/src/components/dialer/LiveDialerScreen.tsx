import React, { useState, useEffect, useRef } from 'react';
import { Campaign, CampaignContact, AppSettings, CallStage } from '../../types/campaign';
import {
  CallOutcome,
  CallPhase,
  SessionConfig,
  SessionStatus,
} from '../../types/dialer';
import { Translations, Language } from '../../i18n/translations';
import {
  applyCallOutcomeToCampaign,
  getNextPendingContact,
  getQueuedContacts,
  removeContactFromCampaign,
  updateContactStageAndNotes,
} from '../../utils/dialerLogic';
import { resetCampaignProgress } from '../../utils/campaignLogic';
import { DialerHeader } from './DialerHeader';
import { ActiveCallingHero } from './ActiveCallingHero';
import { DialerProgress } from './DialerProgress';
import { CallingQueue } from './CallingQueue';
import { SessionConfigStrip } from './SessionConfigStrip';
import { CampaignPickerSheet } from './CampaignPickerSheet';
import { StopConfirmModal } from './StopConfirmModal';
import { DialerSettingsModal } from './DialerSettingsModal';
import { StageClassificationSheet } from '../StageClassificationSheet';

interface LiveDialerScreenProps {
  campaigns: Campaign[];
  activeCampaignId: string | null;
  settings: AppSettings;
  onUpdateSettings: (next: AppSettings) => void;
  onSelectCampaign: (campaign: Campaign) => void;
  onUpdateCampaign: (updatedCampaign: Campaign) => void;
  onOpenCampaignDetails: (campaign: Campaign) => void;
  onOpenResultsForCampaign: (campaign: Campaign) => void;
  onSessionRunningChange?: (isRunning: boolean, callingContactId: string | null) => void;
  currentLanguage: Language;
  onToggleLanguage: () => void;
  onOpenWhatsApp: (phone: string, name: string) => void;
  onTriggerToast: (type: 'success' | 'info' | 'error', message: string) => void;
  t: Translations;
}

export const LiveDialerScreen: React.FC<LiveDialerScreenProps> = ({
  campaigns,
  activeCampaignId,
  settings,
  onUpdateSettings,
  onSelectCampaign,
  onUpdateCampaign,
  onOpenCampaignDetails,
  onOpenResultsForCampaign,
  onSessionRunningChange,
  currentLanguage,
  onToggleLanguage,
  onOpenWhatsApp,
  onTriggerToast,
  t,
}) => {
  const selectedCampaign =
    campaigns.find((c) => c.id === activeCampaignId) ||
    (campaigns.length > 0 ? campaigns[0] : null);

  const [sessionStatus, setSessionStatus] = useState<SessionStatus>('ready');
  const [callPhase, setCallPhase] = useState<CallPhase>('idle');
  const [currentContact, setCurrentContact] = useState<CampaignContact | null>(null);
  const [callDuration, setCallDuration] = useState<number>(0);
  const [delayCountdown, setDelayCountdown] = useState<number>(settings.delayBetweenCalls);
  const [startCountdown, setStartCountdown] = useState<number>(settings.countdownBeforeStart);

  // Stage classification modal state
  const [isStageSheetOpen, setIsStageSheetOpen] = useState(false);

  // Modals state
  const [isCampaignPickerOpen, setIsCampaignPickerOpen] = useState(false);
  const [isStopConfirmOpen, setIsStopConfirmOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  const [sessionConfig, setSessionConfig] = useState<SessionConfig>({
    delayBetweenCalls: settings.delayBetweenCalls,
    retryAttempts: settings.retryUnanswered,
    audioRoute: settings.audioRoute,
    randomizeDelay: settings.randomDelay,
    maxCallDurationSeconds: settings.endCallAfterSeconds || 180,
  });

  const callTimerRef = useRef<NodeJS.Timeout | null>(null);
  const countdownTimerRef = useRef<NodeJS.Timeout | null>(null);
  const startTimerRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    setSessionConfig((prev) => ({
      ...prev,
      delayBetweenCalls: settings.delayBetweenCalls,
      retryAttempts: settings.retryUnanswered,
      audioRoute: settings.audioRoute,
      randomizeDelay: settings.randomDelay,
    }));
  }, [settings]);

  // Notify parent when session running state changes
  useEffect(() => {
    const active =
      sessionStatus === 'starting' ||
      sessionStatus === 'running' ||
      sessionStatus === 'calling' ||
      sessionStatus === 'waiting' ||
      sessionStatus === 'awaiting_result';
    onSessionRunningChange?.(active, currentContact?.id ?? null);
  }, [sessionStatus, currentContact?.id]);

  const queuedContacts = selectedCampaign
    ? getQueuedContacts(selectedCampaign.contacts, currentContact?.id)
    : [];
  const nextContact = queuedContacts.length > 0 ? queuedContacts[0] : null;

  useEffect(() => {
    return () => {
      if (callTimerRef.current) clearInterval(callTimerRef.current);
      if (countdownTimerRef.current) clearInterval(countdownTimerRef.current);
      if (startTimerRef.current) clearInterval(startTimerRef.current);
    };
  }, []);

  // Pre-start countdown timer (Part 7.2 & 7.4)
  useEffect(() => {
    if (sessionStatus === 'starting') {
      startTimerRef.current = setInterval(() => {
        setStartCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(startTimerRef.current!);
            beginFirstOrNextCall();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } else if (startTimerRef.current) {
      clearInterval(startTimerRef.current);
      startTimerRef.current = null;
    }
    return () => {
      if (startTimerRef.current) clearInterval(startTimerRef.current);
    };
  }, [sessionStatus, selectedCampaign]);

  // Live call duration timer
  useEffect(() => {
    if (sessionStatus === 'running' || sessionStatus === 'calling') {
      callTimerRef.current = setInterval(() => {
        setCallDuration((prev) => {
          const next = prev + 1;
          if (next === 2) setCallPhase('ringing');
          if (next === 4) setCallPhase('connected');
          const limit =
            settings.endCallAfterSeconds > 0 ? settings.endCallAfterSeconds : 35;
          if (next >= limit) {
            handleCompleteCurrentCall(
              settings.detectAnswersFromCallLog && settings.permissions.callLog
                ? 'answered'
                : 'unverified',
              limit
            );
          }
          return next;
        });
      }, 1000);
    } else if (callTimerRef.current) {
      clearInterval(callTimerRef.current);
      callTimerRef.current = null;
    }
    return () => {
      if (callTimerRef.current) clearInterval(callTimerRef.current);
    };
  }, [sessionStatus, currentContact?.id, selectedCampaign]);

  // Waiting between calls countdown timer
  useEffect(() => {
    if (sessionStatus === 'waiting') {
      countdownTimerRef.current = setInterval(() => {
        setDelayCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(countdownTimerRef.current!);
            beginFirstOrNextCall(currentContact?.id);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } else if (countdownTimerRef.current) {
      clearInterval(countdownTimerRef.current);
      countdownTimerRef.current = null;
    }
    return () => {
      if (countdownTimerRef.current) clearInterval(countdownTimerRef.current);
    };
  }, [sessionStatus, selectedCampaign, currentContact?.id]);

  const beginFirstOrNextCall = (excludeId?: string) => {
    if (!selectedCampaign) return;
    const next = getNextPendingContact(selectedCampaign.contacts, excludeId);
    if (!next) {
      setSessionStatus('finished');
      setCurrentContact(null);
      onTriggerToast('success', 'All contacts processed. Session completed!');
      return;
    }
    setCurrentContact(next);
    setCallDuration(0);
    setCallPhase('dialing');
    setSessionStatus('calling');
  };

  // Start or Restart Session
  const handleStartSession = () => {
    if (!selectedCampaign) return;

    if (!settings.permissions.calling) {
      setSessionStatus('error');
      onTriggerToast('error', 'Android CALL_PHONE permission is missing.');
      return;
    }

    let targetCampaign = selectedCampaign;
    if (targetCampaign.pendingCalls === 0 && targetCampaign.totalContacts > 0) {
      targetCampaign = resetCampaignProgress(targetCampaign);
      onUpdateCampaign(targetCampaign);
    }

    const firstContact = getNextPendingContact(targetCampaign.contacts);
    if (!firstContact) {
      onTriggerToast('info', 'Add contacts to this campaign first.');
      return;
    }

    if (settings.countdownBeforeStart > 0) {
      setStartCountdown(settings.countdownBeforeStart);
      setSessionStatus('starting');
    } else {
      setCurrentContact(firstContact);
      setCallDuration(0);
      setCallPhase('dialing');
      setSessionStatus('calling');
      onTriggerToast('success', `Calling ${firstContact.name}`);
    }
  };

  const handleCompleteCurrentCall = (outcome: CallOutcome, duration: number) => {
    if (!selectedCampaign || !currentContact) return;

    const effectiveOutcome: CallOutcome =
      outcome === 'answered' &&
      (!settings.detectAnswersFromCallLog || !settings.permissions.callLog)
        ? 'unverified'
        : outcome;

    const updated = applyCallOutcomeToCampaign(
      selectedCampaign,
      currentContact.id,
      effectiveOutcome,
      duration
    );
    onUpdateCampaign(updated);

    const updatedContact =
      updated.contacts.find((c) => c.id === currentContact.id) || currentContact;

    // If answered and Ask for Call Result is enabled (Part 8.3), pause for stage selection
    if (effectiveOutcome === 'answered' && settings.askForCallResult) {
      setCurrentContact(updatedContact);
      setSessionStatus('awaiting_result');
      setIsStageSheetOpen(true);
      return;
    }

    proceedAfterOutcome(updated, currentContact.id);
  };

  const proceedAfterOutcome = (updatedCampaign: Campaign, finishedContactId: string) => {
    const remainingPending = updatedCampaign.contacts.filter(
      (c) => c.status === 'pending' && c.id !== finishedContactId
    );

    if (remainingPending.length === 0) {
      setSessionStatus('finished');
      setCurrentContact(null);
      onTriggerToast('success', 'Session finished.');
    } else {
      const baseDelay = sessionConfig.delayBetweenCalls;
      const jitter = sessionConfig.randomizeDelay
        ? Math.floor(Math.random() * Math.min(5, settings.maxRandomDelay))
        : 0;
      setDelayCountdown(Math.max(1, baseDelay + jitter));
      setSessionStatus('waiting');
      setCallPhase('idle');
      setCurrentContact(null);
    }
  };

  const handleSaveStageFromDialer = (
    contactId: string,
    stage: CallStage,
    feedback: string
  ) => {
    if (!selectedCampaign) return;
    const updated = updateContactStageAndNotes(
      selectedCampaign,
      contactId,
      stage,
      feedback
    );
    onUpdateCampaign(updated);
    onTriggerToast('success', `Saved stage: ${stage.replace('_', ' ')}`);
    setIsStageSheetOpen(false);
    proceedAfterOutcome(updated, contactId);
  };

  const handlePauseResume = () => {
    if (
      sessionStatus === 'running' ||
      sessionStatus === 'calling' ||
      sessionStatus === 'waiting' ||
      sessionStatus === 'starting'
    ) {
      setSessionStatus('paused');
      onTriggerToast('info', 'Automated calling queue paused.');
    } else if (sessionStatus === 'paused') {
      if (currentContact) {
        setSessionStatus('calling');
      } else {
        setSessionStatus('waiting');
      }
      onTriggerToast('success', 'Automated calling queue resumed.');
    }
  };

  const handleSkipCall = () => {
    if (!currentContact && sessionStatus === 'waiting') {
      beginFirstOrNextCall();
      return;
    }
    if (!currentContact) return;
    onTriggerToast('info', `Skipped ${currentContact.name}.`);
    handleCompleteCurrentCall('skipped', callDuration);
  };

  const handleConfirmStop = () => {
    setSessionStatus('stopped');
    setCallPhase('idle');
    setCurrentContact(null);
    setCallDuration(0);
    setIsStopConfirmOpen(false);
    onTriggerToast('info', 'Session stopped. Calling progress saved.');
  };

  const handleRemoveFromQueue = (contact: CampaignContact) => {
    if (!selectedCampaign) return;
    const updated = removeContactFromCampaign(selectedCampaign, contact.id);
    onUpdateCampaign(updated);
    onTriggerToast('info', `Removed ${contact.name} from queue.`);
  };

  const handleToggleSimulatedError = () => {
    if (sessionStatus === 'error') {
      setSessionStatus('ready');
    } else {
      setSessionStatus('error');
    }
  };

  return (
    <div className="flex-1 overflow-y-auto bg-transparent flex flex-col">
      {/* 1. Header */}
      <DialerHeader
        selectedCampaign={selectedCampaign}
        sessionStatus={sessionStatus}
        onOpenCampaignPicker={() => setIsCampaignPickerOpen(true)}
        currentLanguage={currentLanguage}
        onToggleLanguage={onToggleLanguage}
        onSimulateErrorToggle={handleToggleSimulatedError}
        t={t}
      />

      {/* 2. Main Calling Hero (with integrated Call Actions when session is active) */}
      <ActiveCallingHero
        sessionStatus={sessionStatus}
        callPhase={callPhase}
        currentContact={currentContact}
        nextContact={nextContact}
        callDurationSeconds={callDuration}
        delayCountdownSeconds={delayCountdown}
        startCountdownSeconds={startCountdown}
        selectedCampaign={selectedCampaign}
        currentLanguage={currentLanguage}
        onStartSession={handleStartSession}
        onPauseResume={handlePauseResume}
        onSkipCall={handleSkipCall}
        onPromptStop={() => setIsStopConfirmOpen(true)}
        onRecordManualOutcome={(outcome) =>
          handleCompleteCurrentCall(outcome, Math.max(callDuration, 18))
        }
        onOpenStageSheet={() => setIsStageSheetOpen(true)}
        onResolveError={() => setSessionStatus('ready')}
        onSelectAnotherCampaign={() => setIsCampaignPickerOpen(true)}
        onViewResults={() => {
          if (selectedCampaign) onOpenResultsForCampaign(selectedCampaign);
        }}
        onOpenCampaignDetails={() => {
          if (selectedCampaign) onOpenCampaignDetails(selectedCampaign);
        }}
        t={t}
      />

      {/* 3. Live Campaign Progress */}
      <DialerProgress campaign={selectedCampaign} t={t} />

      {/* 5. Upcoming Calling Queue (Next 3 eligible contacts) */}
      <CallingQueue
        queuedContacts={queuedContacts}
        onRemoveContact={handleRemoveFromQueue}
        t={t}
      />

      {/* 6. Session Setup Summary Strip */}
      <SessionConfigStrip
        config={sessionConfig}
        delaySec={sessionConfig.delayBetweenCalls}
        onOpenSettings={() => setIsSettingsOpen(true)}
        t={t}
      />

      {/* Modals & Sheets */}
      <CampaignPickerSheet
        campaigns={campaigns}
        selectedCampaignId={selectedCampaign?.id}
        isOpen={isCampaignPickerOpen}
        onClose={() => setIsCampaignPickerOpen(false)}
        onSelectCampaign={(c) => {
          onSelectCampaign(c);
          setSessionStatus('ready');
          setCurrentContact(null);
          setCallDuration(0);
        }}
        t={t}
      />

      <StopConfirmModal
        isOpen={isStopConfirmOpen}
        campaignName={selectedCampaign?.name ?? 'Campaign'}
        onConfirm={handleConfirmStop}
        onCancel={() => setIsStopConfirmOpen(false)}
        t={t}
      />

      <DialerSettingsModal
        isOpen={isSettingsOpen}
        config={sessionConfig}
        onClose={() => setIsSettingsOpen(false)}
        onUpdateConfig={(newCfg) => {
          setSessionConfig(newCfg);
          onUpdateSettings({
            ...settings,
            delayBetweenCalls: newCfg.delayBetweenCalls,
            retryUnanswered: newCfg.retryAttempts,
            audioRoute: newCfg.audioRoute,
            randomDelay: newCfg.randomizeDelay,
          });
        }}
        t={t}
      />

      {/* Contextual Call Result Classification Sheet (Part 8) */}
      <StageClassificationSheet
        contact={currentContact}
        isOpen={isStageSheetOpen}
        currentLanguage={currentLanguage}
        onClose={() => {
          setIsStageSheetOpen(false);
          if (selectedCampaign && currentContact) {
            proceedAfterOutcome(selectedCampaign, currentContact.id);
          }
        }}
        onSave={handleSaveStageFromDialer}
        onOpenWhatsApp={onOpenWhatsApp}
        t={t}
      />
    </div>
  );
};
