import React, { useState, useRef, useEffect } from 'react';
import {
  MoreHorizontal,
  Edit2,
  Trash2,
  Share2,
  RotateCcw,
  Users2,
  BarChart3,
  Check,
  Play,
  FolderOpen,
} from 'lucide-react';
import { Campaign } from '../types/campaign';
import { Translations, Language } from '../i18n/translations';
import { formatSimLabel, formatTimeAgo } from '../utils/campaignLogic';

interface CampaignCardProps {
  campaign: Campaign;
  isSelected: boolean;
  isMultiSelectMode: boolean;
  isRunningSession?: boolean;
  currentLanguage?: Language;
  onToggleSelect: (id: string) => void;
  onOpenDetails: (campaign: Campaign) => void;
  onEdit: (campaign: Campaign) => void;
  onRename: (campaign: Campaign) => void;
  onStartOrResumeDialer: (campaign: Campaign) => void;
  onResetProgress: (campaign: Campaign) => void;
  onRemoveDuplicates: (campaign: Campaign) => void;
  onViewReport: (campaign: Campaign) => void;
  onShare: (campaign: Campaign) => void;
  onDelete: (campaign: Campaign) => void;
  t: Translations;
}

export const CampaignCard: React.FC<CampaignCardProps> = ({
  campaign,
  isSelected,
  isMultiSelectMode,
  isRunningSession = false,
  currentLanguage = 'en',
  onToggleSelect,
  onOpenDetails,
  onEdit,
  onRename,
  onStartOrResumeDialer,
  onResetProgress,
  onRemoveDuplicates,
  onViewReport,
  onShare,
  onDelete,
  t,
}) => {
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const longPressTimerRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    };
    if (menuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [menuOpen]);

  const handleTouchStart = () => {
    longPressTimerRef.current = setTimeout(() => {
      onToggleSelect(campaign.id);
    }, 450);
  };

  const handleTouchEnd = () => {
    if (longPressTimerRef.current) {
      clearTimeout(longPressTimerRef.current);
    }
  };

  const effectiveStatus = isRunningSession ? 'running' : campaign.status;

  const getStatusLabel = () => {
    switch (effectiveStatus) {
      case 'running':
        return (
          <span className="inline-flex items-center gap-1.5 text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-[#E2532B]/12 text-[#E2532B] whitespace-nowrap shrink-0">
            <span className="relative flex h-1.5 w-1.5">
              <span className="animate-welcome-ring absolute inline-flex h-full w-full rounded-full bg-[#E2532B] opacity-75" />
              <span className="relative inline-flex rounded-full h-1.5 w-1.5 bg-[#E2532B]" />
            </span>
            <span>{t.statusRunning}</span>
          </span>
        );
      case 'active':
        return (
          <span className="inline-flex items-center gap-1.5 text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-emerald-500/12 text-emerald-700 whitespace-nowrap shrink-0">
            <span className="relative flex h-1.5 w-1.5">
              <span className="animate-welcome-ring absolute inline-flex h-full w-full rounded-full bg-emerald-500 opacity-75" />
              <span className="relative inline-flex rounded-full h-1.5 w-1.5 bg-emerald-600" />
            </span>
            <span>{t.statusActive}</span>
          </span>
        );
      case 'paused':
        return (
          <span className="text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-amber-500/12 text-amber-700 whitespace-nowrap shrink-0">
            {t.statusPaused}
          </span>
        );
      case 'completed':
        return (
          <span className="text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-[#111318]/[0.06] text-[#6B7280] whitespace-nowrap shrink-0">
            {t.statusCompleted}
          </span>
        );
      case 'ready':
      default:
        return (
          <span className="text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-[#111318]/[0.06] text-[#6B7280] whitespace-nowrap shrink-0">
            {t.statusReady}
          </span>
        );
    }
  };

  return (
    <div
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      onClick={() => {
        if (isMultiSelectMode) {
          onToggleSelect(campaign.id);
        } else {
          onOpenDetails(campaign);
        }
      }}
      className={`group relative rounded-[24px] p-5 animate-card-cascade hover:-translate-y-0.5 active:scale-[0.985] transition-all duration-200 cursor-pointer select-none backdrop-blur-md ${
        isSelected
          ? 'bg-white/95 ring-2 ring-[#111318] shadow-[0_16px_40px_-10px_rgba(17,19,24,0.12)]'
          : 'bg-white/60 hover:bg-white/80 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)]'
      }`}
    >
      {/* Top Line: Name, Status & Overflow */}
      <div className="flex items-center justify-between gap-2.5">
        <div className="flex items-center gap-2 min-w-0 flex-1">
          {isMultiSelectMode && (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onToggleSelect(campaign.id);
              }}
              className={`w-5 h-5 rounded-full flex items-center justify-center transition-colors shrink-0 ${
                isSelected
                  ? 'bg-[#111318] text-white'
                  : 'bg-[#111318]/10 text-transparent'
              }`}
              aria-label={`Select ${campaign.name}`}
            >
              {isSelected && <Check className="w-3 h-3" strokeWidth={2.5} />}
            </button>
          )}

          <h3 className="text-[15px] font-bold text-[#111318] tracking-tight truncate">
            {campaign.name}
          </h3>
          {getStatusLabel()}
        </div>

        {/* Overflow Menu Trigger */}
        <div className="relative shrink-0" ref={menuRef}>
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              setMenuOpen(!menuOpen);
            }}
            className="min-w-[40px] min-h-[40px] w-10 h-10 -mr-1.5 rounded-full flex items-center justify-center text-[#8A909E] hover:text-[#111318] hover:bg-[#111318]/[0.05] transition-colors"
            aria-label={`Actions for ${campaign.name}`}
          >
            <MoreHorizontal className="w-4 h-4" strokeWidth={1.75} />
          </button>

          {menuOpen && (
            <div
              className="absolute right-0 top-full mt-1 w-56 bg-white/95 backdrop-blur-2xl rounded-[24px] shadow-[0_20px_48px_-12px_rgba(17,19,24,0.18)] py-2 z-30 divide-y divide-[#111318]/[0.06]"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="py-1 px-1.5">
                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onOpenDetails(campaign);
                  }}
                  className="w-full px-3 py-2 rounded-xl text-xs font-medium text-[#111318] hover:bg-[#111318]/[0.05] flex items-center gap-2.5 text-left"
                >
                  <FolderOpen className="w-4 h-4 text-[#8A909E]" strokeWidth={1.75} />
                  <span>{t.campaignDetails}</span>
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onStartOrResumeDialer(campaign);
                  }}
                  className="w-full px-3 py-2 rounded-xl text-xs font-medium text-[#111318] hover:bg-[#111318]/[0.05] flex items-center gap-2.5 text-left"
                >
                  <Play className="w-4 h-4 text-[#8A909E]" strokeWidth={1.75} />
                  <span>
                    {isRunningSession
                      ? 'Return to Live Session'
                      : campaign.completedCalls > 0 && campaign.pendingCalls > 0
                      ? t.actionResumeSession
                      : t.actionStartSession}
                  </span>
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onViewReport(campaign);
                  }}
                  className="w-full px-3 py-2 rounded-xl text-xs font-medium text-[#111318] hover:bg-[#111318]/[0.05] flex items-center gap-2.5 text-left"
                >
                  <BarChart3 className="w-4 h-4 text-[#8A909E]" strokeWidth={1.75} />
                  <span>{t.actionViewResults}</span>
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onEdit(campaign);
                  }}
                  disabled={isRunningSession}
                  className="w-full px-3 py-2 rounded-xl text-xs font-medium text-[#111318] hover:bg-[#111318]/[0.05] disabled:opacity-40 flex items-center gap-2.5 text-left"
                >
                  <Edit2 className="w-4 h-4 text-[#8A909E]" strokeWidth={1.75} />
                  <span>{t.actionEdit}</span>
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onRename(campaign);
                  }}
                  disabled={isRunningSession}
                  className="w-full px-3 py-2 rounded-xl text-xs font-medium text-[#111318] hover:bg-[#111318]/[0.05] disabled:opacity-40 flex items-center gap-2.5 text-left"
                >
                  <Edit2 className="w-4 h-4 text-[#8A909E]" strokeWidth={1.75} />
                  <span>{t.actionRename}</span>
                </button>
              </div>

              <div className="py-1 px-1.5">
                {campaign.duplicateContactsFound > 0 && (
                  <button
                    type="button"
                    onClick={() => {
                      setMenuOpen(false);
                      onRemoveDuplicates(campaign);
                    }}
                    disabled={isRunningSession}
                    className="w-full px-3 py-2 rounded-xl text-xs font-medium text-amber-700 hover:bg-amber-50/70 disabled:opacity-40 flex items-center gap-2.5 text-left"
                  >
                    <Users2 className="w-4 h-4 text-amber-700" strokeWidth={1.75} />
                    <span>
                      {t.actionRemoveDuplicates} ({campaign.duplicateContactsFound})
                    </span>
                  </button>
                )}

                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onResetProgress(campaign);
                  }}
                  disabled={isRunningSession}
                  className="w-full px-3 py-2 rounded-xl text-xs font-medium text-[#111318] hover:bg-[#111318]/[0.05] disabled:opacity-40 flex items-center gap-2.5 text-left"
                >
                  <RotateCcw className="w-4 h-4 text-[#8A909E]" strokeWidth={1.75} />
                  <span>{t.actionReset}</span>
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onShare(campaign);
                  }}
                  className="w-full px-3 py-2 rounded-xl text-xs font-medium text-[#111318] hover:bg-[#111318]/[0.05] flex items-center gap-2.5 text-left"
                >
                  <Share2 className="w-4 h-4 text-[#8A909E]" strokeWidth={1.75} />
                  <span>{t.actionShare}</span>
                </button>
              </div>

              <div className="py-1 px-1.5">
                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onDelete(campaign);
                  }}
                  disabled={isRunningSession}
                  className="w-full px-3 py-2 rounded-xl text-xs font-medium text-[#E2532B] hover:bg-[#E2532B]/10 disabled:opacity-40 flex items-center gap-2.5 text-left"
                >
                  <Trash2 className="w-4 h-4 text-[#E2532B]" strokeWidth={1.75} />
                  <span>
                    {isRunningSession ? 'Cannot Delete While Running' : t.actionDelete}
                  </span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Calling Progress Strip: Total, Called, Remaining & Progress % */}
      <div className="mt-3.5">
        <div className="flex items-center justify-between text-xs">
          <div className="flex items-center gap-1.5 text-[#8A909E] tabular-nums">
            <span>
              <strong className="text-[#111318] font-bold">{campaign.completedCalls}</strong> /{' '}
              {campaign.totalContacts} called
            </span>
            <span aria-hidden="true">·</span>
            <span>{campaign.pendingCalls} remaining</span>
          </div>
          <span className="text-[#111318] font-extrabold tabular-nums text-sm">
            {campaign.progressPercentage}%
          </span>
        </div>

        {/* Orange/Black Soft Progress Bar */}
        <div className="w-full h-2 bg-[#111318]/[0.06] rounded-full overflow-hidden mt-2 mb-2.5 p-0.5">
          <div
            className={`h-full rounded-full animate-bar-grow transition-all duration-500 ${
              effectiveStatus === 'active' || effectiveStatus === 'running'
                ? 'bg-[#E2532B]'
                : 'bg-[#111318]'
            }`}
            style={{ width: `${Math.min(100, Math.max(0, campaign.progressPercentage))}%` }}
          />
        </div>
      </div>

      {/* Footer Metadata: SIM Info & Recent Activity */}
      <div className="flex items-center justify-between text-[11.5px] text-[#8A909E]">
        <div className="flex items-center gap-1.5 truncate">
          <span className="font-semibold text-[#111318]">
            {formatSimLabel(campaign.simSlot, currentLanguage)}
          </span>
          <span aria-hidden="true">·</span>
          <span className="tabular-nums">{campaign.answeredCalls} answered</span>
        </div>
        <div className="shrink-0 tabular-nums">{formatTimeAgo(campaign.lastActivity)}</div>
      </div>
    </div>
  );
};
