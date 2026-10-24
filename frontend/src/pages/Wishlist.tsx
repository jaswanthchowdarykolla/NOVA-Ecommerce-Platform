import { Link } from 'react-router-dom';
import { Heart } from 'lucide-react';
import type { Product } from '../types';
import ProductCard from '../components/ProductCard';

export function Wishlist({products,wishlist,toggleWish}:{products:Product[],wishlist:number[],toggleWish:(id:number)=>void}) {const saved=products.filter(p=>wishlist.includes(p.id));return <div className="page listing-page"><div className="page-heading"><span className="eyebrow">YOUR PERSONAL EDIT</span><h1>Saved for <em>later.</em></h1><p>{saved.length} saved objects</p></div>{saved.length?<div className="product-grid">{saved.map(p=><ProductCard key={p.id} p={p} wishlist={wishlist} toggleWish={toggleWish}/>)}</div>:<div className="empty-state"><Heart size={30}/><h2>Keep what catches your eye.</h2><Link className="button button-gold" to="/search?q=all">Explore the collection</Link></div>}</div>}
