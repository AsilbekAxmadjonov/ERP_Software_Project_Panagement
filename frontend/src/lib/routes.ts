const paths = {
  login: '/login',
  home: '/',
  'users.index': '/settings/users',
  'audit.index': '/settings/audit',
  'partners.index': '/contacts',
  'partners.new': '/contacts/new',
  'partners.show': '/contacts/:id',
  'accounting.dashboard': '/accounting',
  'accounting.accounts': '/accounting/accounts',
  'accounting.journal': '/accounting/journal',
  'accounting.journal.new': '/accounting/journal/new',
  'accounting.journal.show': '/accounting/journal/:id',
  'accounting.trial-balance': '/accounting/trial-balance',
  'accounting.ledger': '/accounting/ledger',
  'accounting.partner-ledger': '/accounting/partner-ledger',
  'accounting.lock-dates': '/accounting/lock-dates',
  'inventory.products': '/inventory/products',
  'inventory.products.new': '/inventory/products/new',
  'inventory.products.show': '/inventory/products/:id',
  'inventory.moves': '/inventory/moves',
  'sales.invoices': '/sales/invoices',
  'sales.invoices.new': '/sales/invoices/new',
  'sales.invoices.show': '/sales/invoices/:id',
  'sales.credit-notes': '/sales/credit-notes',
  'sales.payments': '/sales/payments',
  'sales.customers': '/sales/customers',
  'sales.report': '/sales/report',
  'purchase.orders': '/purchase/orders',
  'purchase.orders.new': '/purchase/orders/new',
  'purchase.orders.show': '/purchase/orders/:id',
  'purchase.payments': '/purchase/payments',
  'purchase.vendors': '/purchase/vendors',
} as const;

export type RouteName = keyof typeof paths;

/** The raw path pattern (with `:param` placeholders), for route definitions. */
export const routePattern = (name: RouteName): string => paths[name];

export const route = (name: RouteName, params: Record<string, string | number> = {}): string =>
  paths[name].replace(/:(\w+)/g, (_, key: string) => encodeURIComponent(String(params[key])));
