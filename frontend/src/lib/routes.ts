const paths = {
  login: '/login',
  home: '/',
  'users.index': '/users',
  'partners.index': '/partners',
  'accounting.accounts': '/accounting/accounts',
  'accounting.journal': '/accounting/journal',
  'accounting.trial-balance': '/accounting/trial-balance',
  'accounting.ledger': '/accounting/ledger',
  'inventory.products': '/inventory/products',
  'sales.invoices': '/sales/invoices',
  'sales.payments': '/sales/payments',
  'sales.customers': '/sales/customers',
  'sales.report': '/sales/report',
} as const;

export type RouteName = keyof typeof paths;

export const route = (name: RouteName): string => paths[name];
