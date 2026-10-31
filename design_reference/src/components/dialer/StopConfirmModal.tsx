import React from 'react';
import { Square, X } from 'lucide-react';
import { Translations } from '../../i18n/translations';

interface StopConfirmModalProps {
  isOpen: boolean;
  campaignName: string;
  onConfirm: () => void;
  onCancel: () => void;
  t: Translations;
}

export const StopConfirmModal: React.FC<StopConfirmModalProps> = ({
  isOpen,
  campaignName,
  onConfirm,
  onCancel,
  t,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs">
      <div
        className="w-full max-w-sm bg-white rounded-2xl border border-[#E9EBEF] shadow-2xl p-5"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="stop-session-title"
      >
        <div className="flex items-start gap-3">
          <div className="w-9 h-9 rounded-full bg-red-50 text-red-600 flex items-center justify-center shrink-0">
            <Square className="w-4 h-4 fill-current" />
          </div>
          <div className="min-w-0 flex-1">
            <h3 id="stop-session-title" className="text-[15px] font-semibold text-[#111827]">
              {t.stopSessionConfirmTitle}
            </h3>
            <p className="text-[12.5px] text-[#6B7280] mt-1 leading-relaxed">
              {t.stopSessionConfirmMsg}
            </p>
          </div>
        </div>

        <div className="flex items-center justify-end gap-2 mt-5 pt-3 border-t border-[#F0F2F5]">
          <button
            type="button"
            onClick={onCancel}
            className="px-3.5 py-1.5 text-[12.5px] font-medium text-[#6B7280] hover:text-[#111827] hover:bg-[#F4F5F7] rounded-lg transition-colors"
          >
            {t.actionCancel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="px-3.5 py-1.5 text-[12.5px] font-medium bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors"
          >
            {t.actionConfirmStop}
          </button>
        </div>
      </div>
    </div>
  );
};
