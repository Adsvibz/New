import React from 'react';
import { Phone, BarChart3, Megaphone, Clock, Settings } from 'lucide-react';
import { Translations } from '../i18n/translations';

export type NavTab = 'dialer' | 'results' | 'campaigns' | 'history' | 'settings';

interface FloatingBottomNavProps {
  currentTab: NavTab;
  onSelectTab: (tab: NavTab) => void;
  t: Translations;
  badgeCount?: number;
  isHidden?: boolean;
}

export const FloatingBottomNav: React.FC<FloatingBottomNavProps> = ({
  currentTab,
  onSelectTab,
  t,
  badgeCount = 0,
  isHidden = false,
}) => {
  const tabs = [
    { id: 'dialer' as const, label: t.navKeypad, icon: Phone },
    { id: 'results' as const, label: t.navResults, icon: BarChart3 },
    { id: 'campaigns' as const, label: t.navCampaigns, icon: Megaphone, badge: badgeCount },
    { id: 'history' as const, label: t.navHistory, icon: Clock },
    { id: 'settings' as const, label: t.navSettings, icon: Settings },
  ];

  return (
    <div
      className="absolute left-0 right-0 bottom-3.5 z-30 px-[20px] pointer-events-none flex justify-center"
      style={{ paddingBottom: 'env(safe-area-inset-bottom, 0px)' }}
      aria-hidden={isHidden}
    >
      <nav
        className={`${
          isHidden
            ? 'pointer-events-none opacity-0 translate-y-5'
            : 'pointer-events-auto opacity-100 translate-y-0'
        } relative overflow-hidden w-full max-w-[390px] rounded-full glass-nav-pill p-1.5 flex items-center justify-between transition-[transform,opacity] duration-300 ease-out`}
        aria-label="Application Navigation"
      >
        {/* Specular top edge refraction highlight */}
        <span
          aria-hidden="true"
          className="pointer-events-none absolute inset-x-8 top-0 h-px bg-gradient-to-r from-transparent via-white to-transparent opacity-90"
        />

        {tabs.map((tab) => {
          const isActive = currentTab === tab.id;
          const Icon = tab.icon;

          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => onSelectTab(tab.id)}
              aria-label={tab.label}
              aria-current={isActive ? 'page' : undefined}
              title={tab.label}
              className={`relative min-w-[46px] min-h-[46px] w-12 h-12 rounded-full flex items-center justify-center transition-all duration-200 active:scale-90 ${
                isActive
                  ? 'bg-[#111318]/90 backdrop-blur-md text-white border border-white/15 shadow-[0_8px_20px_-4px_rgba(17,19,24,0.34),inset_0_1px_1px_rgba(255,255,255,0.22)] animate-nav-pop'
                  : 'text-[#6E7582] hover:text-[#111318] hover:bg-white/45 active:bg-white/60'
              }`}
            >
              <Icon className="w-[19px] h-[19px]" strokeWidth={1.75} />

              {tab.badge && tab.badge > 0 ? (
                <span
                  className={`absolute top-1.5 right-1.5 px-1 min-w-[14px] h-[14px] text-[9px] font-bold rounded-full flex items-center justify-center tabular-nums leading-none ${
                    isActive
                      ? 'bg-[#E2532B] text-white'
                      : 'bg-[#111318] text-white'
                  }`}
                >
                  {tab.badge}
                </span>
              ) : null}

              <span className="sr-only">{tab.label}</span>
            </button>
          );
        })}
      </nav>
    </div>
  );
};
