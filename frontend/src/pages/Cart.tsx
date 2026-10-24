import { Link, useNavigate } from 'react-router-dom';
import { ShoppingBag, ArrowUpRight, ArrowLeft, Plus, Minus } from 'lucide-react';
import { money } from '../utils/format';
import { cartLineKey } from '../utils/productSize';
import type { CartItem } from './pageShared';

export function Cart({
  cart,
  setCart,
  user
}: {
  cart: CartItem[];
  setCart: (x: CartItem[]) => void;
  user: any;
}) {
  const navigate = useNavigate();
  const subtotal = cart.reduce((n, x) => n + x.product.price * x.qty, 0);
  const updateQty = (item: CartItem, qty: number) => {
    const key = cartLineKey(item.product.id, item.size);
    setCart(
      qty <= 0
        ? cart.filter((x) => cartLineKey(x.product.id, x.size) !== key)
        : cart.map((x) => (cartLineKey(x.product.id, x.size) === key ? { ...x, qty } : x))
    );
  };

  return (
    <div className="page account-page">
      <div className="page-heading">
        <span className="eyebrow">YOUR SELECTION</span>
        <h1>
          The <em>bag.</em>
        </h1>
        <p>{cart.reduce((n, x) => n + x.qty, 0)} objects selected</p>
      </div>
      {cart.length ? (
        <div className="cart-layout">
          <div className="cart-lines">
            {cart.map((item) => {
              const { product: p, qty, size } = item;
              return (
                <div className="cart-line" key={cartLineKey(p.id, size)}>
                  <Link to={`/product/${p.id}`} className="cart-image">
                    <img src={p.imageUrl} alt={p.name} />
                  </Link>
                  <div className="cart-line-info">
                    <span className="eyebrow">{p.brand}</span>
                    <Link to={`/product/${p.id}`}>
                      <h3>{p.name}</h3>
                    </Link>
                    <span>
                      {p.color}
                      {size ? ` · Size ${size}` : ''}
                    </span>
                    <b>{money(p.price)}</b>
                    <div className="qty-control">
                      <button onClick={() => updateQty(item, Math.max(1, qty - 1))}>
                        <Minus size={13} />
                      </button>
                      <span>{qty}</span>
                      <button onClick={() => updateQty(item, qty + 1)}>
                        <Plus size={13} />
                      </button>
                      <button className="remove-item" onClick={() => updateQty(item, 0)}>
                        Remove
                      </button>
                    </div>
                  </div>
                  <b className="line-total">{money(p.price * qty)}</b>
                </div>
              );
            })}
          </div>
          <aside className="order-summary">
            <span className="eyebrow">ORDER SUMMARY</span>
            <div>
              <span>Subtotal</span>
              <b>{money(subtotal)}</b>
            </div>
            <div>
              <span>Delivery</span>
              <b>{subtotal > 999 ? 'Complimentary' : money(99)}</b>
            </div>
            <div className="summary-total">
              <span>Total</span>
              <b>{money(subtotal + (subtotal > 999 ? 0 : 99))}</b>
            </div>
            <p>Taxes included where applicable.</p>
            <button
              className="button button-gold full"
              onClick={() => navigate(user ? '/checkout' : '/login?next=checkout')}
            >
              Continue to checkout <ArrowUpRight size={16} />
            </button>
            <Link to="/search?q=all" className="continue-link">
              <ArrowLeft size={14} /> Continue discovering
            </Link>
          </aside>
        </div>
      ) : (
        <div className="empty-state">
          <ShoppingBag size={34} />
          <h2>Your bag is taking a pause.</h2>
          <p>Discover something worth keeping.</p>
          <Link className="button button-gold" to="/search?q=all">
            Explore the collection <ArrowUpRight size={15} />
          </Link>
        </div>
      )}
    </div>
  );
}
