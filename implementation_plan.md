# ASK — Final Implementation Plan (All Clarifications Resolved)

## Decisions Locked In

| # | Decision | Resolution |
|---|----------|------------|
| 1 | 2FA | Email OTP only. Mandatory for SUPER_ADMIN & SYSTEM_ADMIN. Optional for STATE_ADMIN & DISTRICT_ADMIN. Not available for store-level roles. |
| 2 | Login method | Email + password only. No phone OTP login. |
| 3 | Providers | Email: JavaMailSender + Gmail SMTP. SMS: MSG91. WhatsApp: skipped entirely. All behind interfaces. |
| 4 | Aadhaar | Store last 4 digits only. Reject full 12-digit with 400. Photo upload for manual admin review. |
| 5 | Commission | Tracking & reporting only. No money movement. External payouts. |
| 6 | Health Card | Digital QR code + printable PDF card. No physical card integration. |
| 7 | Store assignment | 1:1 strictly. One store per Receptionist/Volunteer. Reassigned by Block Admin+. |
| 8 | Sessions | Multiple sessions allowed. User can view & revoke sessions. |
| 9 | Return window | 7 days default, configurable by Super Admin via system settings. |
| 10 | Auto-reports | Skipped for now. Manual generation only. Scheduled reports in later phase. |
| 11 | Bill format | `ASK-[STORE_CODE]-[YYYYMMDD]-[4 digit seq]`. Resets daily per store. |
| 12 | Password expiry | None. Forced change on first login with temp password only. |
| 13 | Multi-language | English only. Use string constants for future i18n readiness. No i18n library. |
| 14 | Data backup | Out of scope. Documented in README for infra team. |

---

## Phase Plan (6 Phases)

### Phase 1: Foundation, Auth & Geographic Hierarchy
**Backend:**
- Spring Boot project scaffolding with all dependencies (pom.xml)
- Flyway migrations for: `states`, `districts`, `blocks`, `stores`, `roles`, `permissions`, `users`, `user_permissions`, `refresh_tokens`, `user_sessions`, `audit_logs`, `system_config`
- Seed data: 3 states, roles, default permissions, Super Admin account, system config defaults
- `ApiResponse<T>` wrapper, `PageResponse<T>`, `GlobalExceptionHandler`
- Constants: `AppConstants`, `ErrorMessages`, `ApiPaths`, `RoleConstants`
- Security: JWT (access + refresh tokens), `JwtTokenProvider`, `JwtAuthenticationFilter`, `CustomUserDetailsService`, `SecurityConfig`
- BCrypt 12 password hashing
- Account lockout (5 failed attempts → 30 min lock)
- Rate limiting on auth endpoints
- Email OTP 2FA (mandatory SUPER_ADMIN/SYSTEM_ADMIN, optional STATE/DISTRICT_ADMIN)
- Session management: multiple sessions allowed, list/revoke sessions API
- Forced password change on first login (temp password flag)
- Geographic hierarchy CRUD APIs (State, District, Block, Store)
- Geographic scope enforcement at query layer
- Audit logging service (async)
- HikariCP config (pool 20, idle 5, timeout 30s)
- `application.properties`, `application-dev.properties`, `application-prod.properties`

**Frontend:**
- Vite + React 19 project setup
- Tailwind CSS 4 + Shadcn UI setup
- Folder structure (api/, components/, constants/, context/, hooks/, pages/, store/, utils/, styles/)
- Axios instance with interceptors (auth token, 401 refresh, error handling)
- `AuthContext`, `useAuth` hook, `usePermission` hook
- Login page with email + password
- 2FA OTP verification page
- First-login forced password change page
- `ProtectedRoute`, `RoleGuard` components
- Layout: `Sidebar`, `Navbar`, `PageWrapper`
- Error pages: 403, 404, 500
- Common components: `Button`, `Input`, `Modal`, `Loader`, `ErrorBoundary`, `SkeletonLoader`, `PageHeader`, `EmptyState`, `StatusBadge`, `ConfirmDialog`, `Pagination`, `Badge`
- Theme setup (light/dark mode, color system)
- `.env.development`, `.env.production`, `.env.example`
- Geographic hierarchy management pages (CRUD for States, Districts, Blocks, Stores) — Super Admin only

**Why first:** Everything depends on auth, users existing in DB, geography, and permissions infrastructure.

---

### Phase 2: User Management & Permissions
**Backend:**
- User CRUD APIs (create, view, edit, deactivate, reactivate)
- Role hierarchy enforcement (can only manage roles below own)
- Geographic scope on all user queries
- Permission assignment APIs (checkbox grant/revoke)
- Permission request workflow APIs (request → notify → approve/reject)
- User profile APIs (photo upload, KYC document upload, bank details — AES-256 encrypted)
- Aadhaar validation (reject full 12-digit, accept last 4 only)
- Verification workflow APIs (pending → admin review → approved/rejected)
- User activity log query API
- Notification entity + APIs (in-app notifications for permission requests, verification)

**Frontend:**
- User list page (searchable, filterable, sortable, paginated)
- User creation multi-step form (personal details → role + geography → permissions)
- User detail/edit page
- Deactivate/reactivate with confirmation dialog
- Permission management panel (checkbox matrix)
- Permission request form + request list
- User profile page (photo, KYC upload, bank details)
- Verification queue page (Super Admin/System Admin)
- Notification bell + dropdown in navbar
- Role-specific sidebar (show only permitted menu items)
- Session management page (view/revoke active sessions)

---

