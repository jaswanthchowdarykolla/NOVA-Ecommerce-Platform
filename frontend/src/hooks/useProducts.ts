import { useCallback, useEffect, useState } from "react";
import type { Product } from "../types";
import { getProducts } from "../api/productApi";

export function useProducts() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [version, setVersion] = useState(0);
  const refresh = useCallback(() => setVersion(value => value + 1), []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    getProducts()
      .then(data => { if (active) { setProducts(data); setError(""); } })
      .catch(async () => {
        try {
          const local = await import("../seedProducts");
          if (active) { setProducts(local.products); setError("The API is unavailable; showing the local demo catalogue."); }
        } catch {
          if (active) setError("Unable to load the catalogue.");
        }
      })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [version]);

  return { products, loading, error, refresh };
}
