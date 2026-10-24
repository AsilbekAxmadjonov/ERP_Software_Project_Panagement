import { __ } from '../../lib/i18n';
import type { Tone } from '../../ui/Badge';
import { PaymentChannel, PaymentProgress } from './types';

export const STATUS: Record<PaymentProgress, { label: string; tone: Tone }> = {
  [PaymentProgress.UNPAID]: { label: __('Not paid'), tone: 'red' },
  [PaymentProgress.PARTIAL]: { label: __('Partially paid'), tone: 'yellow' },
  [PaymentProgress.PAID]: { label: __('Paid'), tone: 'green' },
  [PaymentProgress.CREDITED]: { label: __('Credited'), tone: 'gray' },
};

export const CHANNEL_LABEL: Record<PaymentChannel, string> = {
  [PaymentChannel.CASH]: __('Cash'),
  [PaymentChannel.BANK]: __('Bank'),
};
