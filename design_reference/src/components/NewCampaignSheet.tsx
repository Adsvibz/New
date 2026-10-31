import React, { useState } from 'react';
import { X, Smartphone } from 'lucide-react';
import { Campaign } from '../types/campaign';
import { Translations, Language } from '../i18n/translations';

interface NewCampaignSheetProps {
  isOpen: boolean;
  onClose: () => void;
  onCreate: (campaignData: Partial<Campaign>) => void;
  currentLanguage?: Language;
  t: Translations;
}

export const NewCampaignSheet: React.FC<NewCampaignSheetProps> = ({
  isOpen,
  onClose,
  onCreate,
  currentLanguage = 'en',
  t,
}) => {
  const [name, setName] = useState('');
  const [simSlot, setSimSlot] = useState<number>(-1); // -1 = Default SIM, 0 = SIM 1, 1 = SIM 2
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const isValid = name.trim().length > 0;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isValid) {
      setError(
        currentLanguage === 'ar'
          ? 'اسم الحملة مطلوب.'
          : 'Campaign name is required.'
      );
      return;
    }

    onCreate({
      name: name.trim(),
      simSlot,
      status: 'ready',
      totalContacts: 0,
      completedCalls: 0,
      pendingCalls: 0,
      answeredCalls: 0,
      noAnswerCalls: 0,
      failedCalls: 0,
      unverifiedCalls: 0,
      progressPercentage: 0,
      contacts: [],
      tags: ['Outbound'],
      duplicateContactsFound: 0,
    });

    setName('');
    setSimSlot(-1);
    setError(null);
    onClose();
  };

  const simOptions = [
    {
      slot: -1,
      label: currentLanguage === 'ar' ? 'الشريحة الافتراضية' : 'Default SIM',
      sub: currentLanguage === 'ar' ? 'اختيار النظام التلقائي' : 'System default SIM',
    },
    {
      slot: 0,
      label: 'SIM 1',
      sub: currentLanguage === 'ar' ? 'الشريحة الأولى' : 'Carrier Line 1',
    },
    {
      slot: 1,
      label: 'SIM 2',
      sub: currentLanguage === 'ar' ? 'الشريحة الثانية' : 'Business Line 2',
    },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/40 backdrop-blur-xs">
      <div
        className="w-full max-w-md bg-white rounded-t-2xl sm:rounded-2xl border border-[#E5E7EB] shadow-2xl overflow-hidden flex flex-col"
        role="dialog"
        aria-modal="true"
        aria-labelledby="new-campaign-heading"
      >
        {/* Header */}
        <div className="px-5 py-4 border-b border-[#F0F2F5] flex items-center justify-between">
          <div>
            <h2 id="new-campaign-heading" className="text-[16px] font-semibold text-[#111827]">
              {t.actionCreate}
            </h2>
            <p className="text-[12px] text-[#8E95A2] mt-0.5">
              {currentLanguage === 'ar'
                ? 'أدخل اسم الحملة واختر شريحة الاتصال الصادرة'
                : 'Enter a campaign name and select the outgoing SIM account'}
            </p>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center text-[#8E95A2] hover:text-[#111827] rounded-lg transition-colors"
            aria-label="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {error && (
            <div className="p-2.5 bg-red-50 border border-red-200 text-[#B42318] text-[12px] rounded-lg">
              {error}
            </div>
          )}

          {/* Required: Campaign Name */}
          <div>
            <label
              htmlFor="campaign-name-input"
              className="block text-[12px] font-medium text-[#111827] mb-1.5"
            >
              {currentLanguage === 'ar' ? 'اسم الحملة *' : 'Campaign Name *'}
            </label>
            <input
              id="campaign-name-input"
              type="text"
              value={name}
              onChange={(e) => {
                setName(e.target.value);
                if (error) setError(null);
              }}
              placeholder={
                currentLanguage === 'ar'
                  ? 'مثال: عملاء العقارات - أكتوبر'
                  : 'e.g. Q4 Customer Outreach Wave 1'
              }
              className="w-full h-10 px-3.5 text-[13.5px] bg-[#F7F8FA] border border-[#E5E7EB] rounded-xl focus:outline-none focus:border-[#111827] text-[#111827] placeholder:text-[#8E95A2]"
              autoFocus
            />
          </div>

          {/* Optional: Outgoing SIM */}
          <div>
            <label className="block text-[12px] font-medium text-[#111827] mb-1.5">
              {currentLanguage === 'ar' ? 'شريحة الاتصال الصادر (SIM)' : 'Outgoing SIM'}
            </label>
            <div className="grid grid-cols-3 gap-2">
              {simOptions.map((opt) => {
                const isSelected = simSlot === opt.slot;
                return (
                  <button
                    key={opt.slot}
                    type="button"
                    onClick={() => setSimSlot(opt.slot)}
                    className={`p-2.5 rounded-xl border text-left transition-all ${
                      isSelected
                        ? 'border-[#17191D] bg-[#17191D] text-white'
                        : 'border-[#E5E7EB] bg-[#F7F8FA] hover:bg-[#F4F5F7] text-[#111827]'
                    }`}
                  >
                    <div className="flex items-center gap-1.5 mb-0.5">
                      <Smartphone
                        className={`w-3.5 h-3.5 ${
                          isSelected ? 'text-white' : 'text-[#8E95A2]'
                        }`}
                      />
                      <span className="text-[12px] font-semibold truncate">{opt.label}</span>
                    </div>
                    <span
                      className={`block text-[10px] truncate ${
                        isSelected ? 'text-neutral-300' : 'text-[#8E95A2]'
                      }`}
                    >
                      {opt.sub}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Actions */}
          <div className="pt-3 border-t border-[#F0F2F5] flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="min-h-[40px] px-4 py-2 text-[12.5px] font-medium text-[#6B7280] hover:text-[#111827] hover:bg-[#F4F5F7] rounded-xl transition-colors"
            >
              {t.actionCancel}
            </button>
            <button
              type="submit"
              disabled={!isValid}
              className="min-h-[40px] px-4 py-2 text-[12.5px] font-medium bg-[#17191D] text-white hover:bg-neutral-800 disabled:opacity-40 disabled:pointer-events-none rounded-xl transition-colors"
            >
              {t.actionCreate}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
