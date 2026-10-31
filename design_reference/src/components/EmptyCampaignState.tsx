import React from 'react';
import { Megaphone, Plus } from 'lucide-react';
import { Translations } from '../i18n/translations';

interface EmptyCampaignStateProps {
  onCreateCampaign: () => void;
  t: Translations;
}

export const EmptyCampaignState: React.FC<EmptyCampaignStateProps> = ({
  onCreateCampaign,
  t,
}) => {
  return (
    <div className="py-14 px-6 flex flex-col items-center justify-center text-center">
      <div className="w-12 h-12 rounded-2xl bg-[#F7F8FA] border border-[#E9EBEF] flex items-center justify-center text-[#777E89] mb-4">
        <Megaphone className="w-5 h-5 stroke-[1.8] text-[#17191D]" />
      </div>

      <h3 className="text-[16px] font-semibold text-[#17191D] tracking-tight mb-1.5">
        {t.emptyTitle}
      </h3>

      <p className="text-[13px] text-[#777E89] max-w-xs leading-relaxed mb-5">
        {t.emptySubtitle}
      </p>

      <button
        onClick={onCreateCampaign}
        className="flex items-center gap-1.5 px-4 py-2 text-[13px] font-medium bg-[#17191D] text-white rounded-lg hover:bg-neutral-800 active:scale-[0.98] transition-all shadow-xs"
      >
        <Plus className="w-4 h-4 stroke-[2.5]" />
        <span>{t.emptyAction}</span>
      </button>
    </div>
  );
};
