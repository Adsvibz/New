import React from 'react';
import { CampaignOverviewStats } from '../types/campaign';
import { Translations } from '../i18n/translations';
import { TrendingUp } from 'lucide-react';

interface CampaignOverviewProps {
  stats: CampaignOverviewStats;
  t: Translations;
}

export const CampaignOverview: React.FC<CampaignOverviewProps> = ({ stats, t }) => {
  return (
    <section className="px-[20px] pt-2 pb-2" aria-label="Overview Statistics">
      {/* 2-Column Bento Grid (drops to 1 column under 340px) */}
      <div className="bento-grid-2 gap-3.5">
        {/* Single Red-Orange (#E2532B) Hero Card: Called & Completion % */}
        <div className="rounded-[24px] bg-[#E2532B] text-white p-4 sm:p-5 shadow-[0_16px_38px_-10px_rgba(226,83,43,0.38)] flex flex-col justify-between min-w-0">
          <span className="block text-xs font-medium text-white/90 truncate">
            {t.statCompletedCalls}
          </span>

          <div className="mt-3 flex items-baseline justify-between gap-2">
            <span className="text-[28px] font-extrabold tabular-nums tracking-tight leading-none truncate">
              {stats.completedCalls.toLocaleString()}
            </span>
            <span className="inline-flex items-center gap-1 text-[11px] font-semibold bg-white/20 text-white px-2.5 py-0.5 rounded-full tabular-nums shrink-0">
              <TrendingUp className="w-3 h-3" strokeWidth={2} />
              <strong>{stats.overallProgress}%</strong>
            </span>
          </div>

          <div className="mt-3">
            <div className="w-full h-1.5 bg-white/25 rounded-full overflow-hidden">
              <div
                className="h-full bg-white rounded-full animate-bar-grow transition-all duration-500"
                style={{ width: `${Math.min(100, Math.max(0, stats.overallProgress))}%` }}
              />
            </div>
            <span className="block text-[10.5px] text-white/80 mt-1.5 truncate">
              {stats.overallProgress}% completed
            </span>
          </div>
        </div>

        {/* Frosted Card 2: Total Contacts */}
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 sm:p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] flex flex-col justify-between min-w-0">
          <span className="block text-xs font-medium text-[#8A909E] truncate">
            {t.statTotalContacts}
          </span>
          <span className="block text-[28px] font-extrabold text-[#111318] tabular-nums tracking-tight leading-none mt-3 truncate">
            {stats.totalContacts.toLocaleString()}
          </span>
          <span className="block text-[11px] text-[#8A909E] mt-3 truncate">
            Total queue size
          </span>
        </div>

        {/* Frosted Card 3: Campaigns */}
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 sm:p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] flex flex-col justify-between min-w-0">
          <span className="block text-xs font-medium text-[#8A909E] truncate">
            {t.statTotalCampaigns}
          </span>
          <span className="block text-[26px] font-extrabold text-[#111318] tabular-nums tracking-tight leading-none mt-2.5 truncate">
            {stats.totalCampaigns}
          </span>
          <span className="block text-[11px] text-[#8A909E] mt-2 truncate">
            {stats.activeCount} active
          </span>
        </div>

        {/* Frosted Card 4: Remaining */}
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 sm:p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] flex flex-col justify-between min-w-0">
          <span className="block text-xs font-medium text-[#8A909E] truncate">
            {t.statPendingCalls}
          </span>
          <span className="block text-[26px] font-extrabold text-[#111318] tabular-nums tracking-tight leading-none mt-2.5 truncate">
            {stats.pendingCalls.toLocaleString()}
          </span>
          <span className="block text-[11px] text-[#8A909E] mt-2 truncate">
            Pending outreach
          </span>
        </div>
      </div>
    </section>
  );
};
