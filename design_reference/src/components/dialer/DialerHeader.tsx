import React from 'react';
import { ChevronDown, Globe, AlertCircle } from 'lucide-react';
import { SessionStatus } from '../../types/dialer';
import { Campaign } from '../../types/campaign';
import { Translations, Language } from '../../i18n/translations';
import { formatSimLabel } from '../../utils/campaignLogic';

interface DialerHeaderProps {
  selectedCampaign: Campaign | null;
  sessionStatus: SessionStatus;
  onOpenCampaignPicker: () => void;
  currentLanguage: Language;
  onToggleLanguage: () => void;
  onSimulateErrorToggle: () => void;
  t: Translations;
}

export const DialerHeader: React.FC<DialerHeaderProps> = ({
  selectedCampaign,
  sessionStatus,
  onOpenCampaignPicker,
  currentLanguage,
  onToggleLanguage,
  onSimulateErrorToggle,
  t,
}) => {
  const getStatusText = () => {
    switch (sessionStatus) {
      case 'starting':
        return { label: t.sessionStatusStarting, color: 'bg-[#E2532B]/12 text-[#E2532B]' };
      case 'running':
      case 'calling':
        return { label: t.sessionStatusCalling, color: 'bg-emerald-500/12 text-emerald-700' };
      case 'waiting':
        return { label: t.sessionStatusWaiting, color: 'bg-[#E2532B]/12 text-[#E2532B]' };
      case 'awaiting_result':
        return { label: t.sessionStatusAwaitingResult, color: 'bg-amber-500/12 text-amber-700' };
      case 'paused':
        return { label: t.sessionStatusPaused, color: 'bg-amber-500/12 text-amber-700' };
      case 'finished':
        return { label: t.sessionStatusFinished, color: 'bg-emerald-500/12 text-emerald-700' };
      case 'stopped':
        return { label: t.sessionStatusStopped, color: 'bg-[#111318]/[0.06] text-[#6B7280]' };
      case 'error':
        return { label: t.sessionStatusError, color: 'bg-rose-500/12 text-rose-700' };
      case 'idle':
        return { label: t.sessionStatusIdle, color: 'bg-[#111318]/[0.06] text-[#6B7280]' };
      case 'ready':
      default:
        return { label: t.sessionStatusReady, color: 'bg-[#111318]/[0.06] text-[#6B7280]' };
    }
  };

  const statusInfo = getStatusText();

  return (
    <header className="px-[20px] pt-3 pb-2 bg-transparent shrink-0">
      <div className="flex items-center justify-between gap-3">
        <div className="min-w-0 flex-1">
          {/* Selected Campaign Selector + SIM Info */}
          <div className="flex items-center gap-1.5">
            <button
              type="button"
              onClick={onOpenCampaignPicker}
              className="flex items-center gap-1 text-left group max-w-[210px]"
              aria-label="Change active campaign"
            >
              <span className="text-xs text-[#8A909E] group-hover:text-[#111318] truncate font-medium transition-colors">
                {selectedCampaign ? selectedCampaign.name : 'Select a campaign'}
              </span>
              <ChevronDown className="w-3.5 h-3.5 text-[#8A909E] group-hover:text-[#111318] shrink-0 transition-colors" />
            </button>
            {selectedCampaign && (
              <>
                <span aria-hidden="true" className="text-[#8A909E]/50">·</span>
                <span className="text-[11px] font-medium text-[#8A909E] whitespace-nowrap">
                  {formatSimLabel(selectedCampaign.simSlot, currentLanguage)}
                </span>
              </>
            )}
          </div>

          <div className="flex items-center gap-2 mt-0.5">
            <h1 className="text-[22px] font-extrabold tracking-tight text-[#111318] leading-snug truncate">
              {t.dialerTitle}
            </h1>
            <span className={`text-[11px] font-semibold px-2.5 py-0.5 rounded-full shrink-0 ${statusInfo.color}`}>
              {statusInfo.label}
            </span>
          </div>
        </div>

        {/* Top utility actions */}
        <div className="flex items-center gap-1.5 shrink-0">
          <button
            type="button"
            onClick={onSimulateErrorToggle}
            className="min-w-[44px] min-h-[44px] w-11 h-11 rounded-full bg-white/60 backdrop-blur-md shadow-[0_8px_24px_-6px_rgba(17,19,24,0.06)] flex items-center justify-center text-[#8A909E] hover:text-[#111318] active:scale-95 transition-all"
            title="Inspect operational / busy state"
            aria-label="Toggle operational state"
          >
            <AlertCircle className="w-[18px] h-[18px]" strokeWidth={1.75} />
          </button>
          <button
            type="button"
            onClick={onToggleLanguage}
            className="min-w-[44px] min-h-[44px] w-11 h-11 rounded-full bg-white/60 backdrop-blur-md shadow-[0_8px_24px_-6px_rgba(17,19,24,0.06)] flex items-center justify-center text-[#8A909E] hover:text-[#111318] active:scale-95 transition-all"
            title={currentLanguage === 'en' ? 'العربية' : 'English'}
            aria-label="Toggle language"
          >
            <Globe className="w-[18px] h-[18px]" strokeWidth={1.75} />
          </button>
        </div>
      </div>
    </header>
  );
};
