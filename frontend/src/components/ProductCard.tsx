import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Heart, Star } from 'lucide-react';
import type { Product } from '../types';
import { money } from '../utils/format';

const FALLBACK_IMAGE = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80';

export default function ProductCard({
  p,
  wishlist,
  toggleWish
}: {
  p: Product;
  wishlist: number[];
  toggleWish: (id: number) => void;
}) {
  const [imgUrl, setImgUrl] = useState(p.imageUrl);

  return (
    <Link to={`/product/${p.id}`} className="product-card">
      <div className="product-image">
        <img
          src={imgUrl}
          alt={p.name}
          loading="lazy"
          onError={() => setImgUrl(FALLBACK_IMAGE)}
        />
        <button
          className={'wish-button ' + (wishlist.includes(p.id) ? 'wished' : '')}
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            toggleWish(p.id);
          }}
          aria-label="Save product"
        >
          <Heart size={16} fill={wishlist.includes(p.id) ? 'currentColor' : 'none'} />
        </button>
        <span className="image-index">{String(p.id).padStart(3, '0')}</span>
      </div>
      <div className="product-meta">
        <span className="eyebrow">{p.brand} · {p.category}</span>
        <h3>{p.name}</h3>
        <div className="product-bottom">
          <b>{money(p.price)}</b>
          <span className="rating">
            <Star size={12} fill="currentColor" />
            {p.rating} <small>({p.reviewCount})</small>
          </span>
        </div>
      </div>
    </Link>
  );
}
