import React, { useState, useRef, useEffect } from 'react';
import { Search, X, SlidersHorizontal, Check } from 'lucide-react';
import { CampaignStatus, SortField, SortOrder } from '../types/campaign';
import { Translations } from '../i18n/translations';

interface SearchAndFilterBarProps {
  searchQuery: string;
  onSearchChange: (query: string) => void;
  selectedStatus: CampaignStatus | 'all';
  onStatusChange: (status: CampaignStatus | 'all') => void;
  sortBy: SortField;
  sortOrder: SortOrder;
  onSortChange: (field: SortField, order: SortOrder) => void;
  t: Translations;
  statusCounts: {
    all: number;
    ready: number;
    active: number;
    completed: number;
    paused: number;
  };
}

export const SearchAndFilterBar: React.FC<SearchAndFilterBarProps> = ({
  searchQuery,
  onSearchChange,
  selectedStatus,
  onStatusChange,
  sortBy,
  sortOrder,
  onSortChange,
  t,
}) => {
  const [isSortOpen, setIsSortOpen] = useState(false);
  const sortRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (sortRef.current && !sortRef.current.contains(e.target as Node)) {
        setIsSortOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const filterTabs: Array<{ id: CampaignStatus | 'all'; label: string }> = [
    { id: 'all', label: t.filterAll },
    { id: 'active', label: t.filterActive },
    { id: 'ready', label: t.filterReady },
    { id: 'paused', label: t.filterPaused },
    { id: 'completed', label: t.filterCompleted },
  ];

  const sortOptions: Array<{ field: SortField; label: string; defaultOrder: SortOrder }> = [
    { field: 'lastActivity', label: t.sortRecent, defaultOrder: 'desc' },
    { field: 'name', label: t.sortName, defaultOrder: 'asc' },
    { field: 'progress', label: t.sortProgress, defaultOrder: 'desc' },
    { field: 'contacts', label: t.sortContacts, defaultOrder: 'desc' },
  ];

  return (
    <div className="px-[20px] py-2 space-y-2.5">
      {/* Frosted Search Input + 44x44 Sort Button */}
      <div className="flex items-center gap-2.5">
        <div className="relative flex-1">
          <Search
            className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-[#8A909E] pointer-events-none"
            strokeWidth={1.75}
          />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder={t.searchPlaceholder}
            className="w-full h-11 pl-10 pr-9 text-[13px] font-medium bg-white/60 backdrop-blur-md text-[#111318] placeholder:text-[#8A909E] rounded-full shadow-[0_10px_28px_-8px_rgba(17,19,24,0.05)] focus:outline-none focus:bg-white/95 transition-all"
          />
          {searchQuery && (
            <button
              type="button"
              onClick={() => onSearchChange('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 w-7 h-7 flex items-center justify-center text-[#8A909E] hover:text-[#111318] rounded-full"
              aria-label="Clear search"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Sort Menu Button */}
        <div className="relative" ref={sortRef}>
          <button
            type="button"
            onClick={() => setIsSortOpen(!isSortOpen)}
            className="min-w-[44px] min-h-[44px] w-11 h-11 flex items-center justify-center rounded-full bg-white/60 backdrop-blur-md text-[#6B7280] hover:text-[#111318] shadow-[0_10px_28px_-8px_rgba(17,19,24,0.05)] transition-all active:scale-95"
            aria-label="Sort options"
            aria-expanded={isSortOpen}
          >
            <SlidersHorizontal className="w-4 h-4" strokeWidth={1.75} />
          </button>

          {isSortOpen && (
            <div className="absolute right-0 top-full mt-2 w-52 bg-white/95 backdrop-blur-2xl rounded-[24px] shadow-[0_20px_48px_-12px_rgba(17,19,24,0.16)] p-2 z-30">
              <span className="block px-3 py-1.5 text-[10px] font-bold text-[#8A909E] uppercase tracking-wider">
                {t.sortBy}
              </span>
              {sortOptions.map((opt) => {
                const isSelected = sortBy === opt.field;
                return (
                  <button
                    key={opt.field}
                    type="button"
                    onClick={() => {
                      if (isSelected) {
                        onSortChange(opt.field, sortOrder === 'asc' ? 'desc' : 'asc');
                      } else {
                        onSortChange(opt.field, opt.defaultOrder);
                      }
                      setIsSortOpen(false);
                    }}
                    className={`w-full flex items-center justify-between px-3 py-2 text-xs rounded-2xl text-left transition-colors ${
                      isSelected
                        ? 'bg-[#111318] text-white font-semibold'
                        : 'text-[#6B7280] hover:bg-[#111318]/[0.05] hover:text-[#111318]'
                    }`}
                  >
                    <span>{opt.label}</span>
                    {isSelected && (
                      <span className="flex items-center gap-1 text-[11px]">
                        <span>{sortOrder === 'asc' ? '↑' : '↓'}</span>
                        <Check className="w-3.5 h-3.5" strokeWidth={2.2} />
                      </span>
                    )}
                  </button>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Filter Tabs (All, Active, Ready, Paused, Completed) */}
      <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-0.5">
        {filterTabs.map((tab) => {
          const isActive = selectedStatus === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => onStatusChange(tab.id)}
              className={`min-h-[36px] px-3.5 py-1.5 rounded-full text-xs font-semibold transition-all whitespace-nowrap shrink-0 ${
                isActive
                  ? 'bg-[#111318] text-white shadow-[0_6px_16px_-4px_rgba(17,19,24,0.25)]'
                  : 'bg-white/60 backdrop-blur-md text-[#8A909E] hover:text-[#111318]'
              }`}
            >
              {tab.label}
            </button>
          );
        })}
      </div>
    </div>
  );
};
