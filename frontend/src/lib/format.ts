const MONEY = new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const QUANTITY = new Intl.NumberFormat('en-US', { maximumFractionDigits: 3 });

export const money = (value: number): string => MONEY.format(value);

export const quantity = (value: number): string => QUANTITY.format(value);

export const today = (): string => new Date().toISOString().slice(0, 10);
