import React from 'react';
import { Sliders, Volume2, Clock, RotateCw } from 'lucide-react';
import { SessionConfig } from '../../types/dialer';
import { Translations } from '../../i18n/translations';

interface SessionConfigStripProps {
  config: SessionConfig;
  delaySec: number;
  onOpenSettings: () => void;
  t: Translations;
}

export const SessionConfigStrip: React.FC<SessionConfigStripProps> = ({
  config,
  delaySec,
  onOpenSettings,
  t,
}) => {
  return (
    <div className="px-[20px] pt-1.5 pb-24">
      <div className="rounded-[24px] bg-white/60 backdrop-blur-md px-4 py-3 shadow-[0_10px_28px_-8px_rgba(17,19,24,0.05)] flex items-center justify-between text-xs text-[#8A909E]">
        <div className="flex items-center gap-2.5 flex-wrap">
          <div className="flex items-center gap-1" title="Delay between calls">
            <Clock className="w-3.5 h-3.5 text-[#8A909E]" strokeWidth={1.75} />
            <span className="font-medium text-[#111318]">{delaySec}s delay</span>
          </div>
          <span aria-hidden="true">·</span>
          <div className="flex items-center gap-1" title="Retry attempts on failed call">
            <RotateCw className="w-3.5 h-3.5 text-[#8A909E]" strokeWidth={1.75} />
            <span>{t.retryCount}</span>
          </div>
          <span aria-hidden="true">·</span>
          <div className="flex items-center gap-1" title="Audio routing">
            <Volume2 className="w-3.5 h-3.5 text-[#8A909E]" strokeWidth={1.75} />
            <span className="capitalize">{config.audioRoute}</span>
          </div>
        </div>

        <button
          type="button"
          onClick={onOpenSettings}
          className="min-h-[36px] px-3 rounded-full bg-[#111318]/[0.05] hover:bg-[#111318]/[0.1] text-[11px] font-semibold text-[#111318] flex items-center gap-1.5 shrink-0 transition-colors"
          aria-label="Configure session preferences"
        >
          <Sliders className="w-3 h-3" strokeWidth={1.75} />
          <span>Settings</span>
        </button>
      </div>
    </div>
  );
};
