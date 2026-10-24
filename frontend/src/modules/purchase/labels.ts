import { __ } from '../../lib/i18n';
import type { Tone } from '../../ui/Badge';
import { OrderStatus, PaymentChannel, PaymentProgress } from './types';

export const ORDER_STATUS: Record<OrderStatus, { label: string; tone: Tone }> = {
  [OrderStatus.DRAFT]: { label: __('RFQ'), tone: 'blue' },
  [OrderStatus.CONFIRMED]: { label: __('Purchase order'), tone: 'purple' },
  [OrderStatus.RECEIVED]: { label: __('Received'), tone: 'green' },
  [OrderStatus.CANCELLED]: { label: __('Cancelled'), tone: 'gray' },
};

export const PAYMENT_STATUS: Record<PaymentProgress, { label: string; tone: Tone }> = {
  [PaymentProgress.UNPAID]: { label: __('Not paid'), tone: 'red' },
  [PaymentProgress.PARTIAL]: { label: __('Partially paid'), tone: 'yellow' },
  [PaymentProgress.PAID]: { label: __('Paid'), tone: 'green' },
};

export const CHANNEL_LABEL: Record<PaymentChannel, string> = {
  [PaymentChannel.CASH]: __('Cash'),
  [PaymentChannel.BANK]: __('Bank'),
};
