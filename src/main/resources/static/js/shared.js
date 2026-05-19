// ── Config ──────────────────────────────────────────
const API = 'http://localhost:8080/api';

// ── Token helpers ────────────────────────────────────
const Auth = {
  get token()   { return localStorage.getItem('token'); },
  get user()    { try { return JSON.parse(localStorage.getItem('user')); } catch { return null; } },
  set(token, user) { localStorage.setItem('token', token); localStorage.setItem('user', JSON.stringify(user)); },
  clear()       { localStorage.removeItem('token'); localStorage.removeItem('user'); },
  isLoggedIn()  { return !!this.token; },
  role()        { return this.user?.role || null; },
  redirect(path){ window.location.href = path; },
};

// ── API client ───────────────────────────────────────
async function api(method, path, body) {
  const headers = { 'Content-Type': 'application/json' };
  if (Auth.token) headers['Authorization'] = 'Bearer ' + Auth.token;

  const res = await fetch(API + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401) { Auth.clear(); Auth.redirect('/signin.html'); return; }

  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = text; }

  if (!res.ok) throw new Error(data?.message || data || `Error ${res.status}`);
  return data;
}

const GET    = (path)       => api('GET',    path);
const POST   = (path, body) => api('POST',   path, body);
const PATCH  = (path, body) => api('PATCH',  path, body);
const DELETE = (path)       => api('DELETE', path);

// ── Toast ────────────────────────────────────────────
function toast(msg, type = 'info', duration = 3500) {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }
  const icons = { success: '✓', error: '✕', info: 'ℹ' };
  const t = document.createElement('div');
  t.className = `toast ${type}`;
  t.innerHTML = `<span style="font-weight:700;font-size:1rem">${icons[type]||'•'}</span><span>${msg}</span>`;
  container.appendChild(t);
  setTimeout(() => { t.style.opacity='0'; t.style.transform='translateX(20px)'; t.style.transition='all 0.3s'; setTimeout(()=>t.remove(), 300); }, duration);
}

// ── Modal helpers ────────────────────────────────────
function openModal(id) { document.getElementById(id)?.classList.add('open'); }
function closeModal(id) { document.getElementById(id)?.classList.remove('open'); }
function closeAllModals() { document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('open')); }

// Close modal on overlay click
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-overlay')) closeAllModals();
});

// ── Format helpers ───────────────────────────────────
const fmt = {
  currency: n => new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND', maximumFractionDigits:0 }).format(n),
  date:     d => d ? new Intl.DateTimeFormat('vi-VN', { day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' }).format(new Date(d)) : '—',
  status:   s => ({ PENDING:'Pending', CONFIRMED:'Confirmed', PREPARING:'Preparing', READY:'Ready', COMPLETED:'Completed', CANCELLED:'Cancelled', SUCCESS:'Success', FAILED:'Failed', REFUNDED:'Refunded' })[s] || s,
  badge:    s => `<span class="badge badge-${s?.toLowerCase()}">${fmt.status(s)}</span>`,
};

// ── Route guard ──────────────────────────────────────
function requireAuth(allowedRoles) {
  if (!Auth.isLoggedIn()) { Auth.redirect('/signin.html'); return false; }
  if (allowedRoles && !allowedRoles.includes(Auth.role())) {
    toast('Access denied', 'error');
    Auth.redirect('/menu.html');
    return false;
  }
  return true;
}

// ── Cart (localStorage) ──────────────────────────────
const Cart = {
  _key: 'cart',
  get items() { try { return JSON.parse(localStorage.getItem(this._key)) || []; } catch { return []; } },
  save(items) { localStorage.setItem(this._key, JSON.stringify(items)); Cart.updateBadge(); },
  add(food, qty, toppings = []) {
    const items = this.items;
    const key = food.id + ':' + toppings.join(',');
    const ex = items.find(i => i.key === key);
    if (ex) { ex.quantity += qty; ex.subtotal = ex.unitPrice * ex.quantity; }
    else items.push({ key, foodId: food.id, name: food.name, type: food.type, unitPrice: food.basePrice, quantity: qty, toppings, subtotal: food.basePrice * qty });
    this.save(items);
  },
  remove(key) { this.save(this.items.filter(i => i.key !== key)); },
  update(key, qty) {
    const items = this.items;
    const i = items.find(i => i.key === key);
    if (i) { if (qty <= 0) return this.remove(key); i.quantity = qty; i.subtotal = i.unitPrice * qty; this.save(items); }
  },
  clear() { localStorage.removeItem(this._key); Cart.updateBadge(); },
  total() { return this.items.reduce((s, i) => s + i.subtotal, 0); },
  count() { return this.items.reduce((s, i) => s + i.quantity, 0); },
  updateBadge() {
    const b = document.getElementById('cart-badge');
    if (b) { const c = Cart.count(); b.textContent = c; b.style.display = c > 0 ? 'inline-flex' : 'none'; }
  },
};

// ── Nav render ───────────────────────────────────────
function renderNav(activePage) {
  const user = Auth.user;
  const role = Auth.role();

  const customerLinks = [
    { href: 'menu.html',          label: 'Menu' },
    { href: 'cart.html',          label: 'Cart', badgeId: 'cart-badge' },
    { href: 'orders.html',        label: 'My Orders' },
    { href: 'notifications.html', label: 'Notifications' },
  ];
  const staffLinks = [
    { href: 'staff-orders.html',  label: 'Orders' },
    { href: 'notifications.html', label: 'Notifications' },
  ];
  const managerLinks = [
    { href: 'manager-foods.html', label: 'Foods' },
    { href: 'manager-staff.html', label: 'Staff' },
    { href: 'staff-orders.html',  label: 'Orders' },
  ];

  let links = customerLinks;
  if (role === 'STAFF')   links = staffLinks;
  if (role === 'MANAGER') links = managerLinks;

  const nav = document.getElementById('main-nav');
  if (!nav) return;

  nav.innerHTML = `
    <a class="nav-brand" href="${role === 'STAFF' || role === 'MANAGER' ? 'staff-orders.html' : 'menu.html'}">FoodOrder</a>
    <div class="nav-links">
      ${links.map(l => `
        <a href="${l.href}" class="nav-link ${activePage === l.href ? 'active' : ''}">
          ${l.label}${l.badgeId ? `<span class="nav-badge" id="${l.badgeId}" style="display:none">0</span>` : ''}
        </a>
      `).join('')}
      ${user ? `
        <span style="color:var(--muted);font-size:0.8rem;padding:0 0.5rem">${user.fullName || user.email}</span>
        <button class="btn btn-ghost btn-sm" onclick="signout()">Sign out</button>
      ` : `<a href="signin.html" class="btn btn-primary btn-sm">Sign in</a>`}
    </div>
  `;
  Cart.updateBadge();
}

function signout() { Auth.clear(); Cart.clear(); Auth.redirect('/signin.html'); }