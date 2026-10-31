import React, { useState, useMemo, useRef } from 'react';
import {
  ArrowLeft,
  Edit2,
  MoreHorizontal,
  UserPlus,
  ClipboardPaste,
  FileUp,
  BookUser,
  Search,
  X,
  Play,
  RotateCcw,
  Trash2,
  MessageCircle,
  Tag,
  Check,
  ShieldAlert,
  Users2,
  Share2,
} from 'lucide-react';
import {
  Campaign,
  CampaignContact,
  CallStage,
  ContactCallStatus,
} from '../types/campaign';
import { Translations, Language } from '../i18n/translations';
import {
  formatDuration,
  formatSimLabel,
  parseBulkContactLines,
  syncCampaignFromContacts,
} from '../utils/campaignLogic';
import { samplePhonebookContacts } from '../data/initialCampaigns';

interface CampaignDetailsScreenProps {
  campaign: Campaign;
  isRunningSession: boolean;
  callingContactId?: string | null;
  currentLanguage: Language;
  defaultCountryCode: string;
  contactsPermissionGranted: boolean;
  onBack: () => void;
  onUpdateCampaign: (updated: Campaign) => void;
  onEditCampaign: (campaign: Campaign) => void;
  onRenameCampaign: (campaign: Campaign) => void;
  onRemoveDuplicates: (campaign: Campaign) => void;
  onResetProgress: (campaign: Campaign) => void;
  onShareReport: (campaign: Campaign) => void;
  onDeleteCampaign: (campaign: Campaign) => void;
  onStartOrResumeSession: (campaign: Campaign) => void;
  onOpenStageSheet: (contact: CampaignContact) => void;
  onOpenWhatsApp: (phone: string, name: string) => void;
  onRequestContactsPermission: () => void;
  onTriggerToast: (type: 'success' | 'info' | 'error', msg: string) => void;
  t: Translations;
}

