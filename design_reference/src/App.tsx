import React, { useState, useMemo, useEffect, useRef } from 'react';
import { initialCampaigns, defaultAppSettings } from './data/initialCampaigns';
import {
  Campaign,
  CampaignContact,
  CampaignStatus,
  CallStage,
  SortField,
  SortOrder,
  AppSettings,
} from './types/campaign';
import {
  calculateOverviewStats,
  filterAndSortCampaigns,
  removeDuplicateContacts,
  resetCampaignProgress,
  formatDuration,
  getThemeColorById,
} from './utils/campaignLogic';
import { updateContactStageAndNotes } from './utils/dialerLogic';
import { translations, Language } from './i18n/translations';
import { AndroidStatusBar } from './components/AndroidStatusBar';
import { AndroidNavBar } from './components/AndroidNavBar';
import { ScreenHeader } from './components/ScreenHeader';
import { CampaignOverview } from './components/CampaignOverview';
import { SearchAndFilterBar } from './components/SearchAndFilterBar';
import { CampaignCard } from './components/CampaignCard';
import { MultiSelectToolbar } from './components/MultiSelectToolbar';
import { FloatingBottomNav, NavTab } from './components/FloatingBottomNav';
import { EmptyCampaignState } from './components/EmptyCampaignState';
import { NewCampaignSheet } from './components/NewCampaignSheet';
import { EditCampaignSheet } from './components/EditCampaignSheet';
import { CampaignDetailsScreen } from './components/CampaignDetailsScreen';
import { ResultsScreen } from './components/ResultsScreen';
import { CallLogsScreen } from './components/CallLogsScreen';
import { SettingsScreen } from './components/SettingsScreen';
import { StageClassificationSheet } from './components/StageClassificationSheet';
import { OnboardingModal } from './components/OnboardingModal';
import { HandoffSpecModal } from './components/HandoffSpecModal';
import { RenameModal } from './components/RenameModal';
import { DeleteConfirmDialog } from './components/DeleteConfirmDialog';
import { DuplicateContactsModal } from './components/DuplicateContactsModal';
import { ShareCampaignModal } from './components/ShareCampaignModal';
import { Toast, ToastMessage } from './components/Toast';
import { DeviceFrameWrapper } from './components/DeviceFrameWrapper';
import { LiveDialerScreen } from './components/dialer/LiveDialerScreen';

