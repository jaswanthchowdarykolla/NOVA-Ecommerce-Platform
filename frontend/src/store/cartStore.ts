import { create } from "zustand";
import type { Product } from "../types";
import { cartLineKey } from "../utils/productSize";

export type CartItem = { product: Product; qty: number; size?: string };
type CartState = {
  items: CartItem[];
  hydrate: () => void;
  add: (p: Product, size?: string) => void;
  remove: (id: number, size?: string) => void;
  setQty: (id: number, qty: number, size?: string) => void;
  clear: () => void;
  replace: (items: CartItem[]) => void;
};

const persist = (items: CartItem[]) => localStorage.setItem("nova-cart", JSON.stringify(items));
const sameLine = (item: CartItem, id: number, size?: string) =>
  cartLineKey(item.product.id, item.size) === cartLineKey(id, size);

export const useCartStore = create<CartState>((set, get) => ({
  items: [],
  hydrate: () => {
    try {
      const raw = JSON.parse(localStorage.getItem("nova-cart") || "[]") as CartItem[];
      set({ items: Array.isArray(raw) ? raw : [] });
    } catch {
      set({ items: [] });
    }
  },
  add: (product, size) => {
    const items = get().items;
    const found = items.find((x) => sameLine(x, product.id, size));
    const next = found
      ? items.map((x) => (sameLine(x, product.id, size) ? { ...x, qty: x.qty + 1 } : x))
      : [...items, { product, qty: 1, size }];
    persist(next);
    set({ items: next });
  },
  remove: (id, size) => {
    const next = get().items.filter((x) => !sameLine(x, id, size));
    persist(next);
    set({ items: next });
  },
  setQty: (id, qty, size) => {
    const next =
      qty <= 0
        ? get().items.filter((x) => !sameLine(x, id, size))
        : get().items.map((x) => (sameLine(x, id, size) ? { ...x, qty } : x));
    persist(next);
    set({ items: next });
  },
  clear: () => {
    persist([]);
    set({ items: [] });
  },
  replace: (items) => {
    persist(items);
    set({ items });
  }
}));