export const CampaignDetailsScreen: React.FC<CampaignDetailsScreenProps> = ({
  campaign,
  isRunningSession,
  callingContactId,
  currentLanguage,
  defaultCountryCode,
  contactsPermissionGranted,
  onBack,
  onUpdateCampaign,
  onEditCampaign,
  onRenameCampaign,
  onRemoveDuplicates,
  onResetProgress,
  onShareReport,
  onDeleteCampaign,
  onStartOrResumeSession,
  onOpenStageSheet,
  onOpenWhatsApp,
  onRequestContactsPermission,
  onTriggerToast,
  t,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<ContactCallStatus | 'all'>('all');
  const [menuOpen, setMenuOpen] = useState(false);

  // Contact Management Modals (6.4: Add Number, Paste Numbers, Import File, Phone Contacts)
  const [activeToolModal, setActiveToolModal] = useState<
    'add' | 'paste' | 'import' | 'phonebook' | null
  >(null);

  // Add / Edit Contact state
  const [editingContact, setEditingContact] = useState<CampaignContact | null>(null);
  const [deletingContact, setDeletingContact] = useState<CampaignContact | null>(null);
  const [contactNameInput, setContactNameInput] = useState('');
  const [contactPhoneInput, setContactPhoneInput] = useState('');
  const [contactFormError, setContactFormError] = useState<string | null>(null);

  // Paste Numbers state
  const [pasteRawText, setPasteRawText] = useState('');
  const [pasteError, setPasteError] = useState<string | null>(null);

  // Import File state
  const [importError, setImportError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Phonebook multi-select state
  const [selectedPbIds, setSelectedPbIds] = useState<Set<string>>(new Set());

  const parsedPastePreview = useMemo(
    () => parseBulkContactLines(pasteRawText, defaultCountryCode),
    [pasteRawText, defaultCountryCode]
  );

  const filteredContacts = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return campaign.contacts.filter((c) => {
      if (statusFilter !== 'all' && c.status !== statusFilter) return false;
      if (q) {
        const matchName = c.name.toLowerCase().includes(q);
        const matchPhone = c.phone.toLowerCase().includes(q);
        const matchNotes = c.notes?.toLowerCase().includes(q) ?? false;
        if (!matchName && !matchPhone && !matchNotes) return false;
      }
      return true;
    });
  }, [campaign.contacts, searchQuery, statusFilter]);

  const getStageLabel = (stage: CallStage) => {
    switch (stage) {
      case 'potential':
        return t.stagePotential;
      case 'not_interested':
        return t.stageNotInterested;
      case 'call_back':
        return t.stageCallBack;
      case 'whatsapp':
        return t.stageWhatsapp;
      case 'voicemail':
        return t.stageVoicemail;
      default:
        return null;
    }
  };

  const getContactStatusLabel = (status: ContactCallStatus) => {
    switch (status) {
      case 'calling':
        return { text: t.sessionStatusCalling, color: 'text-[#2563EB] font-semibold' };
      case 'answered':
        return { text: t.outcomeAnswered, color: 'text-[#047857] font-medium' };
      case 'no_answer':
        return { text: t.outcomeNoAnswer, color: 'text-[#B45309] font-medium' };
      case 'failed':
        return { text: t.outcomeFailed, color: 'text-[#B42318] font-medium' };
      case 'unverified':
        return { text: t.outcomeUnverified, color: 'text-[#6B7280] font-medium' };
      case 'pending':
      default:
        return {
          text: currentLanguage === 'ar' ? 'في الانتظار' : 'Pending',
          color: 'text-[#8E95A2]',
        };
    }
  };

  // Add or Save Edited Contact
  const handleSaveSingleContact = (e: React.FormEvent) => {
    e.preventDefault();
    const digits = contactPhoneInput.replace(/\D/g, '');
    if (digits.length < 7) {
      setContactFormError(
        currentLanguage === 'ar'
          ? 'يرجى إدخال رقم هاتف صحيح (7 أرقام على الأقل).'
          : 'Please enter a valid phone number (at least 7 digits).'
      );
      return;
    }

    let formattedPhone = contactPhoneInput.trim();
    if (!formattedPhone.startsWith('+') && defaultCountryCode) {
      formattedPhone = `+${defaultCountryCode.replace(/^\+/, '')}${digits.replace(/^0+/, '')}`;
    }

    if (editingContact) {
      const updatedContacts = campaign.contacts.map((c) =>
        c.id === editingContact.id
          ? {
              ...c,
              name: contactNameInput.trim() || formattedPhone,
              phone: formattedPhone,
            }
          : c
      );
      onUpdateCampaign(
        syncCampaignFromContacts({
          ...campaign,
          contacts: updatedContacts,
          lastActivity: new Date().toISOString(),
        })
      );
      onTriggerToast('success', 'Contact updated.');
      setEditingContact(null);
    } else {
      const newContact: CampaignContact = {
        id: `c-${Date.now()}`,
        name: contactNameInput.trim() || formattedPhone,
        phone: formattedPhone,
        status: 'pending',
        attempts: 0,
        callDurationSeconds: 0,
        calledAt: null,
        stage: 'none',
        notes: '',
      };
      onUpdateCampaign(
        syncCampaignFromContacts({
          ...campaign,
          contacts: [...campaign.contacts, newContact],
          lastActivity: new Date().toISOString(),
        })
      );
      onTriggerToast('success', `Added ${newContact.name}.`);
      setActiveToolModal(null);
    }

    setContactNameInput('');
    setContactPhoneInput('');
    setContactFormError(null);
  };

  // Handle Paste Numbers Submit
  const handlePasteNumbersSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (parsedPastePreview.validContacts.length === 0) {
      setPasteError(
        currentLanguage === 'ar'
          ? 'لم يتم العثور على أرقام صالحة. يرجى التحقق من التنسيق.'
          : 'No valid phone numbers detected. Use format: Name, +12025550142'
      );
      return;
    }

    const newEntries: CampaignContact[] = parsedPastePreview.validContacts.map((item, idx) => ({
      id: `paste-${Date.now()}-${idx}`,
      name: item.name,
      phone: item.phone,
      status: 'pending',
      attempts: 0,
      callDurationSeconds: 0,
      calledAt: null,
      stage: 'none',
      notes: '',
    }));

    onUpdateCampaign(
      syncCampaignFromContacts({
        ...campaign,
        contacts: [...campaign.contacts, ...newEntries],
        lastActivity: new Date().toISOString(),
      })
    );
    onTriggerToast('success', `Added ${newEntries.length} contacts to campaign.`);
    setPasteRawText('');
    setPasteError(null);
    setActiveToolModal(null);
  };

  // Handle CSV/TXT File Upload
  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 2 * 1024 * 1024) {
      setImportError('File exceeds maximum 2MB safeguard limit.');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const content = String(reader.result || '');
      const parsed = parseBulkContactLines(content, defaultCountryCode);
      if (parsed.validContacts.length === 0) {
        setImportError(
          'Invalid CSV/TXT content: No valid phone numbers found in file.'
        );
        return;
      }

      const imported: CampaignContact[] = parsed.validContacts.map((item, idx) => ({
        id: `imp-${Date.now()}-${idx}`,
        name: item.name,
        phone: item.phone,
        status: 'pending',
        attempts: 0,
        callDurationSeconds: 0,
        calledAt: null,
        stage: 'none',
        notes: '',
      }));

      onUpdateCampaign(
        syncCampaignFromContacts({
          ...campaign,
          contacts: [...campaign.contacts, ...imported],
          lastActivity: new Date().toISOString(),
        })
      );
      onTriggerToast('success', `Imported ${imported.length} contacts from ${file.name}.`);
      setImportError(null);
      setActiveToolModal(null);
    };
    reader.readAsText(file);
  };

  // Simulate sample CSV import
  const handleSampleCsvImport = () => {
    const sampleCsv = `Name,Phone\nNourhan El-Gohary,+201005129940\nChristopher Beck,+14158902210\nSalma Fawzy,+201227714500`;
    const parsed = parseBulkContactLines(sampleCsv, defaultCountryCode);
    const imported: CampaignContact[] = parsed.validContacts.map((item, idx) => ({
      id: `csv-${Date.now()}-${idx}`,
      name: item.name,
      phone: item.phone,
      status: 'pending',
      attempts: 0,
      callDurationSeconds: 0,
      calledAt: null,
      stage: 'none',
      notes: '',
    }));
    onUpdateCampaign(
      syncCampaignFromContacts({
        ...campaign,
        contacts: [...campaign.contacts, ...imported],
        lastActivity: new Date().toISOString(),
      })
    );
    onTriggerToast('success', `Imported ${imported.length} verified contacts from CSV.`);
    setImportError(null);
    setActiveToolModal(null);
  };

  // Add selected Phone Contacts
  const handleImportPhoneContacts = () => {
    const chosen = samplePhonebookContacts.filter((p) => selectedPbIds.has(p.id));
    if (chosen.length === 0) return;

    const added: CampaignContact[] = chosen.map((item, idx) => ({
      id: `pb-${Date.now()}-${idx}`,
      name: item.name,
      phone: item.phone,
      company: item.company,
      status: 'pending',
      attempts: 0,
      callDurationSeconds: 0,
      calledAt: null,
      stage: 'none',
      notes: '',
    }));

    onUpdateCampaign(
      syncCampaignFromContacts({
        ...campaign,
        contacts: [...campaign.contacts, ...added],
        lastActivity: new Date().toISOString(),
      })
    );
    onTriggerToast('success', `Added ${added.length} phone contacts.`);
    setSelectedPbIds(new Set());
    setActiveToolModal(null);
  };

  // Delete single contact with confirmation
  const handleConfirmDeleteContact = () => {
    if (!deletingContact) return;
    if (isRunningSession && callingContactId === deletingContact.id) {
      onTriggerToast('error', 'Cannot delete the contact currently being dialed.');
      setDeletingContact(null);
      return;
    }
    const updatedContacts = campaign.contacts.filter((c) => c.id !== deletingContact.id);
    onUpdateCampaign(
      syncCampaignFromContacts({
        ...campaign,
        contacts: updatedContacts,
        lastActivity: new Date().toISOString(),
      })
    );
    onTriggerToast('info', `Deleted ${deletingContact.name}.`);
    setDeletingContact(null);
  };

  // Dynamic bottom CTA label (Part 6.8)
  const getPrimaryActionLabel = () => {
    if (campaign.totalContacts === 0) {
      return currentLanguage === 'ar' ? 'إضافة جهات اتصال للبدء' : 'Add Contacts to Start';
    }
    if (isRunningSession) {
      return currentLanguage === 'ar' ? 'العودة للجلسة الجارية' : 'Return to Live Session';
    }
    if (campaign.pendingCalls === 0 && campaign.totalContacts > 0) {
      return t.actionRestartCampaign;
    }
    if (campaign.completedCalls > 0) {
      return t.actionResumeSession;
    }
    return t.actionStartSession;
  };

  return (
    <div className="flex-1 overflow-y-auto bg-transparent flex flex-col pb-28">
      {/* 6.2 Header */}
      <header className="px-[20px] pt-3 pb-2 bg-transparent">
        <div className="flex items-center justify-between gap-2 mb-2.5">
          <button
            onClick={onBack}
            className="min-h-[44px] inline-flex items-center gap-1.5 px-3.5 rounded-full bg-white/60 backdrop-blur-md shadow-[0_6px_20px_-4px_rgba(17,19,24,0.06)] text-[12.5px] font-bold text-[#111318] hover:bg-white transition-all"
            aria-label="Back to Campaigns"
          >
            <ArrowLeft className="w-4 h-4 stroke-[1.75]" />
            <span>{t.campaignsTitle}</span>
          </button>

          <div className="flex items-center gap-2">
            <button
              onClick={() => onEditCampaign(campaign)}
              disabled={isRunningSession}
              className="min-h-[44px] px-3.5 rounded-full bg-white/60 backdrop-blur-md shadow-[0_6px_20px_-4px_rgba(17,19,24,0.06)] text-[12px] font-bold text-[#111318] hover:bg-white disabled:opacity-40 flex items-center gap-1.5 transition-all"
              aria-label="Edit Campaign"
            >
              <Edit2 className="w-3.5 h-3.5 stroke-[1.75]" />
              <span>{t.actionEdit}</span>
            </button>

            <div className="relative">
              <button
                onClick={() => setMenuOpen(!menuOpen)}
                className="w-11 h-11 rounded-full bg-white/60 backdrop-blur-md shadow-[0_6px_20px_-4px_rgba(17,19,24,0.06)] flex items-center justify-center text-[#111318] hover:bg-white transition-all"
                aria-label="Campaign secondary actions"
              >
                <MoreHorizontal className="w-4 h-4 stroke-[1.75]" />
              </button>

              {menuOpen && (
                <div className="absolute right-0 top-full mt-1.5 w-52 bg-white/95 backdrop-blur-xl rounded-[22px] shadow-[0_18px_40px_-8px_rgba(17,19,24,0.16)] py-2 z-30 divide-y divide-black/[0.04]">
                  <div className="py-1">
                    <button
                      onClick={() => {
                        setMenuOpen(false);
                        onRenameCampaign(campaign);
                      }}
                      disabled={isRunningSession}
                      className="w-full px-4 py-2 text-[12.5px] font-medium text-[#111318] hover:bg-black/[0.04] disabled:opacity-40 flex items-center gap-2 text-left"
                    >
                      <Edit2 className="w-3.5 h-3.5 text-[#8A909E]" />
                      <span>{t.actionRename}</span>
                    </button>
                    {campaign.duplicateContactsFound > 0 && (
                      <button
                        onClick={() => {
                          setMenuOpen(false);
                          onRemoveDuplicates(campaign);
                        }}
                        disabled={isRunningSession}
                        className="w-full px-4 py-2 text-[12.5px] font-medium text-[#B45309] hover:bg-amber-50 disabled:opacity-40 flex items-center gap-2 text-left"
                      >
                        <Users2 className="w-3.5 h-3.5" />
                        <span>
                          {t.actionRemoveDuplicates} ({campaign.duplicateContactsFound})
                        </span>
                      </button>
                    )}
                    <button
                      onClick={() => {
                        setMenuOpen(false);
                        onResetProgress(campaign);
                      }}
                      disabled={isRunningSession}
                      className="w-full px-4 py-2 text-[12.5px] font-medium text-[#111318] hover:bg-black/[0.04] disabled:opacity-40 flex items-center gap-2 text-left"
                    >
                      <RotateCcw className="w-3.5 h-3.5 text-[#8A909E]" />
                      <span>{t.actionReset}</span>
                    </button>
                    <button
                      onClick={() => {
                        setMenuOpen(false);
                        onShareReport(campaign);
                      }}
                      className="w-full px-4 py-2 text-[12.5px] font-medium text-[#111318] hover:bg-black/[0.04] flex items-center gap-2 text-left"
                    >
                      <Share2 className="w-3.5 h-3.5 text-[#8A909E]" />
                      <span>{t.actionShare}</span>
                    </button>
                  </div>
                  <div className="py-1">
                    <button
                      onClick={() => {
                        setMenuOpen(false);
                        onDeleteCampaign(campaign);
                      }}
                      disabled={isRunningSession}
                      className="w-full px-4 py-2 text-[12.5px] font-medium text-[#B42318] hover:bg-red-50 disabled:opacity-40 flex items-center gap-2 text-left"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      <span>{t.actionDelete}</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        <h1 className="text-[22px] font-extrabold tracking-tight text-[#111318] leading-snug truncate">
          {campaign.name}
        </h1>
        <div className="flex items-center gap-2 text-[12px] text-[#8A909E] mt-1 tabular-nums">
          <span>{campaign.totalContacts} contacts</span>
          <span aria-hidden="true">·</span>
          <span className="text-[#111318] font-semibold">
            {formatSimLabel(campaign.simSlot, currentLanguage)}
          </span>
          <span aria-hidden="true">·</span>
          <span className="capitalize font-semibold text-[#111318]">
            {isRunningSession ? t.statusRunning : campaign.status}
          </span>
        </div>
      </header>

      <div className="px-[20px] space-y-3.5">
        {/* 6.3 Overview Statistics Bento Grid (Single #E2532B Hero Card + Frosted Cards) */}
        <section className="bento-grid-2" aria-label="Campaign Statistics">
          {/* Single Hero Card in #E2532B: Progress & Completed Calls */}
          <div className="rounded-[24px] bg-[#E2532B] text-white p-4 shadow-[0_14px_34px_-8px_rgba(226,83,43,0.42)] flex flex-col justify-between min-h-[112px]">
            <div className="flex items-center justify-between gap-2">
              <span className="text-[11.5px] font-medium text-white/85 truncate">
                {t.statCompletedCalls}
              </span>
              <span className="px-2 py-0.5 rounded-full bg-white/20 text-[10.5px] font-bold tabular-nums">
                {campaign.progressPercentage}%
              </span>
            </div>
            <div className="mt-2">
              <span className="block text-[26px] font-extrabold tracking-tight tabular-nums leading-none">
                {campaign.completedCalls}
              </span>
              <div className="w-full h-1.5 bg-white/25 rounded-full overflow-hidden mt-2.5">
                <div
                  className="h-full bg-white rounded-full transition-all duration-300"
                  style={{ width: `${Math.min(100, Math.max(0, campaign.progressPercentage))}%` }}
                />
              </div>
            </div>
          </div>

          {/* Card 2: Total Contacts */}
          <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] flex flex-col justify-between min-h-[112px]">
            <span className="text-[11.5px] font-medium text-[#8A909E] truncate">
              {t.statTotalContacts}
            </span>
            <div>
              <span className="block text-[24px] font-extrabold tracking-tight text-[#111318] tabular-nums leading-none">
                {campaign.totalContacts}
              </span>
              <span className="inline-block mt-1.5 text-[10.5px] font-semibold text-[#8A909E]">
                Progress {campaign.progressPercentage}%
              </span>
            </div>
          </div>

          {/* Card 3: Remaining */}
          <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] flex flex-col justify-between min-h-[104px]">
            <span className="text-[11.5px] font-medium text-[#8A909E] truncate">
              {t.statPendingCalls}
            </span>
            <span className="block text-[22px] font-extrabold tracking-tight text-[#111318] tabular-nums leading-none mt-2">
              {campaign.pendingCalls}
            </span>
          </div>

          {/* Card 4: Answered */}
          <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] flex flex-col justify-between min-h-[104px]">
            <span className="text-[11.5px] font-medium text-[#8A909E] truncate">
              {t.outcomeAnswered}
            </span>
            <span className="block text-[22px] font-extrabold tracking-tight text-[#047857] tabular-nums leading-none mt-2">
              {campaign.answeredCalls}
            </span>
          </div>
        </section>

        {/* 6.4 Contact Management Toolbar (2x2 Bento Action Buttons) */}
        <section className="bento-grid-2">
          <button
            type="button"
            onClick={() => {
              setEditingContact(null);
              setContactNameInput('');
              setContactPhoneInput('');
              setContactFormError(null);
              setActiveToolModal('add');
            }}
            className="min-h-[52px] py-2.5 px-3 rounded-[20px] bg-white/60 backdrop-blur-md shadow-[0_10px_28px_-8px_rgba(17,19,24,0.06)] hover:bg-white text-[#111318] flex items-center gap-2.5 transition-all"
          >
            <div className="w-8 h-8 rounded-full bg-[#111318]/[0.05] flex items-center justify-center shrink-0">
              <UserPlus className="w-4 h-4 text-[#111318] stroke-[1.75]" />
            </div>
            <span className="text-[12px] font-bold truncate">
              {currentLanguage === 'ar' ? 'إضافة رقم' : 'Add Number'}
            </span>
          </button>

          <button
            type="button"
            onClick={() => {
              setPasteRawText('');
              setPasteError(null);
              setActiveToolModal('paste');
            }}
            className="min-h-[52px] py-2.5 px-3 rounded-[20px] bg-white/60 backdrop-blur-md shadow-[0_10px_28px_-8px_rgba(17,19,24,0.06)] hover:bg-white text-[#111318] flex items-center gap-2.5 transition-all"
          >
            <div className="w-8 h-8 rounded-full bg-[#111318]/[0.05] flex items-center justify-center shrink-0">
              <ClipboardPaste className="w-4 h-4 text-[#111318] stroke-[1.75]" />
            </div>
            <span className="text-[12px] font-bold truncate">
              {currentLanguage === 'ar' ? 'لصق أرقام' : 'Paste Numbers'}
            </span>
          </button>

          <button
            type="button"
            onClick={() => {
              setImportError(null);
              setActiveToolModal('import');
            }}
            className="min-h-[52px] py-2.5 px-3 rounded-[20px] bg-white/60 backdrop-blur-md shadow-[0_10px_28px_-8px_rgba(17,19,24,0.06)] hover:bg-white text-[#111318] flex items-center gap-2.5 transition-all"
          >
            <div className="w-8 h-8 rounded-full bg-[#111318]/[0.05] flex items-center justify-center shrink-0">
              <FileUp className="w-4 h-4 text-[#111318] stroke-[1.75]" />
            </div>
            <span className="text-[12px] font-bold truncate">
              {currentLanguage === 'ar' ? 'استيراد ملف' : 'Import File'}
            </span>
          </button>

          <button
            type="button"
            onClick={() => {
              setActiveToolModal('phonebook');
            }}
            className="min-h-[52px] py-2.5 px-3 rounded-[20px] bg-white/60 backdrop-blur-md shadow-[0_10px_28px_-8px_rgba(17,19,24,0.06)] hover:bg-white text-[#111318] flex items-center gap-2.5 transition-all"
          >
            <div className="w-8 h-8 rounded-full bg-[#111318]/[0.05] flex items-center justify-center shrink-0">
              <BookUser className="w-4 h-4 text-[#111318] stroke-[1.75]" />
            </div>
            <span className="text-[12px] font-bold truncate">
              {currentLanguage === 'ar' ? 'دليل الهاتف' : 'Phone Contacts'}
            </span>
          </button>
        </section>

        {/* 6.7 Live Search & Contact Status Filter + 6.5 Contact List Card */}
        <section className="rounded-[24px] bg-white/60 backdrop-blur-md p-4 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)] space-y-3">
          <div className="relative">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-[#8A909E] stroke-[1.75]" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder={
                currentLanguage === 'ar'
                  ? 'ابحث بالاسم أو رقم الهاتف...'
                  : 'Search contacts by name or phone number...'
              }
              className="w-full h-11 pl-10 pr-9 text-[12.5px] font-medium bg-[#111318]/[0.04] text-[#111318] placeholder:text-[#8A909E] rounded-full focus:outline-none focus:bg-[#111318]/[0.06]"
            />
            {searchQuery && (
              <button
                onClick={() => setSearchQuery('')}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 p-1.5 text-[#8A909E] hover:text-[#111318]"
                aria-label="Clear contact search"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-0.5">
            {(
              [
                { id: 'all', label: t.filterAll },
                { id: 'pending', label: 'Pending' },
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
                  onClick={() => setStatusFilter(tab.id)}
                  className={`min-h-[34px] px-3 py-1 rounded-full text-[11.5px] font-semibold whitespace-nowrap shrink-0 transition-all ${
                    isActive
                      ? 'bg-[#111318] text-white shadow-xs'
                      : 'bg-[#111318]/[0.04] text-[#8A909E] hover:text-[#111318]'
                  }`}
                >
                  {tab.label}
                </button>
              );
            })}
          </div>

          <div className="divide-y divide-black/[0.04]">
            {campaign.contacts.length === 0 ? (
              <div className="py-10 text-center">
                <p className="text-[14px] font-bold text-[#111318]">
                  {currentLanguage === 'ar'
                    ? 'لا توجد جهات اتصال في هذه الحملة'
                    : 'No contacts in this campaign yet'}
                </p>
                <p className="text-[12.5px] text-[#8A909E] mt-1 max-w-xs mx-auto">
                  {currentLanguage === 'ar'
                    ? 'أضف رقماً يدوياً، أو الصق قائمة أرقام، أو استورد ملف CSV/TXT.'
                    : 'Use Add Number, Paste Numbers, or Import File above to populate your calling queue.'}
                </p>
              </div>
            ) : filteredContacts.length === 0 ? (
              <div className="py-10 text-center">
                <p className="text-[13.5px] font-bold text-[#111318]">
                  {t.noSearchResultsTitle}
                </p>
                <button
                  onClick={() => {
                    setSearchQuery('');
                    setStatusFilter('all');
                  }}
                  className="mt-2 px-4 py-1.5 rounded-full bg-[#111318] text-white text-[12px] font-semibold"
                >
                  {t.clearSearch}
                </button>
              </div>
            ) : (
              filteredContacts.map((contact) => {
                const statusInfo = getContactStatusLabel(contact.status);
                const stageLabel = getStageLabel(contact.stage);
                const isCurrentlyDialing =
                  (isRunningSession && callingContactId === contact.id) ||
                  contact.status === 'calling';

                return (
                  <div
                    key={contact.id}
                    className="py-3 flex items-start justify-between gap-2.5"
                  >
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <span className="text-[13.5px] font-bold text-[#111318] truncate">
                          {contact.name}
                        </span>
                      </div>

                      <div className="flex items-center gap-1.5 text-[12px] text-[#8A909E] mt-0.5 min-w-0">
                        <span
                          className="font-mono tabular-nums whitespace-nowrap shrink-0 text-[#111318]/80"
                          dir="ltr"
                        >
                          {contact.phone}
                        </span>
                        {contact.company && (
                          <>
                            <span aria-hidden="true" className="text-neutral-300 shrink-0">
                              ·
                            </span>
                            <span className="text-[11.5px] text-[#8A909E] truncate">
                              {contact.company}
                            </span>
                          </>
                        )}
                      </div>

                      {/* Status · Duration · Stage */}
                      <div className="flex items-center flex-wrap gap-1.5 text-[11.5px] mt-1">
                        <span className={statusInfo.color}>{statusInfo.text}</span>
                        {contact.callDurationSeconds > 0 && (
                          <>
                            <span aria-hidden="true" className="text-neutral-300">
                              ·
                            </span>
                            <span className="tabular-nums text-[#8A909E]">
                              {formatDuration(contact.callDurationSeconds)}
                            </span>
                          </>
                        )}
                        {stageLabel && (
                          <>
                            <span aria-hidden="true" className="text-neutral-300">
                              ·
                            </span>
                            <span className="text-[#111318] font-semibold">{stageLabel}</span>
                          </>
                        )}
                      </div>

                      {contact.notes && (
                        <p className="text-[11.5px] text-[#8A909E] mt-1 line-clamp-2">
                          “{contact.notes}”
                        </p>
                      )}
                    </div>

                    {/* Contact Row Actions */}
                    <div className="flex items-center gap-1 shrink-0 pt-0.5">
                      <button
                        type="button"
                        onClick={() => onOpenStageSheet(contact)}
                        className="w-8 h-8 rounded-full flex items-center justify-center text-[#8A909E] hover:text-[#111318] hover:bg-black/[0.04] transition-colors"
                        title="Classify stage & notes"
                        aria-label={`Classify ${contact.name}`}
                      >
                        <Tag className="w-3.5 h-3.5 stroke-[1.75]" />
                      </button>

                      <button
                        type="button"
                        onClick={() => onOpenWhatsApp(contact.phone, contact.name)}
                        className="w-8 h-8 rounded-full flex items-center justify-center text-[#8A909E] hover:text-[#047857] hover:bg-emerald-50 transition-colors"
                        title="WhatsApp follow-up"
                        aria-label={`WhatsApp ${contact.name}`}
                      >
                        <MessageCircle className="w-3.5 h-3.5 stroke-[1.75]" />
                      </button>

                      <button
                        type="button"
                        disabled={isCurrentlyDialing}
                        onClick={() => {
                          setEditingContact(contact);
                          setContactNameInput(contact.name);
                          setContactPhoneInput(contact.phone);
                          setContactFormError(null);
                          setActiveToolModal('add');
                        }}
                        className="w-8 h-8 rounded-full flex items-center justify-center text-[#8A909E] hover:text-[#111318] hover:bg-black/[0.04] disabled:opacity-30 transition-colors"
                        title="Edit contact"
                        aria-label={`Edit ${contact.name}`}
                      >
                        <Edit2 className="w-3.5 h-3.5 stroke-[1.75]" />
                      </button>

                      <button
                        type="button"
                        disabled={isCurrentlyDialing}
                        onClick={() => setDeletingContact(contact)}
                        className="w-8 h-8 rounded-full flex items-center justify-center text-[#8A909E] hover:text-[#B42318] hover:bg-red-50 disabled:opacity-30 transition-colors"
                        title="Delete contact"
                        aria-label={`Delete ${contact.name}`}
                      >
                        <Trash2 className="w-3.5 h-3.5 stroke-[1.75]" />
                      </button>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </section>

        {/* 6.8 Prominent Bottom Campaign Action */}
        <div className="pt-1">
          <button
            type="button"
            onClick={() => {
              if (campaign.totalContacts === 0) {
                setActiveToolModal('paste');
              } else {
                onStartOrResumeSession(campaign);
              }
            }}
            className="w-full min-h-[48px] rounded-full bg-[#111318] text-white text-[13.5px] font-bold hover:bg-neutral-800 active:scale-[0.99] transition-all flex items-center justify-center gap-2 shadow-[0_12px_28px_-6px_rgba(17,19,24,0.28)]"
          >
            <Play className="w-4 h-4 fill-current" />
            <span>{getPrimaryActionLabel()}</span>
          </button>
        </div>
      </div>

      {/* Modal 1: Add / Edit Single Number */}
      {activeToolModal === 'add' && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/40 backdrop-blur-xs">
          <div
            className="w-full max-w-md bg-white rounded-t-2xl sm:rounded-2xl border border-[#E5E7EB] shadow-2xl p-5"
            role="dialog"
            aria-modal="true"
          >
            <div className="flex items-center justify-between pb-3 border-b border-[#F0F2F5]">
              <h3 className="text-[15px] font-semibold text-[#111827]">
                {editingContact ? 'Edit Contact' : 'Add Number to Campaign'}
              </h3>
              <button
                onClick={() => {
                  setActiveToolModal(null);
                  setEditingContact(null);
                }}
                className="w-8 h-8 flex items-center justify-center text-[#8E95A2] hover:text-[#111827] rounded-lg"
                aria-label="Close"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleSaveSingleContact} className="mt-4 space-y-3.5">
              {contactFormError && (
                <div className="p-2.5 bg-red-50 border border-red-200 text-[#B42318] text-[12px] rounded-lg">
                  {contactFormError}
                </div>
              )}

              <div>
                <label className="block text-[12px] font-medium text-[#111827] mb-1">
                  Phone Number *
                </label>
                <input
                  type="tel"
                  value={contactPhoneInput}
                  onChange={(e) => {
                    setContactPhoneInput(e.target.value);
                    if (contactFormError) setContactFormError(null);
                  }}
                  placeholder="+1 (202) 555-0142"
                  className="w-full h-10 px-3 text-[13.5px] font-mono bg-[#F7F8FA] border border-[#E5E7EB] rounded-xl text-[#111827] focus:outline-none focus:border-[#111827]"
                  autoFocus
                />
              </div>

              <div>
                <label className="block text-[12px] font-medium text-[#111827] mb-1">
                  Contact Name (Optional)
                </label>
                <input
                  type="text"
                  value={contactNameInput}
                  onChange={(e) => setContactNameInput(e.target.value)}
                  placeholder="e.g. Sarah Mitchell"
                  className="w-full h-10 px-3 text-[13.5px] bg-[#F7F8FA] border border-[#E5E7EB] rounded-xl text-[#111827] focus:outline-none focus:border-[#111827]"
                />
              </div>

              <div className="pt-3 border-t border-[#F0F2F5] flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={() => {
                    setActiveToolModal(null);
                    setEditingContact(null);
                  }}
                  className="min-h-[40px] px-4 py-2 text-[12.5px] font-medium text-[#6B7280] hover:text-[#111827] rounded-xl"
                >
                  {t.actionCancel}
                </button>
                <button
                  type="submit"
                  className="min-h-[40px] px-4 py-2 text-[12.5px] font-medium bg-[#17191D] text-white hover:bg-neutral-800 rounded-xl"
                >
                  {editingContact ? t.actionSave : 'Add Contact'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal 2: Paste Multiple Numbers */}
      {activeToolModal === 'paste' && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/40 backdrop-blur-xs">
          <div
            className="w-full max-w-md bg-white rounded-t-2xl sm:rounded-2xl border border-[#E5E7EB] shadow-2xl p-5"
            role="dialog"
            aria-modal="true"
          >
            <div className="flex items-center justify-between pb-3 border-b border-[#F0F2F5]">
              <div>
                <h3 className="text-[15px] font-semibold text-[#111827]">Paste Numbers</h3>
                <p className="text-[11.5px] text-[#8E95A2]">
                  One entry per line (Name, Phone or Phone only)
                </p>
              </div>
              <button
                onClick={() => setActiveToolModal(null)}
                className="w-8 h-8 flex items-center justify-center text-[#8E95A2] hover:text-[#111827] rounded-lg"
                aria-label="Close"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handlePasteNumbersSubmit} className="mt-3.5 space-y-3">
              {pasteError && (
                <div className="p-2.5 bg-red-50 border border-red-200 text-[#B42318] text-[12px] rounded-lg">
                  {pasteError}
                </div>
              )}

              <textarea
                rows={5}
                value={pasteRawText}
                onChange={(e) => {
                  setPasteRawText(e.target.value);
                  if (pasteError) setPasteError(null);
                }}
                placeholder={`Sarah Mitchell, +12025550142\nJames Wilson, +12025550168\n+201001234567`}
                className="w-full p-3 text-[12.5px] font-mono bg-[#F7F8FA] border border-[#E5E7EB] rounded-xl text-[#111827] focus:outline-none focus:border-[#111827] resize-none"
                autoFocus
              />

              <div className="flex items-center justify-between text-[11.5px] text-[#6B7280] bg-[#F7F8FA] px-3 py-2 rounded-lg border border-[#F0F2F5]">
                <span>
                  Valid numbers:{' '}
                  <strong className="text-[#047857] tabular-nums">
                    {parsedPastePreview.validContacts.length}
                  </strong>
                </span>
                {parsedPastePreview.invalidLines.length > 0 && (
                  <span className="text-[#B42318] tabular-nums">
                    {parsedPastePreview.invalidLines.length} invalid line(s) skipped
                  </span>
                )}
                <button
                  type="button"
                  onClick={() =>
                    setPasteRawText(
                      `Sarah Mitchell, +12025550142\nJames Wilson, +12025550168\n+201001234567`
                    )
                  }
                  className="text-[#2563EB] hover:underline font-medium"
                >
                  Insert sample
                </button>
              </div>

              <div className="pt-3 border-t border-[#F0F2F5] flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setActiveToolModal(null)}
                  className="min-h-[40px] px-4 py-2 text-[12.5px] font-medium text-[#6B7280] hover:text-[#111827] rounded-xl"
                >
                  {t.actionCancel}
                </button>
                <button
                  type="submit"
                  className="min-h-[40px] px-4 py-2 text-[12.5px] font-medium bg-[#17191D] text-white hover:bg-neutral-800 rounded-xl"
                >
                  Add to Campaign ({parsedPastePreview.validContacts.length})
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal 3: Import CSV / TXT File */}
      {activeToolModal === 'import' && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/40 backdrop-blur-xs">
          <div
            className="w-full max-w-md bg-white rounded-t-2xl sm:rounded-2xl border border-[#E5E7EB] shadow-2xl p-5"
            role="dialog"
            aria-modal="true"
          >
            <div className="flex items-center justify-between pb-3 border-b border-[#F0F2F5]">
              <div>
                <h3 className="text-[15px] font-semibold text-[#111827]">
                  Import Contacts (CSV / TXT)
                </h3>
                <p className="text-[11.5px] text-[#8E95A2]">
                  Supports UTF-8 CSV or plain-text contact lists up to 2MB
                </p>
              </div>
              <button
                onClick={() => setActiveToolModal(null)}
                className="w-8 h-8 flex items-center justify-center text-[#8E95A2] hover:text-[#111827] rounded-lg"
                aria-label="Close"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="mt-4 space-y-3">
              {importError && (
                <div className="p-2.5 bg-red-50 border border-red-200 text-[#B42318] text-[12px] rounded-lg">
                  {importError}
                </div>
              )}

              <input
                ref={fileInputRef}
                type="file"
                accept=".csv,.txt"
                onChange={handleFileUpload}
                className="hidden"
              />

              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="w-full py-6 px-4 border border-dashed border-[#D1D5DB] hover:border-[#111827] rounded-2xl bg-[#F7F8FA] text-center transition-colors"
              >
                <FileUp className="w-5 h-5 text-[#111827] mx-auto mb-1.5" />
                <span className="block text-[13px] font-semibold text-[#111827]">
                  Choose CSV or TXT File from Device
                </span>
                <span className="block text-[11.5px] text-[#8E95A2] mt-0.5">
                  Columns: Name, Phone Number
                </span>
              </button>

              <div className="flex items-center justify-between gap-2 pt-1">
                <button
                  type="button"
                  onClick={handleSampleCsvImport}
                  className="flex-1 py-2 px-3 rounded-xl bg-[#17191D] text-white text-[12px] font-medium hover:bg-neutral-800 transition-colors"
                >
                  Load Verified Sample CSV (3 Contacts)
                </button>
                <button
                  type="button"
                  onClick={() =>
                    setImportError(
                      'Import failed: File contains malformed phone column on rows 2–4.'
                    )
                  }
                  className="py-2 px-3 rounded-xl border border-[#E5E7EB] text-[12px] font-medium text-[#6B7280] hover:text-[#111827]"
                >
                  Test Invalid File
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal 4: Phone Contacts (with Android Permission Check) */}
      {activeToolModal === 'phonebook' && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/40 backdrop-blur-xs">
          <div
            className="w-full max-w-md bg-white rounded-t-2xl sm:rounded-2xl border border-[#E5E7EB] shadow-2xl p-5 max-h-[82vh] flex flex-col"
            role="dialog"
            aria-modal="true"
          >
            <div className="flex items-center justify-between pb-3 border-b border-[#F0F2F5]">
              <div>
                <h3 className="text-[15px] font-semibold text-[#111827]">
                  Select Phone Contacts
                </h3>
                <p className="text-[11.5px] text-[#8E95A2]">
                  Import directly from your Android Contacts directory
                </p>
              </div>
              <button
                onClick={() => setActiveToolModal(null)}
                className="w-8 h-8 flex items-center justify-center text-[#8E95A2] hover:text-[#111827] rounded-lg"
                aria-label="Close"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {!contactsPermissionGranted ? (
              <div className="py-6 text-center space-y-3">
                <div className="w-10 h-10 rounded-full bg-amber-50 text-[#B45309] flex items-center justify-center mx-auto">
                  <ShieldAlert className="w-5 h-5" />
                </div>
                <h4 className="text-[14px] font-semibold text-[#111827]">
                  Phone Contacts Permission Required
                </h4>
                <p className="text-[12px] text-[#6B7280] max-w-xs mx-auto leading-relaxed">
                  O2Dialer requires Android READ_CONTACTS access to display your device address book. Paste Numbers and CSV Import remain available without this permission.
                </p>
                <button
                  type="button"
                  onClick={onRequestContactsPermission}
                  className="px-4 py-2 rounded-xl bg-[#17191D] text-white text-[12.5px] font-medium"
                >
                  Open Permission Settings
                </button>
              </div>
            ) : (
              <>
                <div className="overflow-y-auto divide-y divide-[#F0F2F5] my-2">
                  {samplePhonebookContacts.map((pb) => {
                    const isChecked = selectedPbIds.has(pb.id);
                    return (
                      <button
                        key={pb.id}
                        type="button"
                        onClick={() => {
                          setSelectedPbIds((prev) => {
                            const next = new Set(prev);
                            if (next.has(pb.id)) next.delete(pb.id);
                            else next.add(pb.id);
                            return next;
                          });
                        }}
                        className="w-full py-2.5 px-2 flex items-center justify-between gap-3 text-left hover:bg-[#F7F8FA] rounded-xl"
                      >
                        <div className="min-w-0">
                          <div className="text-[13px] font-semibold text-[#111827] truncate">
                            {pb.name}
                          </div>
                          <div className="text-[11.5px] font-mono text-[#6B7280]">
                            {pb.phone} · {pb.company}
                          </div>
                        </div>
                        <div
                          className={`w-5 h-5 rounded-md border flex items-center justify-center ${
                            isChecked
                              ? 'bg-[#17191D] border-[#17191D] text-white'
                              : 'border-[#D1D5DB] bg-white'
                          }`}
                        >
                          {isChecked && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                        </div>
                      </button>
                    );
                  })}
                </div>

                <div className="pt-3 border-t border-[#F0F2F5] flex items-center justify-between">
                  <span className="text-[12px] text-[#6B7280] tabular-nums">
                    {selectedPbIds.size} selected
                  </span>
                  <button
                    type="button"
                    disabled={selectedPbIds.size === 0}
                    onClick={handleImportPhoneContacts}
                    className="px-4 py-2 rounded-xl bg-[#17191D] text-white text-[12.5px] font-medium disabled:opacity-40"
                  >
                    Import Selected
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {/* Delete Contact Confirmation Dialog */}
      {deletingContact && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs">
          <div
            className="w-full max-w-sm bg-white rounded-2xl border border-[#E5E7EB] shadow-2xl p-5"
            role="alertdialog"
            aria-modal="true"
          >
            <h3 className="text-[15px] font-semibold text-[#111827]">Delete Contact</h3>
            <p className="text-[12.5px] text-[#6B7280] mt-1 leading-relaxed">
              Are you sure you want to remove <strong>{deletingContact.name}</strong> ({deletingContact.phone}) from "{campaign.name}"?
            </p>
            <div className="flex items-center justify-end gap-2 mt-4 pt-3 border-t border-[#F0F2F5]">
              <button
                type="button"
                onClick={() => setDeletingContact(null)}
                className="px-3.5 py-1.5 text-[12.5px] font-medium text-[#6B7280] hover:text-[#111827] rounded-lg"
              >
                {t.actionCancel}
              </button>
              <button
                type="button"
                onClick={handleConfirmDeleteContact}
                className="px-3.5 py-1.5 text-[12.5px] font-medium bg-[#B42318] text-white rounded-lg"
              >
                {t.actionDelete}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
