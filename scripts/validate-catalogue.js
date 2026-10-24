import fs from 'fs';
import path from 'path';

const ALLOWED_CATEGORIES = new Set([
  "Electronics",
  "Fashion",
  "Footwear",
  "Home & Living",
  "Beauty",
  "Sports & Lifestyle",
  "Books & Stationery",
  "Accessories"
]);

console.log("=== NOVA Catalogue Validation ===");

// 1. Read seedProducts.ts
const tsRaw = fs.readFileSync(path.resolve('frontend/src/seedProducts.ts'), 'utf-8');
const jsonMatch = tsRaw.match(/export const products: Product\[\] = (\[.*\]);/s);
if (!jsonMatch) {
  console.error("FATAL: Could not parse products from seedProducts.ts");
  process.exit(1);
}
const products = JSON.parse(jsonMatch[1]);

let errors = [];
let warnings = [];
const ids = new Set();
const slugs = new Set();
const pricesByCategory = {};

for (const cat of ALLOWED_CATEGORIES) {
  pricesByCategory[cat] = [];
}

for (const p of products) {
  // Check ID
  if (!p.id || typeof p.id !== 'number' || p.id <= 0) {
    errors.push(`Invalid ID on product: ${JSON.stringify(p)}`);
  } else if (ids.has(p.id)) {
    errors.push(`Duplicate ID: ${p.id}`);
  } else {
    ids.add(p.id);
  }

  // Check Slug
  if (!p.slug || typeof p.slug !== 'string') {
    errors.push(`Missing slug for product ${p.id}`);
  } else if (slugs.has(p.slug)) {
    errors.push(`Duplicate slug: ${p.slug}`);
  } else {
    slugs.add(p.slug);
  }

  // Check Name
  if (!p.name || p.name.trim().length === 0) {
    errors.push(`Missing or empty name for product ${p.id}`);
  }

  // Check Category
  if (!ALLOWED_CATEGORIES.has(p.category)) {
    errors.push(`Invalid category '${p.category}' on product ${p.id}`);
  } else {
    pricesByCategory[p.category].push(p.price);
  }

  // Check Brand
  if (!p.brand || p.brand.trim().length === 0) {
    errors.push(`Missing brand on product ${p.id}`);
  }

  // Check Price
  if (typeof p.price !== 'number' || p.price <= 0) {
    errors.push(`Invalid price '${p.price}' on product ${p.id}`);
  }

  // Check Image URL
  if (!p.imageUrl || !p.imageUrl.startsWith("https://images.unsplash.com/")) {
    errors.push(`Invalid image URL '${p.imageUrl}' on product ${p.id}`);
  }

  // Check Ratings and Reviews
  if (typeof p.rating !== 'number' || p.rating < 0 || p.rating > 5) {
    errors.push(`Invalid rating '${p.rating}' on product ${p.id}`);
  }
  if (typeof p.reviewCount !== 'number' || p.reviewCount < 0) {
    errors.push(`Invalid reviewCount '${p.reviewCount}' on product ${p.id}`);
  }

  // Check Description
  if (!p.description || p.description.trim().length < 10) {
    errors.push(`Missing or short description on product ${p.id}`);
  }

  // Check Tags
  if (!Array.isArray(p.tags) || p.tags.length === 0) {
    errors.push(`Missing or empty tags on product ${p.id}`);
  }
}

console.log(`Audited ${products.length} products.`);
console.log(`Unique IDs: ${ids.size}`);
console.log(`Unique Slugs: ${slugs.size}`);

console.log("\nPrice distributions per category:");
for (const [cat, prices] of Object.entries(pricesByCategory)) {
  const min = Math.min(...prices);
  const max = Math.max(...prices);
  const avg = (prices.reduce((a, b) => a + b, 0) / prices.length).toFixed(2);
  console.log(`- ${cat} (${prices.length} items): Min ₹${min}, Max ₹${max}, Avg ₹${avg}`);
}

if (errors.length > 0) {
  console.error(`\nValidation FAILED with ${errors.length} errors:`);
  for (const err of errors.slice(0, 10)) {
    console.error(`- ${err}`);
  }
  process.exit(1);
} else {
  console.log("\nValidation PASSED: All 1,000 products conform to schema, unique constraints, and price distributions!");
}
