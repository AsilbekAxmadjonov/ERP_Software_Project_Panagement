const MONEY = new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const QUANTITY = new Intl.NumberFormat('en-US', { maximumFractionDigits: 3 });
const DATE = new Intl.DateTimeFormat('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });

export const money = (value: number): string => MONEY.format(value);

export const quantity = (value: number): string => QUANTITY.format(value);

/** Today's date in the user's time zone; toISOString() would give the UTC date, which is wrong around midnight. */
export const today = (): string => {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
};

/** Formats an ISO date (yyyy-mm-dd) for display without shifting it through the local time zone. */
export const date = (iso: string): string => {
  const [y, m, d] = iso.split('-').map(Number);
  return DATE.format(new Date(y, m - 1, d));
};

export const initials = (name: string): string =>
  name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0].toUpperCase()).join('');
