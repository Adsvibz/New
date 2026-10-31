import React, { useState } from 'react';
import { X, Copy, Check, Download, Share2 } from 'lucide-react';
import { Campaign } from '../types/campaign';
import { Translations } from '../i18n/translations';
import { formatDuration } from '../utils/campaignLogic';

interface ShareCampaignModalProps {
  campaign: Campaign | null;
  isOpen: boolean;
  onClose: () => void;
  onCopied: () => void;
  t: Translations;
}

export const ShareCampaignModal: React.FC<ShareCampaignModalProps> = ({
  campaign,
  isOpen,
  onClose,
  onCopied,
  t,
}) => {
  const [copied, setCopied] = useState(false);

  if (!isOpen || !campaign) return null;

  // Generate real CSV-formatted content per Part 9.7:
  // Name, Number, Status, Duration, Called At, Stage, Feedback
  const csvHeader = 'Name,Number,Status,Duration,Called At,Stage,Feedback';
  const csvRows = campaign.contacts.map((c) => {
    const calledAtStr = c.calledAt
      ? new Date(c.calledAt).toISOString()
      : 'Not called';
    const stageStr = c.stage && c.stage !== 'none' ? c.stage : '';
    const escapedFeedback = (c.notes || '').replace(/"/g, '""');
    return `"${c.name}","${c.phone}","${c.status}","${formatDuration(
      c.callDurationSeconds
    )}","${calledAtStr}","${stageStr}","${escapedFeedback}"`;
  });
  const csvContent = [csvHeader, ...csvRows].join('\n');

  const handleCopy = () => {
    navigator.clipboard?.writeText(csvContent);
    setCopied(true);
    onCopied();
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadCsv = () => {
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${campaign.name.toLowerCase().replace(/\s+/g, '-')}-report.csv`;
    a.click();
    URL.revokeObjectURL(url);
    onCopied();
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs">
      <div
        className="w-full max-w-md bg-white rounded-2xl border border-[#E5E7EB] shadow-2xl p-5"
        role="dialog"
        aria-modal="true"
        aria-labelledby="share-dialog-title"
      >
        <div className="flex items-center justify-between pb-3 border-b border-[#F0F2F5]">
          <div className="flex items-center gap-2">
            <Share2 className="w-4 h-4 text-[#111827]" />
            <h3 id="share-dialog-title" className="text-[15px] font-semibold text-[#111827]">
              {t.actionShare} (CSV)
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 text-[#8E95A2] hover:text-[#111827] rounded-md transition-colors"
            aria-label="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="py-3">
          <p className="text-[12px] text-[#6B7280] mb-2">
            CSV report for <strong>{campaign.name}</strong> ({campaign.contacts.length} contacts):
          </p>
          <pre className="p-3 bg-[#F7F8FA] border border-[#E5E7EB] rounded-xl text-[10.5px] font-mono text-[#111827] overflow-x-auto max-h-48 select-all">
            {csvContent}
          </pre>
        </div>

        <div className="flex items-center justify-end gap-2 pt-2 border-t border-[#F0F2F5]">
          <button
            onClick={onClose}
            className="px-3.5 py-1.5 text-[12px] font-medium text-[#6B7280] hover:text-[#111827] rounded-xl"
          >
            Close
          </button>
          <button
            onClick={handleCopy}
            className="flex items-center gap-1.5 px-3.5 py-1.5 text-[12px] font-medium border border-[#E5E7EB] text-[#111827] hover:bg-[#F4F5F7] rounded-xl transition-colors"
          >
            {copied ? (
              <Check className="w-3.5 h-3.5 text-[#047857]" />
            ) : (
              <Copy className="w-3.5 h-3.5" />
            )}
            <span>{copied ? 'Copied' : 'Copy CSV'}</span>
          </button>
          <button
            onClick={handleDownloadCsv}
            className="flex items-center gap-1.5 px-3.5 py-1.5 text-[12px] font-medium bg-[#17191D] hover:bg-neutral-800 text-white rounded-xl transition-colors"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Download .CSV</span>
          </button>
        </div>
      </div>
    </div>
  );
};
