import { http } from '../../lib/http';
import type { AdjustmentInput, Product, ProductInput, StockMovement } from './types';

export const inventoryApi = {
  products: () => http.get<Product[]>('/api/inventory/products'),
  createProduct: (input: ProductInput) => http.post<Product>('/api/inventory/products', input),
  movements: () => http.get<StockMovement[]>('/api/inventory/movements'),
  adjust: (input: AdjustmentInput) => http.post<Product>('/api/inventory/adjustments', input),
};
