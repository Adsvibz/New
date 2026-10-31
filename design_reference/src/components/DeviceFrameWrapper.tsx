import React from 'react';

interface DeviceFrameWrapperProps {
  isDeviceFrame: boolean;
  children: React.ReactNode;
}

export const DeviceFrameWrapper: React.FC<DeviceFrameWrapperProps> = ({
  isDeviceFrame,
  children,
}) => {
  return (
    <main className="w-full min-h-dvh bg-[var(--app-canvas-outer)] flex items-center justify-center sm:py-6 sm:px-4 overflow-x-hidden transition-colors duration-300">
      {/* Mobile-First Viewport Frame (320px to 430px, or expanded container) */}
      <div
        className={`relative w-full ${
          isDeviceFrame
            ? 'max-w-[430px] sm:rounded-[44px] sm:h-[860px]'
            : 'max-w-3xl sm:rounded-[36px] sm:h-[880px]'
        } app-device-canvas overflow-hidden sm:shadow-[0_24px_60px_-14px_rgba(17,19,24,0.12)] sm:ring-1 sm:ring-black/[0.04] flex flex-col min-h-dvh sm:min-h-0 h-dvh select-none transition-all duration-200`}
      >
        {/* Subtle camera punch-hole (only visible on desktop bezel preview) */}
        {isDeviceFrame && (
          <div className="hidden sm:block absolute top-2.5 left-1/2 -translate-x-1/2 w-3.5 h-3.5 bg-[#111318] rounded-full z-40 pointer-events-none" />
        )}

        {/* Screen container */}
        <div className="relative z-10 flex-1 flex flex-col overflow-hidden">
          {children}
        </div>
      </div>
    </main>
  );
};
