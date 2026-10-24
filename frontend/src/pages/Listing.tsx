import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { Search, ChevronRight, SlidersHorizontal, X, ArrowUpRight } from 'lucide-react';
import type { Product } from '../types';
import { money } from '../utils/format';
import ProductCard from '../components/ProductCard';
import { categories, categoryToSlug, normalizeCategorySlug } from './pageShared';

interface ListingProps {
  products: Product[];
  toggleWish: (id: number) => void;
  wishlist: number[];
}

export function Listing({ products, toggleWish, wishlist }: ListingProps) {
  const [params, setParams] = useSearchParams();
  const route = useParams<{ slug?: string }>();
  const navigate = useNavigate();

  const rawQ = params.get('q') || '';
  const [searchInput, setSearchInput] = useState(rawQ);

  // Derive category from route slug or search params
  const initialCategory = route.slug
    ? normalizeCategorySlug(route.slug)
    : (params.get('cat') ? normalizeCategorySlug(params.get('cat')) : 'All');

  const [cat, setCat] = useState<string>(initialCategory);
  const [sort, setSort] = useState<string>('featured');
  const [maxPrice, setMaxPrice] = useState<number>(50000);
  const [visibleCount, setVisibleCount] = useState<number>(48);

  // Keep state in sync with URL changes
  useEffect(() => {
    if (route.slug) {
      setCat(normalizeCategorySlug(route.slug));
    } else if (params.get('cat')) {
      setCat(normalizeCategorySlug(params.get('cat')));
    } else {
      setCat('All');
    }
  }, [route.slug, params]);

  useEffect(() => {
    setSearchInput(rawQ);
  }, [rawQ]);

  // Handle category change
  const handleSelectCategory = (newCat: string) => {
    setCat(newCat);
    setVisibleCount(48);
    if (rawQ) {
      const nextParams = new URLSearchParams(params);
      if (newCat === 'All') {
        nextParams.delete('cat');
      } else {
        nextParams.set('cat', categoryToSlug(newCat));
      }
      navigate(`/search?${nextParams.toString()}`);
    } else {
      if (newCat === 'All') {
        navigate('/search?q=all');
      } else {
        navigate(`/category/${categoryToSlug(newCat)}`);
      }
    }
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const clean = searchInput.trim();
    if (!clean) {
      navigate('/search?q=all');
      return;
    }
    navigate(`/search?q=${encodeURIComponent(clean)}${cat !== 'All' ? `&cat=${categoryToSlug(cat)}` : ''}`);
  };

  const clearSearch = () => {
    setSearchInput('');
    if (cat !== 'All') {
      navigate(`/category/${categoryToSlug(cat)}`);
    } else {
      navigate('/search?q=all');
    }
  };

  const resetAllFilters = () => {
    setCat('All');
    setMaxPrice(50000);
    setSort('featured');
    setSearchInput('');
    navigate('/search?q=all');
  };

  // Filter products
  const filtered = useMemo(() => {
    const qLower = rawQ.trim().toLowerCase();
    const isAll = !qLower || qLower === 'all';
    const isFeatured = qLower === 'featured';
    const isNew = qLower === 'new';

    const tokens = (!isAll && !isFeatured && !isNew)
      ? qLower.split(/\s+/).filter(Boolean)
      : [];

    return products.filter((p) => {
      // 1. Category check
      if (cat !== 'All' && p.category !== cat) {
        return false;
      }

      // 2. Price check
      if (p.price > maxPrice) {
        return false;
      }

      // 3. Search query check
      if (isAll) return true;
      if (isFeatured) return p.featured;
      if (isNew) return true; // Handled in sort

      // Multi-token search: all tokens must match
      const searchableText = `${p.name} ${p.brand} ${p.category} ${p.description} ${p.color} ${(p.tags || []).join(' ')} ${(p.availableSizes || []).join(' ')}`.toLowerCase();
      return tokens.every((token) => searchableText.includes(token));
    }).sort((a, b) => {
      if (sort === 'price-low') return a.price - b.price;
      if (sort === 'price-high') return b.price - a.price;
      if (sort === 'rating') return b.rating - a.rating;
      if (sort === 'new' || rawQ.toLowerCase().trim() === 'new') return b.id - a.id;
      // Default: featured first, then stable by ID
      if (a.featured !== b.featured) return a.featured ? -1 : 1;
      return a.id - b.id;
    });
  }, [products, rawQ, cat, maxPrice, sort]);

  const displayedProducts = useMemo(() => {
    return filtered.slice(0, visibleCount);
  }, [filtered, visibleCount]);

  const titleText = useMemo(() => {
    if (cat !== 'All') return cat;
    if (rawQ && rawQ !== 'all' && rawQ !== 'featured' && rawQ !== 'new') return `Search for “${rawQ}”`;
    if (rawQ === 'featured') return 'Curated Essentials';
    if (rawQ === 'new') return 'The New Edit';
    return 'Objects worth finding.';
  }, [cat, rawQ]);

  return (
    <div className="page listing-page">
      <div className="breadcrumbs">
        <Link to="/">Home</Link>
        <ChevronRight size={13} />
        {cat !== 'All' ? (
          <>
            <Link to="/search?q=all">Catalogue</Link>
            <ChevronRight size={13} />
            <span>{cat}</span>
          </>
        ) : (
          <span>{rawQ ? `Search: ${rawQ}` : 'Discover'}</span>
        )}
      </div>

      <div className="listing-title">
        <div>
          <span className="eyebrow">THE NOVA CATALOGUE / SEARCH &amp; DISCOVERY</span>
          <h1>{titleText}</h1>
          <p>{filtered.length.toLocaleString()} pieces available across the collection</p>
        </div>

        <div className="listing-controls">
          <label className="sort-select">
            Sort by{' '}
            <select value={sort} onChange={(e) => setSort(e.target.value)}>
              <option value="featured">Curated</option>
              <option value="new">Newest</option>
              <option value="price-low">Price: low to high</option>
              <option value="price-high">Price: high to low</option>
              <option value="rating">Top rated</option>
            </select>
          </label>
        </div>
      </div>

      <div className="listing-layout">
        <aside className="filters">
          <span className="filter-title">
            <SlidersHorizontal size={15} /> Refine your search
          </span>

          {/* Inline search input */}
          <form className="filter-search-box" onSubmit={handleSearchSubmit}>
            <input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="Filter by keyword…"
            />
            {searchInput ? (
              <button type="button" onClick={clearSearch} aria-label="Clear keyword">
                <X size={14} />
              </button>
            ) : (
              <button type="submit" aria-label="Search">
                <Search size={14} />
              </button>
            )}
          </form>

          <label className="filter-label">Category</label>
          <div className="category-filter-list">
            {categories.map((c) => (
              <label className="check-row" key={c}>
                <input
                  type="radio"
                  name="cat"
                  checked={cat === c}
                  onChange={() => handleSelectCategory(c)}
                />
                <span>{c}</span>
              </label>
            ))}
          </div>

          <label className="filter-label">
            Price ceiling <b>{money(maxPrice)}</b>
          </label>
          <input
            className="range"
            type="range"
            min="500"
            max="50000"
            step="500"
            value={maxPrice}
            onChange={(e) => setMaxPrice(+e.target.value)}
          />

          <button className="reset-filter" onClick={resetAllFilters}>
            Reset filters
          </button>
        </aside>

        <div className="listing-results">
          {filtered.length > 0 ? (
            <>
              <div className="product-grid">
                {displayedProducts.map((p) => (
                  <ProductCard
                    key={p.id}
                    p={p}
                    wishlist={wishlist}
                    toggleWish={toggleWish}
                  />
                ))}
              </div>

              {filtered.length > visibleCount && (
                <div className="load-more-wrap">
                  <p className="load-more-status">
                    Showing {displayedProducts.length} of {filtered.length} products
                  </p>
                  <button
                    className="button button-outline"
                    onClick={() => setVisibleCount((prev) => prev + 48)}
                  >
                    Load more objects <ArrowUpRight size={14} />
                  </button>
                </div>
              )}
            </>
          ) : (
            <div className="empty-state">
              <Search size={28} />
              <h2>No exact matches yet.</h2>
              <p>
                No products found matching your current filters. Try selecting another category or increasing the price ceiling.
              </p>
              <button className="button button-gold" onClick={resetAllFilters}>
                Show all products
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