### Phase 3: Product Catalogue & Inventory
**Backend:**
- Product category CRUD
- Product catalogue CRUD (name, brand, category, HSN, MRP, ASK price, GST, min threshold)
- Central stock receipt APIs (Pharmacist logs items: batch, expiry, qty)
- Transfer order APIs (create, list, confirm receipt, cancel)
- Store stock tracking APIs (current levels per store, geo-scoped)
- Stock request APIs (receptionist → Super Admin/Pharmacist)
- Low-stock auto-alert (check threshold on stock change)
- Expiry tracking (flag items expiring in 30/60/90 days)
- Stock adjustment APIs (damage, return, correction — with reason + audit)
- Store management APIs (profile, hours, staff assignment, performance)
- Email notification service (JavaMailSender + Gmail SMTP) wired for stock alerts
- SMS service interface + MSG91 implementation (for future use)

**Frontend:**
- Product catalogue pages (list, create, edit, detail)
- Central stock receipt page (Pharmacist view)
- Transfer order creation + management pages
- Store stock view (per-store, geo-scoped)
- Stock request form + request list
- Low stock alerts dashboard widget
- Expiry tracking page (30/60/90 day filters)
- Stock adjustment form
- Store management pages (list, create, edit, detail, staff assignment)

---

### Phase 4: Patients, Health Cards & Billing
**Backend:**
- Patient CRUD APIs (single entry)
- Bulk patient upload API (CSV parsing, duplicate detection by phone)
- Patient search API (name, phone, card ID)
- Duplicate prevention logic
- Hospital/clinic partner CRUD APIs
- Health card issuance API (generate unique ID + QR code)
- Health card PDF generation (QR, patient name, card ID, store details)
- Family member management APIs (add up to 5 members per card)
- Bill creation API (add items → calculate MRP/ASK/GST/discount → confirm)
- Auto stock reduction on bill confirm
- Bill number generation: `ASK-[STORE_CODE]-[YYYYMMDD]-[SEQ]` with daily reset
- Scheme/discount management APIs (Super Admin creates, auto-applied at billing)
- Bill management APIs (view, cancel with reason, PDF download)
- Product return/refund APIs (7-day configurable window, approval workflow)
- Commission entry auto-creation on bill confirm
- System config API for return window + other settings

**Frontend:**
- Patient list page (searchable, filterable, paginated, geo-scoped)
- Patient registration form
- Bulk upload page (CSV upload + preview + validation)
- Patient detail page (profile, health card, purchase history)
- Hospital partner management pages
- Health card issuance flow (multi-step)
- Health card detail page (members, QR, download PDF)
- POS billing page (search patient → add items → preview → confirm)
- Bill list page (filterable by date, store, status)
- Bill detail page + PDF download/print
- Return/refund form
- Scheme management pages (Super Admin)
- System settings page (return window, other config)

---

### Phase 5: Commission, Reports & Dashboards
**Backend:**
- Commission config APIs (Super Admin sets % per role level)
- Commission calculation engine (distributes up geographic chain per sale)
- Monthly commission report generation APIs
- Commission report download (PDF/Excel)
- All 9 report types with APIs:
  - Sales, Stock, Commission, Patient, Bill, Expiry, User Activity, Revenue, Low Stock
- Report filter parameters (date range, geography, product category)
- PDF export service (using iText or OpenPDF)
- Excel export service (using Apache POI)
- Dashboard data APIs (role-specific aggregations)

**Frontend:**
- Commission settings page (Super Admin — set % per level)
- Commission reports page (monthly summary, drill-down)
- Report pages for each report type (filters + table + export buttons)
- Role-specific dashboards with Recharts:
  - Super Admin: system-wide KPIs, state-wise breakdown, charts, trends, pending actions
  - State Admin: state-level stats
  - District Admin: district-level stats
  - Block Admin: block-level stats
  - Receptionist: today's sales, low stock alerts, pending tasks
  - Pharmacist: pending transfers, stock levels

---

### Phase 6: Messaging, Notifications & Production Polish
**Backend:**
- Full notification centre APIs (all types: stock, permission, expiry, commission, verification)
- Stock request notification flow (end-to-end)
- Message template CRUD APIs
- Bulk message APIs (compose, target audience, schedule, send)
- Email dispatch service (bulk email via JavaMailSender)
- SMS dispatch service (MSG91 integration)
- Message delivery tracking
- Audience filtering engine (broadcast, state, district, store, custom filters)

**Frontend:**
- Notification centre page (all notifications, mark read, filter by type)
- Message template management pages
- Bulk message composer (select channel, audience, template/custom, schedule)
- Message history + delivery stats page
- Dark mode toggle + full dark mode styling
- Mobile responsiveness audit + fixes across all pages
- Performance: lazy loading all pages, virtual scrolling for long lists, debounce search inputs
- Help/FAQ section
- Docker setup (Dockerfile for backend, docker-compose with MySQL)
- README with setup instructions + production notes (backup, HTTPS, etc.)
- Final security audit + penetration testing checklist

---

## Schema Changes Based on Your Answers

Added to schema (see updated schema_and_structure.md):
1. **`user_sessions`** — tracks multiple active sessions per user, supports view/revoke
2. **`system_config`** — key-value store for configurable values (return window, etc.)
3. **`two_factor_config`** — per-user 2FA settings (enabled flag, last OTP, OTP expiry)
4. Modified `users` table: added `force_password_change` boolean, removed password expiry fields

---

> [!IMPORTANT]
> **Please confirm this final plan is correct.** Once confirmed, I will start building Phase 1. I will not start any phase without your explicit instruction.
