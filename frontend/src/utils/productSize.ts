import type { Product } from '../types';

export const CLOTHING_SIZE_CHART = ['XS', 'S', 'M', 'L', 'XL', 'XXL', 'XXXL'];
export const FOOTWEAR_SIZE_CHART = ['UK 5', 'UK 6', 'UK 7', 'UK 8', 'UK 9', 'UK 10', 'UK 11', 'UK 12'];

export function isFootwearProduct(product?: Partial<Product> | null): boolean {
  if (!product) return false;
  const cat = String(product.category || '').toLowerCase();
  const name = String(product.name || '').toLowerCase();
  return (
    cat === 'footwear' ||
    cat === 'shoes' ||
    name.includes('shoe') ||
    name.includes('sneaker') ||
    name.includes('runner') ||
    name.includes('boot') ||
    name.includes('loafer') ||
    name.includes('slide')
  );
}

export function parseAvailableSizes(raw?: string | string[] | null): string[] {
  if (Array.isArray(raw)) return raw.map((s) => String(s).trim()).filter(Boolean);
  if (!raw) return [];
  return String(raw)
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean);
}

export function productRequiresSize(product: Product): boolean {
  const explicit = parseAvailableSizes(product.availableSizes);
  if (explicit.length > 0) return true;
  return isFootwearProduct(product);
}

export function sizeChartFor(product: Product): string[] {
  const available = parseAvailableSizes(product.availableSizes);
  const isFootwear = isFootwearProduct(product);
  const base = isFootwear ? FOOTWEAR_SIZE_CHART : CLOTHING_SIZE_CHART;

  if (!available.length) {
    return isFootwear ? [...FOOTWEAR_SIZE_CHART] : [];
  }

  const extra = available.filter((size) => !base.includes(size));
  return [...base, ...extra];
}

export function isSizeAvailable(product: Product, size: string): boolean {
  const available = parseAvailableSizes(product.availableSizes);
  if (!available.length) {
    if (isFootwearProduct(product)) {
      return FOOTWEAR_SIZE_CHART.some((s) => s.toLowerCase() === size.trim().toLowerCase());
    }
    return false;
  }
  return available.some((s) => s.toLowerCase() === size.trim().toLowerCase());
}

export function cartLineKey(productId: number, size?: string): string {
  return `${productId}::${size || ''}`;
}
