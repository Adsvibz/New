import React from 'react';
import { Plus, Globe } from 'lucide-react';
import { Translations, Language } from '../i18n/translations';

interface ScreenHeaderProps {
  t: Translations;
  currentLanguage: Language;
  onToggleLanguage: () => void;
  campaignCount: number;
  contactCount: number;
  onNewCampaign: () => void;
  isMultiSelectMode?: boolean;
  onToggleMultiSelect?: () => void;
}

export const ScreenHeader: React.FC<ScreenHeaderProps> = ({
  t,
  currentLanguage,
  onToggleLanguage,
  campaignCount,
  contactCount,
  onNewCampaign,
}) => {
  return (
    <header className="px-[20px] pt-3 pb-2 bg-transparent shrink-0">
      <div className="flex items-center justify-between gap-2.5">
        <div className="min-w-0 flex-1">
          <p className="text-[11px] font-medium text-[#8A909E] tabular-nums truncate">
            {t.campaignsCount(campaignCount, contactCount)}
          </p>
          <h1 className="text-[22px] font-extrabold tracking-tight text-[#111318] leading-snug truncate">
            {t.campaignsTitle}
          </h1>
        </div>

        <div className="flex items-center gap-1.5 shrink-0">
          {/* Language switcher */}
          <button
            type="button"
            onClick={onToggleLanguage}
            className="min-w-[44px] min-h-[44px] w-11 h-11 rounded-full bg-white/60 backdrop-blur-md shadow-[0_8px_24px_-6px_rgba(17,19,24,0.06)] flex items-center justify-center text-[#8A909E] hover:text-[#111318] active:scale-95 transition-all"
            title={currentLanguage === 'en' ? 'العربية' : 'English'}
            aria-label="Toggle language"
          >
            <Globe className="w-[18px] h-[18px]" strokeWidth={1.75} />
          </button>

          {/* Primary Black Rounded Button: + New Campaign */}
          <button
            type="button"
            onClick={onNewCampaign}
            className="min-h-[44px] px-4 rounded-full text-xs font-semibold bg-[#111318] text-white hover:bg-[#1e222b] active:scale-[0.98] transition-all flex items-center gap-1.5 shadow-[0_10px_24px_-6px_rgba(17,19,24,0.28)] whitespace-nowrap"
            aria-label="Create new campaign"
          >
            <Plus className="w-4 h-4" strokeWidth={2} />
            <span>{t.newCampaign.replace('+ ', '')}</span>
          </button>
        </div>
      </div>
    </header>
  );
};
