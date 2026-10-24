import type { Product } from '../types';

export const CLOTHING_SIZE_CHART = ['XS', 'S', 'M', 'L', 'XL', 'XXL', 'XXXL'];
export const FOOTWEAR_SIZE_CHART = ['UK 5', 'UK 6', 'UK 7', 'UK 8', 'UK 9', 'UK 10', 'UK 11', 'UK 12'];

export function parseAvailableSizes(raw?: string | string[] | null): string[] {
  if (Array.isArray(raw)) return raw.map((s) => String(s).trim()).filter(Boolean);
  if (!raw) return [];
  return String(raw)
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean);
}

export function productRequiresSize(product: Product): boolean {
  return parseAvailableSizes(product.availableSizes).length > 0;
}

export function sizeChartFor(product: Product): string[] {
  const available = parseAvailableSizes(product.availableSizes);
  if (!available.length) return [];
  const base = product.category === 'Footwear' ? FOOTWEAR_SIZE_CHART : CLOTHING_SIZE_CHART;
  const extra = available.filter((size) => !base.includes(size));
  return [...base, ...extra];
}

export function isSizeAvailable(product: Product, size: string): boolean {
  return parseAvailableSizes(product.availableSizes).some((s) => s.toLowerCase() === size.trim().toLowerCase());
}

export function cartLineKey(productId: number, size?: string): string {
  return `${productId}::${size || ''}`;
}
