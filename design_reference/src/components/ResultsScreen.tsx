import React, { useState, useMemo } from 'react';
import {
  ChevronDown,
  Share2,
  Globe,
  Tag,
  MessageCircle,
  Search,
  X,
  TrendingUp,
} from 'lucide-react';
import {
  Campaign,
  CampaignContact,
  CallStage,
  ContactCallStatus,
} from '../types/campaign';
import { Translations, Language } from '../i18n/translations';
import { formatDuration, formatSimLabel, formatTimeAgo } from '../utils/campaignLogic';

interface ResultsScreenProps {
  campaigns: Campaign[];
  selectedCampaignId: string | null;
  currentLanguage: Language;
  onSelectCampaign: (campaignId: string) => void;
  onToggleLanguage: () => void;
  onOpenStageSheet: (campaignId: string, contact: CampaignContact) => void;
  onOpenWhatsApp: (phone: string, name: string) => void;
  onShareCampaignReport: (campaign: Campaign) => void;
  t: Translations;
}

export const ResultsScreen: React.FC<ResultsScreenProps> = ({
  campaigns,
  selectedCampaignId,
  currentLanguage,
  onSelectCampaign,
  onToggleLanguage,
  onOpenStageSheet,
  onOpenWhatsApp,
  onShareCampaignReport,
  t,
}) => {
  const campaign =
    campaigns.find((c) => c.id === selectedCampaignId) ||
    (campaigns.length > 0 ? campaigns[0] : null);

  const [outcomeFilter, setOutcomeFilter] = useState<ContactCallStatus | 'all'>('all');
  const [stageFilter, setStageFilter] = useState<CallStage | 'all'>('all');
  const [searchQuery, setSearchQuery] = useState('');

  // Stage counts across the selected campaign
  const stageStats = useMemo(() => {
    const counts: Record<CallStage, number> = {
      potential: 0,
      call_back: 0,
      whatsapp: 0,
      voicemail: 0,
      not_interested: 0,
      none: 0,
    };
    if (!campaign) return { counts, totalClassified: 0 };

    for (const c of campaign.contacts) {
      counts[c.stage || 'none'] = (counts[c.stage || 'none'] || 0) + 1;
    }
    const totalClassified =
      counts.potential +
      counts.call_back +
      counts.whatsapp +
      counts.voicemail +
      counts.not_interested;

    return { counts, totalClassified };
  }, [campaign]);

  const filteredContacts = useMemo(() => {
    if (!campaign) return [];
    const q = searchQuery.trim().toLowerCase();
    return campaign.contacts.filter((c) => {
      if (outcomeFilter !== 'all' && c.status !== outcomeFilter) return false;
      if (stageFilter !== 'all' && c.stage !== stageFilter) return false;
      if (q) {
        const matchName = c.name.toLowerCase().includes(q);
        const matchPhone = c.phone.toLowerCase().includes(q);
        const matchNotes = c.notes?.toLowerCase().includes(q) ?? false;
        if (!matchName && !matchPhone && !matchNotes) return false;
      }
      return true;
    });
  }, [campaign, outcomeFilter, stageFilter, searchQuery]);

  const stageItems: Array<{ id: CallStage; label: string; barColor: string }> = [
    { id: 'potential', label: t.stagePotential, barColor: 'bg-[#E2532B]' },
    { id: 'call_back', label: t.stageCallBack, barColor: 'bg-[#111318]' },
    { id: 'whatsapp', label: t.stageWhatsapp, barColor: 'bg-emerald-600' },
    { id: 'voicemail', label: t.stageVoicemail, barColor: 'bg-amber-600' },
    { id: 'not_interested', label: t.stageNotInterested, barColor: 'bg-slate-400' },
  ];

  const getStageLabel = (stage: CallStage) => {
    const found = stageItems.find((s) => s.id === stage);
    return found ? found.label : null;
  };

  if (!campaign) {
    return (
      <div className="flex-1 bg-transparent flex flex-col items-center justify-center p-8 text-center">
        <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-6 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)]">
          <h2 className="text-[16px] font-bold text-[#111318]">{t.emptyTitle}</h2>
          <p className="text-xs text-[#8A909E] mt-1">{t.emptySubtitle}</p>
        </div>
      </div>
    );
  }

  const hasActiveFilter =
    outcomeFilter !== 'all' || stageFilter !== 'all' || searchQuery.trim().length > 0;

  return (
    <div className="flex-1 overflow-y-auto bg-transparent flex flex-col pb-28">
      {/* 9.2 Clean Minimal Header & Campaign Switcher */}
      <header className="px-[20px] pt-3 pb-2.5 bg-transparent shrink-0">
        <div className="flex items-center justify-between gap-3">
          <div className="min-w-0 flex-1">
            <p className="text-[11px] font-medium text-[#8A909E] tabular-nums truncate">
              {formatSimLabel(campaign.simSlot, currentLanguage)} · {campaign.totalContacts}{' '}
              {t.statTotalContacts.toLowerCase()}
            </p>
            <h1 className="text-[22px] font-extrabold tracking-tight text-[#111318] leading-snug truncate">
              {t.navResults}
            </h1>
          </div>

          <div className="flex items-center gap-1.5 shrink-0">
            <button
              type="button"
              onClick={onToggleLanguage}
              className="min-w-[44px] min-h-[44px] w-11 h-11 rounded-full bg-white/60 backdrop-blur-md shadow-[0_8px_24px_-6px_rgba(17,19,24,0.05)] flex items-center justify-center text-[#8A909E] hover:text-[#111318] transition-all active:scale-95"
              aria-label="Toggle language"
            >
              <Globe className="w-[18px] h-[18px]" strokeWidth={1.75} />
            </button>
            <button
              type="button"
              onClick={() => onShareCampaignReport(campaign)}
              className="min-h-[44px] px-4 rounded-full text-xs font-semibold bg-[#111318] text-white hover:bg-[#1e222b] shadow-[0_10px_24px_-6px_rgba(17,19,24,0.22)] transition-all active:scale-95 flex items-center gap-1.5 whitespace-nowrap shrink-0"
              aria-label={t.actionShare}
            >
              <Share2 className="w-3.5 h-3.5" strokeWidth={1.75} />
              <span>{t.actionShare}</span>
            </button>
          </div>
        </div>

        {/* Sleek Frosted Campaign Selector Pill */}
        <div className="mt-3 relative">
          <select
            value={campaign.id}
            onChange={(e) => {
              onSelectCampaign(e.target.value);
              setOutcomeFilter('all');
              setStageFilter('all');
            }}
            aria-label="Select campaign for results"
            className="w-full h-11 pl-4 pr-10 rounded-full bg-white/60 backdrop-blur-md text-[13px] font-bold text-[#111318] shadow-[0_8px_24px_-8px_rgba(17,19,24,0.05)] appearance-none focus:outline-none cursor-pointer truncate transition-all"
          >
            {campaigns.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} ({c.progressPercentage}%)
              </option>
            ))}
          </select>
          <ChevronDown className="w-4 h-4 text-[#8A909E] pointer-events-none absolute right-4 top-1/2 -translate-y-1/2" />
        </div>
      </header>

      <div className="px-[20px] space-y-3.5">
        {/* 9.3 Overview Bento Grid (Hero Completion Card + Calm Summary Card) */}
        <section aria-label="Results Overview" className="bento-grid-2 gap-3.5">
          {/* Single Accent Hero Card: Completion & Progress Bar */}
          <div className="rounded-[24px] bg-[#E2532B] text-white p-5 shadow-[0_14px_34px_-8px_rgba(226,83,43,0.42)] flex flex-col justify-between min-w-0">
            <div className="flex items-center justify-between gap-2">
              <span className="text-xs font-medium text-white/90 truncate">
                {currentLanguage === 'ar' ? 'نسبة الإنجاز' : 'Completion'}
              </span>
              <span className="inline-flex items-center gap-0.5 text-[10.5px] font-semibold bg-white/20 text-white px-2 py-0.5 rounded-full tabular-nums shrink-0">
                <TrendingUp className="w-2.5 h-2.5" strokeWidth={2} />
                {campaign.completedCalls}/{campaign.totalContacts}
              </span>
            </div>

            <span className="block text-[30px] font-extrabold tabular-nums tracking-tight leading-none my-3">
              {campaign.progressPercentage}%
            </span>

            <div>
              <div className="w-full h-1.5 bg-white/25 rounded-full overflow-hidden">
                <div
                  className="h-full bg-white rounded-full animate-bar-grow transition-all duration-500"
                  style={{
                    width: `${Math.min(100, Math.max(0, campaign.progressPercentage))}%`,
                  }}
                />
              </div>
              <span className="block text-[11px] text-white/85 mt-1.5 truncate">
                {campaign.completedCalls} {t.statCompletedCalls.toLowerCase()}
              </span>
            </div>
          </div>

          {/* Frosted Card: Answered Rate & Remaining Queue */}
          <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.05)] flex flex-col justify-between min-w-0">
            <span className="block text-xs font-medium text-[#8A909E] truncate">
              {t.outcomeAnswered}
            </span>

            <div className="my-3 flex items-baseline gap-2">
              <span className="text-[30px] font-extrabold text-[#111318] tabular-nums tracking-tight leading-none">
                {campaign.answeredCalls}
              </span>
              <span className="text-xs font-semibold text-emerald-700 tabular-nums">
                {campaign.completedCalls > 0
                  ? `${Math.round((campaign.answeredCalls / campaign.completedCalls) * 100)}%`
                  : '0%'}
              </span>
            </div>

            <span className="block text-[11px] text-[#8A909E] truncate">
              {campaign.pendingCalls} {t.statPendingCalls.toLowerCase()}
            </span>
          </div>
        </section>

        {/* 9.4 Simply Elegant Outcome Breakdown Card (2x2 Tactile Filter Grid) */}
        <section
          aria-label="Call Outcomes"
          className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 sm:p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.05)] animate-card-cascade"
        >
          <div className="flex items-center justify-between mb-3 px-0.5">
            <h2 className="text-xs font-bold text-[#111318] tracking-tight">
              {currentLanguage === 'ar' ? 'نتائج الاتصال' : 'Call Outcomes'}
            </h2>
            {outcomeFilter !== 'all' ? (
              <button
                type="button"
                onClick={() => setOutcomeFilter('all')}
                className="text-[11px] font-semibold text-[#E2532B] hover:underline"
              >
                {currentLanguage === 'ar' ? 'إظهار الكل' : 'Show all'}
              </button>
            ) : (
              <span className="text-[11px] text-[#8A909E]">
                {currentLanguage === 'ar' ? 'اضغط للتصفية' : 'Tap to filter'}
              </span>
            )}
          </div>

          <div className="bento-grid-2 gap-2.5">
            {(
              [
                {
                  id: 'answered' as const,
                  label: t.outcomeAnswered,
                  count: campaign.answeredCalls,
                  dot: 'bg-emerald-500',
                },
                {
                  id: 'no_answer' as const,
                  label: t.outcomeNoAnswer,
                  count: campaign.noAnswerCalls,
                  dot: 'bg-amber-500',
                },
                {
                  id: 'failed' as const,
                  label: t.outcomeFailed,
                  count: campaign.failedCalls,
                  dot: 'bg-[#E2532B]',
                },
                {
                  id: 'unverified' as const,
                  label: t.outcomeUnverified,
                  count: campaign.unverifiedCalls,
                  dot: 'bg-slate-400',
                },
              ]
            ).map((cell) => {
              const isSelected = outcomeFilter === cell.id;
              return (
                <button
                  key={cell.id}
                  type="button"
                  onClick={() =>
                    setOutcomeFilter((prev) => (prev === cell.id ? 'all' : cell.id))
                  }
                  className={`min-h-[48px] px-3.5 py-2.5 rounded-[18px] flex items-center justify-between gap-2 transition-all active:scale-[0.98] ${
                    isSelected
                      ? 'bg-[#111318] text-white shadow-[0_8px_20px_-6px_rgba(17,19,24,0.25)]'
                      : 'bg-[#111318]/[0.035] hover:bg-[#111318]/[0.06] text-[#111318]'
                  }`}
                >
                  <div className="flex items-center gap-2 min-w-0">
                    <span
                      className={`w-2 h-2 rounded-full shrink-0 ${
                        isSelected ? 'bg-white' : cell.dot
                      }`}
                    />
                    <span
                      className={`text-xs font-semibold truncate ${
                        isSelected ? 'text-white' : 'text-[#4B5563]'
                      }`}
                    >
                      {cell.label}
                    </span>
                  </div>
                  <span
                    className={`text-[15px] font-extrabold tabular-nums shrink-0 ${
                      isSelected ? 'text-white' : 'text-[#111318]'
                    }`}
                  >
                    {cell.count}
                  </span>
                </button>
              );
            })}
          </div>
        </section>

        {/* 9.5 Minimal Stage Distribution Card */}
        <section
          className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 sm:p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.05)] animate-card-cascade"
          aria-label="Stage Distribution"
        >
          <div className="flex items-center justify-between mb-3 px-0.5">
            <h2 className="text-xs font-bold text-[#111318] tracking-tight">
              {currentLanguage === 'ar' ? 'مراحل المتابعة' : 'Follow-up Stages'}
            </h2>
            {stageFilter !== 'all' ? (
              <button
                type="button"
                onClick={() => setStageFilter('all')}
                className="text-[11px] font-semibold text-[#E2532B] hover:underline"
              >
                {currentLanguage === 'ar' ? 'إلغاء التصفية' : 'Clear stage'}
              </button>
            ) : (
              <span className="text-[11px] text-[#8A909E] tabular-nums">
                {stageStats.totalClassified}{' '}
                {currentLanguage === 'ar' ? 'مصنفة' : 'classified'}
              </span>
            )}
          </div>

          {stageStats.totalClassified > 0 && (
            <div className="w-full h-2 rounded-full bg-[#111318]/[0.05] overflow-hidden flex gap-0.5 mb-3">
              {stageItems.map((st) => {
                const count = stageStats.counts[st.id] || 0;
                if (count === 0) return null;
                const pct = Math.max(
                  8,
                  Math.round((count / stageStats.totalClassified) * 100)
                );
                return (
                  <div
                    key={st.id}
                    className={`h-full first:rounded-l-full last:rounded-r-full ${st.barColor} transition-all duration-300`}
                    style={{ width: `${pct}%` }}
                    title={`${st.label}: ${count}`}
                  />
                );
              })}
            </div>
          )}

          {/* Clean Horizontal Scrollable Stage Pills */}
          <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-0.5">
            {stageItems.map((st) => {
              const count = stageStats.counts[st.id] || 0;
              const isSelected = stageFilter === st.id;
              return (
                <button
                  key={st.id}
                  type="button"
                  onClick={() =>
                    setStageFilter((prev) => (prev === st.id ? 'all' : st.id))
                  }
                  className={`min-h-[34px] px-3 py-1 rounded-full text-xs flex items-center gap-1.5 transition-all whitespace-nowrap shrink-0 ${
                    isSelected
                      ? 'bg-[#111318] text-white font-semibold shadow-xs'
                      : 'bg-[#111318]/[0.04] text-[#4B5563] hover:text-[#111318]'
                  }`}
                >
                  <span
                    className={`w-1.5 h-1.5 rounded-full ${
                      isSelected ? 'bg-white' : st.barColor
                    }`}
                  />
                  <span>{st.label}</span>
                  <span
                    className={`tabular-nums font-bold ${
                      isSelected ? 'text-white/90' : 'text-[#111318]'
                    }`}
                  >
                    {count}
                  </span>
                </button>
              );
            })}
          </div>
        </section>

        {/* 9.6 Results Contact List Card */}
        <section
          aria-label="Results Contact List"
          className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 sm:p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.05)]"
        >
          {/* Search within Results */}
          <div className="relative mb-3">
            <Search
              className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-[#8A909E]"
              strokeWidth={1.75}
            />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder={
                currentLanguage === 'ar'
                  ? 'ابحث في النتائج والملاحظات...'
                  : 'Search contacts or feedback notes...'
              }
              className="w-full h-10 pl-10 pr-8 text-xs font-medium bg-[#111318]/[0.04] text-[#111318] placeholder:text-[#8A909E] rounded-full focus:outline-none focus:bg-[#111318]/[0.07] transition-colors"
            />
            {searchQuery && (
              <button
                type="button"
                onClick={() => setSearchQuery('')}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 p-1 text-[#8A909E] hover:text-[#111318]"
                aria-label="Clear results search"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          <div className="divide-y divide-[#111318]/[0.05]">
            {filteredContacts.length === 0 ? (
              <div className="py-10 text-center">
                <p className="text-sm font-bold text-[#111318]">
                  {currentLanguage === 'ar'
                    ? 'لا توجد نتائج مطابقة'
                    : 'No matching contact results'}
                </p>
                {hasActiveFilter && (
                  <button
                    type="button"
                    onClick={() => {
                      setOutcomeFilter('all');
                      setStageFilter('all');
                      setSearchQuery('');
                    }}
                    className="mt-2.5 min-h-[36px] px-4 rounded-full bg-[#111318] text-white text-xs font-semibold"
                  >
                    {t.clearSearch}
                  </button>
                )}
              </div>
            ) : (
              filteredContacts.map((contact) => {
                const stageLabel = getStageLabel(contact.stage);
                const statusDotColor =
                  contact.status === 'answered'
                    ? 'bg-emerald-500'
                    : contact.status === 'no_answer'
                    ? 'bg-amber-500'
                    : contact.status === 'failed'
                    ? 'bg-[#E2532B]'
                    : 'bg-slate-300';

                return (
                  <div
                    key={contact.id}
                    className="py-3.5 first:pt-1 last:pb-0 flex items-start justify-between gap-3"
                  >
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <span
                          className={`w-2 h-2 rounded-full shrink-0 ${statusDotColor}`}
                          aria-hidden="true"
                        />
                        <span className="text-[14px] font-bold text-[#111318] truncate">
                          {contact.name}
                        </span>
                        {stageLabel && (
                          <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-[#111318]/[0.05] text-[#111318] whitespace-nowrap shrink-0">
                            {stageLabel}
                          </span>
                        )}
                      </div>

                      <div className="pl-4 mt-0.5 flex items-center flex-wrap gap-1.5 text-[11.5px] text-[#8A909E]">
                        <span className="font-mono tabular-nums text-[#4B5563]" dir="ltr">
                          {contact.phone}
                        </span>
                        <span aria-hidden="true">·</span>
                        <span className="capitalize font-medium">
                          {contact.status.replace('_', ' ')}
                        </span>
                        {contact.callDurationSeconds > 0 && (
                          <>
                            <span aria-hidden="true">·</span>
                            <span className="tabular-nums">
                              {formatDuration(contact.callDurationSeconds)}
                            </span>
                          </>
                        )}
                        {contact.calledAt && (
                          <>
                            <span aria-hidden="true">·</span>
                            <span className="tabular-nums">
                              {formatTimeAgo(contact.calledAt)}
                            </span>
                          </>
                        )}
                      </div>

                      {contact.notes && (
                        <p className="ml-4 mt-1.5 pl-2.5 border-l-2 border-[#E2532B]/40 text-xs text-[#4B5563] leading-relaxed">
                          {contact.notes}
                        </p>
                      )}
                    </div>

                    {/* Minimal Follow-up Actions */}
                    <div className="flex items-center gap-1.5 shrink-0 pt-0.5">
                      <button
                        type="button"
                        onClick={() => onOpenStageSheet(campaign.id, contact)}
                        className="min-h-[36px] px-3 rounded-full bg-[#111318]/[0.05] hover:bg-[#111318]/[0.09] text-[11px] font-semibold text-[#111318] flex items-center gap-1 transition-colors whitespace-nowrap"
                        aria-label={`Classify ${contact.name}`}
                      >
                        <Tag className="w-3 h-3 text-[#6B7280]" strokeWidth={1.75} />
                        <span>{stageLabel ? 'Stage' : 'Classify'}</span>
                      </button>

                      <button
                        type="button"
                        onClick={() => onOpenWhatsApp(contact.phone, contact.name)}
                        className="min-w-[36px] min-h-[36px] w-9 h-9 rounded-full bg-emerald-500/10 flex items-center justify-center text-emerald-700 hover:bg-emerald-500/20 transition-colors"
                        title="Open WhatsApp follow-up"
                        aria-label={`WhatsApp ${contact.name}`}
                      >
                        <MessageCircle className="w-3.5 h-3.5" strokeWidth={1.75} />
                      </button>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </section>
      </div>
    </div>
  );
};
