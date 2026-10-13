export const PartnerKind = { CUSTOMER: 'CUSTOMER', SUPPLIER: 'SUPPLIER', BOTH: 'BOTH' } as const;
export type PartnerKind = (typeof PartnerKind)[keyof typeof PartnerKind];

export interface Partner {
  id: number;
  name: string;
  type: PartnerKind;
  contactPerson: string | null;
  phone: string | null;
  email: string | null;
  address: string | null;
  taxId: string | null;
  active: boolean;
  customer: boolean;
  supplier: boolean;
}

export interface PartnerInput {
  name: string;
  type: PartnerKind;
  contactPerson: string;
  phone: string;
  email: string;
  address: string;
  taxId: string;
  active: boolean;
}
