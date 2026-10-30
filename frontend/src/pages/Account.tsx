import { useEffect, useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import {
  UserRound,
  ChevronRight,
  LogOut,
  Package,
  Heart,
  MapPin,
  CreditCard,
  Crown,
  Check,
  Plus,
  Trash2,
  Edit2,
  AlertCircle
} from 'lucide-react';
import { orderService } from '../services/orderService';
import { useAuthStore } from '../store/authStore';
import { money } from '../utils/format';
import { getProfile, updateProfile } from '../api/authApi';
import {
  listAddresses,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  type Address,
  type AddressInput
} from '../api/addressApi';
import { createPaymentOrder, getMyPayments, verifyPayment, type PaymentSummary } from '../api/paymentApi';
import type { OrderSummary } from '../api/orderApi';

interface AccountProps {
  user: any;
  setUser: (x: any) => void;
  products?: any[];
}

type TabType = 'profile' | 'orders' | 'addresses' | 'payments' | 'subscription';

export function Account({ user, setUser, products = [] }: AccountProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const storeLogout = useAuthStore((state) => state.logout);

  // Active tab state
  const [activeTab, setActiveTab] = useState<TabType>(() => {
    if (location.pathname === '/account/orders') return 'orders';
    if (location.hash === '#addresses') return 'addresses';
    if (location.hash === '#payments') return 'payments';
    if (location.hash === '#subscription') return 'subscription';
    return 'profile';
  });

  // Profile edit state
  const [profileName, setProfileName] = useState(user?.name || '');
  const [newPassword, setNewPassword] = useState('');
  const [profileSaving, setProfileSaving] = useState(false);
  const [profileFeedback, setProfileFeedback] = useState<{ type: 'success' | 'error'; msg: string } | null>(null);

  // Orders state
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [ordersLoading, setOrdersLoading] = useState(false);
  const [expandedOrder, setExpandedOrder] = useState<number | null>(null);
  const [payingOrder, setPayingOrder] = useState<number | null>(null);
  const [paymentError, setPaymentError] = useState('');

  // Addresses state
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [addressLoading, setAddressLoading] = useState(false);
  const [editingAddressId, setEditingAddressId] = useState<number | null>(null);
  const [showAddressForm, setShowAddressForm] = useState(false);
  const [addressFormData, setAddressFormData] = useState<AddressInput>({
    recipientName: '',
    phone: '',
    addressLine1: '',
    addressLine2: '',
    locality: '',
    city: '',
    state: '',
    postalCode: '',
    country: 'India',
    isDefault: false
  });
  const [addressError, setAddressError] = useState('');

  // Payments state
  const [payments, setPayments] = useState<PaymentSummary[]>([]);
  const [paymentsLoading, setPaymentsLoading] = useState(false);

  const retryPayment = async (orderId: number) => {
    setPaymentError('');
    setPayingOrder(orderId);
    try {
      const paymentOrder = await createPaymentOrder(orderId);
      if (!window.Razorpay) throw new Error('Razorpay Checkout could not be loaded. Refresh and try again.');
      const checkout = new window.Razorpay({
        key: paymentOrder.keyId,
        amount: paymentOrder.amount,
        currency: paymentOrder.currency,
        name: 'NOVA Studio',
        description: `Order #${orderId}`,
        order_id: paymentOrder.razorpayOrderId,
        handler: async (response: any) => {
          try {
            await verifyPayment({
              orderId,
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature
            });
            setOrders((current) => current.map((order) => order.id === orderId ? { ...order, status: 'PAID' } : order));
          } catch (error) {
            setPaymentError(error instanceof Error ? error.message : 'Payment verification failed. Refresh your orders before trying again.');
          } finally {
            setPayingOrder(null);
          }
        },
        modal: { ondismiss: () => setPayingOrder(null) },
        prefill: { name: user?.name, email: user?.email },
        theme: { color: '#c9a96e' }
      });
      checkout.on('payment.failed', (response) => {
        const description = response.error?.description;
        setPaymentError(description
          ? `Payment failed: ${description}. This order is still pending; you can retry it.`
          : 'Razorpay could not complete this payment. This order is still pending; you can retry it.');
        setPayingOrder(null);
      });
      checkout.open();
    } catch (error) {
      setPaymentError(error instanceof Error ? error.message : 'Unable to start Razorpay checkout.');
      setPayingOrder(null);
    }
  };

  // Load fresh profile on mount
  useEffect(() => {
    if (user) {
      getProfile()
        .then((p) => {
          setProfileName(p.name);
          setUser({ ...user, name: p.name, email: p.email, role: p.role });
        })
        .catch(() => {});
    }
  }, []);

  // Listen to location changes
  useEffect(() => {
    if (location.pathname === '/account/orders') {
      setActiveTab('orders');
    }
  }, [location.pathname]);

  // Load orders when active tab is orders
  useEffect(() => {
    if (user && activeTab === 'orders') {
      setOrdersLoading(true);
      orderService
        .listMine()
        .then((data) => {
          setOrders(data);
          setOrdersLoading(false);
        })
        .catch(() => {
          setOrders([]);
          setOrdersLoading(false);
        });
    }
  }, [user, activeTab]);

  // Load addresses when active tab is addresses
  useEffect(() => {
    if (user && activeTab === 'addresses') {
      loadAddresses();
    }
  }, [user, activeTab]);

  const loadAddresses = () => {
    setAddressLoading(true);
    listAddresses()
      .then((data) => {
        setAddresses(data);
        setAddressLoading(false);
      })
      .catch(() => {
        setAddresses([]);
        setAddressLoading(false);
      });
  };

  // Load payments when active tab is payments
  useEffect(() => {
    if (user && activeTab === 'payments') {
      setPaymentsLoading(true);
      getMyPayments()
        .then((data) => {
          setPayments(data);
          setPaymentsLoading(false);
        })
        .catch(() => {
          setPayments([]);
          setPaymentsLoading(false);
        });
    }
  }, [user, activeTab]);

  if (!user) {
    return (
      <div className="page empty-state">
        <UserRound size={32} />
        <h2>Your NOVA account</h2>
        <p>Sign in to manage your profile, delivery addresses, and orders.</p>
        <Link className="button button-gold" to="/login?next=/account">
          Sign in
        </Link>
      </div>
    );
  }

  const handleLogout = () => {
    storeLogout();
    setUser(null);
    navigate('/');
  };

  // Profile save
  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setProfileFeedback(null);
    if (!profileName.trim()) {
      setProfileFeedback({ type: 'error', msg: 'Full name cannot be blank.' });
      return;
    }
    if (newPassword && newPassword.length < 8) {
      setProfileFeedback({ type: 'error', msg: 'New password must be at least 8 characters long.' });
      return;
    }

    setProfileSaving(true);
    try {
      const updated = await updateProfile({
        name: profileName.trim(),
        newPassword: newPassword || undefined
      });
      setUser({ ...user, name: updated.name });
      setNewPassword('');
      setProfileFeedback({ type: 'success', msg: 'Profile updated successfully.' });
    } catch (err: any) {
      setProfileFeedback({
        type: 'error',
        msg: err.message || 'Failed to update profile. Please try again.'
      });
    } finally {
      setProfileSaving(false);
    }
  };

  // Address operations
  const startAddAddress = () => {
    setEditingAddressId(null);
    setAddressFormData({
      recipientName: user.name || '',
      phone: '',
      addressLine1: '',
      addressLine2: '',
      locality: '',
      city: '',
      state: '',
      postalCode: '',
      country: 'India',
      isDefault: addresses.length === 0
    });
    setAddressError('');
    setShowAddressForm(true);
  };

  const startEditAddress = (addr: Address) => {
    setEditingAddressId(addr.id);
    setAddressFormData({
      recipientName: addr.recipientName,
      phone: addr.phone,
      addressLine1: addr.addressLine1,
      addressLine2: addr.addressLine2 || '',
      locality: addr.locality || '',
      city: addr.city,
      state: addr.state,
      postalCode: addr.postalCode,
      country: addr.country || 'India',
      isDefault: addr.isDefault
    });
    setAddressError('');
    setShowAddressForm(true);
  };

  const handleSaveAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    setAddressError('');
    if (!addressFormData.recipientName.trim() || !addressFormData.phone.trim() || !addressFormData.addressLine1.trim() || !addressFormData.city.trim() || !addressFormData.postalCode.trim()) {
      setAddressError('Please fill in all required fields (*).');
      return;
    }

    try {
      if (editingAddressId) {
        await updateAddress(editingAddressId, addressFormData);
      } else {
        await createAddress(addressFormData);
      }
      setShowAddressForm(false);
      setEditingAddressId(null);
      loadAddresses();
    } catch (err: any) {
      setAddressError(err.message || 'Failed to save address.');
    }
  };

  const handleDeleteAddress = async (id: number) => {
    if (!window.confirm('Are you sure you want to remove this address?')) return;
    try {
      await deleteAddress(id);
      loadAddresses();
    } catch (err: any) {
      alert(err.message || 'Failed to delete address.');
    }
  };

  const handleSetDefaultAddress = async (id: number) => {
    try {
      await setDefaultAddress(id);
      loadAddresses();
    } catch (err: any) {
      alert(err.message || 'Failed to set default address.');
    }
  };

  return (
    <div className="page account-dashboard">
      <aside className="account-nav">
        <div className="account-avatar">{user.name.slice(0, 1).toUpperCase()}</div>
        <b>{user.name}</b>
        <small>{user.email}</small>

        <button
          className={activeTab === 'profile' ? 'active' : ''}
          onClick={() => setActiveTab('profile')}
        >
          <UserRound size={15} /> Profile <ChevronRight size={14} />
        </button>
        <button
          className={activeTab === 'orders' ? 'active' : ''}
          onClick={() => setActiveTab('orders')}
        >
          <Package size={15} /> My Orders <ChevronRight size={14} />
        </button>
        <button
          className={activeTab === 'addresses' ? 'active' : ''}
          onClick={() => setActiveTab('addresses')}
        >
          <MapPin size={15} /> Addresses <ChevronRight size={14} />
        </button>
        <button
          className={activeTab === 'payments' ? 'active' : ''}
          onClick={() => setActiveTab('payments')}
        >
          <CreditCard size={15} /> Payment History <ChevronRight size={14} />
        </button>
        <Link to="/wishlist">
          <Heart size={15} /> Wishlist <ChevronRight size={14} />
        </Link>
        <button
          className={activeTab === 'subscription' ? 'active' : ''}
          onClick={() => setActiveTab('subscription')}
        >
          <Crown size={15} /> Subscription <ChevronRight size={14} />
        </button>
        <button className="signout-button" onClick={handleLogout}>
          <LogOut size={15} /> Sign out
        </button>
      </aside>

      <div className="account-main">
        <span className="eyebrow">YOUR NOVA / PERSONAL SPACE</span>
        <h1>
          {activeTab === 'orders' && <>Your <em>orders.</em></>}
          {activeTab === 'profile' && <>Your <em>profile.</em></>}
          {activeTab === 'addresses' && <>Saved <em>addresses.</em></>}
          {activeTab === 'payments' && <>Payment <em>history.</em></>}
          {activeTab === 'subscription' && <>Your <em>membership.</em></>}
        </h1>

        {/* Quick navigation summary cards */}
        <div className="account-cards">
          <button
            className={`card-button ${activeTab === 'orders' ? 'selected' : ''}`}
            onClick={() => setActiveTab('orders')}
          >
            <Package />
            <b>Orders</b>
            <span>Track purchases &amp; history</span>
          </button>
          <button
            className={`card-button ${activeTab === 'addresses' ? 'selected' : ''}`}
            onClick={() => setActiveTab('addresses')}
          >
            <MapPin />
            <b>Addresses</b>
            <span>Manage delivery destinations</span>
          </button>
          <button
            className={`card-button ${activeTab === 'payments' ? 'selected' : ''}`}
            onClick={() => setActiveTab('payments')}
          >
            <CreditCard />
            <b>Payments</b>
            <span>Transactions &amp; receipts</span>
          </button>
        </div>

        {/* PROFILE TAB */}
        {activeTab === 'profile' && (
          <section className="profile-panel">
            <div className="profile-panel-head">
              <div>
                <span className="eyebrow">ACCOUNT INFORMATION</span>
                <h2>{user.name}</h2>
                <p>{user.email}</p>
              </div>
              <span className="status-pill">
                <i /> Active customer account
              </span>
            </div>

            <form onSubmit={handleSaveProfile} className="profile-edit-form">
              <span className="eyebrow">UPDATE DETAILS</span>

              {profileFeedback && (
                <div className={`feedback-alert ${profileFeedback.type}`}>
                  {profileFeedback.type === 'success' ? <Check size={16} /> : <AlertCircle size={16} />}
                  <span>{profileFeedback.msg}</span>
                </div>
              )}

              <label className="form-label">
                Full name *
                <input
                  type="text"
                  value={profileName}
                  onChange={(e) => setProfileName(e.target.value)}
                  placeholder="Your full name"
                  required
                />
              </label>

              <label className="form-label">
                Email address (read-only)
                <input type="email" value={user.email} disabled />
              </label>

              <label className="form-label">
                New password (optional)
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Leave blank to keep current password (min 8 chars)"
                />
              </label>

              <button className="button button-gold" type="submit" disabled={profileSaving}>
                {profileSaving ? 'Saving…' : 'Save profile changes'}
              </button>
            </form>
          </section>
        )}

        {/* ORDERS TAB */}
        {activeTab === 'orders' && (
          <section className="profile-panel">
            <span className="eyebrow">VERIFIED PURCHASE HISTORY</span>
            {paymentError && <div className="feedback-alert error"><AlertCircle size={16} /><span>{paymentError}</span></div>}
            {ordersLoading ? (
              <p className="loading-state">Loading your order history…</p>
            ) : orders.length > 0 ? (
              <div className="orders-list">
                {orders.map((o) => {
                  let parsedItems: any[] = [];
                  try {
                    parsedItems = JSON.parse(o.itemsJson || '[]');
                  } catch {
                    parsedItems = [];
                  }

                  const isExpanded = expandedOrder === o.id;

                  return (
                    <div className="order-card" key={o.id}>
                      <div className="order-card-header" onClick={() => setExpandedOrder(isExpanded ? null : o.id)}>
                        <div className="order-id-block">
                          <b>Order #{o.id}</b>
                          <span>{new Date(o.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}</span>
                        </div>

                        <div className="order-status-block">
                          <span className={`status-badge ${o.status.toLowerCase()}`}>
                            {o.status}
                          </span>
                        </div>

                        <div className="order-amount-block">
                          <strong>{money(Number(o.totalAmount))}</strong>
                          <ChevronRight
                            size={16}
                            style={{
                              transform: isExpanded ? 'rotate(90deg)' : 'none',
                              transition: 'transform .2s'
                            }}
                          />
                        </div>
                      </div>

                      {isExpanded && (
                        <div className="order-card-details">
                          {o.status === 'PENDING_PAYMENT' && (
                            <button className="button button-gold" disabled={payingOrder === o.id} onClick={() => void retryPayment(o.id)}>
                              <CreditCard size={15} /> {payingOrder === o.id ? 'Opening secure checkout…' : 'Pay now'}
                            </button>
                          )}
                          <div className="order-meta-info">
                            <div>
                              <span>RECIPIENT</span>
                              <b>{o.recipientName || user.name}</b>
                            </div>
                            <div>
                              <span>PHONE</span>
                              <b>{o.recipientPhone || '—'}</b>
                            </div>
                            <div>
                              <span>DELIVERY ADDRESS</span>
                              <b>{o.deliveryAddress || '—'}</b>
                            </div>
                          </div>

                          <div className="order-items-list">
                            <span className="eyebrow">ORDER ITEMS ({parsedItems.length})</span>
                            {parsedItems.map((item: any, idx: number) => {
                              const matchProd = products.find((p) => p.id === item.productId);
                              return (
                                <div className="order-item-row" key={idx}>
                                  {matchProd ? (
                                    <img src={matchProd.imageUrl} alt={matchProd.name} />
                                  ) : (
                                    <div className="item-placeholder">NV</div>
                                  )}
                                  <div className="item-info">
                                    <b>{matchProd ? matchProd.name : `Product #${item.productId}`}</b>
                                    <span>
                                      Quantity: {item.quantity}
                                      {item.size ? ` · Size ${item.size}` : ''}
                                    </span>
                                  </div>
                                  <strong>{matchProd ? money(matchProd.price * item.quantity) : '—'}</strong>
                                </div>
                              );
                            })}
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="empty-inner">
                <p>No orders recorded yet. Purchases placed through checkout will be tracked here with live status updates.</p>
                <Link className="button button-gold" to="/search?q=all">
                  Explore catalogue
                </Link>
              </div>
            )}
          </section>
        )}

        {/* ADDRESSES TAB */}
        {activeTab === 'addresses' && (
          <section className="profile-panel">
            <div className="panel-header-row">
              <div>
                <span className="eyebrow">DELIVERY DIRECTORY</span>
                <h2>Saved addresses</h2>
              </div>
              {!showAddressForm && (
                <button className="button button-gold" onClick={startAddAddress}>
                  <Plus size={15} /> Add new address
                </button>
              )}
            </div>

            {showAddressForm && (
              <form onSubmit={handleSaveAddress} className="address-form-modal">
                <div className="form-head">
                  <h3>{editingAddressId ? 'Edit Address' : 'New Delivery Address'}</h3>
                  <button
                    type="button"
                    className="button button-outline"
                    onClick={() => {
                      setShowAddressForm(false);
                      setEditingAddressId(null);
                    }}
                  >
                    Cancel
                  </button>
                </div>

                {addressError && (
                  <div className="feedback-alert error">
                    <AlertCircle size={16} />
                    <span>{addressError}</span>
                  </div>
                )}

                <div className="form-grid">
                  <label className="form-label">
                    Recipient name *
                    <input
                      type="text"
                      value={addressFormData.recipientName}
                      onChange={(e) => setAddressFormData({ ...addressFormData, recipientName: e.target.value })}
                      required
                    />
                  </label>

                  <label className="form-label">
                    Phone number *
                    <input
                      type="tel"
                      value={addressFormData.phone}
                      onChange={(e) => setAddressFormData({ ...addressFormData, phone: e.target.value })}
                      placeholder="+91 98765 43210"
                      required
                    />
                  </label>

                  <label className="form-label span-2">
                    Address line 1 (Flat / House / Street) *
                    <input
                      type="text"
                      value={addressFormData.addressLine1}
                      onChange={(e) => setAddressFormData({ ...addressFormData, addressLine1: e.target.value })}
                      placeholder="e.g. 42 Oberoi Chambers, Link Road"
                      required
                    />
                  </label>

                  <label className="form-label span-2">
                    Address line 2 (Apartment / Building / Area)
                    <input
                      type="text"
                      value={addressFormData.addressLine2 || ''}
                      onChange={(e) => setAddressFormData({ ...addressFormData, addressLine2: e.target.value })}
                    />
                  </label>

                  <label className="form-label">
                    Locality / Landmark
                    <input
                      type="text"
                      value={addressFormData.locality || ''}
                      onChange={(e) => setAddressFormData({ ...addressFormData, locality: e.target.value })}
                    />
                  </label>

                  <label className="form-label">
                    City *
                    <input
                      type="text"
                      value={addressFormData.city}
                      onChange={(e) => setAddressFormData({ ...addressFormData, city: e.target.value })}
                      required
                    />
                  </label>

                  <label className="form-label">
                    State *
                    <input
                      type="text"
                      value={addressFormData.state}
                      onChange={(e) => setAddressFormData({ ...addressFormData, state: e.target.value })}
                      required
                    />
                  </label>

                  <label className="form-label">
                    PIN code *
                    <input
                      type="text"
                      value={addressFormData.postalCode}
                      onChange={(e) => setAddressFormData({ ...addressFormData, postalCode: e.target.value })}
                      placeholder="400053"
                      required
                    />
                  </label>

                  <label className="form-label">
                    Country
                    <input
                      type="text"
                      value={addressFormData.country || 'India'}
                      onChange={(e) => setAddressFormData({ ...addressFormData, country: e.target.value })}
                    />
                  </label>

                  <label className="admin-checkbox span-2">
                    <input
                      type="checkbox"
                      checked={!!addressFormData.isDefault}
                      onChange={(e) => setAddressFormData({ ...addressFormData, isDefault: e.target.checked })}
                    />
                    <span>Set as primary default address for checkout</span>
                  </label>
                </div>

                <div className="button-row">
                  <button
                    type="button"
                    className="button button-outline"
                    onClick={() => {
                      setShowAddressForm(false);
                      setEditingAddressId(null);
                    }}
                  >
                    Cancel
                  </button>
                  <button type="submit" className="button button-gold">
                    {editingAddressId ? 'Update Address' : 'Save Address'}
                  </button>
                </div>
              </form>
            )}

            {addressLoading ? (
              <p className="loading-state">Loading saved addresses…</p>
            ) : addresses.length > 0 ? (
              <div className="addresses-grid">
                {addresses.map((addr) => (
                  <div className={`address-card ${addr.isDefault ? 'is-default' : ''}`} key={addr.id}>
                    <div className="address-card-head">
                      <b>{addr.recipientName}</b>
                      {addr.isDefault && <span className="default-pill">DEFAULT</span>}
                    </div>

                    <p className="address-body">
                      {addr.addressLine1}
                      {addr.addressLine2 ? `, ${addr.addressLine2}` : ''}
                      {addr.locality ? `, ${addr.locality}` : ''}
                      <br />
                      {addr.city}, {addr.state} — {addr.postalCode}
                      <br />
                      {addr.country || 'India'}
                    </p>

                    <p className="address-phone">Phone: {addr.phone}</p>

                    <div className="address-card-actions">
                      {!addr.isDefault && (
                        <button
                          type="button"
                          className="button-link"
                          onClick={() => handleSetDefaultAddress(addr.id)}
                        >
                          Set default
                        </button>
                      )}
                      <button
                        type="button"
                        className="button-link"
                        onClick={() => startEditAddress(addr)}
                      >
                        <Edit2 size={13} /> Edit
                      </button>
                      <button
                        type="button"
                        className="button-link danger"
                        onClick={() => handleDeleteAddress(addr.id)}
                      >
                        <Trash2 size={13} /> Delete
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="empty-inner">
                <p>No saved addresses found. Add an address now to accelerate future checkouts.</p>
                <button className="button button-gold" onClick={startAddAddress}>
                  <Plus size={15} /> Add first address
                </button>
              </div>
            )}
          </section>
        )}

        {/* PAYMENTS TAB */}
        {activeTab === 'payments' && (
          <section className="profile-panel">
            <span className="eyebrow">TRANSACTION LOG</span>
            <h2>Payment history</h2>

            {paymentsLoading ? (
              <p className="loading-state">Loading transaction history…</p>
            ) : payments.length > 0 ? (
              <div className="payments-table-wrap">
                <div className="payments-header-row">
                  <span>DATE</span>
                  <span>ORDER #</span>
                  <span>RAZORPAY ID</span>
                  <span>STATUS</span>
                  <span className="align-right">AMOUNT</span>
                </div>
                {payments.map((p) => (
                  <div className="payment-row" key={p.id}>
                    <span>{new Date(p.updatedAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}</span>
                    <b>Order #{p.orderId}</b>
                    <span className="mono-code">{p.razorpayPaymentId || p.razorpayOrderId}</span>
                    <span className={`status-badge ${p.status.toLowerCase()}`}>{p.status}</span>
                    <strong className="align-right">{money(Number(p.amount))}</strong>
                  </div>
                ))}
              </div>
            ) : (
              <div className="empty-inner">
                <p>No transactions yet. Payment records verified through the Razorpay gateway will automatically appear here.</p>
              </div>
            )}
          </section>
        )}

        {/* SUBSCRIPTION TAB */}
        {activeTab === 'subscription' && (
          <section className="profile-panel">
            <div className="subscription-card">
              <div className="subscription-head">
                <Crown size={28} className="gold-icon" />
                <div>
                  <span className="eyebrow">TIER: ELEVATED</span>
                  <h2>NOVA Standard Membership</h2>
                </div>
                <span className="status-pill">
                  <i /> Active
                </span>
              </div>

              <p className="subscription-desc">
                Your account includes lifetime NOVA Standard privileges across all digital and physical touchpoints.
              </p>

              <div className="subscription-perks">
                <div>
                  <Check size={16} />
                  <span>Complimentary express delivery on orders over ₹999</span>
                </div>
                <div>
                  <Check size={16} />
                  <span>Priority access to seasonal capsule releases and studio editions</span>
                </div>
                <div>
                  <Check size={16} />
                  <span>Personal shopping assistance via Ask NOVA concierge</span>
                </div>
                <div>
                  <Check size={16} />
                  <span>Hassle-free 7-day doorstep return verification</span>
                </div>
              </div>
            </div>
          </section>
        )}
      </div>
    </div>
  );
}
