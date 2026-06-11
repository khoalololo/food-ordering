# FoodOrder

A full-stack online food ordering system built with **Spring Boot** and vanilla **HTML/CSS/JS**. Built as a university project demonstrating GoF design patterns in a real working application.

---

## Features

| Role | Capabilities |
|---|---|
| **Customer** | Browse menu, customise with toppings, cart, checkout, payment, live order tracking, notifications |
| **Staff** | Kanban board of all orders, advance/cancel status, auto-refresh, detail view |
| **Manager** | Everything above + food CRUD, staff management, role assignment |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17 · Spring Boot 3 · Spring Security · Spring Data JPA |
| Database | PostgreSQL (Supabase) |
| Auth | JWT (JJWT 0.12) |
| Frontend | Vanilla HTML/CSS/JS · served as Thymeleaf templates |
| Build | Maven |

---

## Design Patterns

| Pattern | Where |
|---|---|
| **Strategy** | `PaymentStrategy` → `CashPaymentStrategy`, `BankPaymentStrategy`, `MomoPaymentStrategy` |
| **Observer** | Spring `ApplicationEvent` + `@EventListener` — `OrderService` publishes events, `NotificationService` listens |
| **State** | Order status machine in `OrderService.advanceStatus()` — `PENDING → CONFIRMED → PREPARING → READY → COMPLETED` |
| **Singleton** | Spring manages all `@Service` and `@Component` beans as singletons |
| **Facade** | `OrderService` hides the complexity of creating items, calculating totals, and firing notifications behind one `createOrder()` call |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/example/food_ordering/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java       # JWT filter chain, CORS, route guards
│   │   │   ├── JwtAuthFilter.java        # Reads Bearer token on every request
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── controller/
│   │   │   ├── AuthController.java       # POST /api/auth/register, /login
│   │   │   ├── FoodController.java       # GET /api/food, POST/PATCH/DELETE (manager)
│   │   │   ├── OrderController.java      # CRUD + /my, /all, /advance, /cancel
│   │   │   ├── PaymentController.java    # POST /api/payment
│   │   │   ├── NotificationController.java
│   │   │   ├── UserController.java       # Manager-only user/role management
│   │   │   └── ViewController.java       # Serves HTML templates at clean URLs
│   │   ├── dto/
│   │   │   ├── request/                  # Validated inbound payloads
│   │   │   └── response/                 # Outbound safe views (no passwords)
│   │   ├── entity/                       # JPA entities → DB tables
│   │   │   ├── User, Food, Order, OrderItem, Payment, Notification
│   │   ├── enums/
│   │   │   ├── Role                      # CUSTOMER, STAFF, MANAGER, ADMIN
│   │   │   ├── OrderStatus               # PENDING → CONFIRMED → PREPARING → READY → COMPLETED / CANCELLED
│   │   │   ├── PaymentStatus             # PENDING, SUCCESS, FAILED, REFUNDED
│   │   │   └── PaymentMethod             # CASH, BANK, MOMO
│   │   ├── repository/                   # Spring Data JPA interfaces
│   │   ├── service/
│   │   │   ├── AuthService.java          # register, login → returns JWT + user
│   │   │   ├── FoodService.java
│   │   │   ├── OrderService.java         # createOrder, advanceStatus, cancelOrder + events
│   │   │   ├── PaymentService.java       # delegates to PaymentStrategy
│   │   │   ├── NotificationService.java  # @EventListener — handles all order/payment events
│   │   │   ├── UserService.java          # getAll, update, updateRole
│   │   │   ├── UserDetailsServiceImpl.java
│   │   │   ├── payment/                  # Strategy pattern implementations
│   │   │   │   ├── PaymentStrategy.java  # interface
│   │   │   │   ├── CashPaymentStrategy.java
│   │   │   │   ├── BankPaymentStrategy.java
│   │   │   │   └── MomoPaymentStrategy.java
│   │   │   └── *Event.java               # OrderCreatedEvent, OrderStatusChangedEvent, etc.
│   │   └── util/
│   │       └── JwtUtil.java              # generate, extractEmail, isValid
│   └── resources/
│       ├── application.properties
│       ├── static/
│       │   ├── css/shared.css            # Design system — tokens, components, layout
│       │   └── js/shared.js              # API client, Auth, Cart, toast, nav
│       └── templates/
│           ├── index.html                # Smart redirect based on role
│           ├── 404.html
│           ├── auth/
│           │   ├── signin.html
│           │   └── signup.html
│           ├── customer/
│           │   ├── menu.html             # Food grid, filters, topping modal
│           │   ├── cart.html             # Cart with quantity controls
│           │   ├── payment.html          # Method selection + order summary
│           │   ├── orders.html           # Order history with status tabs
│           │   ├── tracking.html         # Live stepper with 5s polling
│           │   └── notifications.html    # User + kitchen notification tabs
│           ├── staff/
│           │   └── staff-orders.html     # Kanban board + table view, auto-refresh
│           └── manager/
│               ├── manager-foods.html    # Food grid with CRUD modals
│               └── manager-staff.html    # User cards with role management
```

---

## Getting Started

### Prerequisites
- Java 17+
- Maven
- PostgreSQL (or a free [Supabase](https://supabase.com) project)

### 1. Clone and configure

```bash
git clone https://github.com/your-username/food-ordering.git
cd food-ordering
```

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://YOUR_HOST:5432/YOUR_DB
spring.datasource.username=YOUR_USER
spring.datasource.password=YOUR_PASSWORD

jwt.secret=your-secret-key-must-be-at-least-32-characters
jwt.expiration=86400000
```

