import React from 'react';
import { AlertTriangle, Trash2, X } from 'lucide-react';
import { Translations } from '../i18n/translations';

interface DeleteConfirmDialogProps {
  isOpen: boolean;
  title?: string;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
  t: Translations;
}

export const DeleteConfirmDialog: React.FC<DeleteConfirmDialogProps> = ({
  isOpen,
  title,
  message,
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
        aria-labelledby="delete-dialog-title"
      >
        <div className="flex items-start gap-3">
          <div className="w-9 h-9 rounded-full bg-red-50 text-red-600 flex items-center justify-center shrink-0">
            <Trash2 className="w-4 h-4" />
          </div>
          <div className="min-w-0 flex-1">
            <h3 id="delete-dialog-title" className="text-[15px] font-semibold text-[#17191D]">
              {title || t.confirmDeleteTitle}
            </h3>
            <p className="text-[12.5px] text-[#777E89] mt-1 leading-relaxed">
              {message}
            </p>
          </div>
        </div>

        <div className="flex items-center justify-end gap-2 mt-5 pt-3 border-t border-[#E9EBEF]/60">
          <button
            type="button"
            onClick={onCancel}
            className="px-3.5 py-1.5 text-[12.5px] font-medium text-[#777E89] hover:text-[#17191D] hover:bg-[#F7F8FA] rounded-lg transition-colors"
          >
            {t.actionCancel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="px-3.5 py-1.5 text-[12.5px] font-medium bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors"
          >
            {t.actionDelete}
          </button>
        </div>
      </div>
    </div>
  );
};