export default function App() {
  // Core application state
  const [campaigns, setCampaigns] = useState<Campaign[]>(initialCampaigns);
  const [settings, setSettings] = useState<AppSettings>(defaultAppSettings);
  const [language, setLanguage] = useState<Language>('en');
  const [isDeviceFrame, setIsDeviceFrame] = useState<boolean>(true);
  const [currentNavTab, setCurrentNavTab] = useState<NavTab>('campaigns');

  // Auto-fade state for AndroidStatusBar, FloatingBottomNav, and AndroidNavBar on scroll down
  const [isBarsHidden, setIsBarsHidden] = useState<boolean>(false);
  const isBarsHiddenRef = useRef<boolean>(false);
  const lastScrollTopRef = useRef<number>(0);
  const scrollDeltaAccRef = useRef<number>(0);
  const rafLockRef = useRef<boolean>(false);

  const updateBarsHidden = (nextHidden: boolean) => {
    if (isBarsHiddenRef.current !== nextHidden) {
      isBarsHiddenRef.current = nextHidden;
      setIsBarsHidden(nextHidden);
    }
  };

  const handleViewportScroll = (e: React.UIEvent<HTMLElement>) => {
    const target = e.target as HTMLElement;
    if (
      !target ||
      typeof target.scrollTop !== 'number' ||
      !target.classList?.contains('overflow-y-auto')
    ) {
      return;
    }

    if (rafLockRef.current) return;
    rafLockRef.current = true;

    window.requestAnimationFrame(() => {
      rafLockRef.current = false;
      const currentScrollTop = target.scrollTop;
      const delta = currentScrollTop - lastScrollTopRef.current;
      lastScrollTopRef.current = currentScrollTop;

      if (currentScrollTop <= 20) {
        scrollDeltaAccRef.current = 0;
        updateBarsHidden(false);
        return;
      }

      // Accumulate directional scroll distance for smooth hysteresis
      if (
        (delta > 0 && scrollDeltaAccRef.current < 0) ||
        (delta < 0 && scrollDeltaAccRef.current > 0)
      ) {
        scrollDeltaAccRef.current = 0;
      }
      scrollDeltaAccRef.current += delta;

      if (scrollDeltaAccRef.current > 16 && currentScrollTop > 28) {
        updateBarsHidden(true);
        scrollDeltaAccRef.current = 0;
      } else if (scrollDeltaAccRef.current < -10) {
        updateBarsHidden(false);
        scrollDeltaAccRef.current = 0;
      }
    });
  };

  // Sync selected app color (10 colors) and appearance mode to root CSS variables
  useEffect(() => {
    const themeColor = getThemeColorById(settings.appColor);
    const root = document.documentElement;
    root.style.setProperty('--app-accent', themeColor.hex);
    root.style.setProperty('--app-accent-shadow', `rgba(${themeColor.rgb}, 0.24)`);
    root.style.setProperty('--app-accent-glow', `rgba(${themeColor.rgb}, 0.06)`);
    root.style.setProperty('--app-accent-soft', `rgba(${themeColor.rgb}, 0.11)`);

    const resolvedTheme =
      settings.appearance === 'system'
        ? window.matchMedia('(prefers-color-scheme: dark)').matches
          ? 'dark'
          : 'light'
        : settings.appearance;
    root.dataset.theme = resolvedTheme;
  }, [settings.appColor, settings.appearance]);

  // Active campaign IDs across screens
  const [activeDialerCampaignId, setActiveDialerCampaignId] = useState<string | null>('camp-101');
  const [openedDetailCampaignId, setOpenedDetailCampaignId] = useState<string | null>(null);
  const [resultsCampaignId, setResultsCampaignId] = useState<string | null>('camp-101');

  useEffect(() => {
    updateBarsHidden(false);
    lastScrollTopRef.current = 0;
    scrollDeltaAccRef.current = 0;
  }, [currentNavTab, openedDetailCampaignId]);

  // Running session tracking (protects running campaign & contact from deletion/edits)
  const [isDialerSessionRunning, setIsDialerSessionRunning] = useState<boolean>(false);
  const [callingContactId, setCallingContactId] = useState<string | null>(null);

  // Call History clear state
  const [isHistoryCleared, setIsHistoryCleared] = useState<boolean>(false);

  // Search & Filter state in Campaigns Overview
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<CampaignStatus | 'all'>('all');
  const [sortBy, setSortBy] = useState<SortField>('lastActivity');
  const [sortOrder, setSortOrder] = useState<SortOrder>('desc');

  // Multi-selection state
  const [isMultiSelectMode, setIsMultiSelectMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());

  // Modal & Sheet states
  const [isNewSheetOpen, setIsNewSheetOpen] = useState(false);
  const [editingCampaign, setEditingCampaign] = useState<Campaign | null>(null);
  const [renamingCampaign, setRenamingCampaign] = useState<Campaign | null>(null);
  const [deletingCampaign, setDeletingCampaign] = useState<Campaign | null>(null);
  const [isBulkDeleteOpen, setIsBulkDeleteOpen] = useState(false);
  const [duplicateCampaign, setDuplicateCampaign] = useState<Campaign | null>(null);
  const [shareCampaign, setShareCampaign] = useState<Campaign | null>(null);
  const [isOnboardingOpen, setIsOnboardingOpen] = useState<boolean>(() => {
    try {
      return localStorage.getItem('o2dialer_welcomed_v1') !== 'true';
    } catch {
      return true;
    }
  });
  const [isHandoffOpen, setIsHandoffOpen] = useState(false);

  // Stage classification sheet state (accessible from Campaign Details or Results)
  const [stageTarget, setStageTarget] = useState<{
    campaignId: string;
    contact: CampaignContact;
  } | null>(null);

  // Toast feedback
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const t = translations[language];

  const addToast = (type: 'success' | 'info' | 'error', message: string) => {
    const id = `toast-${Date.now()}-${Math.random()}`;
    setToasts((prev) => [...prev, { id, type, message }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((item) => item.id !== id));
    }, 3000);
  };

  const dismissToast = (id: string) => {
    setToasts((prev) => prev.filter((item) => item.id !== id));
  };

  const overviewStats = useMemo(() => calculateOverviewStats(campaigns), [campaigns]);

  const statusCounts = useMemo(() => {
    return {
      all: campaigns.length,
      ready: campaigns.filter((c) => c.status === 'ready').length,
      active: campaigns.filter((c) => c.status === 'active').length,
      completed: campaigns.filter((c) => c.status === 'completed').length,
      paused: campaigns.filter((c) => c.status === 'paused').length,
    };
  }, [campaigns]);

  const filteredCampaigns = useMemo(() => {
    return filterAndSortCampaigns(campaigns, {
      query: searchQuery,
      status: selectedStatus,
      sortBy,
      sortOrder,
    });
  }, [campaigns, searchQuery, selectedStatus, sortBy, sortOrder]);

  const openedDetailCampaign = useMemo(
    () => campaigns.find((c) => c.id === openedDetailCampaignId) || null,
    [campaigns, openedDetailCampaignId]
  );

  // Multi-selection helpers
  const handleToggleSelect = (id: string) => {
    setIsMultiSelectMode(true);
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const handleSelectAll = () => {
    setSelectedIds(new Set(filteredCampaigns.map((c) => c.id)));
  };

  const handleDeselectAll = () => {
    setSelectedIds(new Set());
  };

  const handleCancelMultiSelect = () => {
    setIsMultiSelectMode(false);
    setSelectedIds(new Set());
  };

  // Essential Flow A: Create Campaign -> Open Campaign Details to Add Contacts
  const handleCreateCampaign = (data: Partial<Campaign>) => {
    const newCamp: Campaign = {
      id: `camp-${Date.now()}`,
      name: data.name || 'Untitled Campaign',
      simSlot: data.simSlot ?? -1,
      status: 'ready',
      totalContacts: 0,
      completedCalls: 0,
      pendingCalls: 0,
      answeredCalls: 0,
      noAnswerCalls: 0,
      failedCalls: 0,
      unverifiedCalls: 0,
      progressPercentage: 0,
      createdAt: new Date().toISOString(),
      lastActivity: new Date().toISOString(),
      delayBetweenCalls: settings.delayBetweenCalls,
      duplicateContactsFound: 0,
      tags: ['Outbound'],
      contacts: [],
    };

    setCampaigns((prev) => [newCamp, ...prev]);
    setOpenedDetailCampaignId(newCamp.id);
    setCurrentNavTab('campaigns');
    addToast('success', `Campaign "${newCamp.name}" created. Add contacts to begin.`);
  };

  const handleSaveEditedCampaign = (updated: Campaign) => {
    setCampaigns((prev) => prev.map((c) => (c.id === updated.id ? updated : c)));
    setEditingCampaign(null);
    addToast('success', `Saved changes to "${updated.name}".`);
  };

  const handleRenameCampaign = (campaignId: string, newName: string) => {
    setCampaigns((prev) =>
      prev.map((c) => (c.id === campaignId ? { ...c, name: newName } : c))
    );
    addToast('success', `Renamed to "${newName}".`);
  };

  const handleResetProgress = (campaign: Campaign) => {
    if (isDialerSessionRunning && activeDialerCampaignId === campaign.id) {
      addToast('error', 'Stop the active calling session before resetting progress.');
      return;
    }
    const reset = resetCampaignProgress(campaign);
    setCampaigns((prev) => prev.map((c) => (c.id === campaign.id ? reset : c)));
    addToast('info', t.progressResetMsg);
  };

  const handleConfirmRemoveDuplicates = (campaign: Campaign) => {
    const { updatedCampaign, removedCount } = removeDuplicateContacts(campaign);
    setCampaigns((prev) =>
      prev.map((c) => (c.id === campaign.id ? updatedCampaign : c))
    );
    setDuplicateCampaign(null);
    addToast('success', t.duplicatesCleanedMsg(removedCount));
  };

  const handleDeleteCampaign = (campaign: Campaign) => {
    if (isDialerSessionRunning && activeDialerCampaignId === campaign.id) {
      setDeletingCampaign(null);
      addToast('error', 'Cannot delete a campaign while its calling session is running.');
      return;
    }
    setCampaigns((prev) => prev.filter((c) => c.id !== campaign.id));
    if (openedDetailCampaignId === campaign.id) {
      setOpenedDetailCampaignId(null);
    }
    setDeletingCampaign(null);
    addToast('info', 'Campaign deleted.');
  };

  // Bulk actions (Part 4.6)
  const handleBulkDelete = () => {
    if (
      isDialerSessionRunning &&
      activeDialerCampaignId &&
      selectedIds.has(activeDialerCampaignId)
    ) {
      setIsBulkDeleteOpen(false);
      addToast(
        'error',
        'Cannot delete an actively running campaign. Deselect it or stop the session first.'
      );
      return;
    }
    setCampaigns((prev) => prev.filter((c) => !selectedIds.has(c.id)));
    const count = selectedIds.size;
    handleCancelMultiSelect();
    setIsBulkDeleteOpen(false);
    addToast('info', `${count} campaigns deleted.`);
  };

  const handleBulkPause = () => {
    setCampaigns((prev) =>
      prev.map((c) => (selectedIds.has(c.id) ? { ...c, status: 'paused' } : c))
    );
    addToast('info', `${selectedIds.size} campaigns paused.`);
    handleCancelMultiSelect();
  };

  const handleBulkResume = () => {
    setCampaigns((prev) =>
      prev.map((c) =>
        selectedIds.has(c.id) && c.status !== 'completed' ? { ...c, status: 'active' } : c
      )
    );
    addToast('success', `${selectedIds.size} campaigns marked active.`);
    handleCancelMultiSelect();
  };

  const handleBulkShare = () => {
    addToast('info', `Exported summary of ${selectedIds.size} campaigns.`);
    handleCancelMultiSelect();
  };

  // External WhatsApp follow-up helper (Part 8.5)
  const handleOpenWhatsApp = (phone: string, name: string) => {
    const cleanDigits = phone.replace(/[^\d]/g, '');
    addToast('info', `Opening external WhatsApp chat for ${name} (+${cleanDigits})…`);
  };

  // Share Call History CSV (Part 10.7)
  const handleShareHistoryLog = () => {
    const rows: string[] = [
      'Time,Campaign,Contact Name,Phone Number,Status,Duration,Attempt',
    ];
    for (const camp of campaigns) {
      for (const c of camp.contacts) {
        if (c.status !== 'pending' && c.status !== 'calling') {
          const timeStr = c.calledAt
            ? new Date(c.calledAt).toISOString()
            : camp.lastActivity;
          rows.push(
            `"${timeStr}","${camp.name}","${c.name}","${c.phone}","${c.status}","${formatDuration(
              c.callDurationSeconds
            )}","${Math.max(1, c.attempts || 1)}"`
          );
        }
      }
    }
    navigator.clipboard?.writeText(rows.join('\n'));
    addToast('success', 'Call history CSV copied to clipboard.');
  };

  return (
    <div
      dir={language === 'ar' ? 'rtl' : 'ltr'}
      onScrollCapture={handleViewportScroll}
      className={`min-h-screen bg-[#F5F6F8] text-[#111827] ${
        settings.appearance === 'dark' ? 'invert hue-rotate-180' : ''
      }`}
    >
      <DeviceFrameWrapper isDeviceFrame={isDeviceFrame}>
        {/* Android Status Bar */}
        <AndroidStatusBar
          isRtl={language === 'ar'}
          isHidden={isBarsHidden && !isOnboardingOpen}
        />

        {/* Dynamic Screen Viewport */}
        {currentNavTab === 'dialer' ? (
          <LiveDialerScreen
            campaigns={campaigns}
            activeCampaignId={activeDialerCampaignId}
            settings={settings}
            onUpdateSettings={setSettings}
            onSelectCampaign={(c) => setActiveDialerCampaignId(c.id)}
            onUpdateCampaign={(updated) => {
              setIsHistoryCleared(false);
              setCampaigns((prev) =>
                prev.map((c) => (c.id === updated.id ? updated : c))
              );
            }}
            onOpenCampaignDetails={(c) => {
              setOpenedDetailCampaignId(c.id);
              setCurrentNavTab('campaigns');
            }}
            onOpenResultsForCampaign={(c) => {
              setResultsCampaignId(c.id);
              setCurrentNavTab('results');
            }}
            onSessionRunningChange={(running, contactId) => {
              setIsDialerSessionRunning(running);
              setCallingContactId(contactId);
            }}
            currentLanguage={language}
            onToggleLanguage={() => setLanguage((prev) => (prev === 'en' ? 'ar' : 'en'))}
            onOpenWhatsApp={handleOpenWhatsApp}
            onTriggerToast={addToast}
            t={t}
          />
        ) : currentNavTab === 'results' ? (
          <ResultsScreen
            campaigns={campaigns}
            selectedCampaignId={resultsCampaignId}
            currentLanguage={language}
            onSelectCampaign={setResultsCampaignId}
            onToggleLanguage={() => setLanguage((prev) => (prev === 'en' ? 'ar' : 'en'))}
            onOpenStageSheet={(campaignId, contact) =>
              setStageTarget({ campaignId, contact })
            }
            onOpenWhatsApp={handleOpenWhatsApp}
            onShareCampaignReport={(camp) => setShareCampaign(camp)}
            t={t}
          />
        ) : currentNavTab === 'campaigns' ? (
          openedDetailCampaign ? (
            <CampaignDetailsScreen
              campaign={openedDetailCampaign}
              isRunningSession={
                isDialerSessionRunning &&
                activeDialerCampaignId === openedDetailCampaign.id
              }
              callingContactId={callingContactId}
              currentLanguage={language}
              defaultCountryCode={settings.defaultCountryCode}
              contactsPermissionGranted={settings.permissions.phoneContacts}
              onBack={() => setOpenedDetailCampaignId(null)}
              onUpdateCampaign={(updated) => {
                setCampaigns((prev) =>
                  prev.map((c) => (c.id === updated.id ? updated : c))
                );
              }}
              onEditCampaign={(c) => setEditingCampaign(c)}
              onRenameCampaign={(c) => setRenamingCampaign(c)}
              onRemoveDuplicates={(c) => setDuplicateCampaign(c)}
              onResetProgress={handleResetProgress}
              onShareReport={(c) => setShareCampaign(c)}
              onDeleteCampaign={(c) => setDeletingCampaign(c)}
              onStartOrResumeSession={(c) => {
                setActiveDialerCampaignId(c.id);
                setCurrentNavTab('dialer');
              }}
              onOpenStageSheet={(contact) =>
                setStageTarget({
                  campaignId: openedDetailCampaign.id,
                  contact,
                })
              }
              onOpenWhatsApp={handleOpenWhatsApp}
              onRequestContactsPermission={() => {
                setCurrentNavTab('settings');
                addToast('info', 'Enable Phone Contacts (READ_CONTACTS) in Settings.');
              }}
              onTriggerToast={addToast}
              t={t}
            />
          ) : (
            <div className="flex-1 overflow-y-auto bg-transparent flex flex-col">
              {isMultiSelectMode ? (
                <MultiSelectToolbar
                  selectedCount={selectedIds.size}
                  totalCount={filteredCampaigns.length}
                  onSelectAll={handleSelectAll}
                  onDeselectAll={handleDeselectAll}
                  onBulkDelete={() => setIsBulkDeleteOpen(true)}
                  onBulkPause={handleBulkPause}
                  onBulkResume={handleBulkResume}
                  onBulkShare={handleBulkShare}
                  onCancel={handleCancelMultiSelect}
                  t={t}
                />
              ) : (
                <ScreenHeader
                  t={t}
                  currentLanguage={language}
                  onToggleLanguage={() =>
                    setLanguage((prev) => (prev === 'en' ? 'ar' : 'en'))
                  }
                  campaignCount={overviewStats.totalCampaigns}
                  contactCount={overviewStats.totalContacts}
                  onNewCampaign={() => setIsNewSheetOpen(true)}
                  isMultiSelectMode={isMultiSelectMode}
                  onToggleMultiSelect={() => {
                    setIsMultiSelectMode(!isMultiSelectMode);
                    setSelectedIds(new Set());
                  }}
                />
              )}

              {campaigns.length > 0 && (
                <CampaignOverview stats={overviewStats} t={t} />
              )}

                {campaigns.length > 0 && (
                  <SearchAndFilterBar
                    searchQuery={searchQuery}
                    onSearchChange={setSearchQuery}
                    selectedStatus={selectedStatus}
                    onStatusChange={setSelectedStatus}
                    sortBy={sortBy}
                    sortOrder={sortOrder}
                    onSortChange={(field, order) => {
                      setSortBy(field);
                      setSortOrder(order);
                    }}
                    t={t}
                    statusCounts={statusCounts}
                  />
                )}

                <div className="px-[20px] py-2 space-y-3.5 flex-1 pb-24">
                  {campaigns.length === 0 ? (
                    <EmptyCampaignState
                      onCreateCampaign={() => setIsNewSheetOpen(true)}
                      t={t}
                    />
                  ) : filteredCampaigns.length === 0 ? (
                    <div className="py-12 px-5 rounded-[24px] bg-white/75 backdrop-blur-xl shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] text-center">
                      <p className="text-[14px] font-bold text-[#111318]">
                        {t.noSearchResultsTitle}
                      </p>
                      <p className="text-[12.5px] text-[#8A909E] mt-1 max-w-xs mx-auto">
                        {t.noSearchResultsSubtitle}
                      </p>
                      <button
                        onClick={() => {
                          setSearchQuery('');
                          setSelectedStatus('all');
                        }}
                        className="mt-3 min-h-[40px] px-4 rounded-full bg-[#111318] text-white text-xs font-semibold"
                      >
                        {t.clearSearch}
                      </button>
                    </div>
                  ) : (
                    filteredCampaigns.map((campaign) => (
                      <CampaignCard
                        key={campaign.id}
                        campaign={campaign}
                        isSelected={selectedIds.has(campaign.id)}
                        isMultiSelectMode={isMultiSelectMode}
                        isRunningSession={
                          isDialerSessionRunning &&
                          activeDialerCampaignId === campaign.id
                        }
                        currentLanguage={language}
                        onToggleSelect={handleToggleSelect}
                        onOpenDetails={(c) => setOpenedDetailCampaignId(c.id)}
                        onEdit={(c) => setEditingCampaign(c)}
                        onRename={(c) => setRenamingCampaign(c)}
                        onStartOrResumeDialer={(c) => {
                          setActiveDialerCampaignId(c.id);
                          setCurrentNavTab('dialer');
                        }}
                        onResetProgress={handleResetProgress}
                        onRemoveDuplicates={(c) => setDuplicateCampaign(c)}
                        onViewReport={(c) => {
                          setResultsCampaignId(c.id);
                          setCurrentNavTab('results');
                        }}
                        onShare={(c) => setShareCampaign(c)}
                        onDelete={(c) => setDeletingCampaign(c)}
                        t={t}
                      />
                    ))
                  )}
                </div>
              </div>
          )
        ) : currentNavTab === 'history' ? (
          <CallLogsScreen
            campaigns={campaigns}
            isHistoryCleared={isHistoryCleared}
            currentLanguage={language}
            onToggleLanguage={() => setLanguage((prev) => (prev === 'en' ? 'ar' : 'en'))}
            onShareHistoryLog={handleShareHistoryLog}
            onClearHistoryLog={() => {
              setIsHistoryCleared(true);
              addToast('info', 'Local call history cleared.');
            }}
            t={t}
          />
        ) : (
          <SettingsScreen
            settings={settings}
            onUpdateSettings={setSettings}
            currentLanguage={language}
            onToggleLanguage={() => setLanguage((prev) => (prev === 'en' ? 'ar' : 'en'))}
            isDeviceFrame={isDeviceFrame}
            onToggleDeviceFrame={() => setIsDeviceFrame((prev) => !prev)}
            onOpenOnboarding={() => setIsOnboardingOpen(true)}
            onOpenHandoffSpec={() => setIsHandoffOpen(true)}
            onTriggerToast={addToast}
            t={t}
          />
        )}

        {/* 5-Tab Bottom Navigation Bar: Dialer, Results, Campaigns, History, Settings */}
        <FloatingBottomNav
          currentTab={currentNavTab}
          onSelectTab={(tab) => {
            if (tab === 'campaigns') {
              setOpenedDetailCampaignId(null);
            }
            updateBarsHidden(false);
            setCurrentNavTab(tab);
          }}
          t={t}
          badgeCount={overviewStats.activeCount}
          isHidden={isBarsHidden || isOnboardingOpen}
        />

        {/* First-Run 3-Screen Animated Welcome Experience */}
        <OnboardingModal
          isOpen={isOnboardingOpen}
          currentLanguage={language}
          onToggleLanguage={() => setLanguage((prev) => (prev === 'en' ? 'ar' : 'en'))}
          onComplete={() => {
            try {
              localStorage.setItem('o2dialer_welcomed_v1', 'true');
            } catch {
              // ignore storage errors
            }
            setIsOnboardingOpen(false);
          }}
        />

        {/* Android Gesture Bar */}
        <AndroidNavBar isHidden={isBarsHidden && !isOnboardingOpen} />
      </DeviceFrameWrapper>

      {/* Modals & Bottom Sheets */}
      <NewCampaignSheet
        isOpen={isNewSheetOpen}
        onClose={() => setIsNewSheetOpen(false)}
        onCreate={handleCreateCampaign}
        currentLanguage={language}
        t={t}
      />

      <EditCampaignSheet
        campaign={editingCampaign}
        isOpen={!!editingCampaign}
        onClose={() => setEditingCampaign(null)}
        onSave={handleSaveEditedCampaign}
        currentLanguage={language}
        t={t}
      />

      <RenameModal
        campaign={renamingCampaign}
        isOpen={!!renamingCampaign}
        onClose={() => setRenamingCampaign(null)}
        onRename={handleRenameCampaign}
        t={t}
      />

      <DeleteConfirmDialog
        isOpen={!!deletingCampaign}
        message={deletingCampaign ? t.confirmDeleteMsg(deletingCampaign.name) : ''}
        onConfirm={() => deletingCampaign && handleDeleteCampaign(deletingCampaign)}
        onCancel={() => setDeletingCampaign(null)}
        t={t}
      />

      <DeleteConfirmDialog
        isOpen={isBulkDeleteOpen}
        title="Delete Selected Campaigns"
        message={t.confirmBulkDeleteMsg(selectedIds.size)}
        onConfirm={handleBulkDelete}
        onCancel={() => setIsBulkDeleteOpen(false)}
        t={t}
      />

      <DuplicateContactsModal
        campaign={duplicateCampaign}
        isOpen={!!duplicateCampaign}
        onClose={() => setDuplicateCampaign(null)}
        onConfirmRemove={handleConfirmRemoveDuplicates}
        t={t}
      />

      <ShareCampaignModal
        campaign={shareCampaign}
        isOpen={!!shareCampaign}
        onClose={() => setShareCampaign(null)}
        onCopied={() => addToast('success', 'CSV report exported.')}
        t={t}
      />

      <StageClassificationSheet
        contact={stageTarget?.contact || null}
        isOpen={!!stageTarget}
        currentLanguage={language}
        onClose={() => setStageTarget(null)}
        onSave={(contactId, stage, feedback) => {
          if (!stageTarget) return;
          setCampaigns((prev) =>
            prev.map((c) =>
              c.id === stageTarget.campaignId
                ? updateContactStageAndNotes(c, contactId, stage, feedback)
                : c
            )
          );
          addToast('success', `Saved stage: ${stage.replace('_', ' ')}`);
          setStageTarget(null);
        }}
        onOpenWhatsApp={handleOpenWhatsApp}
        t={t}
      />

      <HandoffSpecModal
        isOpen={isHandoffOpen}
        onClose={() => setIsHandoffOpen(false)}
      />

      {/* Global Toast notifications */}
      <Toast toasts={toasts} onDismiss={dismissToast} />
    </div>
  );
}
