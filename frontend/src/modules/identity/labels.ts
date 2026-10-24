import { __ } from '../../lib/i18n';
import { UserRole } from './types';

export const ROLE_LABEL: Record<UserRole, string> = {
  [UserRole.CEO]: __('CEO'),
  [UserRole.MANAGER]: __('Manager'),
  [UserRole.ACCOUNTANT]: __('Accountant'),
};
