import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Check, ArrowUpRight, ShoppingBag, MapPin, Plus } from 'lucide-react';
import { orderService } from '../services/orderService';
import { validateCheckout } from '../schemas/checkoutSchema';
import { createPaymentOrder, verifyPayment } from '../api/paymentApi';
import { listAddresses, type Address } from '../api/addressApi';
import { money } from '../utils/format';
import type { CartItem } from './pageShared';

export function Checkout({
  cart,
  setCart,
  user
}: {
  cart: CartItem[];
  setCart: (x: CartItem[]) => void;
  user: any;
}) {
  const [step, setStep] = useState(1);
  const [name, setName] = useState(user?.name || '');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [email, setEmail] = useState(user?.email || '');
  const [done, setDone] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [orderId, setOrderId] = useState<number | null>(null);
  const [paymentStatus, setPaymentStatus] = useState('PENDING_PAYMENT');

  // Saved addresses
  const [savedAddresses, setSavedAddresses] = useState<Address[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);

  useEffect(() => {
    if (user) {
      listAddresses()
        .then((addrs) => {
          setSavedAddresses(addrs);
          const defaultAddr = addrs.find((a) => a.isDefault) || addrs[0];
          if (defaultAddr) {
            setSelectedAddressId(defaultAddr.id);
            setName(defaultAddr.recipientName);
            setPhone(defaultAddr.phone);
            setAddress(
              `${defaultAddr.addressLine1}${defaultAddr.addressLine2 ? ', ' + defaultAddr.addressLine2 : ''}${defaultAddr.locality ? ', ' + defaultAddr.locality : ''}, ${defaultAddr.city}, ${defaultAddr.state} - ${defaultAddr.postalCode}, ${defaultAddr.country || 'India'}`
            );
          }
        })
        .catch(() => {});
    }
  }, [user]);

  const selectAddress = (addr: Address) => {
    setSelectedAddressId(addr.id);
    setName(addr.recipientName);
    setPhone(addr.phone);
    setAddress(
      `${addr.addressLine1}${addr.addressLine2 ? ', ' + addr.addressLine2 : ''}${addr.locality ? ', ' + addr.locality : ''}, ${addr.city}, ${addr.state} - ${addr.postalCode}, ${addr.country || 'India'}`
    );
  };

  const selectCustomAddress = () => {
    setSelectedAddressId(null);
    setAddress('');
  };

  const total = cart.reduce((n, x) => n + x.product.price * x.qty, 0);
  const deliveryFee = total > 999 ? 0 : 99;
  const payable = total + deliveryFee;

  if (!user) {
    return (
      <div className="page empty-state">
        <span className="eyebrow">NOVA CHECKOUT</span>
        <h2>Sign in to continue</h2>
        <p>Purchases and delivery records are securely linked to your customer account.</p>
        <Link className="button button-gold" to="/login?next=/checkout">
          Sign in <ArrowUpRight size={15} />
        </Link>
      </div>
    );
  }

  if (!cart.length && !done) {
    return (
      <div className="page empty-state">
        <ShoppingBag size={32} />
        <h2>Your bag is empty.</h2>
        <p>Discover thoughtful objects in the catalogue before checking out.</p>
        <Link className="button button-gold" to="/search?q=all">
          Explore the collection
        </Link>
      </div>
    );
  }

  const placeOrder = async () => {
    setError('');
    const errors = validateCheckout({
      recipientName: name,
      recipientPhone: phone,
      deliveryAddress: address,
      contactEmail: email
    });
    if (errors.length) {
      setError(errors[0]);
      return;
    }

    setBusy(true);
    try {
      const missingSize = cart.find((x) => (x.product.availableSizes || []).length > 0 && !x.size);
      if (missingSize) {
        setError(`Select a size for ${missingSize.product.name} before checkout.`);
        setBusy(false);
        return;
      }
      const order = await orderService.create({
        items: cart.map((x) => ({ productId: x.product.id, quantity: x.qty, size: x.size })),
        recipientName: name,
        recipientPhone: phone,
        deliveryAddress: address,
        contactEmail: email
      });
      setOrderId(order.id);

      try {
        const paymentOrder = await createPaymentOrder(order.id);
        if (!window.Razorpay) {
          throw new Error('Razorpay Checkout script is unavailable. The order remains pending payment.');
        }

        const checkout = new window.Razorpay({
          key: paymentOrder.keyId,
          amount: paymentOrder.amount,
          currency: paymentOrder.currency,
          name: 'NOVA Studio',
          description: `Order #${order.id}`,
          order_id: paymentOrder.razorpayOrderId,
          handler: async (response: any) => {
            try {
              await verifyPayment({
                orderId: order.id,
                razorpayOrderId: response.razorpay_order_id,
                razorpayPaymentId: response.razorpay_payment_id,
                razorpaySignature: response.razorpay_signature
              });
              setPaymentStatus('PAID');
              setCart([]);
              setDone(true);
            } catch (e) {
              setError(e instanceof Error ? e.message : 'Payment verification failed. Do not retry until checking payment status.');
              setDone(true);
            }
          },
          modal: {
            ondismiss: () => {
              setError('Checkout closed. Your order is saved as pending payment.');
              setDone(true);
            }
          },
          prefill: { name, email, contact: phone },
          theme: { color: '#c9a96e' }
        });
        checkout.open();
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Payment gateway is not configured; your order remains saved as pending payment.');
        setDone(true);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unable to create order. Please confirm the backend is running.');
    } finally {
      setBusy(false);
    }
  };

  if (done) {
    return (
      <div className="page empty-state">
        <div className="success-icon">
          <Check size={32} />
        </div>
        <span className="eyebrow">ORDER RECORDED</span>
        <h1>Consider it <em>in motion.</em></h1>
        <p>
          Order #{orderId} has been saved to your account with status <strong>{paymentStatus}</strong>.
          {paymentStatus === 'PAID'
            ? ' Payment was verified successfully. We are preparing your shipment.'
            : ' Payment was not completed yet. You can inspect this order under Your Orders in your account.'}
        </p>
        <div className="button-row" style={{ marginTop: 24, gap: 16 }}>
          <Link className="button button-gold" to="/account/orders">
            View order details <ArrowUpRight size={14} />
          </Link>
          <Link className="button button-outline" to="/search?q=all">
            Continue shopping
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="page checkout-page">
      <div className="page-heading">
        <span className="eyebrow">A CONSIDERED CHECKOUT / STEP {step} OF 3</span>
        <h1>Almost <em>yours.</em></h1>
      </div>

      <div className="checkout-steps">
        {['Recipient', 'Delivery', 'Review'].map((x, i) => (
          <div className={step === i + 1 ? 'current' : step > i + 1 ? 'done' : ''} key={x}>
            <span>{step > i + 1 ? <Check size={14} /> : i + 1}</span>
            {x}
          </div>
        ))}
      </div>

      <div className="checkout-layout">
        <div className="checkout-form">
          <span className="eyebrow">
            {step === 1 ? '01 / WHO IS THIS FOR?' : step === 2 ? '02 / DELIVERY DESTINATION' : '03 / FINAL REVIEW & PAYMENT'}
          </span>

          {step === 1 && (
            <>
              <h2>Recipient information</h2>
              <label className="form-label">
                Full name *
                <input
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Full name"
                  required
                />
              </label>

              <label className="form-label">
                Contact email *
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@example.com"
                  required
                />
              </label>

              <button
                className="button button-gold full"
                onClick={() => {
                  if (!name.trim()) {
                    setError('Please provide the recipient name.');
                    return;
                  }
                  if (!email.trim()) {
                    setError('Please provide a contact email address.');
                    return;
                  }
                  setError('');
                  setStep(2);
                }}
              >
                Continue to delivery <ArrowUpRight size={15} />
              </button>
            </>
          )}

          {step === 2 && (
            <>
              <h2>Delivery details</h2>

              {savedAddresses.length > 0 && (
                <div className="saved-address-selector">
                  <span className="eyebrow">SELECT A SAVED ADDRESS</span>
                  <div className="address-options">
                    {savedAddresses.map((addr) => (
                      <div
                        key={addr.id}
                        className={`recipient-option ${selectedAddressId === addr.id ? 'selected' : ''}`}
                        onClick={() => selectAddress(addr)}
                      >
                        <input
                          type="radio"
                          name="checkoutAddress"
                          checked={selectedAddressId === addr.id}
                          onChange={() => selectAddress(addr)}
                        />
                        <div>
                          <b>
                            {addr.recipientName} {addr.isDefault && <span className="default-pill">DEFAULT</span>}
                          </b>
                          <span>
                            {addr.addressLine1}, {addr.city}, {addr.state} — {addr.postalCode}
                          </span>
                          <small>Phone: {addr.phone}</small>
                        </div>
                      </div>
                    ))}

                    <div
                      className={`recipient-option ${selectedAddressId === null ? 'selected' : ''}`}
                      onClick={selectCustomAddress}
                    >
                      <input
                        type="radio"
                        name="checkoutAddress"
                        checked={selectedAddressId === null}
                        onChange={selectCustomAddress}
                      />
                      <div>
                        <b>Enter a new delivery address</b>
                        <span>Provide a different shipping location for this order</span>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              <label className="form-label">
                Contact phone number *
                <input
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="+91 98765 43210"
                  required
                />
              </label>

              <label className="form-label">
                Complete delivery address *
                <textarea
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  placeholder="House / flat number, street name, locality, city and PIN code"
                  required
                />
              </label>

              <div className="button-row">
                <button className="button button-outline" onClick={() => setStep(1)}>
                  Back
                </button>
                <button
                  className="button button-gold"
                  onClick={() => {
                    const errors = validateCheckout({
                      recipientName: name,
                      recipientPhone: phone,
                      deliveryAddress: address,
                      contactEmail: email
                    });
                    if (errors.length) {
                      setError(errors[0]);
                      return;
                    }
                    setError('');
                    setStep(3);
                  }}
                >
                  Review order <ArrowUpRight size={15} />
                </button>
              </div>
            </>
          )}

          {step === 3 && (
            <>
              <h2>Review &amp; confirm</h2>
              <p className="muted">
                Your order is secured by PostgreSQL and verified through Razorpay.
              </p>

              <div className="review-summary-block">
                <div>
                  <span className="eyebrow">RECIPIENT</span>
                  <b>{name}</b>
                  <p>{email}</p>
                </div>
                <div>
                  <span className="eyebrow">SHIPPING TO</span>
                  <b>{address}</b>
                  <p>Contact: {phone}</p>
                </div>
              </div>

              <div className="button-row">
                <button className="button button-outline" onClick={() => setStep(2)}>
                  Back
                </button>
                <button
                  className="button button-gold"
                  disabled={busy}
                  onClick={placeOrder}
                >
                  {busy ? 'Initiating checkout…' : `Pay ${money(payable)}`} <Check size={15} />
                </button>
              </div>
            </>
          )}

          {error && <p className="error-message">{error}</p>}
        </div>

        <aside className="order-summary">
          <span className="eyebrow">IN YOUR BAG ({cart.reduce((n, x) => n + x.qty, 0)})</span>
          {cart.map((x) => (
            <div className="mini-order" key={`${x.product.id}-${x.size || ''}`}>
              <img src={x.product.imageUrl} alt={x.product.name} />
              <span>
                {x.product.name}
                <small>
                  Qty {x.qty}
                  {x.size ? ` · Size ${x.size}` : ''}
                </small>
              </span>
              <b>{money(x.product.price * x.qty)}</b>
            </div>
          ))}

          <div>
            <span>Subtotal</span>
            <b>{money(total)}</b>
          </div>
          <div>
            <span>Delivery</span>
            <b>{deliveryFee === 0 ? 'Complimentary' : money(deliveryFee)}</b>
          </div>
          <div className="summary-total">
            <span>Total payable</span>
            <b>{money(payable)}</b>
          </div>
          <p>Complimentary shipping applied on orders over ₹999.</p>
        </aside>
      </div>
    </div>
  );
}
