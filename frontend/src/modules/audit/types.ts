/** Payload values are scalars, nulls, lists or nested objects, exactly as the publishing module sent them (secrets removed). */
export type AuditValue = string | number | boolean | null | AuditValue[] | { [key: string]: AuditValue };

export interface AuditEntry {
  id: number;
  /** ISO-8601 instant. */
  occurredAt: string;
  actor: string | null;
  /** Stable event name such as "sales.invoice-issued"; the part before the dot is the action group. */
  action: string;
  summary: string;
  subject: string | null;
  payload: Record<string, AuditValue>;
}

export interface AuditFilter {
  /** yyyy-mm-dd, inclusive; empty for no bound. */
  from: string;
  to: string;
}
