import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  Star,
  ShoppingBag,
  Heart,
  Truck,
  RotateCcw,
  ShieldCheck,
  Check,
  ChevronRight,
  ArrowUpRight
} from 'lucide-react';
import type { Product } from '../types';
import { money } from '../utils/format';
import ProductCard from '../components/ProductCard';
import { categoryToSlug } from './pageShared';
import { productService } from '../services/productService';
import { isFootwearProduct, isSizeAvailable, productRequiresSize, sizeChartFor } from '../utils/productSize';

const FALLBACK_IMAGE = 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=1500&q=80';

export function ProductPage({
  products,
  addToCart,
  wishlist,
  toggleWish
}: {
  products: Product[];
  addToCart: (p: Product, size?: string) => void;
  wishlist: number[];
  toggleWish: (id: number) => void;
}) {
  const { id } = useParams<{ id: string }>();
  const numId = Number(id);

  // Look in memory products first
  const memoryProduct = products.find((x) => x.id === numId);

  // Fallback state if opened directly or refreshed before products load
  const [fetchedProduct, setFetchedProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(false);
  const [fetchError, setFetchError] = useState(false);

  const [tab, setTab] = useState<'Details' | 'Specifications' | 'Delivery & returns'>('Details');
  const [added, setAdded] = useState(false);
  const [imgSrc, setImgSrc] = useState<string>('');
  const [selectedSize, setSelectedSize] = useState('');
  const [sizeError, setSizeError] = useState('');

  const p = memoryProduct || fetchedProduct;

  // Scroll to top and reset state when product ID changes
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    setTab('Details');
    setAdded(false);
    setFetchError(false);
    setSelectedSize('');
    setSizeError('');

    if (!memoryProduct && numId) {
      setLoading(true);
      productService
        .getById(numId)
        .then((item) => {
          setFetchedProduct(item);
          setLoading(false);
        })
        .catch(() => {
          setFetchError(true);
          setLoading(false);
        });
    }
  }, [id, numId, memoryProduct]);

  useEffect(() => {
    if (p) {
      setImgSrc(p.imageUrl ? p.imageUrl.replace('w=900', 'w=1500') : FALLBACK_IMAGE);
    }
  }, [p]);

  if (loading) {
    return (
      <div className="page empty-state">
        <span className="eyebrow">NOVA STUDIO / CATALOGUE</span>
        <h2>Finding your object…</h2>
        <p>Loading curated product details from the collection.</p>
      </div>
    );
  }

  if (!p || fetchError) {
    return (
      <div className="empty-state page">
        <span className="eyebrow">NOVA STUDIO / 404</span>
        <h2>Object not found.</h2>
        <p>The piece you are looking for may have been retired or does not exist in the collection.</p>
        <Link className="button button-gold" to="/search?q=all">
          Browse the catalogue <ArrowUpRight size={14} />
        </Link>
      </div>
    );
  }

  const relatedProducts = products
    .filter((x) => x.category === p.category && x.id !== p.id)
    .slice(0, 4);

  return (
    <div className="page detail-page">
      <div className="breadcrumbs">
        <Link to="/">Home</Link>
        <ChevronRight size={13} />
        <Link to={`/category/${categoryToSlug(p.category)}`}>{p.category}</Link>
        <ChevronRight size={13} />
        <span>{p.name}</span>
      </div>

      <div className="detail-layout">
        <div className="detail-photo">
          <img
            src={imgSrc}
            alt={p.name}
            onError={() => setImgSrc(FALLBACK_IMAGE)}
          />
          <span className="photo-tag">
            NOVA / OBJECT {String(p.id).padStart(3, '0')}
          </span>
        </div>

        <div className="detail-info">
          <span className="eyebrow">
            {p.brand} / {p.category}
          </span>
          <h1>{p.name}</h1>

          <div className="detail-rating">
            <span className="rating">
              <Star size={14} fill="currentColor" />
              {p.rating}
            </span>
            <span>{p.reviewCount} verified reviews</span>
          </div>

          <div className="detail-price">
            {money(p.price)} <span>Inclusive of all taxes</span>
          </div>

          <p className="detail-description">{p.description}</p>

          {productRequiresSize(p) && (
            <div className="size-selector">
              <div className="size-selector-head">
                <span>SIZE {isFootwearProduct(p) ? '(UK)' : ''}</span>
                {selectedSize ? <b>{selectedSize}</b> : <b>Select a size</b>}
              </div>
              <div className="size-options" role="listbox" aria-label="Available sizes">
                {sizeChartFor(p).map((size) => {
                  const available = isSizeAvailable(p, size);
                  return (
                    <button
                      type="button"
                      key={size}
                      role="option"
                      aria-selected={selectedSize === size}
                      className={'size-chip' + (selectedSize === size ? ' selected' : '')}
                      disabled={!available}
                      onClick={() => {
                        setSelectedSize(size);
                        setSizeError('');
                        setAdded(false);
                      }}
                    >
                      {isFootwearProduct(p) ? size.replace('UK ', '') : size}
                    </button>
                  );
                })}
              </div>
              {sizeError && <p className="size-error" role="alert">{sizeError}</p>}
            </div>
          )}

          <div className="detail-attributes">
            <div>
              <span>COLOUR</span>
              <b>{p.color || 'Signature'}</b>
            </div>
            <div>
              <span>COLLECTION</span>
              <b>{p.category}</b>
            </div>
            <div>
              <span>REFERENCE</span>
              <b>NV-{String(p.id).padStart(5, '0')}</b>
            </div>
          </div>

          <div className="detail-actions">
            <button
              className="button button-gold"
              onClick={() => {
                if (productRequiresSize(p) && !selectedSize) {
                  setSizeError('Please select a size before adding this piece to your bag.');
                  setAdded(false);
                  return;
                }
                if (productRequiresSize(p) && !isSizeAvailable(p, selectedSize)) {
                  setSizeError('That size is not available for this piece.');
                  return;
                }
                addToCart(p, selectedSize || undefined);
                setAdded(true);
                setSizeError('');
              }}
            >
              {added ? <Check size={16} /> : <ShoppingBag size={16} />}
              {added ? 'Added to bag' : 'Add to bag'}
            </button>
            <button
              className={'button button-outline ' + (wishlist.includes(p.id) ? 'wished' : '')}
              onClick={() => toggleWish(p.id)}
            >
              <Heart
                size={16}
                fill={wishlist.includes(p.id) ? 'currentColor' : 'none'}
              />
              {wishlist.includes(p.id) ? 'Saved' : 'Save'}
            </button>
          </div>

          <div className="service-points">
            <span>
              <Truck size={16} /> Complimentary delivery over ₹999
            </span>
            <span>
              <RotateCcw size={16} /> 7-day return window
            </span>
            <span>
              <ShieldCheck size={16} /> Verified authenticity &amp; warranty
            </span>
          </div>

          <div className="detail-tabs">
            {(['Details', 'Specifications', 'Delivery & returns'] as const).map((t) => (
              <button
                className={tab === t ? 'active' : ''}
                key={t}
                onClick={() => setTab(t)}
              >
                {t}
              </button>
            ))}
          </div>

          <div className="tab-copy">
            {tab === 'Details' && <p>{p.description}</p>}
            {tab === 'Specifications' && (
              <p>
                Brand: <strong>{p.brand}</strong> · Category: <strong>{p.category}</strong> · Colour:{' '}
                <strong>{p.color || 'Signature'}</strong>
                {productRequiresSize(p) ? <> · Sizes: <strong>{sizeChartFor(p).join(', ')}</strong></> : null}
                {' '}· Product reference: <strong>NV-{p.id}</strong>.
              </p>
            )}
            {tab === 'Delivery & returns' && (
              <p>
                Delivery estimates are confirmed at checkout. Orders over ₹999 qualify for complimentary expedited shipping. Eligible items can be returned within 7 days of delivery, subject to store policy.
              </p>
            )}
          </div>
        </div>
      </div>

      {relatedProducts.length > 0 && (
        <section className="collection-section">
          <div className="section-heading">
            <div>
              <span className="eyebrow">MORE IN {p.category.toUpperCase()}</span>
              <h2>Related <em>objects.</em></h2>
            </div>
            <Link className="text-link" to={`/category/${categoryToSlug(p.category)}`}>
              Explore category <ArrowUpRight size={14} />
            </Link>
          </div>
          <div className="product-grid">
            {relatedProducts.map((x) => (
              <ProductCard
                key={x.id}
                p={x}
                wishlist={wishlist}
                toggleWish={toggleWish}
              />
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
