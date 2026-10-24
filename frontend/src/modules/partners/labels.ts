import { __ } from '../../lib/i18n';
import type { Tone } from '../../ui/Badge';
import { PartnerKind } from './types';

export const KIND: Record<PartnerKind, { label: string; tone: Tone }> = {
  [PartnerKind.CUSTOMER]: { label: __('Customer'), tone: 'green' },
  [PartnerKind.SUPPLIER]: { label: __('Vendor'), tone: 'purple' },
  [PartnerKind.BOTH]: { label: __('Customer & vendor'), tone: 'blue' },
};
