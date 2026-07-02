// Config
const API = '/api';

// Role helper
function normalizeRole(role) {
  return String(role || '')
    .replace('ROLE_', '')
    .toUpperCase();
}

// Token helpers
const Auth = {
  get token() {
    return localStorage.getItem('token');
  },

  get user() {
    try {
      return JSON.parse(localStorage.getItem('user'));
    } catch {
      return null;
    }
  },

  set(token, user) {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
  },

  clear() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  isLoggedIn() {
    return !!this.token;
  },

  role() {
    return normalizeRole(this.user?.role);
  },

  redirect(path) {
    window.location.href = path;
  },
};

// API client
async function api(method, path, body) {
  const headers = {
    'Content-Type': 'application/json'
  };

  if (Auth.token) {
    headers.Authorization = 'Bearer ' + Auth.token;
  }

  const res = await fetch(API + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401) {
    Auth.clear();
    Auth.redirect('/signin');
    return;
  }

  const text = await res.text();

  let data;
  try {
    data = JSON.parse(text);
  } catch {
    data = text;
  }

  if (!res.ok) {
    let message = `Error ${res.status}`;

    if (data && typeof data === 'object' && data.message) {
      message = data.message;
    } else if (typeof data === 'string' && data) {
      message = data;
    }

    throw new Error(message);
  }

  return data;
}

const GET = path => api('GET', path);
const POST = (path, body) => api('POST', path, body);
const PATCH = (path, body) => api('PATCH', path, body);
const DELETE = path => api('DELETE', path);

// Toast
function toast(msg, type = 'info', duration = 3500) {
  let container = document.getElementById('toast-container');

  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const item = document.createElement('div');
  item.className = `toast ${type}`;
  item.innerHTML = `<span>${msg}</span>`;

  container.appendChild(item);

  setTimeout(() => {
    item.style.opacity = '0';
    item.style.transform = 'translateX(20px)';
    item.style.transition = 'all 0.3s';

    setTimeout(() => item.remove(), 300);
  }, duration);
}

// Modal helpers
function openModal(id) {
  document.getElementById(id)?.classList.add('open');
}

function closeModal(id) {
  document.getElementById(id)?.classList.remove('open');
}

function closeAllModals() {
  document
    .querySelectorAll('.modal-overlay')
    .forEach(modal => modal.classList.remove('open'));
}

document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-overlay')) {
    closeAllModals();
  }
});

