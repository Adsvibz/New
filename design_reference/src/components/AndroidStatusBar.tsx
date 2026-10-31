import React from 'react';
import { Wifi, Signal } from 'lucide-react';

interface AndroidStatusBarProps {
  isRtl?: boolean;
  isHidden?: boolean;
}

export const AndroidStatusBar: React.FC<AndroidStatusBarProps> = ({
  isHidden = false,
}) => {
  return (
    <div
      className={`absolute top-0 left-0 right-0 z-30 w-full px-[20px] pt-3 pb-1.5 flex items-center justify-between text-[#111318] text-[12px] font-semibold tracking-tight select-none bg-transparent pointer-events-none will-change-[transform,opacity] transition-[transform,opacity] duration-300 ease-out ${
        isHidden
          ? 'opacity-0 -translate-y-2'
          : 'opacity-100 translate-y-0'
      }`}
      dir="ltr"
      aria-label="Android System Status Bar"
      aria-hidden={isHidden}
    >
      <div className="font-bold text-[#111318] tabular-nums">
        09:41
      </div>

      <div className="flex items-center gap-2 text-[#111318]/80">
        <Signal className="w-3.5 h-3.5 stroke-[1.8]" />
        <Wifi className="w-3.5 h-3.5 stroke-[1.8]" />
        <div className="w-5 h-2.5 rounded-[3px] border border-[#111318] p-[1.5px] flex items-center ml-0.5">
          <div className="w-3.5 h-full bg-[#111318] rounded-[1.5px]" />
        </div>
      </div>
    </div>
  );
};
