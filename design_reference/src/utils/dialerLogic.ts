import { Campaign, CampaignContact, CallStage, ContactCallStatus } from '../types/campaign';
import { CallOutcome } from '../types/dialer';
import { syncCampaignFromContacts } from './campaignLogic';

/**
 * Formats seconds into mm:ss (e.g. 00:42, 02:15)
 */
export function formatCallTimer(seconds: number): string {
  if (isNaN(seconds) || seconds < 0) return '00:00';
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  const mStr = mins < 10 ? `0${mins}` : `${mins}`;
  const sStr = secs < 10 ? `0${secs}` : `${secs}`;
  return `${mStr}:${sStr}`;
}

/**
 * Returns the list of upcoming queued contacts for an active campaign,
 * skipping completed contacts and the contact currently on the line.
 */
export function getQueuedContacts(
  contacts: CampaignContact[],
  currentContactId?: string
): CampaignContact[] {
  return contacts.filter(
    (c) => c.status === 'pending' && c.id !== currentContactId
  );
}

/**
 * Finds the next pending contact to call in the campaign.
 */
export function getNextPendingContact(
  contacts: CampaignContact[],
  excludeContactId?: string
): CampaignContact | null {
  const pending = contacts.filter(
    (c) => c.status === 'pending' && c.id !== excludeContactId
  );
  return pending.length > 0 ? pending[0] : null;
}

/**
 * Pure function to record an outcome for the current call and update the campaign contacts.
 */
export function applyCallOutcomeToCampaign(
  campaign: Campaign,
  contactId: string,
  outcome: CallOutcome,
  durationSeconds: number,
  stage?: CallStage,
  feedback?: string
): Campaign {
  const nowMs = Date.now();
  const updatedContacts = campaign.contacts.map((c) => {
    if (c.id !== contactId) return c;

    let newStatus: ContactCallStatus = 'pending';
    if (outcome === 'answered') newStatus = 'answered';
    else if (outcome === 'no_answer') newStatus = 'no_answer';
    else if (outcome === 'failed') newStatus = 'failed';
    else if (outcome === 'unverified') newStatus = 'unverified';
    else newStatus = 'pending'; // skipped stays pending

    return {
      ...c,
      status: newStatus,
      attempts: (c.attempts || 0) + 1,
      callDurationSeconds: outcome === 'answered' ? durationSeconds : 0,
      calledAt: nowMs,
      lastAttempt: new Date(nowMs).toISOString(),
      stage: stage ?? c.stage ?? 'none',
      notes: feedback !== undefined ? feedback : c.notes,
    };
  });

  return syncCampaignFromContacts({
    ...campaign,
    contacts: updatedContacts,
    lastActivity: new Date(nowMs).toISOString(),
  });
}

/**
 * Updates a contact's stage and feedback notes inside a campaign
 */
export function updateContactStageAndNotes(
  campaign: Campaign,
  contactId: string,
  stage: CallStage,
  notes: string
): Campaign {
  const updatedContacts = campaign.contacts.map((c) =>
    c.id === contactId ? { ...c, stage, notes } : c
  );
  return {
    ...campaign,
    contacts: updatedContacts,
    lastActivity: new Date().toISOString(),
  };
}

/**
 * Removes a contact from the campaign contact list
 */
export function removeContactFromCampaign(
  campaign: Campaign,
  contactId: string
): Campaign {
  const updatedContacts = campaign.contacts.filter((c) => c.id !== contactId);
  return syncCampaignFromContacts({
    ...campaign,
    contacts: updatedContacts,
    lastActivity: new Date().toISOString(),
  });
}
