import { http } from '../../lib/http';
import type { AdjustmentInput, Product, ProductInput, StockMovement } from './types';

export const inventoryApi = {
  products: () => http.get<Product[]>('/api/inventory/products'),
  product: (id: number) => http.get<Product>(`/api/inventory/products/${id}`),
  createProduct: (input: ProductInput) => http.post<Product>('/api/inventory/products', input),
  updateProduct: (id: number, input: ProductInput) => http.put<Product>(`/api/inventory/products/${id}`, input),
  movements: (productId?: number) => http.get<StockMovement[]>(productId === undefined ? '/api/inventory/movements' : `/api/inventory/movements?productId=${productId}`),
  adjust: (input: AdjustmentInput) => http.post<Product>('/api/inventory/adjustments', input),
};
