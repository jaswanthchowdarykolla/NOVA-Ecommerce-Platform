import type { Product } from "../types";
import { createProduct, deleteProduct, getProducts, getProductById, updateProduct } from "../api/productApi";
import type { ProductWrite } from "../api/productApi";
export const productService = {
  list: getProducts,
  getById: getProductById,
  create: (input: ProductWrite) => createProduct(input),
  update: (id: number, input: ProductWrite) => updateProduct(id, input),
  delete: (id: number) => deleteProduct(id),
};
export function searchCatalogue(products: Product[], query: string, limit = 4): Product[] {
 const q = query.toLowerCase().trim(); if (!q) return [];
 const budget = q.match(/(?:under|below|less than|budget of)\s*₹?\s*([\d,]+)/i);
 const max = budget ? Number(budget[1].replace(/,/g, "")) : undefined;
 const terms = q.split(/[^a-z0-9]+/).filter(x => x.length > 2 && !["under","below","with","for","the","and","less","than","budget"].includes(x));
 return products.filter(p => (max === undefined || p.price <= max) && terms.some(t => `${p.name} ${p.brand} ${p.category} ${p.description} ${p.tags.join(" ")} ${p.color}`.toLowerCase().includes(t))).sort((a,b) => Number(b.featured)-Number(a.featured) || b.rating-a.rating).slice(0,limit);
}
