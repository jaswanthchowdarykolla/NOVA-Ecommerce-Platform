import type { Product } from '../types';

export type CartItem = { product: Product; qty: number; size?: string };

export const categories = [
  'All',
  'Electronics',
  'Fashion',
  'Footwear',
  'Home & Living',
  'Beauty',
  'Sports & Lifestyle',
  'Books & Stationery',
  'Novels',
  'Textbooks',
  'Accessories'
];

export const CATEGORY_SLUG_MAP: Record<string, string> = {
  'all': 'All',
  'electronics': 'Electronics',
  'technology': 'Electronics',
  'tech': 'Electronics',
  'fashion': 'Fashion',
  'apparel': 'Fashion',
  'footwear': 'Footwear',
  'shoes': 'Footwear',
  'home-and-living': 'Home & Living',
  'home-living': 'Home & Living',
  'home & living': 'Home & Living',
  'home and living': 'Home & Living',
  'home': 'Home & Living',
  'living': 'Home & Living',
  'beauty': 'Beauty',
  'sports-and-lifestyle': 'Sports & Lifestyle',
  'sports-lifestyle': 'Sports & Lifestyle',
  'sports & lifestyle': 'Sports & Lifestyle',
  'sports and lifestyle': 'Sports & Lifestyle',
  'sports': 'Sports & Lifestyle',
  'lifestyle': 'Sports & Lifestyle',
  'books-and-stationery': 'Books & Stationery',
  'books-stationery': 'Books & Stationery',
  'books & stationery': 'Books & Stationery',
  'books and stationery': 'Books & Stationery',
  'books': 'Books & Stationery',
  'stationery': 'Books & Stationery',
  'novels': 'Novels',
  'novel': 'Novels',
  'textbooks': 'Textbooks',
  'textbook': 'Textbooks',
  'accessories': 'Accessories',
};

export function categoryToSlug(cat: string): string {
  if (!cat || cat === 'All') return 'all';
  return cat.toLowerCase().replace(/&/g, 'and').replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');
}

export function normalizeCategorySlug(raw?: string | null): string {
  if (!raw) return 'All';
  const decoded = decodeURIComponent(raw).trim().toLowerCase();
  if (CATEGORY_SLUG_MAP[decoded]) return CATEGORY_SLUG_MAP[decoded];
  const clean = decoded.replace(/&/g, 'and').replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');
  if (CATEGORY_SLUG_MAP[clean]) return CATEGORY_SLUG_MAP[clean];
  const found = categories.find(c =>
    c.toLowerCase() === decoded ||
    categoryToSlug(c) === clean ||
    c.toLowerCase().replace(/[^a-z0-9]/g, '') === decoded.replace(/[^a-z0-9]/g, '')
  );
  return found || 'All';
}
