import { http } from '../../lib/http';
import type { AuditEntry, AuditFilter } from './types';

export const auditApi = {
  /** Newest first; the server caps the result at its latest 500 entries unless a limit is given. */
  entries: (f: AuditFilter) => {
    const query = new URLSearchParams(Object.entries(f).filter(([, value]) => value !== ''));
    return http.get<AuditEntry[]>(`/api/audit?${query.toString()}`);
  },
};
