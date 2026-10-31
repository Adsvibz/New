import React from 'react';
import { X, Sliders, Volume2, Clock, RotateCw, Shuffle } from 'lucide-react';
import { SessionConfig } from '../../types/dialer';
import { Translations } from '../../i18n/translations';

interface DialerSettingsModalProps {
  isOpen: boolean;
  config: SessionConfig;
  onClose: () => void;
  onUpdateConfig: (newConfig: SessionConfig) => void;
  t: Translations;
}

export const DialerSettingsModal: React.FC<DialerSettingsModalProps> = ({
  isOpen,
  config,
  onClose,
  onUpdateConfig,
  t,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/40 backdrop-blur-xs">
      <div
        className="w-full max-w-sm bg-white rounded-t-2xl sm:rounded-2xl border border-[#E9EBEF] shadow-2xl p-5"
        role="dialog"
        aria-modal="true"
        aria-labelledby="dialer-settings-title"
      >
        <div className="flex items-center justify-between pb-3 border-b border-[#F0F2F5]">
          <div className="flex items-center gap-2">
            <Sliders className="w-4 h-4 text-[#111827]" />
            <h3 id="dialer-settings-title" className="text-[15px] font-semibold text-[#111827]">
              Session Configuration
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 text-[#8E95A2] hover:text-[#111827] rounded-md transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="py-4 space-y-4">
          {/* Audio Output */}
          <div>
            <label className="block text-[12px] font-medium text-[#111827] mb-1.5">
              Audio Routing
            </label>
            <div className="grid grid-cols-3 gap-1.5">
              {(['speaker', 'earpiece', 'headset'] as const).map((route) => (
                <button
                  key={route}
                  type="button"
                  onClick={() => onUpdateConfig({ ...config, audioRoute: route })}
                  className={`py-2 text-[12px] font-medium rounded-xl capitalize transition-all border ${
                    config.audioRoute === route
                      ? 'bg-[#111827] text-white border-[#111827]'
                      : 'bg-[#FAFAFA] text-[#6B7280] border-[#EBECEF] hover:text-[#111827]'
                  }`}
                >
                  {route}
                </button>
              ))}
            </div>
          </div>

          {/* Retry Attempts */}
          <div>
            <label className="block text-[12px] font-medium text-[#111827] mb-1.5">
              Auto-Retry on No-Answer
            </label>
            <div className="grid grid-cols-4 gap-1.5">
              {[0, 1, 2, 3].map((num) => (
                <button
                  key={num}
                  type="button"
                  onClick={() => onUpdateConfig({ ...config, retryAttempts: num })}
                  className={`py-1.5 text-[12px] font-medium rounded-xl transition-all border ${
                    config.retryAttempts === num
                      ? 'bg-[#111827] text-white border-[#111827]'
                      : 'bg-[#FAFAFA] text-[#6B7280] border-[#EBECEF] hover:text-[#111827]'
                  }`}
                >
                  {num === 0 ? 'None' : `${num}x`}
                </button>
              ))}
            </div>
          </div>

          {/* Random delay jitter */}
          <div className="pt-2 flex items-center justify-between text-[12.5px]">
            <div>
              <span className="font-medium text-[#111827] block">Randomize Pacing</span>
              <span className="text-[11px] text-[#8E95A2]">Adds ±1s jitter between calls</span>
            </div>
            <input
              type="checkbox"
              checked={config.randomizeDelay}
              onChange={(e) => onUpdateConfig({ ...config, randomizeDelay: e.target.checked })}
              className="w-4 h-4 accent-[#111827] rounded cursor-pointer"
            />
          </div>
        </div>

        <div className="pt-3 border-t border-[#F0F2F5] flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-1.5 text-[12.5px] font-medium bg-[#111827] text-white rounded-lg hover:bg-neutral-800 transition-colors"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
};
