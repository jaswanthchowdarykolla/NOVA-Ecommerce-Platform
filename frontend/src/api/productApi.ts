import type { Product } from "../types";
import { apiRequest } from "./httpClient";

const BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
export type ProductWrite = {
  name: string; slug?: string; category: string; brand: string; price: number;
  rating: number; reviewCount: number; description: string; imageUrl: string;
  color: string; featured: boolean; tags: string; availableSizes?: string;
};
function normalize(p: any): Product {
  const tags = Array.isArray(p.tags) ? p.tags : String(p.tags || "").split(",").filter(Boolean);
  const availableSizes = Array.isArray(p.availableSizes)
    ? p.availableSizes
    : String(p.availableSizes || "").split(",").map((s: string) => s.trim()).filter(Boolean);
  return { ...p, id: Number(p.id), tags, availableSizes } as Product;
}
export async function getProducts(): Promise<Product[]> {
  const pageSize = 1000;
  const first = await fetch(`${BASE}/products?page=0&size=${pageSize}`);
  if (!first.ok) throw new Error(`Product request failed (${first.status})`);
  const firstBody = await first.json();
  if (Array.isArray(firstBody)) return firstBody.map(normalize);
  const rows: any[] = [...(firstBody.content || [])];
  const totalPages = Number(firstBody.totalPages || 1);
  for (let page = 1; page < totalPages; page++) {
    const response = await fetch(`${BASE}/products?page=${page}&size=${pageSize}`);
    if (!response.ok) throw new Error(`Product request failed (${response.status})`);
    const body = await response.json();
    rows.push(...(body.content || []));
  }
  return rows.map(normalize);
}
export async function getProductById(id: number): Promise<Product> {
  const res = await fetch(`${BASE}/products/${id}`);
  if (!res.ok) throw new Error("Product not found");
  return normalize(await res.json());
}
export async function createProduct(input: ProductWrite): Promise<Product> {
  return normalize(await apiRequest<any>("/products", { method: "POST", body: JSON.stringify(input) }));
}
export async function updateProduct(id: number, input: ProductWrite): Promise<Product> {
  return normalize(await apiRequest<any>(`/products/${id}`, { method: "PUT", body: JSON.stringify(input) }));
}
export async function deleteProduct(id: number): Promise<void> {
  await apiRequest<void>(`/products/${id}`, { method: "DELETE" });
}
