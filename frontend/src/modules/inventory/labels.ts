import { __ } from '../../lib/i18n';
import type { Tone } from '../../ui/Badge';
import { MovementKind, UnitOfMeasure } from './types';

export const UNIT_LABEL: Record<UnitOfMeasure, string> = {
  [UnitOfMeasure.KG]: __('kg'),
  [UnitOfMeasure.PCS]: __('pcs'),
};

export const MOVEMENT: Record<MovementKind, { label: string; tone: Tone }> = {
  [MovementKind.ADJUSTMENT]: { label: __('Adjustment'), tone: 'yellow' },
  [MovementKind.SALE]: { label: __('Delivery'), tone: 'blue' },
  [MovementKind.PURCHASE]: { label: __('Receipt'), tone: 'green' },
  [MovementKind.SALES_RETURN]: { label: __('Return'), tone: 'purple' },
};
