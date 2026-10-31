import React, { useState, useEffect } from 'react';
import {
  X,
  Star,
  PhoneOff,
  PhoneForwarded,
  MessageCircle,
  Voicemail,
  ExternalLink,
} from 'lucide-react';
import { CallStage, CampaignContact } from '../types/campaign';
import { Translations, Language } from '../i18n/translations';
import { formatDuration } from '../utils/campaignLogic';

interface StageClassificationSheetProps {
  contact: CampaignContact | null;
  isOpen: boolean;
  currentLanguage?: Language;
  onClose: () => void;
  onSave: (contactId: string, stage: CallStage, feedback: string) => void;
  onOpenWhatsApp?: (phone: string, name: string) => void;
  t: Translations;
}

export const StageClassificationSheet: React.FC<StageClassificationSheetProps> = ({
  contact,
  isOpen,
  currentLanguage = 'en',
  onClose,
  onSave,
  onOpenWhatsApp,
  t,
}) => {
  const [selectedStage, setSelectedStage] = useState<CallStage>('potential');
  const [feedback, setFeedback] = useState<string>('');

  useEffect(() => {
    if (contact) {
      setSelectedStage(contact.stage && contact.stage !== 'none' ? contact.stage : 'potential');
      setFeedback(contact.notes || '');
    }
  }, [contact]);

  if (!isOpen || !contact) return null;

  const stages: Array<{
    id: CallStage;
    label: string;
    desc: string;
    icon: React.FC<{ className?: string }>;
  }> = [
    {
      id: 'potential',
      label: t.stagePotential,
      desc: currentLanguage === 'ar' ? 'عميل مهتم بالعرض' : 'Interested prospect',
      icon: Star,
    },
    {
      id: 'call_back',
      label: t.stageCallBack,
      desc: currentLanguage === 'ar' ? 'طلب الاتصال لاحقاً' : 'Requested follow-up call',
      icon: PhoneForwarded,
    },
    {
      id: 'whatsapp',
      label: t.stageWhatsapp,
      desc: currentLanguage === 'ar' ? 'متابعة عبر واتساب' : 'Send details via WhatsApp',
      icon: MessageCircle,
    },
    {
      id: 'voicemail',
      label: t.stageVoicemail,
      desc: currentLanguage === 'ar' ? 'تم ترك رسالة صوتية' : 'Left voice message',
      icon: Voicemail,
    },
    {
      id: 'not_interested',
      label: t.stageNotInterested,
      desc: currentLanguage === 'ar' ? 'غير مهتم حالياً' : 'Declined offer',
      icon: PhoneOff,
    },
  ];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(contact.id, selectedStage, feedback.trim());
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/40 backdrop-blur-xs">
      <div
        className="w-full max-w-md bg-white rounded-t-2xl sm:rounded-2xl border border-[#E5E7EB] shadow-2xl overflow-hidden max-h-[90vh] flex flex-col"
        role="dialog"
        aria-modal="true"
        aria-labelledby="stage-sheet-heading"
      >
        {/* Header */}
        <div className="px-5 py-4 border-b border-[#F0F2F5] flex items-center justify-between">
          <div className="min-w-0">
            <h2 id="stage-sheet-heading" className="text-[16px] font-semibold text-[#111827] truncate">
              {currentLanguage === 'ar' ? 'تصنيف نتيجة المكالمة' : 'Classify Call Outcome'}
            </h2>
            <div className="flex items-center gap-1.5 text-[12px] text-[#8E95A2] mt-0.5">
              <span className="font-medium text-[#111827] truncate">{contact.name}</span>
              <span aria-hidden="true">·</span>
              <span className="font-mono tabular-nums" dir="ltr">
                {contact.phone}
              </span>
              {contact.callDurationSeconds > 0 && (
                <>
                  <span aria-hidden="true">·</span>
                  <span className="tabular-nums">
                    {formatDuration(contact.callDurationSeconds)}
                  </span>
                </>
              )}
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center text-[#8E95A2] hover:text-[#111827] rounded-lg"
            aria-label="Skip classification"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4 overflow-y-auto">
          {/* 5 Core Stage Categories */}
          <div>
            <label className="block text-[12px] font-medium text-[#111827] mb-2">
              {currentLanguage === 'ar' ? 'اختر المرحلة (5 فئات أساسية)' : 'Select Call Stage'}
            </label>
            <div className="grid grid-cols-2 gap-2">
              {stages.map((st) => {
                const Icon = st.icon;
                const isSelected = selectedStage === st.id;
                return (
                  <button
                    key={st.id}
                    type="button"
                    onClick={() => setSelectedStage(st.id)}
                    className={`p-3 rounded-xl border text-left transition-all flex items-start gap-2.5 ${
                      isSelected
                        ? 'border-[#17191D] bg-[#17191D] text-white'
                        : 'border-[#E5E7EB] bg-[#F7F8FA] hover:bg-[#F4F5F7] text-[#111827]'
                    }`}
                  >
                    <Icon
                      className={`w-4 h-4 mt-0.5 shrink-0 ${
                        isSelected ? 'text-white' : 'text-[#6B7280]'
                      }`}
                    />
                    <div className="min-w-0">
                      <span className="block text-[12.5px] font-semibold leading-tight truncate">
                        {st.label}
                      </span>
                      <span
                        className={`block text-[10.5px] mt-0.5 truncate ${
                          isSelected ? 'text-neutral-300' : 'text-[#8E95A2]'
                        }`}
                      >
                        {st.desc}
                      </span>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Multiline Feedback / Notes */}
          <div>
            <label
              htmlFor="call-feedback-notes"
              className="block text-[12px] font-medium text-[#111827] mb-1.5"
            >
              {currentLanguage === 'ar' ? 'الملاحظات والتعليقات' : 'Feedback & Follow-up Notes'}
            </label>
            <textarea
              id="call-feedback-notes"
              rows={3}
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
              placeholder={
                currentLanguage === 'ar'
                  ? 'مثال: مهتم بشقة ٣ غرف. المتابعة يوم الخميس.'
                  : 'Interested in a three-bedroom apartment. Follow up on Thursday.'
              }
              className="w-full p-3 text-[13px] bg-[#F7F8FA] border border-[#E5E7EB] rounded-xl focus:outline-none focus:border-[#111827] text-[#111827] placeholder:text-[#8E95A2] resize-none"
            />
          </div>

          {/* External WhatsApp Shortcut when relevant */}
          {(selectedStage === 'whatsapp' || selectedStage === 'potential') && onOpenWhatsApp && (
            <div className="p-3 bg-[#F7F8FA] border border-[#E5E7EB] rounded-xl flex items-center justify-between gap-3">
              <div className="min-w-0">
                <span className="block text-[12px] font-semibold text-[#111827]">
                  {currentLanguage === 'ar' ? 'متابعة فورية عبر واتساب' : 'WhatsApp Follow-up Shortcut'}
                </span>
                <span className="block text-[11px] text-[#8E95A2] truncate">
                  {currentLanguage === 'ar'
                    ? 'يفتح محادثة واتساب خارجية مع الرقم مباشرة'
                    : 'Opens external WhatsApp chat with this contact'}
                </span>
              </div>
              <button
                type="button"
                onClick={() => onOpenWhatsApp(contact.phone, contact.name)}
                className="px-3 py-1.5 rounded-lg border border-[#E5E7EB] bg-white hover:bg-[#F4F5F7] text-[12px] font-medium text-[#111827] flex items-center gap-1.5 shrink-0"
              >
                <ExternalLink className="w-3.5 h-3.5" />
                <span>WhatsApp</span>
              </button>
            </div>
          )}

          {/* Footer Actions */}
          <div className="pt-3 border-t border-[#F0F2F5] flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="min-h-[40px] px-4 py-2 text-[12.5px] font-medium text-[#6B7280] hover:text-[#111827] hover:bg-[#F4F5F7] rounded-xl transition-colors"
            >
              {currentLanguage === 'ar' ? 'تخطي' : 'Skip'}
            </button>
            <button
              type="submit"
              className="min-h-[40px] px-4 py-2 text-[12.5px] font-medium bg-[#17191D] text-white hover:bg-neutral-800 rounded-xl transition-colors"
            >
              {currentLanguage === 'ar' ? 'حفظ التصنيف' : 'Save Result'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
