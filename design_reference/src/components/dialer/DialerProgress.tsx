import React from 'react';
import { Campaign } from '../../types/campaign';
import { Translations } from '../../i18n/translations';
import { TrendingUp } from 'lucide-react';

interface DialerProgressProps {
  campaign: Campaign | null;
  t: Translations;
}

export const DialerProgress: React.FC<DialerProgressProps> = ({ campaign, t }) => {
  if (!campaign) return null;

  return (
    <section
      className="px-[20px] py-1.5"
      aria-label="Campaign Progress"
    >
      {/* 2-Column Bento Grid: Single Red-Orange (#E2532B) Hero Card + 3 Frosted Cards */}
      <div className="bento-grid-2 gap-3.5">
        {/* Hero Card: Called & Progress % */}
        <div className="rounded-[24px] bg-[#E2532B] text-white p-4 shadow-[0_16px_38px_-10px_rgba(226,83,43,0.38)] flex flex-col justify-between min-w-0">
          <span className="block text-xs font-medium text-white/90 truncate">
            {t.statCompletedCalls}
          </span>
          <div className="mt-2.5 flex items-baseline justify-between gap-2">
            <span className="text-[26px] font-extrabold tabular-nums tracking-tight leading-none truncate">
              {campaign.completedCalls.toLocaleString()}
            </span>
            <span className="inline-flex items-center gap-0.5 text-[10.5px] font-semibold bg-white/20 text-white px-2 py-0.5 rounded-full tabular-nums shrink-0">
              <TrendingUp className="w-2.5 h-2.5" strokeWidth={2} />
              {campaign.progressPercentage}%
            </span>
          </div>
          <div className="w-full h-1.5 bg-white/25 rounded-full overflow-hidden mt-3">
            <div
              className="h-full bg-white rounded-full transition-all duration-300"
              style={{ width: `${Math.min(100, Math.max(0, campaign.progressPercentage))}%` }}
            />
          </div>
        </div>

        {/* Frosted Card 2: Remaining */}
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] flex flex-col justify-between min-w-0">
          <span className="block text-xs font-medium text-[#8A909E] truncate">
            {t.statPendingCalls}
          </span>
          <span className="block text-[26px] font-extrabold text-[#111318] tabular-nums tracking-tight leading-none mt-2.5 truncate">
            {campaign.pendingCalls.toLocaleString()}
          </span>
          <span className="block text-[11px] text-[#8A909E] mt-2 truncate">
            of {campaign.totalContacts.toLocaleString()} {t.statTotalContacts.toLowerCase()}
          </span>
        </div>

        {/* Frosted Card 3: Answered */}
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] flex flex-col justify-between min-w-0">
          <span className="block text-xs font-medium text-[#8A909E] truncate">
            {t.outcomeAnswered}
          </span>
          <span className="block text-[24px] font-extrabold text-emerald-700 tabular-nums tracking-tight leading-none mt-2 truncate">
            {campaign.answeredCalls}
          </span>
          <span className="block text-[11px] text-[#8A909E] mt-1.5 truncate">
            Connected calls
          </span>
        </div>

        {/* Frosted Card 4: No Answer & Failed */}
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] flex flex-col justify-between min-w-0">
          <span className="block text-xs font-medium text-[#8A909E] truncate">
            {t.outcomeNoAnswer} / {t.outcomeFailed}
          </span>
          <span className="block text-[24px] font-extrabold text-[#111318] tabular-nums tracking-tight leading-none mt-2 truncate">
            {campaign.noAnswerCalls + campaign.failedCalls}
          </span>
          <span className="block text-[11px] text-[#8A909E] mt-1.5 truncate">
            {campaign.noAnswerCalls} missed · {campaign.failedCalls} failed
          </span>
        </div>
      </div>
    </section>
  );
};
