export const UnitOfMeasure = { KG: 'KG', PCS: 'PCS' } as const;
export type UnitOfMeasure = (typeof UnitOfMeasure)[keyof typeof UnitOfMeasure];

export const MovementKind = { ADJUSTMENT: 'ADJUSTMENT', SALE: 'SALE', PURCHASE: 'PURCHASE', SALES_RETURN: 'SALES_RETURN' } as const;
export type MovementKind = (typeof MovementKind)[keyof typeof MovementKind];

export interface Product {
  id: number;
  code: string;
  name: string;
  unit: UnitOfMeasure;
  salePrice: number;
  quantity: number;
  averageCost: number;
  stockValue: number;
  active: boolean;
  /** Minimum quantity before the product counts as low on stock; null when not tracked. */
  reorderLevel: number | null;
  lowStock: boolean;
}

export interface StockMovement {
  id: number;
  productId: number;
  productName: string;
  date: string;
  type: MovementKind;
  quantity: number;
  unitCost: number;
  reference: string | null;
  reason: string | null;
}

export interface ProductInput {
  code: string;
  name: string;
  unit: UnitOfMeasure;
  salePrice: number;
  active: boolean;
  reorderLevel: number | null;
}

export interface AdjustmentInput {
  productId: number;
  quantity: number;
  unitCost?: number;
  reason: string;
}
