import { http } from '../../lib/http';
import type { Partner, PartnerInput } from './types';

export const partnersApi = {
  list: () => http.get<Partner[]>('/api/partners'),
  get: (id: number) => http.get<Partner>(`/api/partners/${id}`),
  create: (input: PartnerInput) => http.post<Partner>('/api/partners', input),
  update: (id: number, input: PartnerInput) => http.put<Partner>(`/api/partners/${id}`, input),
};
