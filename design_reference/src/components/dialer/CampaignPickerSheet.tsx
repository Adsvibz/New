import React from 'react';
import { X, Check } from 'lucide-react';
import { Campaign } from '../../types/campaign';
import { Translations } from '../../i18n/translations';

interface CampaignPickerSheetProps {
  campaigns: Campaign[];
  selectedCampaignId?: string;
  isOpen: boolean;
  onClose: () => void;
  onSelectCampaign: (campaign: Campaign) => void;
  t: Translations;
}

export const CampaignPickerSheet: React.FC<CampaignPickerSheetProps> = ({
  campaigns,
  selectedCampaignId,
  isOpen,
  onClose,
  onSelectCampaign,
  t,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/40 backdrop-blur-xs">
      <div
        className="w-full max-w-sm bg-white rounded-t-2xl sm:rounded-2xl border border-[#E9EBEF] shadow-2xl p-5 max-h-[80vh] flex flex-col"
        role="dialog"
        aria-modal="true"
        aria-labelledby="campaign-picker-title"
      >
        <div className="flex items-center justify-between pb-3 border-b border-[#F0F2F5]">
          <h3 id="campaign-picker-title" className="text-[15px] font-semibold text-[#111827]">
            Select Campaign
          </h3>
          <button
            onClick={onClose}
            className="p-1 text-[#8E95A2] hover:text-[#111827] rounded-md transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="overflow-y-auto py-2 divide-y divide-[#F0F2F5]">
          {campaigns.map((camp) => {
            const isSelected = camp.id === selectedCampaignId;
            return (
              <button
                key={camp.id}
                onClick={() => {
                  onSelectCampaign(camp);
                  onClose();
                }}
                className={`w-full py-3 px-2 text-left flex items-center justify-between gap-3 hover:bg-[#FAFAFA] rounded-xl transition-colors ${
                  isSelected ? 'bg-[#F4F5F7]' : ''
                }`}
              >
                <div className="min-w-0 flex-1">
                  <div className="text-[13.5px] font-semibold text-[#111827] truncate">
                    {camp.name}
                  </div>
                  <div className="text-[11.5px] text-[#8E95A2] mt-0.5 tabular-nums">
                    {camp.pendingCalls} pending of {camp.totalContacts} · {camp.progressPercentage}%
                  </div>
                </div>

                {isSelected && (
                  <Check className="w-4 h-4 text-[#111827] stroke-[2.5] shrink-0" />
                )}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
