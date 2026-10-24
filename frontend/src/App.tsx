import { useEffect, useState } from 'react'
import { Link, Route, Routes, useNavigate } from 'react-router-dom'
import { Search, Heart, ShoppingBag, UserRound, Sun, Moon, Menu, X, ArrowUpRight, Sparkles, Send } from 'lucide-react'
import { useProducts } from './hooks/useProducts'
import { getAssistantReply } from './services/assistantService'
import { useCartStore } from './store/cartStore'
import { useAuthStore } from './store/authStore'
import type { Product } from './types'
import { money } from './utils/format'
import { Home, Listing, ProductPage, Cart, Checkout, Auth, Account, Wishlist, Admin } from './pages/StorePages'

function App() {
 const {products, refresh: refreshProducts} = useProducts()
 const [dark,setDark] = useState(()=>localStorage.getItem('nova-theme') !== 'light')
 const cart = useCartStore(state=>state.items)
 const setCart = useCartStore(state=>state.replace)
 const addCartItem = useCartStore(state=>state.add)
 const [wishlist,setWishlist] = useState<number[]>(()=>{try{return JSON.parse(localStorage.getItem('nova-wishlist')||'[]')}catch{return []}})
 const [query,setQuery] = useState('')
 const [chatOpen,setChatOpen] = useState(false)
 const [chatText,setChatText] = useState('')
 const [chatMessages,setChatMessages] = useState<{role:string,text:string,products?:Product[]}[]>([{role:'assistant',text:'Welcome to NOVA. Tell me what you are looking for and I’ll help you discover it.'}])
 const [user,setUser] = useState<{name:string,email:string,role:string}|null>(()=>{try{return JSON.parse(localStorage.getItem('nova-user')||'null')}catch{return null}})
 const [mobileMenu,setMobileMenu] = useState(false)
 const navigate = useNavigate()
 useEffect(()=>{useCartStore.getState().hydrate();useAuthStore.getState().hydrate()},[])
 useEffect(()=>{localStorage.setItem('nova-theme',dark?'dark':'light');document.documentElement.dataset.theme=dark?'dark':'light'},[dark])
 useEffect(()=>{localStorage.setItem('nova-wishlist',JSON.stringify(wishlist))},[wishlist])
 
 const addToCart=(p:Product,size?:string)=>addCartItem(p,size)
 const toggleWish=(id:number)=>setWishlist(old=>old.includes(id)?old.filter(x=>x!==id):[...old,id])
 const submitSearch=(e:React.FormEvent)=>{e.preventDefault();navigate(`/search?q=${encodeURIComponent(query)}`)}
 const sendChat=async()=>{const q=chatText.trim();if(!q)return;setChatMessages(m=>[...m,{role:'user',text:q}]);setChatText('');const result=await getAssistantReply(products,q);setChatMessages(m=>[...m,{role:'assistant',text:result.reply,products:result.products}])}
 const shell={products,cart,setCart,addToCart,wishlist,toggleWish,user,setUser}
 return <div className="app-shell">
  <div className="announcement"><span>THE NOVA EDIT</span><span>Objects for a more considered everyday</span><span>Explore the collection <ArrowUpRight size={12}/></span></div>
  <header className="header">
   <button className="icon-button mobile-only" onClick={()=>setMobileMenu(!mobileMenu)} aria-label="Menu">{mobileMenu?<X/>:<Menu/>}</button>
   <Link to="/" className="wordmark"><span className="mark">N</span>OVA<span className="wordmark-dot">.</span></Link>
   <nav className={mobileMenu?'nav nav-open':'nav'}><Link to="/" onClick={()=>setMobileMenu(false)}>Discover</Link><Link to="/category/footwear" onClick={()=>setMobileMenu(false)}>Footwear</Link><Link to="/category/electronics" onClick={()=>setMobileMenu(false)}>Technology</Link><Link to="/category/home-and-living" onClick={()=>setMobileMenu(false)}>Living</Link><Link to="/category/novels" onClick={()=>setMobileMenu(false)}>Novels</Link><Link to="/category/textbooks" onClick={()=>setMobileMenu(false)}>Textbooks</Link><Link to="/search?q=new" onClick={()=>setMobileMenu(false)}>The Edit</Link>{user?.role==='ADMIN'&&<Link to="/admin" onClick={()=>setMobileMenu(false)}>Admin</Link>}</nav>
   <form className="header-search" onSubmit={submitSearch}><Search size={16}/><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search objects, brands, ideas…"/><kbd>↵</kbd></form>
   <div className="header-actions">
    <button className="icon-button theme-switch" onClick={()=>setDark(!dark)} title={dark?'Switch to light mode':'Switch to dark mode'} aria-label="Toggle theme">{dark?<Sun size={18}/>:<Moon size={18}/>}</button>
    <Link className="icon-button" to="/wishlist" aria-label="Wishlist"><Heart size={18}/>{wishlist.length>0&&<i className="count">{wishlist.length}</i>}</Link>
    <Link className="icon-button" to="/cart" aria-label="Cart"><ShoppingBag size={18}/>{cart.reduce((n,x)=>n+x.qty,0)>0&&<i className="count">{cart.reduce((n,x)=>n+x.qty,0)}</i>}</Link>
    <Link className="avatar" to={user?'/account':'/login'} aria-label="Account">{user?user.name.slice(0,1).toUpperCase():<UserRound size={17}/>}</Link>
   </div>
  </header>
  <main><Routes>
   <Route path="/" element={<Home products={products} toggleWish={toggleWish} wishlist={wishlist}/>}/>
   <Route path="/search" element={<Listing products={products} toggleWish={toggleWish} wishlist={wishlist}/>}/>
   <Route path="/category/:slug" element={<Listing products={products} toggleWish={toggleWish} wishlist={wishlist}/>}/>
   <Route path="/product/:id" element={<ProductPage {...shell} products={products} addToCart={addToCart} wishlist={wishlist} toggleWish={toggleWish}/>}/>
   <Route path="/cart" element={<Cart {...shell}/>}/>
   <Route path="/checkout" element={<Checkout {...shell}/>}/>
   <Route path="/login" element={<Auth setUser={setUser}/>}/>
   <Route path="/register" element={<Auth setUser={setUser} register/>}/>
   <Route path="/account" element={<Account {...shell}/>}/>
   <Route path="/account/orders" element={<Account {...shell}/>}/>
   <Route path="/wishlist" element={<Wishlist products={products} wishlist={wishlist} toggleWish={toggleWish}/>}/>
   <Route path="/admin" element={<Admin products={products} user={user} refreshProducts={refreshProducts}/>}/>
  </Routes></main>
  <footer className="footer"><div className="footer-top"><Link to="/" className="wordmark"><span className="mark">N</span>OVA<span className="wordmark-dot">.</span></Link><p>Objects for a more considered everyday.</p><form onSubmit={e=>{e.preventDefault();alert('You are on the NOVA updates list (demo).')}} className="newsletter"><input type="email" placeholder="Your email address"/><button className="button button-gold">Join the edit <ArrowUpRight size={14}/></button></form></div><div className="footer-bottom"><span>© 2026 NOVA Studio</span><span>Thoughtfully selected. Made to last.</span><div><Link to="/search?q=all">Shop all</Link><Link to="/account">Your account</Link><button onClick={()=>setDark(!dark)}>{dark?'Light':'Dark'} mode</button></div></div></footer>
  <button className="assistant-launcher" onClick={()=>setChatOpen(!chatOpen)} aria-label="Open NOVA assistant"><span className="assistant-orb"><Sparkles size={19}/></span><span>Ask NOVA</span><span className="online-dot"/></button>
  {chatOpen&&<section className="chat-panel"><div className="chat-head"><span className="assistant-orb"><Sparkles size={17}/></span><div><b>NOVA Assistant</b><small>Product discovery · Online</small></div><button className="icon-button" onClick={()=>setChatOpen(false)}><X size={17}/></button></div><div className="chat-body">{chatMessages.map((m,i)=><div key={i}><div className={'chat-message '+m.role}>{m.text}</div>{m.products?.map(p=><Link className="chat-product" to={`/product/${p.id}`} key={p.id} onClick={()=>setChatOpen(false)}><img src={p.imageUrl} alt=""/><span>{p.name}<small>{money(p.price)}</small></span><ArrowUpRight size={13}/></Link>)}</div>)}</div><form className="chat-input" onSubmit={e=>{e.preventDefault();sendChat()}}><input value={chatText} onChange={e=>setChatText(e.target.value)} placeholder="What are you looking for?"/><button className="send-button"><Send size={16}/></button></form><small className="chat-note">Catalogue-backed recommendations · AI responses available when configured</small></section>}
 </div>
}

export default App
