import React, { useState } from 'react';
import { CampaignContact } from '../../types/campaign';
import { Translations } from '../../i18n/translations';
import { Trash2 } from 'lucide-react';

interface CallingQueueProps {
  queuedContacts: CampaignContact[];
  onRemoveContact: (contact: CampaignContact) => void;
  t: Translations;
}

export const CallingQueue: React.FC<CallingQueueProps> = ({
  queuedContacts,
  onRemoveContact,
  t,
}) => {
  const [confirmingContact, setConfirmingContact] = useState<CampaignContact | null>(null);

  return (
    <section className="px-[20px] py-1.5 flex-1" aria-label="Calling Queue">
      <div className="rounded-[24px] bg-white/60 backdrop-blur-md p-5 shadow-[0_12px_36px_-8px_rgba(17,19,24,0.06)]">
        {/* Queue Header: UP NEXT — X remaining (Part 7.9) */}
        <div className="flex items-center justify-between pb-2.5 mb-1 border-b border-[#111318]/[0.06]">
          <div className="flex items-center gap-2">
            <h3 className="text-xs font-bold text-[#111318] uppercase tracking-wider">
              {t.upNext}
            </h3>
            <span className="text-xs text-[#8A909E] font-medium tabular-nums">
              — {t.upNextRemaining(queuedContacts.length)}
            </span>
          </div>
        </div>

        {/* Display the next 3 eligible contacts by default (Part 7.9) */}
        {queuedContacts.length === 0 ? (
          <div className="py-6 text-center text-[#8A909E] text-xs">
            Queue is clear. No remaining pending contacts.
          </div>
        ) : (
          <div className="divide-y divide-[#111318]/[0.05]">
            {queuedContacts.slice(0, 3).map((contact, index) => {
              const numPrefix = index < 9 ? `0${index + 1}` : `${index + 1}`;
              return (
                <div
                  key={contact.id}
                  className="py-2.5 flex items-center justify-between gap-3 group transition-colors"
                >
                  <div className="flex items-center gap-2.5 min-w-0 flex-1">
                    <span className="w-6 h-6 rounded-full bg-[#111318]/[0.05] text-[11px] font-mono font-bold text-[#8A909E] flex items-center justify-center shrink-0 tabular-nums">
                      {numPrefix}
                    </span>
                    <div className="min-w-0 flex-1">
                      <span className="block text-[13.5px] font-bold text-[#111318] truncate">
                        {contact.name}
                      </span>
                      <span
                        className="block font-mono text-[11.5px] text-[#8A909E] tabular-nums truncate"
                        dir="ltr"
                      >
                        {contact.phone}
                      </span>
                    </div>
                  </div>

                  <button
                    type="button"
                    onClick={() => setConfirmingContact(contact)}
                    className="min-w-[38px] min-h-[38px] w-9 h-9 rounded-full text-[#8A909E] hover:text-[#E2532B] hover:bg-[#E2532B]/10 flex items-center justify-center transition-colors shrink-0"
                    title={t.removeFromQueue}
                    aria-label={`Remove ${contact.name} from queue`}
                  >
                    <Trash2 className="w-3.5 h-3.5" strokeWidth={1.75} />
                  </button>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Confirmation for removing from queue (Part 7.9) */}
      {confirmingContact && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-[#111318]/40 backdrop-blur-xs">
          <div
            className="w-full max-w-sm bg-white/95 backdrop-blur-2xl rounded-[24px] shadow-[0_24px_60px_-12px_rgba(17,19,24,0.25)] p-5"
            role="alertdialog"
            aria-modal="true"
          >
            <h4 className="text-[15px] font-bold text-[#111318]">
              {t.removeFromQueue}
            </h4>
            <p className="text-xs text-[#6B7280] mt-1.5 leading-relaxed">
              Remove <strong>{confirmingContact.name}</strong> ({confirmingContact.phone}) from this campaign's calling queue?
            </p>
            <div className="flex items-center justify-end gap-2 mt-4 pt-3 border-t border-[#111318]/[0.06]">
              <button
                type="button"
                onClick={() => setConfirmingContact(null)}
                className="min-h-[40px] px-4 text-xs font-semibold text-[#111318] bg-[#111318]/[0.06] rounded-full"
              >
                {t.actionCancel}
              </button>
              <button
                type="button"
                onClick={() => {
                  onRemoveContact(confirmingContact);
                  setConfirmingContact(null);
                }}
                className="min-h-[40px] px-4 text-xs font-semibold bg-[#111318] text-white rounded-full"
              >
                Remove
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};
