import React from 'react';
import { Campaign, CampaignContact } from '../../types/campaign';
import { CallOutcome, CallPhase, SessionStatus } from '../../types/dialer';
import { Translations, Language } from '../../i18n/translations';
import { formatCallTimer } from '../../utils/dialerLogic';
import { formatSimLabel } from '../../utils/campaignLogic';
import {
  PhoneCall,
  AlertCircle,
  RefreshCw,
  CheckCircle2,
  UserPlus,
  Tag,
  Pause,
  Play,
  SkipForward,
  Square,
} from 'lucide-react';

interface ActiveCallingHeroProps {
  sessionStatus: SessionStatus;
  callPhase: CallPhase;
  currentContact: CampaignContact | null;
  nextContact: CampaignContact | null;
  callDurationSeconds: number;
  delayCountdownSeconds: number;
  startCountdownSeconds: number;
  waitingReason?: string | null;
  selectedCampaign: Campaign | null;
  currentLanguage?: Language;
  onStartSession: () => void;
  onPauseResume: () => void;
  onSkipCall: () => void;
  onPromptStop: () => void;
  onRecordManualOutcome: (outcome: CallOutcome) => void;
  onOpenStageSheet: () => void;
  onResolveError: () => void;
  onSelectAnotherCampaign: () => void;
  onViewResults: () => void;
  onOpenCampaignDetails: () => void;
  t: Translations;
}

