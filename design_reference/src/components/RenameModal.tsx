import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { Campaign } from '../types/campaign';
import { Translations } from '../i18n/translations';

interface RenameModalProps {
  campaign: Campaign | null;
  isOpen: boolean;
  onClose: () => void;
  onRename: (campaignId: string, newName: string) => void;
  t: Translations;
}

export const RenameModal: React.FC<RenameModalProps> = ({
  campaign,
  isOpen,
  onClose,
  onRename,
  t,
}) => {
  const [name, setName] = useState('');

  useEffect(() => {
    if (campaign) {
      setName(campaign.name);
    }
  }, [campaign]);

  if (!isOpen || !campaign) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (name.trim()) {
      onRename(campaign.id, name.trim());
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs">
      <div
        className="w-full max-w-sm bg-white rounded-2xl border border-[#E9EBEF] shadow-2xl p-5"
        role="dialog"
        aria-modal="true"
        aria-labelledby="rename-campaign-heading"
      >
        <div className="flex items-center justify-between mb-3">
          <h3 id="rename-campaign-heading" className="text-[15px] font-semibold text-[#17191D]">
            {t.actionRename}
          </h3>
          <button
            onClick={onClose}
            className="p-1 text-[#777E89] hover:text-[#17191D] rounded-md transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-[11.5px] font-medium text-[#777E89] mb-1">
              New Campaign Name
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full h-9 px-3 text-[13px] bg-[#F7F8FA] border border-[#E9EBEF] rounded-lg text-[#17191D] focus:outline-none focus:border-[#2563EB]"
              autoFocus
            />
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-3 py-1.5 text-[12px] font-medium text-[#777E89] hover:text-[#17191D] rounded-lg transition-colors"
            >
              {t.actionCancel}
            </button>
            <button
              type="submit"
              disabled={!name.trim()}
              className="px-3.5 py-1.5 text-[12px] font-medium bg-[#17191D] text-white hover:bg-neutral-800 disabled:opacity-50 rounded-lg transition-colors"
            >
              {t.actionSave}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
