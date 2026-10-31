import React from 'react';
import { Users2, Check, X } from 'lucide-react';
import { Campaign } from '../types/campaign';
import { Translations } from '../i18n/translations';

interface DuplicateContactsModalProps {
  campaign: Campaign | null;
  isOpen: boolean;
  onClose: () => void;
  onConfirmRemove: (campaign: Campaign) => void;
  t: Translations;
}

export const DuplicateContactsModal: React.FC<DuplicateContactsModalProps> = ({
  campaign,
  isOpen,
  onClose,
  onConfirmRemove,
  t,
}) => {
  if (!isOpen || !campaign) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs">
      <div
        className="w-full max-w-sm bg-white rounded-2xl border border-[#E9EBEF] shadow-2xl p-5"
        role="dialog"
        aria-modal="true"
        aria-labelledby="duplicate-dialog-title"
      >
        <div className="flex items-start gap-3">
          <div className="w-9 h-9 rounded-full bg-amber-50 text-amber-600 flex items-center justify-center shrink-0">
            <Users2 className="w-4 h-4" />
          </div>
          <div className="min-w-0 flex-1">
            <h3 id="duplicate-dialog-title" className="text-[15px] font-semibold text-[#17191D]">
              {t.actionRemoveDuplicates}
            </h3>
            <p className="text-[12.5px] text-[#777E89] mt-1 leading-relaxed">
              Found <strong className="text-[#17191D] font-semibold">{campaign.duplicateContactsFound} duplicate phone numbers</strong> in{' '}
              <span className="font-medium text-[#17191D]">"{campaign.name}"</span>. Removing them will optimize auto-dialing and prevent repeat calls.
            </p>
          </div>
        </div>

        <div className="flex items-center justify-end gap-2 mt-5 pt-3 border-t border-[#E9EBEF]/60">
          <button
            type="button"
            onClick={onClose}
            className="px-3.5 py-1.5 text-[12.5px] font-medium text-[#777E89] hover:text-[#17191D] rounded-lg transition-colors"
          >
            {t.actionCancel}
          </button>
          <button
            type="button"
            onClick={() => onConfirmRemove(campaign)}
            className="px-3.5 py-1.5 text-[12.5px] font-medium bg-[#17191D] hover:bg-neutral-800 text-white rounded-lg transition-colors"
          >
            Clean Duplicates
          </button>
        </div>
      </div>
    </div>
  );
};