// Format helpers
const fmt = {
  currency: n => new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(Number(n || 0)),

  date: d => d
    ? new Intl.DateTimeFormat('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }).format(new Date(d))
    : '—',

  time: d => d
    ? new Intl.DateTimeFormat('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }).format(new Date(d))
    : '—',

  status: s => ({
    PENDING: 'Pending',
    CONFIRMED: 'Confirmed',
    PREPARING: 'Preparing',
    READY: 'Ready',
    COMPLETED: 'Completed',
    CANCELLED: 'Cancelled',
    SUCCESS: 'Success',
    FAILED: 'Failed',
    REFUNDED: 'Refunded'
  })[s] || s,

  badge: s => `<span class="badge badge-${String(s || '').toLowerCase()}">${fmt.status(s)}</span>`,
};

// Route guard
function requireAuth(allowedRoles) {
  if (!Auth.isLoggedIn()) {
    Auth.redirect('/signin');
    return false;
  }

  const role = Auth.role();

  if (allowedRoles && !allowedRoles.map(normalizeRole).includes(role)) {
    toast('Access denied', 'error');

    if (role === 'MANAGER') {
      Auth.redirect('/manager/foods');
    } else if (role === 'STAFF') {
      Auth.redirect('/staff/orders');
    } else {
      Auth.redirect('/customer/menu');
    }

    return false;
  }

  return true;
}

// Cart localStorage
const Cart = {
  _key: 'cart',

  get items() {
    try {
      return JSON.parse(localStorage.getItem(this._key)) || [];
    } catch {
      return [];
    }
  },

  save(items) {
    localStorage.setItem(this._key, JSON.stringify(items));
    Cart.updateBadge();
  },

  add(food, qty, toppings = []) {
    const items = this.items;
    const key = food.id + ':' + toppings.join(',');

    const existing = items.find(item => item.key === key);

    if (existing) {
      existing.quantity += qty;
      existing.subtotal = existing.unitPrice * existing.quantity;
    } else {
      items.push({
        key,
        foodId: food.id,
        name: food.name,
        type: food.type,
        unitPrice: food.basePrice,
        quantity: qty,
        toppings,
        subtotal: food.basePrice * qty
      });
    }

    this.save(items);
  },

  remove(key) {
    this.save(this.items.filter(item => item.key !== key));
  },

  update(key, qty) {
    const items = this.items;
    const item = items.find(item => item.key === key);

    if (!item) return;

    if (qty <= 0) {
      this.remove(key);
      return;
    }

    item.quantity = qty;
    item.subtotal = item.unitPrice * qty;

    this.save(items);
  },

  clear() {
    localStorage.removeItem(this._key);
    Cart.updateBadge();
  },

  total() {
    return this.items.reduce((sum, item) => sum + item.subtotal, 0);
  },

  count() {
    return this.items.reduce((sum, item) => sum + item.quantity, 0);
  },

  updateBadge() {
    const badge = document.getElementById('cart-badge');
    if (!badge) return;

    const count = Cart.count();

    badge.textContent = count;
    badge.style.display = count > 0 ? 'inline-flex' : 'none';
  },
};

// Nav render
function renderNav(activePage) {
  const user = Auth.user;
  const role = Auth.role();

  const customerLinks = [
    { href: '/customer/menu', label: 'Menu' },
    { href: '/customer/cart', label: 'Cart', badgeId: 'cart-badge' },
    { href: '/customer/orders', label: 'My Orders' },
    { href: '/customer/notifications', label: 'Notifications' },
  ];

  const staffLinks = [
    { href: '/staff/orders', label: 'Orders' },
    { href: '/customer/notifications', label: 'Notifications' },
  ];

  const managerLinks = [
    { href: '/manager/foods', label: 'Foods' },
    { href: '/manager/staff', label: 'Staff' },
    { href: '/staff/orders', label: 'Orders' },
  ];

  let links = customerLinks;
  let homeHref = '/customer/menu';

  if (role === 'STAFF') {
    links = staffLinks;
    homeHref = '/staff/orders';
  }

  if (role === 'MANAGER') {
    links = managerLinks;
    homeHref = '/manager/foods';
  }

  const nav = document.getElementById('main-nav');
  if (!nav) return;

  nav.innerHTML = `
    <a class="nav-brand" href="${homeHref}">KhoaFastFood</a>

    <div class="nav-links">
      ${links.map(link => `
        <a href="${link.href}" class="nav-link ${activePage === link.href ? 'active' : ''}">
          ${link.label}
          ${link.badgeId ? `<span class="nav-badge" id="${link.badgeId}" style="display:none">0</span>` : ''}
        </a>
      `).join('')}

      ${user ? `
        <span style="color:var(--muted);font-size:0.8rem;padding:0 0.5rem">
          ${user.fullName || user.email}
        </span>

        <button class="btn btn-ghost btn-sm" onclick="signout()">
          Sign out
        </button>
      ` : `
        <a href="/signin" class="btn btn-primary btn-sm">
          Sign in
        </a>
      `}
    </div>
  `;

  Cart.updateBadge();
}

function signout() {
  Auth.clear();
  Cart.clear();
  Auth.redirect('/signin');
}

// Expose helpers globally
window.Auth = Auth;
window.GET = GET;
window.POST = POST;
window.PATCH = PATCH;
window.DELETE = DELETE;
window.toast = toast;
window.openModal = openModal;
window.closeModal = closeModal;
window.closeAllModals = closeAllModals;
window.fmt = fmt;
window.Cart = Cart;
window.renderNav = renderNav;
window.requireAuth = requireAuth;
window.signout = signout;