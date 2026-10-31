import React from 'react';
import { Trash2, Pause, Play, Share2, CheckSquare, X } from 'lucide-react';
import { Translations } from '../i18n/translations';

interface MultiSelectToolbarProps {
  selectedCount: number;
  totalCount: number;
  onSelectAll: () => void;
  onDeselectAll: () => void;
  onBulkDelete: () => void;
  onBulkPause: () => void;
  onBulkResume: () => void;
  onBulkShare: () => void;
  onCancel: () => void;
  t: Translations;
}

export const MultiSelectToolbar: React.FC<MultiSelectToolbarProps> = ({
  selectedCount,
  totalCount,
  onSelectAll,
  onDeselectAll,
  onBulkDelete,
  onBulkPause,
  onBulkResume,
  onBulkShare,
  onCancel,
  t,
}) => {
  const isAllSelected = selectedCount > 0 && selectedCount === totalCount;

  return (
    <div className="px-[20px] py-2.5 bg-transparent text-[#111318] flex items-center justify-between gap-2 shrink-0">
      {/* Selection count & select/deselect all */}
      <div className="flex items-center gap-2.5">
        <button
          onClick={onCancel}
          className="w-8 h-8 rounded-full bg-white/60 backdrop-blur-md flex items-center justify-center text-[#6E7582] hover:text-[#111318] transition-colors"
          aria-label="Cancel selection"
        >
          <X className="w-4 h-4" />
        </button>
        <span className="text-[13px] font-bold tabular-nums text-[#111318]">
          {t.selectedCount(selectedCount)}
        </span>
        <button
          onClick={isAllSelected ? onDeselectAll : onSelectAll}
          className="text-[11.5px] text-[#E2532B] font-semibold ml-1 transition-colors"
        >
          {isAllSelected ? t.deselectAll : t.selectAll}
        </button>
      </div>

      {/* Action buttons */}
      <div className="flex items-center gap-1.5">
        <button
          onClick={onBulkResume}
          disabled={selectedCount === 0}
          className="w-9 h-9 rounded-full bg-white/60 backdrop-blur-md flex items-center justify-center text-[#111318] hover:bg-white disabled:opacity-40 disabled:pointer-events-none transition-colors"
          title={t.bulkResume}
          aria-label={t.bulkResume}
        >
          <Play className="w-4 h-4 fill-current" />
        </button>
        <button
          onClick={onBulkPause}
          disabled={selectedCount === 0}
          className="w-9 h-9 rounded-full bg-white/60 backdrop-blur-md flex items-center justify-center text-[#111318] hover:bg-white disabled:opacity-40 disabled:pointer-events-none transition-colors"
          title={t.bulkPause}
          aria-label={t.bulkPause}
        >
          <Pause className="w-4 h-4 fill-current" />
        </button>
        <button
          onClick={onBulkShare}
          disabled={selectedCount === 0}
          className="w-9 h-9 rounded-full bg-white/60 backdrop-blur-md flex items-center justify-center text-[#111318] hover:bg-white disabled:opacity-40 disabled:pointer-events-none transition-colors"
          title={t.bulkShare}
          aria-label={t.bulkShare}
        >
          <Share2 className="w-4 h-4" />
        </button>
        <button
          onClick={onBulkDelete}
          disabled={selectedCount === 0}
          className="w-9 h-9 rounded-full bg-white/60 backdrop-blur-md flex items-center justify-center text-[#B42318] hover:bg-rose-50 disabled:opacity-40 disabled:pointer-events-none transition-colors ml-0.5"
          title={t.bulkDelete}
          aria-label={t.bulkDelete}
        >
          <Trash2 className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
