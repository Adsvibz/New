import React from 'react';
import { Pause, Play, SkipForward, Square, RotateCcw } from 'lucide-react';
import { SessionStatus } from '../../types/dialer';
import { Translations } from '../../i18n/translations';

interface SessionControlsProps {
  sessionStatus: SessionStatus;
  isCompleted: boolean;
  onPauseResume: () => void;
  onSkipCall: () => void;
  onPromptStop: () => void;
  onRestartSession: () => void;
  t: Translations;
}

export const SessionControls: React.FC<SessionControlsProps> = ({
  sessionStatus,
  isCompleted,
  onPauseResume,
  onSkipCall,
  onPromptStop,
  onRestartSession,
  t,
}) => {
  const isRunning = sessionStatus === 'running' || sessionStatus === 'waiting';
  const isPaused = sessionStatus === 'paused';
  const isReady = sessionStatus === 'ready';

  return (
    <div className="px-[20px] py-1.5">
      <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-3 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] grid grid-cols-3 gap-2.5">
        {/* Secondary: Skip Call */}
        <button
          type="button"
          onClick={onSkipCall}
          disabled={!isRunning && !isPaused}
          className="min-h-[44px] px-3 rounded-full bg-[#111318]/[0.05] text-[#111318] hover:bg-[#111318]/[0.09] disabled:opacity-35 disabled:pointer-events-none transition-all flex items-center justify-center gap-1.5 text-xs font-semibold whitespace-nowrap shrink-0"
          aria-label={t.actionSkipCall}
        >
          <SkipForward className="w-3.5 h-3.5 shrink-0" strokeWidth={1.75} />
          <span>{t.actionSkipCall}</span>
        </button>

        {/* Primary Action: Start, Pause, or Resume (Black Rounded Pill) */}
        {isReady || isCompleted ? (
          <button
            type="button"
            onClick={onRestartSession}
            className="min-h-[44px] px-3 rounded-full bg-[#111318] text-white hover:bg-[#1e222b] transition-all active:scale-[0.98] flex items-center justify-center gap-1.5 text-xs font-semibold shadow-[0_8px_20px_-4px_rgba(17,19,24,0.28)] whitespace-nowrap shrink-0"
            aria-label={isCompleted ? t.actionRestartCampaign : t.actionStartSession}
          >
            {isCompleted ? (
              <>
                <RotateCcw className="w-3.5 h-3.5 shrink-0" strokeWidth={1.75} />
                <span>Restart</span>
              </>
            ) : (
              <>
                <Play className="w-3.5 h-3.5 fill-current shrink-0" />
                <span>Start</span>
              </>
            )}
          </button>
        ) : (
          <button
            type="button"
            onClick={onPauseResume}
            className="min-h-[44px] px-3 rounded-full bg-[#111318] text-white hover:bg-[#1e222b] transition-all active:scale-[0.98] flex items-center justify-center gap-1.5 text-xs font-semibold shadow-[0_8px_20px_-4px_rgba(17,19,24,0.28)] whitespace-nowrap shrink-0"
            aria-label={isPaused ? t.actionResumeSession : t.actionPauseSession}
          >
            {isPaused ? (
              <>
                <Play className="w-3.5 h-3.5 fill-current shrink-0" />
                <span>Resume</span>
              </>
            ) : (
              <>
                <Pause className="w-3.5 h-3.5 fill-current shrink-0" />
                <span>Pause</span>
              </>
            )}
          </button>
        )}

        {/* Secondary / Destructive: Stop Session */}
        <button
          type="button"
          onClick={onPromptStop}
          disabled={isReady || isCompleted}
          className="min-h-[44px] px-3 rounded-full bg-[#E2532B]/10 text-[#E2532B] hover:bg-[#E2532B]/18 disabled:opacity-35 disabled:pointer-events-none transition-all flex items-center justify-center gap-1.5 text-xs font-semibold whitespace-nowrap shrink-0"
          aria-label={t.actionStopSession}
        >
          <Square className="w-3 h-3 fill-current shrink-0" />
          <span>Stop</span>
        </button>
      </div>
    </div>
  );
};
