import React from 'react';
import { X, FileCode2 } from 'lucide-react';

interface HandoffSpecModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const HandoffSpecModal: React.FC<HandoffSpecModalProps> = ({
  isOpen,
  onClose,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs">
      <div
        className="w-full max-w-lg bg-white rounded-2xl border border-[#E5E7EB] shadow-2xl overflow-hidden max-h-[88vh] flex flex-col"
        role="dialog"
        aria-modal="true"
        aria-labelledby="handoff-spec-title"
      >
        <div className="px-5 py-4 border-b border-[#F0F2F5] flex items-center justify-between">
          <div className="flex items-center gap-2">
            <FileCode2 className="w-4 h-4 text-[#111827]" />
            <h2 id="handoff-spec-title" className="text-[15px] font-semibold text-[#111827]">
              O2Dialer 7.0 — Native Android Handoff Specification
            </h2>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 flex items-center justify-center text-[#8E95A2] hover:text-[#111827] rounded-lg"
            aria-label="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="p-5 overflow-y-auto space-y-4 text-[12px] text-[#4B5563]">
          <div className="p-3 bg-[#F7F8FA] border border-[#E5E7EB] rounded-xl">
            <strong className="text-[#111827] block mb-1">
              1. Primary Navigation (5 Destinations)
            </strong>
            <span>1. Dialer · 2. Results · 3. Campaigns · 4. History · 5. Settings</span>
          </div>

          <div className="p-3 bg-[#F7F8FA] border border-[#E5E7EB] rounded-xl space-y-1">
            <strong className="text-[#111827] block">2. Design Tokens (Compose ColorScheme)</strong>
            <div className="grid grid-cols-2 gap-1 font-mono text-[11px]">
              <span>Surface: #FFFFFF</span>
              <span>Primary Ink: #111827</span>
              <span>Secondary Text: #8E95A2</span>
              <span>Quiet Surface: #F4F5F7</span>
              <span>Hairline Border: #E5E7EB</span>
              <span>Primary CTA: #17191D</span>
              <span>Accent Blue: #2563EB</span>
              <span>Success: #047857</span>
              <span>Warning: #B45309</span>
              <span>Destructive: #B42318</span>
            </div>
          </div>

          <div className="p-3 bg-[#F7F8FA] border border-[#E5E7EB] rounded-xl space-y-1">
            <strong className="text-[#111827] block">
              3. Typography & Spacing Scale
            </strong>
            <p>
              English: <strong>Outfit</strong> · Arabic RTL: <strong>IBM Plex Sans Arabic</strong> · Numbers/Phones: <strong>JetBrains Mono (tabular-nums, dir="ltr")</strong>
            </p>
            <p className="font-mono text-[11px]">
              Spacing: 4dp / 8dp / 12dp / 16dp / 20dp / 24dp / 32dp · Radius: 8dp–16dp
            </p>
          </div>

          <div className="p-3 bg-[#F7F8FA] border border-[#E5E7EB] rounded-xl space-y-1">
            <strong className="text-[#111827] block">
              4. Real Data Schema & Android Telephony Notes
            </strong>
            <p>
              Campaigns store <code>id</code>, <code>name</code>, <code>simSlot (-1, 0, 1)</code>, and <code>entries: ContactEntry[]</code>. All progress metrics are derived dynamically from <code>ContactEntry.status</code> (<code>pending | calling | answered | no_answer | failed | unverified</code>) and <code>ContactEntry.stage</code> (<code>none | potential | not_interested | call_back | whatsapp | voicemail</code>).
            </p>
            <p className="text-[11px] text-[#6B7280] mt-1">
              Full specification is saved in <code>/HANDOFF_SPECIFICATION.md</code> in the project root.
            </p>
          </div>
        </div>

        <div className="px-5 py-3 border-t border-[#F0F2F5] flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-1.5 rounded-xl bg-[#17191D] text-white text-[12.5px] font-medium"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
};
