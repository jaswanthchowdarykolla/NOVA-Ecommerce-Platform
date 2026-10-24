export type Product = {
 id: number; name: string; slug: string; category: string; brand: string; price: number;
 rating: number; reviewCount: number; description: string; imageUrl: string;
 color: string; featured?: boolean; tags: string[]; availableSizes?: string[]
}