### 2. Run

```bash
mvn spring-boot:run
```

The app starts at `http://localhost:9090`.

Spring will create all tables automatically on first run (`ddl-auto=update`).

### 3. Default manager account

A default manager is seeded on startup:

```
Email:    manager@food.com
Password: admin123
```

Use this to log in as manager, create foods, and promote users to staff.

---

## API Overview

All endpoints are prefixed with `/api`.

### Auth
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Create customer account |
| POST | `/auth/login` | Public | Returns `{ token, user }` |

### Food
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/food` | Public | All available foods |
| GET | `/food/all` | Manager | All foods including unavailable |
| POST | `/food` | Manager | Create food |
| PATCH | `/food/{id}` | Manager | Update food |
| DELETE | `/food/{id}` | Manager | Delete food |

### Orders
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/order` | Customer | Create order from cart items |
| GET | `/order/my` | Customer | Own order history |
| GET | `/order/all` | Staff/Manager | All orders |
| GET | `/order/{id}` | Authenticated | Single order |
| PATCH | `/order/{id}/advance` | Staff/Manager | Move to next status |
| PATCH | `/order/{id}/cancel` | Owner or Staff | Cancel order |

### Payment
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/payment` | Customer | Process payment (CASH / BANK / MOMO) |

### Notifications
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/notification/my` | Authenticated | User's own notifications |
| GET | `/notification/kitchen` | Staff/Manager | Kitchen notifications |
| PATCH | `/notification/{id}/read` | Authenticated | Mark one as read |
| PATCH | `/notification/read-all` | Authenticated | Mark all as read |

### Users (Manager only)
| Method | Path | Description |
|---|---|---|
| GET | `/user/all` | All users |
| GET | `/user/{id}` | Single user |
| PATCH | `/user/{id}` | Update profile |
| PATCH | `/user/{id}/role` | Change role |

---

## Frontend Routes

Served by `ViewController.java` at clean URLs (no `.html` extension):

| URL | Template | Who sees it |
|---|---|---|
| `/` | `index.html` | Everyone — redirects by role |
| `/signin` | `auth/signin.html` | Public |
| `/signup` | `auth/signup.html` | Public |
| `/customer/menu` | `customer/menu.html` | Customer |
| `/customer/cart` | `customer/cart.html` | Customer |
| `/customer/payment` | `customer/payment.html` | Customer |
| `/customer/orders` | `customer/orders.html` | Customer |
| `/customer/tracking` | `customer/tracking.html` | Customer |
| `/customer/notifications` | `customer/notifications.html` | Customer |
| `/staff/orders` | `staff/staff-orders.html` | Staff + Manager |
| `/manager/foods` | `manager/manager-foods.html` | Manager |
| `/manager/staff` | `manager/manager-staff.html` | Manager |

---

## How the Frontend Works

There are no frameworks. Every page loads two shared files:

- **`/css/shared.css`** — design tokens (colors, spacing, radius), all reusable components (buttons, cards, modals, badges, tables, toasts)
- **`/js/shared.js`** — everything the pages need:
  - `Auth` — reads/writes JWT and user from `localStorage`
  - `api()` / `GET` / `POST` / `PATCH` / `DELETE` — fetch wrapper that injects the Bearer token automatically and redirects to `/signin` on 401
  - `Cart` — localStorage-backed cart with add, update, remove, totals
  - `toast()` — notification system
  - `renderNav()` — builds the correct navbar based on role
  - `requireAuth()` — route guard, redirects if not logged in or wrong role
  - `fmt` — currency, date, status label, badge HTML helpers

Each page then has its own `<script>` block that calls the API and builds the UI.

---

## Order Flow

```
Customer adds items to cart (localStorage)
        ↓
POST /api/order  →  OrderService.createOrder()
        ↓               ↓
    Order saved    OrderCreatedEvent published
                        ↓
               NotificationService listens
                   saves USER + KITCHEN notifications
        ↓
Customer selects payment method
        ↓
POST /api/payment  →  PaymentService.process()
        ↓                    ↓
   Payment saved       PaymentStrategy.pay()  ← Strategy Pattern
                             ↓
                    PaymentSuccessEvent published
                             ↓
                   NotificationService listens
                       saves USER + KITCHEN notifications
        ↓
Staff advances order through statuses
PENDING → CONFIRMED → PREPARING → READY → COMPLETED
        ↓
Customer tracking page polls GET /api/order/{id} every 5 seconds
and updates the stepper UI in real time
```

---

## Known Limitations

- No real payment gateway integration — strategies generate mock transaction IDs
- Cart lives in `localStorage` only — not persisted server-side between devices
- No image upload — food images are external URLs
- No email notifications — only in-app
- Polling-based tracking instead of WebSockets

---

## License

MIT
