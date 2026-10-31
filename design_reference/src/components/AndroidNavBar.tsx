import React from 'react';

interface AndroidNavBarProps {
  isHidden?: boolean;
}

export const AndroidNavBar: React.FC<AndroidNavBarProps> = ({
  isHidden = false,
}) => {
  return (
    <div
      className={`absolute bottom-0 left-0 right-0 z-30 w-full py-1.5 flex items-center justify-center select-none bg-transparent pointer-events-none will-change-[transform,opacity] transition-[transform,opacity] duration-300 ease-out ${
        isHidden
          ? 'opacity-0 translate-y-2'
          : 'opacity-100 translate-y-0'
      }`}
      aria-hidden={isHidden}
    >
      <div className="w-28 h-1 bg-[#111318]/15 rounded-full" />
    </div>
  );
};
