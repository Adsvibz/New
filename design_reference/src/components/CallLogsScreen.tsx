import React, { useState, useMemo } from 'react';
import {
  Search,
  Share2,
  Trash2,
  Globe,
  X,
} from 'lucide-react';
import { Campaign, ContactCallStatus } from '../types/campaign';
import { Translations, Language } from '../i18n/translations';
import { formatDuration } from '../utils/campaignLogic';

interface CallLogsScreenProps {
  campaigns: Campaign[];
  isHistoryCleared: boolean;
  currentLanguage: Language;
  onToggleLanguage: () => void;
  onShareHistoryLog: () => void;
  onClearHistoryLog: () => void;
  t: Translations;
}

interface CallLogRow {
  id: string;
  contactName: string;
  phone: string;
  campaignId: string;
  campaignName: string;
  status: ContactCallStatus;
  durationSeconds: number;
  attemptNumber: number;
  timestampMs: number;
}

export const CallLogsScreen: React.FC<CallLogsScreenProps> = ({
  campaigns,
  isHistoryCleared,
  currentLanguage,
  onToggleLanguage,
  onShareHistoryLog,
  onClearHistoryLog,
  t,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<
    'all' | 'answered' | 'no_answer' | 'failed' | 'unverified'
  >('all');
  const [isClearConfirmOpen, setIsClearConfirmOpen] = useState(false);

  const callLogs = useMemo<CallLogRow[]>(() => {
    if (isHistoryCleared) return [];
    const rows: CallLogRow[] = [];
    for (const camp of campaigns) {
      for (const c of camp.contacts) {
        if (c.status !== 'pending' && c.status !== 'calling') {
          rows.push({
            id: `${camp.id}-${c.id}`,
            contactName: c.name,
            phone: c.phone,
            campaignId: camp.id,
            campaignName: camp.name,
            status: c.status,
            durationSeconds: c.callDurationSeconds || 0,
            attemptNumber: Math.max(1, c.attempts || 1),
            timestampMs: c.calledAt || new Date(camp.lastActivity).getTime(),
          });
        }
      }
    }
    return rows.sort((a, b) => b.timestampMs - a.timestampMs);
  }, [campaigns, isHistoryCleared]);

  const filteredLogs = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return callLogs.filter((log) => {
      if (statusFilter !== 'all' && log.status !== statusFilter) return false;
      if (q) {
        const matchName = log.contactName.toLowerCase().includes(q);
        const matchPhone = log.phone.toLowerCase().includes(q);
        const matchCamp = log.campaignName.toLowerCase().includes(q);
        if (!matchName && !matchPhone && !matchCamp) return false;
      }
      return true;
    });
  }, [callLogs, searchQuery, statusFilter]);

  // Group entries by date: Today, Yesterday, Older (Part 10.5)
  const groupedLogs = useMemo(() => {
    const groups: Array<{ label: string; items: CallLogRow[] }> = [];
    const todayStart = new Date();
    todayStart.setHours(0, 0, 0, 0);
    const yesterdayStart = new Date(todayStart.getTime() - 24 * 3600 * 1000);

    const map = new Map<string, CallLogRow[]>();

    for (const item of filteredLogs) {
      let groupLabel = '';
      if (item.timestampMs >= todayStart.getTime()) {
        groupLabel = currentLanguage === 'ar' ? 'اليوم' : 'Today';
      } else if (item.timestampMs >= yesterdayStart.getTime()) {
        groupLabel = currentLanguage === 'ar' ? 'أمس' : 'Yesterday';
      } else {
        groupLabel = new Date(item.timestampMs).toLocaleDateString(
          currentLanguage === 'ar' ? 'ar-EG' : 'en-US',
          { month: 'short', day: 'numeric', year: 'numeric' }
        );
      }
      if (!map.has(groupLabel)) {
        map.set(groupLabel, []);
      }
      map.get(groupLabel)!.push(item);
    }

    for (const [label, items] of map.entries()) {
      groups.push({ label, items });
    }
    return groups;
  }, [filteredLogs, currentLanguage]);

  const answeredCount = useMemo(
    () => callLogs.filter((l) => l.status === 'answered').length,
    [callLogs]
  );
  const noAnswerCount = useMemo(
    () => callLogs.filter((l) => l.status === 'no_answer').length,
    [callLogs]
  );

  const formatClockTime = (ms: number) => {
    try {
      return new Date(ms).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
      return '09:15 AM';
    }
  };

  return (
    <div className="flex-1 overflow-y-auto bg-transparent flex flex-col pb-24">
      {/* 10.2 Screen Header */}
      <header className="px-[20px] pt-3 pb-2 bg-transparent shrink-0">
        <div className="flex items-center justify-between gap-2.5">
          <div className="min-w-0 flex-1">
            <p className="text-[11px] font-medium text-[#8A909E] tabular-nums truncate">
              {callLogs.length} {currentLanguage === 'ar' ? 'مكالمة مسجلة محلياً' : 'calls recorded in local log'}
            </p>
            <h1 className="text-[22px] font-extrabold tracking-tight text-[#111318] leading-snug truncate">
              {currentLanguage === 'ar' ? 'سجل المكالمات' : 'Call History'}
            </h1>
          </div>

          <div className="flex items-center gap-1.5 shrink-0">
            <button
              type="button"
              onClick={onToggleLanguage}
              className="min-w-[44px] min-h-[44px] w-11 h-11 rounded-full bg-white/60 backdrop-blur-md shadow-[0_8px_24px_-6px_rgba(17,19,24,0.06)] flex items-center justify-center text-[#8A909E] hover:text-[#111318] transition-all active:scale-95"
              aria-label="Toggle language"
            >
              <Globe className="w-[18px] h-[18px]" strokeWidth={1.75} />
            </button>
            <button
              type="button"
              onClick={onShareHistoryLog}
              disabled={callLogs.length === 0}
              className="min-h-[44px] px-4 rounded-full text-xs font-semibold bg-[#111318] text-white hover:bg-[#1e222b] disabled:opacity-40 shadow-[0_10px_24px_-6px_rgba(17,19,24,0.28)] transition-all flex items-center gap-1.5 whitespace-nowrap shrink-0"
              aria-label="Share Log"
            >
              <Share2 className="w-3.5 h-3.5" strokeWidth={1.75} />
              <span>{currentLanguage === 'ar' ? 'مشاركة' : 'Share Log'}</span>
            </button>
            <button
              type="button"
              onClick={() => setIsClearConfirmOpen(true)}
              disabled={callLogs.length === 0}
              className="min-w-[44px] min-h-[44px] w-11 h-11 rounded-full bg-white/60 backdrop-blur-md shadow-[0_8px_24px_-6px_rgba(17,19,24,0.06)] text-[#8A909E] hover:text-[#E2532B] disabled:opacity-40 transition-all flex items-center justify-center shrink-0"
              title="Clear Log"
              aria-label="Clear Log"
            >
              <Trash2 className="w-4 h-4" strokeWidth={1.75} />
            </button>
          </div>
        </div>
      </header>

      <div className="px-[20px] space-y-3.5 flex-1">
        {/* 10.3 History Overview Bento Grid (Single Red-Orange #E2532B Hero Card + Frosted Cards) */}
        <section aria-label="Call History Overview">
          <div className="bento-grid-2 gap-3.5">
            {/* Single Red-Orange (#E2532B) Hero Card: Total Calls */}
            <div className="rounded-[24px] bg-[#E2532B] text-white p-4 sm:p-5 shadow-[0_16px_38px_-10px_rgba(226,83,43,0.38)] flex flex-col justify-between min-w-0">
              <span className="block text-xs font-medium text-white/90 truncate">
                {currentLanguage === 'ar' ? 'إجمالي المكالمات' : 'Total Calls'}
              </span>
              <span className="block text-[28px] font-extrabold tabular-nums tracking-tight leading-none mt-2.5">
                {callLogs.length}
              </span>
              <span className="block text-[11px] text-white/80 mt-2 truncate">
                Chronological log
              </span>
            </div>

            {/* Frosted Card: Answered & No Answer */}
            <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 sm:p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] flex flex-col justify-between min-w-0">
              <div className="flex items-center justify-between gap-2">
                <div>
                  <span className="block text-xs font-medium text-[#8A909E]">
                    {t.outcomeAnswered}
                  </span>
                  <span className="block text-[24px] font-extrabold text-emerald-700 tabular-nums leading-none mt-1.5">
                    {answeredCount}
                  </span>
                </div>
                <div className="text-right">
                  <span className="block text-xs font-medium text-[#8A909E]">
                    {t.outcomeNoAnswer}
                  </span>
                  <span className="block text-[24px] font-extrabold text-[#111318] tabular-nums leading-none mt-1.5">
                    {noAnswerCount}
                  </span>
                </div>
              </div>
              <span className="block text-[11px] text-[#8A909E] mt-2.5 truncate">
                Verified outcomes
              </span>
            </div>
          </div>
        </section>

        {/* 10.4 Filtering & Search */}
        <div className="space-y-2.5">
          <div className="relative">
            <Search
              className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-[#8A909E] pointer-events-none"
              strokeWidth={1.75}
            />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder={
                currentLanguage === 'ar'
                  ? 'ابحث بالاسم، الرقم، أو الحملة...'
                  : 'Search contact, phone number, or campaign...'
              }
              className="w-full h-11 pl-10 pr-9 text-xs font-medium bg-white/60 backdrop-blur-md text-[#111318] placeholder:text-[#8A909E] rounded-full shadow-[0_10px_28px_-8px_rgba(17,19,24,0.05)] focus:outline-none focus:bg-white/95 transition-all"
            />
            {searchQuery && (
              <button
                type="button"
                onClick={() => setSearchQuery('')}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 p-1 text-[#8A909E] hover:text-[#111318] rounded-full"
                aria-label="Clear call history search"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-0.5">
            {(
              [
                { id: 'all', label: t.filterAll },
                { id: 'answered', label: t.outcomeAnswered },
                { id: 'no_answer', label: t.outcomeNoAnswer },
                { id: 'failed', label: t.outcomeFailed },
                { id: 'unverified', label: t.outcomeUnverified },
              ] as const
            ).map((tab) => {
              const isActive = statusFilter === tab.id;
              return (
                <button
                  key={tab.id}
                  type="button"
                  onClick={() => setStatusFilter(tab.id)}
                  className={`min-h-[36px] px-3.5 py-1.5 rounded-full text-xs font-semibold transition-all whitespace-nowrap shrink-0 ${
                    isActive
                      ? 'bg-[#111318] text-white shadow-[0_6px_16px_-4px_rgba(17,19,24,0.25)]'
                      : 'bg-white/60 backdrop-blur-md text-[#8A909E] hover:text-[#111318]'
                  }`}
                >
                  {tab.label}
                </button>
              );
            })}
          </div>
        </div>

        {/* 10.5 Date Grouping Cards & 10.6 Call Rows */}
        <div>
          {groupedLogs.length === 0 ? (
            <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-8 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] text-center">
              <p className="text-sm font-bold text-[#111318]">
                {currentLanguage === 'ar' ? 'لا يوجد سجل مكالمات' : 'No call history entries'}
              </p>
              <p className="text-xs text-[#8A909E] mt-1 max-w-xs mx-auto">
                {currentLanguage === 'ar'
                  ? 'ستظهر المكالمات الصادرة عبر الحملات هنا مرتبة زمنياً.'
                  : 'Calls placed through your campaigns will be logged chronologically here.'}
              </p>
            </div>
          ) : (
            <div className="space-y-3.5">
              {groupedLogs.map((group) => (
                <div
                  key={group.label}
                  className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)]"
                >
                  <div className="pb-2 mb-1 text-[11px] font-bold text-[#8A909E] uppercase tracking-wider border-b border-[#111318]/[0.06]">
                    {group.label}
                  </div>
                  <div className="divide-y divide-[#111318]/[0.05]">
                    {group.items.map((log) => (
                      <div
                        key={log.id}
                        className="py-3 first:pt-1.5 last:pb-0 flex items-start justify-between gap-3"
                      >
                        <div className="min-w-0 flex-1">
                          <div className="flex items-center gap-2">
                            <span className="text-sm font-bold text-[#111318] truncate">
                              {log.contactName}
                            </span>
                            {log.attemptNumber > 1 && (
                              <span className="text-[10.5px] font-semibold text-[#8A909E] tabular-nums shrink-0">
                                · #{log.attemptNumber}
                              </span>
                            )}
                          </div>

                          <div className="flex items-center gap-1.5 text-xs text-[#8A909E] mt-0.5 min-w-0">
                            <span
                              className="font-mono tabular-nums text-[#4B5563] whitespace-nowrap shrink-0"
                              dir="ltr"
                            >
                              {log.phone}
                            </span>
                            <span aria-hidden="true" className="shrink-0">·</span>
                            <span className="truncate">{log.campaignName}</span>
                          </div>
                        </div>

                        <div className="text-right shrink-0">
                          <div className="flex items-center justify-end gap-1.5 text-xs tabular-nums">
                            <span
                              className={`font-semibold capitalize ${
                                log.status === 'answered'
                                  ? 'text-emerald-700'
                                  : log.status === 'no_answer'
                                  ? 'text-amber-700'
                                  : log.status === 'failed'
                                  ? 'text-[#E2532B]'
                                  : 'text-[#6B7280]'
                              }`}
                            >
                              {log.status.replace('_', ' ')}
                            </span>
                            {log.durationSeconds > 0 && (
                              <>
                                <span aria-hidden="true">·</span>
                                <span className="text-[#111318] font-bold">
                                  {formatDuration(log.durationSeconds)}
                                </span>
                              </>
                            )}
                          </div>
                          <div className="text-[11px] text-[#8A909E] tabular-nums mt-0.5">
                            {formatClockTime(log.timestampMs)}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Clear Log Confirmation Dialog */}
      {isClearConfirmOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[#111318]/40 backdrop-blur-xs">
          <div
            className="w-full max-w-sm bg-white/95 backdrop-blur-2xl rounded-[24px] shadow-[0_24px_60px_-12px_rgba(17,19,24,0.25)] p-5"
            role="alertdialog"
            aria-modal="true"
          >
            <h3 className="text-[16px] font-bold text-[#111318]">
              {currentLanguage === 'ar' ? 'مسح سجل المكالمات' : 'Clear Call History'}
            </h3>
            <p className="text-xs text-[#6B7280] mt-1.5 leading-relaxed">
              {currentLanguage === 'ar'
                ? 'هل أنت متأكد من مسح سجل المكالمات المحلي؟ لن يؤثر ذلك على جهات الاتصال داخل الحملات.'
                : 'Are you sure you want to clear the local call history log? Campaign contacts and saved notes will remain intact.'}
            </p>
            <div className="flex items-center justify-end gap-2 mt-4 pt-3 border-t border-[#111318]/[0.06]">
              <button
                type="button"
                onClick={() => setIsClearConfirmOpen(false)}
                className="min-h-[40px] px-4 text-xs font-semibold text-[#111318] bg-[#111318]/[0.06] rounded-full"
              >
                {t.actionCancel}
              </button>
              <button
                type="button"
                onClick={() => {
                  setIsClearConfirmOpen(false);
                  onClearHistoryLog();
                }}
                className="min-h-[40px] px-4 text-xs font-semibold bg-[#111318] text-white rounded-full"
              >
                {currentLanguage === 'ar' ? 'مسح السجل' : 'Clear Log'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
