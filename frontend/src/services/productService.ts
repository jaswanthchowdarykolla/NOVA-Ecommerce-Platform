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
  const q = query.toLowerCase().trim();
  if (!q) return [];
  const budget = q.match(/(?:under|below|less than|budget of|within)\s*₹?\s*([\d,]+)/i);
  const max = budget ? Number(budget[1].replace(/,/g, "")) : undefined;
  const stopWords = ["under","below","with","for","the","and","less","than","budget","search","show","find","recommend","products","product","items","item","things","thing","give","tell","want","need"];
  const terms = q.split(/[^a-z0-9]+/).filter(x => x.length > 2 && !stopWords.includes(x) && !/^\d+$/.test(x));

  return products
    .filter(p => {
      if (max !== undefined && p.price > max) return false;
      if (terms.length === 0) return true;
      const text = `${p.name} ${p.brand} ${p.category} ${p.description} ${(p.tags || []).join(" ")} ${p.color}`.toLowerCase();
      return terms.some(t => {
        if (text.includes(t)) return true;
        if (t.endsWith("s") && text.includes(t.slice(0, -1))) return true;
        return false;
      });
    })
    .sort((a,b) => Number(b.featured) - Number(a.featured) || b.rating - a.rating)
    .slice(0, limit);
}