export const ActiveCallingHero: React.FC<ActiveCallingHeroProps> = ({
  sessionStatus,
  callPhase,
  currentContact,
  nextContact,
  callDurationSeconds,
  delayCountdownSeconds,
  startCountdownSeconds,
  waitingReason,
  selectedCampaign,
  currentLanguage = 'en',
  onStartSession,
  onPauseResume,
  onSkipCall,
  onPromptStop,
  onRecordManualOutcome,
  onOpenStageSheet,
  onResolveError,
  onSelectAnotherCampaign,
  onViewResults,
  onOpenCampaignDetails,
  t,
}) => {
  const isAr = currentLanguage === 'ar';
  const isPaused = sessionStatus === 'paused';
  const canSkip =
    sessionStatus === 'calling' ||
    sessionStatus === 'running' ||
    sessionStatus === 'waiting' ||
    sessionStatus === 'paused';

  const renderLiveCallActions = () => (
    <div className="mt-4 pt-3.5 border-t border-[#111318]/[0.06] grid grid-cols-3 gap-2">
      {/* Secondary: Skip Call */}
      <button
        type="button"
        onClick={onSkipCall}
        disabled={!canSkip}
        className="min-h-[42px] px-3 rounded-full bg-[#111318]/[0.05] text-[#111318] hover:bg-[#111318]/[0.09] disabled:opacity-35 disabled:pointer-events-none transition-all active:scale-[0.98] flex items-center justify-center gap-1.5 text-xs font-semibold whitespace-nowrap"
        aria-label={t.actionSkipCall}
      >
        <SkipForward className="w-3.5 h-3.5 shrink-0" strokeWidth={1.75} />
        <span>{t.actionSkipCall}</span>
      </button>

      {/* Primary Action: Pause or Resume */}
      <button
        type="button"
        onClick={onPauseResume}
        className="min-h-[42px] px-3 rounded-full bg-[#111318] text-white hover:bg-[#1e222b] transition-all active:scale-[0.98] flex items-center justify-center gap-1.5 text-xs font-semibold shadow-[0_8px_20px_-4px_rgba(17,19,24,0.28)] whitespace-nowrap"
        aria-label={isPaused ? t.actionResumeSession : t.actionPauseSession}
      >
        {isPaused ? (
          <>
            <Play className="w-3.5 h-3.5 fill-current shrink-0" />
            <span>{isAr ? 'استئناف' : 'Resume'}</span>
          </>
        ) : (
          <>
            <Pause className="w-3.5 h-3.5 fill-current shrink-0" />
            <span>{isAr ? 'إيقاف مؤقت' : 'Pause'}</span>
          </>
        )}
      </button>

      {/* Destructive: Stop Session */}
      <button
        type="button"
        onClick={onPromptStop}
        className="min-h-[42px] px-3 rounded-full bg-[#E2532B]/10 text-[#E2532B] hover:bg-[#E2532B]/18 transition-all active:scale-[0.98] flex items-center justify-center gap-1.5 text-xs font-semibold whitespace-nowrap"
        aria-label={t.actionStopSession}
      >
        <Square className="w-3 h-3 fill-current shrink-0" />
        <span>{isAr ? 'إيقاف' : 'Stop'}</span>
      </button>
    </div>
  );

  // 1. Error State (Part 7.4 & 13.10)
  if (sessionStatus === 'error') {
    return (
      <div className="px-[20px] py-1.5">
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_14px_40px_-10px_rgba(17,19,24,0.07)] text-center">
          <div className="w-11 h-11 rounded-full bg-[#E2532B]/12 text-[#E2532B] flex items-center justify-center mx-auto mb-3">
            <AlertCircle className="w-5 h-5" strokeWidth={1.75} />
          </div>
          <h2 className="text-[16px] font-bold text-[#111318] tracking-tight">
            {isAr ? 'توقفت الجلسة بسبب مشكلة تشغيلية' : 'Operational Pause — Line Busy or Restricted'}
          </h2>
          <p className="text-xs text-[#8A909E] max-w-xs mx-auto mt-1.5 mb-4 leading-relaxed">
            {isAr
              ? 'اكتشف المتصل الآلي مكالمة أخرى نشطة على الهاتف أو نقصاً في صلاحية الاتصال. تم حفظ التقدم.'
              : 'Another phone call is currently active or Android CALL_PHONE permission requires verification. Progress is preserved.'}
          </p>
          <button
            type="button"
            onClick={onResolveError}
            className="min-h-[44px] px-5 py-2 text-xs font-semibold bg-[#111318] text-white hover:bg-[#1e222b] rounded-full transition-all inline-flex items-center gap-1.5"
          >
            <RefreshCw className="w-3.5 h-3.5" strokeWidth={1.75} />
            <span>{isAr ? 'استئناف الجلسة' : 'Return to Ready State'}</span>
          </button>
        </div>
      </div>
    );
  }

  // 2. Finished or Stopped State (Part 7.11)
  if (sessionStatus === 'finished' || sessionStatus === 'stopped') {
    const isStopped = sessionStatus === 'stopped';
    return (
      <div className="px-[20px] py-1.5">
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_14px_40px_-10px_rgba(17,19,24,0.07)] text-center">
          <div
            className={`w-11 h-11 rounded-full flex items-center justify-center mx-auto mb-3 ${
              isStopped
                ? 'bg-[#111318]/[0.06] text-[#111318]'
                : 'bg-emerald-500/12 text-emerald-700'
            }`}
          >
            <CheckCircle2 className="w-5 h-5" strokeWidth={1.75} />
          </div>
          <h2 className="text-[17px] font-bold text-[#111318] tracking-tight">
            {isStopped
              ? isAr
                ? 'تم إيقاف الجلسة وحفظ التقدم'
                : 'Session Stopped — Progress Saved'
              : t.sessionCompletedTitle}
          </h2>
          <p className="text-xs text-[#8A909E] max-w-xs mx-auto mt-1 mb-4 leading-relaxed">
            {isStopped
              ? isAr
                ? `تمت معالجة ${selectedCampaign?.completedCalls ?? 0} وتبقى ${selectedCampaign?.pendingCalls ?? 0} جهة اتصال.`
                : `${selectedCampaign?.completedCalls ?? 0} processed · ${selectedCampaign?.pendingCalls ?? 0} remaining in queue.`
              : t.sessionCompletedSubtitle}
          </p>
          <div className="flex items-center justify-center gap-2.5">
            <button
              type="button"
              onClick={onViewResults}
              className="min-h-[44px] px-4 py-2 text-xs font-semibold text-[#111318] bg-[#111318]/[0.06] hover:bg-[#111318]/[0.1] rounded-full transition-colors"
            >
              {t.actionViewResults}
            </button>
            <button
              type="button"
              onClick={onSelectAnotherCampaign}
              className="min-h-[44px] px-4 py-2 text-xs font-semibold bg-[#111318] text-white hover:bg-[#1e222b] rounded-full transition-colors"
            >
              {t.chooseAnotherCampaign}
            </button>
          </div>
        </div>
      </div>
    );
  }

  // 3. Starting Countdown State (Part 7.2 & 7.4)
  if (sessionStatus === 'starting') {
    return (
      <div className="px-[20px] py-1.5">
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_14px_40px_-10px_rgba(17,19,24,0.07)] text-center">
          <span className="text-xs font-medium text-[#8A909E] block mb-1">
            {isAr ? 'بدء الجلسة خلال' : 'Starting Session Countdown'}
          </span>
          <div className="text-[36px] font-extrabold text-[#111318] tabular-nums tracking-tight leading-none my-2">
            00:0{startCountdownSeconds}
          </div>
          {nextContact && (
            <p className="text-xs text-[#8A909E]">
              {isAr ? 'جهة الاتصال الأولى:' : 'First contact:'}{' '}
              <strong className="text-[#111318] font-bold">{nextContact.name}</strong>
            </p>
          )}
          {renderLiveCallActions()}
        </div>
      </div>
    );
  }

  // 4. Awaiting Result State (Part 7.4 & Part 8)
  if (sessionStatus === 'awaiting_result' && currentContact) {
    return (
      <div className="px-[20px] py-1.5">
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_14px_40px_-10px_rgba(17,19,24,0.07)] text-center">
          <span className="text-xs font-semibold text-[#E2532B] block mb-1">
            {t.sessionStatusAwaitingResult}
          </span>
          <h2 className="text-[22px] font-extrabold text-[#111318] tracking-tight truncate">
            {currentContact.name}
          </h2>
          <div className="text-xs font-mono text-[#8A909E] mt-0.5" dir="ltr">
            {currentContact.phone}
          </div>
          <p className="text-xs text-[#8A909E] mt-2 mb-4">
            {isAr
              ? 'انتهت المكالمة. صنّف النتيجة أو أضف ملاحظة للمتابعة للرقم التالي.'
              : 'Call finished. Classify the call stage or add notes to continue the queue.'}
          </p>
          <button
            type="button"
            onClick={onOpenStageSheet}
            className="min-h-[44px] px-5 py-2.5 rounded-full bg-[#111318] text-white text-xs font-semibold inline-flex items-center gap-2 shadow-[0_10px_24px_-6px_rgba(17,19,24,0.28)]"
          >
            <Tag className="w-3.5 h-3.5" strokeWidth={1.75} />
            <span>{isAr ? 'تصنيف نتيجة المكالمة' : 'Classify Call Outcome'}</span>
          </button>
        </div>
      </div>
    );
  }

  // 5. Pre-Session / Ready or Idle State (Part 7.3)
  if (
    sessionStatus === 'ready' ||
    sessionStatus === 'idle' ||
    (!currentContact && sessionStatus !== 'waiting')
  ) {
    const isEmpty = (selectedCampaign?.totalContacts ?? 0) === 0;
    const hasProgress = (selectedCampaign?.completedCalls ?? 0) > 0;
    const isCompleted =
      (selectedCampaign?.totalContacts ?? 0) > 0 &&
      (selectedCampaign?.pendingCalls ?? 0) === 0;

    return (
      <div className="px-[20px] py-1.5">
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_14px_40px_-10px_rgba(17,19,24,0.07)] text-center">
          <span className="text-xs font-medium text-[#8A909E] block mb-1">
            {t.readyToCallHeading}
          </span>
          <h2 className="text-[20px] font-extrabold text-[#111318] tracking-tight truncate">
            {selectedCampaign ? selectedCampaign.name : 'No Campaign Selected'}
          </h2>
          <p className="text-xs text-[#8A909E] max-w-xs mx-auto mt-1 mb-4 leading-relaxed tabular-nums">
            {selectedCampaign
              ? isEmpty
                ? isAr
                  ? 'هذه الحملة فارغة حالياً. أضف جهات اتصال للبدء.'
                  : 'This campaign has no contacts yet. Add contacts to start calling.'
                : `${selectedCampaign.pendingCalls} remaining of ${selectedCampaign.totalContacts} · ${formatSimLabel(selectedCampaign.simSlot, currentLanguage)}`
              : t.readyToCallSubtitle}
          </p>

          {selectedCampaign && (
            <>
              {isEmpty ? (
                <button
                  type="button"
                  onClick={onOpenCampaignDetails}
                  className="min-h-[46px] px-6 py-2.5 text-xs font-semibold bg-[#111318] text-white hover:bg-[#1e222b] rounded-full transition-all inline-flex items-center gap-2 shadow-[0_10px_24px_-6px_rgba(17,19,24,0.28)]"
                >
                  <UserPlus className="w-4 h-4" strokeWidth={1.75} />
                  <span>{isAr ? 'إضافة جهات اتصال' : 'Add Contacts'}</span>
                </button>
              ) : (
                <button
                  type="button"
                  onClick={onStartSession}
                  className="min-h-[46px] px-6 py-2.5 text-xs font-semibold bg-[#111318] text-white hover:bg-[#1e222b] rounded-full transition-all active:scale-[0.98] inline-flex items-center gap-2 shadow-[0_10px_24px_-6px_rgba(17,19,24,0.28)]"
                >
                  <PhoneCall className="w-4 h-4" strokeWidth={1.75} />
                  <span>
                    {isCompleted
                      ? t.actionRestartCampaign
                      : hasProgress
                      ? t.actionResumeSession
                      : t.actionStartSession}
                  </span>
                </button>
              )}
            </>
          )}
        </div>
      </div>
    );
  }

  // 6. Waiting Between Calls State (Part 7.8)
  if (sessionStatus === 'waiting') {
    return (
      <div className="px-[20px] py-1.5">
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_14px_40px_-10px_rgba(17,19,24,0.07)] text-center">
          <span className="text-xs font-medium text-[#8A909E] block mb-1">
            {waitingReason || (isAr ? 'الفاصل الزمني بين المكالمات' : 'Waiting Before Next Call')}
          </span>

          <div className="text-[32px] font-extrabold text-[#111318] tabular-nums tracking-tight leading-none mb-2">
            {t.nextCallIn(delayCountdownSeconds)}
          </div>

          {nextContact && (
            <p className="text-xs text-[#8A909E]">
              {isAr ? 'التالي:' : 'Next:'}{' '}
              <strong className="text-[#111318] font-bold">{nextContact.name}</strong>{' '}
              <span className="font-mono text-[11.5px] text-[#8A909E]" dir="ltr">
                ({nextContact.phone})
              </span>
            </p>
          )}
          {renderLiveCallActions()}
        </div>
      </div>
    );
  }

  // 7. Active Calling or Paused Hero (Part 7.5)
  return (
    <div className="px-[20px] py-1.5">
      <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_14px_40px_-10px_rgba(17,19,24,0.07)] text-center transition-all animate-card-cascade">
        {/* Kicker with Live Animated Signal Waveform */}
        <div className="flex items-center justify-center gap-2 text-xs font-medium text-[#8A909E] mb-1.5">
          <span>{t.currentlyCalling}</span>
          <span aria-hidden="true">·</span>
          <span
            className={`inline-flex items-center gap-1.5 ${
              sessionStatus === 'paused'
                ? 'text-amber-700 font-semibold'
                : 'text-emerald-700 font-semibold'
            }`}
          >
            {sessionStatus !== 'paused' && (
              <span className="inline-flex items-center gap-0.5 h-3" aria-hidden="true">
                <span className="w-0.5 h-3 rounded-full bg-emerald-600 animate-wave-1" />
                <span className="w-0.5 h-3 rounded-full bg-emerald-600 animate-wave-2" />
                <span className="w-0.5 h-3 rounded-full bg-emerald-600 animate-wave-3" />
                <span className="w-0.5 h-3 rounded-full bg-emerald-600 animate-wave-4" />
                <span className="w-0.5 h-3 rounded-full bg-emerald-600 animate-wave-5" />
              </span>
            )}
            <span>
              {sessionStatus === 'paused'
                ? t.sessionStatusPaused
                : callPhase === 'dialing'
                ? t.callingPhaseDialing
                : callPhase === 'ringing'
                ? t.callingPhaseRinging
                : t.callingPhaseConnected}
            </span>
          </span>
        </div>

        {/* Contact Name */}
        <h2 className="text-[24px] font-extrabold text-[#111318] tracking-tight leading-tight truncate">
          {currentContact?.name || 'Contact'}
        </h2>

        {/* Phone Number & Company */}
        <div className="flex items-center justify-center gap-1.5 text-xs text-[#8A909E] mt-1">
          {currentContact?.company && (
            <>
              <span className="truncate max-w-[160px]">{currentContact.company}</span>
              <span aria-hidden="true">·</span>
            </>
          )}
          <span className="font-mono text-xs tabular-nums text-[#111318] font-semibold" dir="ltr">
            {currentContact?.phone}
          </span>
        </div>

        {/* Elapsed Call Timer */}
        <div className="mt-3 text-[28px] font-extrabold text-[#111318] tabular-nums tracking-tight leading-none font-mono">
          {formatCallTimer(callDurationSeconds)}
        </div>

        {/* Live Call Actions (Skip, Pause/Resume, Stop) */}
        {renderLiveCallActions()}

        {/* Prototype Telephony Outcome Simulator */}
        {(sessionStatus === 'running' || sessionStatus === 'calling') && (
          <div className="mt-3 pt-3 border-t border-[#111318]/[0.06] flex items-center justify-center flex-wrap gap-1.5">
            <span className="text-[11px] text-[#8A909E] mr-1">
              {isAr ? 'محاكاة انتهاء المكالمة:' : 'Simulate call outcome:'}
            </span>
            <button
              type="button"
              onClick={() => onRecordManualOutcome('answered')}
              className="min-h-[34px] px-3 py-1 text-[11px] font-semibold bg-emerald-500/12 text-emerald-700 hover:bg-emerald-500/20 rounded-full transition-colors"
            >
              {t.outcomeAnswered}
            </button>
            <button
              type="button"
              onClick={() => onRecordManualOutcome('no_answer')}
              className="min-h-[34px] px-3 py-1 text-[11px] font-semibold bg-[#111318]/[0.06] text-[#111318] hover:bg-[#111318]/[0.1] rounded-full transition-colors"
            >
              {t.outcomeNoAnswer}
            </button>
            <button
              type="button"
              onClick={() => onRecordManualOutcome('failed')}
              className="min-h-[34px] px-3 py-1 text-[11px] font-semibold bg-[#E2532B]/12 text-[#E2532B] hover:bg-[#E2532B]/20 rounded-full transition-colors"
            >
              {t.outcomeFailed}
            </button>
            <button
              type="button"
              onClick={() => onRecordManualOutcome('unverified')}
              className="min-h-[34px] px-3 py-1 text-[11px] font-semibold bg-[#111318]/[0.05] text-[#8A909E] hover:text-[#111318] rounded-full transition-colors"
            >
              {t.outcomeUnverified}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
