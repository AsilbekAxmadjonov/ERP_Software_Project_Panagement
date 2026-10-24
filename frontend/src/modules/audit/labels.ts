import { __ } from '../../lib/i18n';
import type { Tone } from '../../ui/Badge';

/**
 * Display names for the action groups known today. The log is open-ended: a module added later shows up under its
 * own group name until it is listed here.
 */
const KNOWN_GROUPS: Record<string, { label: string; tone: Tone }> = {
  identity: { label: __('Users & access'), tone: 'purple' },
  partners: { label: __('Contacts'), tone: 'blue' },
  sales: { label: __('Sales'), tone: 'green' },
  purchasing: { label: __('Purchase'), tone: 'red' },
  inventory: { label: __('Inventory'), tone: 'yellow' },
  accounting: { label: __('Accounting'), tone: 'gray' },
};

export const actionGroup = (action: string): string => action.split('.')[0];

const capitalize = (text: string) => text.charAt(0).toUpperCase() + text.slice(1).replace(/[-_]/g, ' ');

export const groupLabel = (group: string): string => KNOWN_GROUPS[group]?.label ?? capitalize(group);

export const groupTone = (group: string): Tone => KNOWN_GROUPS[group]?.tone ?? 'gray';

/** "sales.invoice-issued" becomes "Invoice issued". */
export const actionLabel = (action: string): string => capitalize(action.slice(action.indexOf('.') + 1));
