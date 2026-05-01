# ASK — Database Schema & Folder Structure

## Database Schema (All Tables)

### Geographic Hierarchy

#### `states`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| name | VARCHAR(100) NOT NULL | |
| code | VARCHAR(10) UNIQUE | e.g., BR, UP, JH |
| status | ENUM('ACTIVE','INACTIVE') | Default ACTIVE |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

#### `districts`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| name | VARCHAR(100) NOT NULL | |
| state_id | BIGINT FK → states | INDEX |
| status | ENUM('ACTIVE','INACTIVE') | |
| created_at / updated_at | TIMESTAMP | |

#### `blocks`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| name | VARCHAR(100) NOT NULL | |
| district_id | BIGINT FK → districts | INDEX |
| status | ENUM('ACTIVE','INACTIVE') | |
| created_at / updated_at | TIMESTAMP | |

#### `stores`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| name | VARCHAR(150) NOT NULL | |
| code | VARCHAR(20) UNIQUE | Store code |
| address | TEXT | |
| phone | VARCHAR(15) | |
| operating_hours | VARCHAR(100) | |
| block_id | BIGINT FK → blocks | INDEX |
| status | ENUM('ACTIVE','INACTIVE') | |
| created_at / updated_at | TIMESTAMP | |

---

### Auth & Users

#### `roles`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| name | VARCHAR(50) UNIQUE | SUPER_ADMIN, SYSTEM_ADMIN, etc. |
| display_name | VARCHAR(100) | |
| hierarchy_level | INT | 1=Super Admin ... 8=Volunteer |

#### `users`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| full_name | VARCHAR(150) NOT NULL | |
| email | VARCHAR(150) UNIQUE | INDEX |
| phone | VARCHAR(15) UNIQUE | INDEX |
| password_hash | VARCHAR(255) | BCrypt 12 |
| profile_photo_url | VARCHAR(500) | |
| date_of_birth | DATE | |
| gender | ENUM('MALE','FEMALE','OTHER') | |
| address | TEXT | |
| role_id | BIGINT FK → roles | INDEX |
| state_id | BIGINT FK → states NULL | INDEX |
| district_id | BIGINT FK → districts NULL | INDEX |
| block_id | BIGINT FK → blocks NULL | INDEX |
| store_id | BIGINT FK → stores NULL | INDEX |
| bank_account_encrypted | TEXT | AES-256 |
| bank_ifsc | VARCHAR(20) | |
| bank_name | VARCHAR(100) | |
| pan_number | VARCHAR(10) | |
| aadhaar_last_four | VARCHAR(4) | Last 4 only |
| verification_status | ENUM('PENDING','VERIFIED','REJECTED') | |
| status | ENUM('ACTIVE','INACTIVE','LOCKED') | |
| failed_login_attempts | INT DEFAULT 0 | |
| locked_until | TIMESTAMP NULL | |
| force_password_change | BOOLEAN DEFAULT FALSE | True for temp password |
| password_changed_at | TIMESTAMP | |
| last_login_at | TIMESTAMP | |
| created_by | BIGINT FK → users NULL | |
| created_at / updated_at | TIMESTAMP | |

#### `permissions`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| module | VARCHAR(50) | e.g., USERS, PATIENTS |
| action | VARCHAR(50) | e.g., VIEW, CREATE |
| description | VARCHAR(255) | |

#### `user_permissions`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| user_id | BIGINT FK → users | INDEX, UNIQUE(user_id, permission_id) |
| permission_id | BIGINT FK → permissions | INDEX |
| granted_by | BIGINT FK → users | |
| granted_at | TIMESTAMP | |

#### `permission_requests`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| user_id | BIGINT FK → users | INDEX |
| permission_id | BIGINT FK → permissions | |
| reason | TEXT | |
| status | ENUM('PENDING','APPROVED','REJECTED') | INDEX |
| reviewed_by | BIGINT FK → users NULL | |
| reviewed_at | TIMESTAMP NULL | |
| created_at | TIMESTAMP | |

#### `refresh_tokens`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| user_id | BIGINT FK → users | INDEX |
| token | VARCHAR(500) UNIQUE | |
| expires_at | TIMESTAMP | |
| created_at | TIMESTAMP | |

#### `user_sessions`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| user_id | BIGINT FK → users | INDEX |
| token_fingerprint | VARCHAR(255) UNIQUE | Hash of refresh token |
| device_info | VARCHAR(255) | Browser/device string |
| ip_address | VARCHAR(45) | |
| last_active_at | TIMESTAMP | |
| expires_at | TIMESTAMP | |
| is_revoked | BOOLEAN DEFAULT FALSE | |
| created_at | TIMESTAMP | |

#### `two_factor_config`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| user_id | BIGINT FK → users UNIQUE | INDEX |
| is_enabled | BOOLEAN DEFAULT FALSE | |
| is_mandatory | BOOLEAN DEFAULT FALSE | True for SUPER/SYSTEM_ADMIN |
| otp_code | VARCHAR(10) NULL | Hashed OTP |
| otp_expires_at | TIMESTAMP NULL | |
| created_at / updated_at | TIMESTAMP | |

#### `system_config`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| config_key | VARCHAR(100) UNIQUE | e.g., RETURN_WINDOW_DAYS |
| config_value | VARCHAR(500) | e.g., 7 |
| description | VARCHAR(255) | Human-readable description |
| updated_by | BIGINT FK → users NULL | |
| updated_at | TIMESTAMP | |

---

### Patients & Health Cards

#### `hospitals`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| name | VARCHAR(200) | |
| address | TEXT | |
| phone | VARCHAR(15) | |
| contact_person | VARCHAR(150) | |
| state_id | BIGINT FK → states | INDEX |
| district_id | BIGINT FK → districts | INDEX |
| status | ENUM('ACTIVE','INACTIVE') | |
| created_at / updated_at | TIMESTAMP | |

#### `patients`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| full_name | VARCHAR(150) NOT NULL | |
| age | INT | |
| gender | ENUM('MALE','FEMALE','OTHER') | |
| phone | VARCHAR(15) | INDEX |
| email | VARCHAR(150) | |
| address | TEXT | |
| state_id | BIGINT FK → states | INDEX |
| district_id | BIGINT FK → districts | INDEX |
| block_id | BIGINT FK → blocks | INDEX |
| store_id | BIGINT FK → stores NULL | Nearest store |
| hospital_id | BIGINT FK → hospitals NULL | Referring hospital |
| messaging_pref | ENUM('SMS','EMAIL','WHATSAPP','ALL') | |
| status | ENUM('ACTIVE','INACTIVE') | |
| created_by | BIGINT FK → users | |
| created_at / updated_at | TIMESTAMP | |

#### `health_cards`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| card_number | VARCHAR(30) UNIQUE | INDEX |
| patient_id | BIGINT FK → patients UNIQUE | Primary holder |
| store_id | BIGINT FK → stores | Store of issue |
| issued_by | BIGINT FK → users | |
| issued_at | TIMESTAMP | |
| status | ENUM('ACTIVE','INACTIVE') | |

#### `health_card_members`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| health_card_id | BIGINT FK → health_cards | INDEX |
| name | VARCHAR(150) | |
| relation | VARCHAR(50) | e.g., Spouse, Child |
| age | INT | |
| gender | ENUM('MALE','FEMALE','OTHER') | |
| created_at | TIMESTAMP | |

---

### Products & Inventory

#### `product_categories`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| name | VARCHAR(100) | Medicine, Baby Food, Cosmetics, Other |
| status | ENUM('ACTIVE','INACTIVE') | |

#### `products`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| name | VARCHAR(200) NOT NULL | INDEX |
| brand | VARCHAR(100) | |
| category_id | BIGINT FK → product_categories | INDEX |
| hsn_code | VARCHAR(20) | |
| mrp | DECIMAL(10,2) | |
| ask_price | DECIMAL(10,2) | |
| gst_percentage | DECIMAL(5,2) | |
| min_stock_threshold | INT DEFAULT 10 | |
| status | ENUM('ACTIVE','INACTIVE') | |
| created_at / updated_at | TIMESTAMP | |

#### `stock_central`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| product_id | BIGINT FK → products | INDEX |
| batch_number | VARCHAR(50) | INDEX |
| manufacturing_date | DATE | |
| expiry_date | DATE | INDEX |
| quantity | INT | |
| received_by | BIGINT FK → users | Pharmacist |
| received_at | TIMESTAMP | |
| status | ENUM('AVAILABLE','TRANSFERRED','EXPIRED') | |
| created_at | TIMESTAMP | |

#### `transfer_orders`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| transfer_number | VARCHAR(30) UNIQUE | |
| store_id | BIGINT FK → stores | INDEX |
| created_by | BIGINT FK → users | Pharmacist |
| status | ENUM('PENDING','IN_TRANSIT','RECEIVED','CANCELLED') | INDEX |
| notes | TEXT | |
| confirmed_by | BIGINT FK → users NULL | |
| confirmed_at | TIMESTAMP NULL | |
| created_at / updated_at | TIMESTAMP | |

#### `transfer_order_items`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| transfer_order_id | BIGINT FK → transfer_orders | INDEX |
| product_id | BIGINT FK → products | |
| batch_number | VARCHAR(50) | |
| expiry_date | DATE | |
| quantity | INT | |

#### `stock_store`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| store_id | BIGINT FK → stores | INDEX, UNIQUE(store_id, product_id, batch_number) |
| product_id | BIGINT FK → products | INDEX |
| batch_number | VARCHAR(50) | |
| expiry_date | DATE | INDEX |
| quantity | INT | |
| updated_at | TIMESTAMP | |

#### `stock_adjustments`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| store_id | BIGINT FK → stores | INDEX |
| product_id | BIGINT FK → products | |
| batch_number | VARCHAR(50) | |
| adjustment_type | ENUM('DAMAGE','RETURN','CORRECTION','EXPIRY') | |
| quantity_change | INT | Positive or negative |
| reason | TEXT | |
| adjusted_by | BIGINT FK → users | |
| created_at | TIMESTAMP | |

---

### Billing & Sales

#### `bills`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| bill_number | VARCHAR(30) UNIQUE | INDEX |
| store_id | BIGINT FK → stores | INDEX |
| patient_id | BIGINT FK → patients | INDEX |
| health_card_id | BIGINT FK → health_cards NULL | |
| total_mrp | DECIMAL(12,2) | |
| total_ask_price | DECIMAL(12,2) | |
| total_gst | DECIMAL(10,2) | |
| total_discount | DECIMAL(10,2) | |
| net_amount | DECIMAL(12,2) | |
| total_savings | DECIMAL(12,2) | MRP - net |
| payment_mode | ENUM('CASH','UPI','CARD') | |
| status | ENUM('ACTIVE','CANCELLED') | INDEX |
| cancel_reason | TEXT NULL | |
| cancelled_by | BIGINT FK → users NULL | |
| created_by | BIGINT FK → users | Receptionist |
| bill_date | TIMESTAMP | INDEX |
| created_at / updated_at | TIMESTAMP | |

#### `bill_items`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| bill_id | BIGINT FK → bills | INDEX |
| product_id | BIGINT FK → products | |
| batch_number | VARCHAR(50) | |
| quantity | INT | |
| mrp | DECIMAL(10,2) | |
| ask_price | DECIMAL(10,2) | |
| gst_amount | DECIMAL(10,2) | |
| discount_amount | DECIMAL(10,2) | |
| subtotal | DECIMAL(10,2) | |
| return_status | ENUM('NONE','REQUESTED','APPROVED','REJECTED') | |
| return_quantity | INT DEFAULT 0 | |
| return_reason | TEXT NULL | |

#### `schemes`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| name | VARCHAR(200) | |
| description | TEXT | |
| discount_type | ENUM('PERCENTAGE','FLAT') | |
| discount_value | DECIMAL(10,2) | |
| category_id | BIGINT FK → product_categories NULL | |
| state_id | BIGINT FK → states NULL | |
| start_date | DATE | |
| end_date | DATE | |
| status | ENUM('ACTIVE','INACTIVE') | |
| created_by | BIGINT FK → users | |
| created_at / updated_at | TIMESTAMP | |

---

### Commission

#### `commission_config`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| role_id | BIGINT FK → roles | UNIQUE |
| percentage | DECIMAL(5,2) | |
| updated_by | BIGINT FK → users | |
| updated_at | TIMESTAMP | |

#### `commission_entries`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| bill_id | BIGINT FK → bills | INDEX |
| user_id | BIGINT FK → users | INDEX (recipient) |
| role_id | BIGINT FK → roles | |
| amount | DECIMAL(12,2) | |
| month | VARCHAR(7) | e.g., 2026-05, INDEX |
| status | ENUM('CALCULATED','PAID') | |
| created_at | TIMESTAMP | |

---

### Notifications & Messaging

#### `notifications`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| user_id | BIGINT FK → users | INDEX (recipient) |
| type | VARCHAR(50) | STOCK_REQUEST, PERMISSION_REQUEST, etc. |
| title | VARCHAR(200) | |
| message | TEXT | |
| reference_type | VARCHAR(50) NULL | Entity type |
| reference_id | BIGINT NULL | Entity ID |
| is_read | BOOLEAN DEFAULT FALSE | INDEX |
| created_at | TIMESTAMP | INDEX |

#### `stock_requests`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| store_id | BIGINT FK → stores | INDEX |
| product_id | BIGINT FK → products | |
| quantity_requested | INT | |
| urgency | ENUM('LOW','MEDIUM','HIGH','CRITICAL') | |
| status | ENUM('PENDING','APPROVED','FULFILLED','REJECTED') | INDEX |
| requested_by | BIGINT FK → users | |
| reviewed_by | BIGINT FK → users NULL | |
| reviewed_at | TIMESTAMP NULL | |
| notes | TEXT NULL | |
| created_at | TIMESTAMP | |

#### `message_templates`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| name | VARCHAR(100) | |
| channel | ENUM('SMS','EMAIL','WHATSAPP') | |
| subject | VARCHAR(200) NULL | For email |
| body | TEXT | With {variables} |
| created_by | BIGINT FK → users | |
| status | ENUM('ACTIVE','INACTIVE') | |
| created_at / updated_at | TIMESTAMP | |

#### `bulk_messages`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| channel | ENUM('SMS','EMAIL','WHATSAPP') | |
| template_id | BIGINT FK → message_templates NULL | |
| audience_type | ENUM('BROADCAST','STATE','DISTRICT','STORE','FILTER') | |
| audience_filter | JSON NULL | Filter criteria |
| state_id | BIGINT FK → states NULL | |
| district_id | BIGINT FK → districts NULL | |
| store_id | BIGINT FK → stores NULL | |
| subject | VARCHAR(200) NULL | |
| body | TEXT | |
| total_recipients | INT | |
| sent_count | INT DEFAULT 0 | |
| failed_count | INT DEFAULT 0 | |
| scheduled_at | TIMESTAMP NULL | |
| status | ENUM('DRAFT','SCHEDULED','SENDING','SENT','FAILED') | |
| sent_by | BIGINT FK → users | |
| created_at / updated_at | TIMESTAMP | |

---

### Audit

#### `audit_logs`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK AUTO | |
| user_id | BIGINT FK → users | INDEX |
| action | VARCHAR(100) | INDEX |
| entity_type | VARCHAR(50) | INDEX |
| entity_id | BIGINT | INDEX |
| old_value | JSON NULL | |
| new_value | JSON NULL | |
| ip_address | VARCHAR(45) | |
| description | TEXT | |
| created_at | TIMESTAMP | INDEX |

**Total: 30 tables**

---

## Key Indexes (Beyond PKs and FKs)

- `users`: email, phone, status, role_id, state_id, district_id, block_id, store_id
- `patients`: phone, state_id, district_id, store_id
- `products`: name, category_id
- `stock_store`: (store_id, product_id, batch_number) UNIQUE, expiry_date
- `bills`: bill_number, store_id, patient_id, bill_date, status
- `commission_entries`: user_id, month, bill_id
- `audit_logs`: user_id, entity_type, entity_id, action, created_at
- `notifications`: user_id, is_read, created_at

---

## Key Relationships Diagram

```
states 1──N districts 1──N blocks 1──N stores
                                         │
users ──FK──► role, state, district,      │
             block, store                 │
                                         │
patients ──FK──► state, district,    ◄────┘
                 block, store, hospital
                    │
health_cards ──FK──► patient (1:1), store
    │
health_card_members ──FK──► health_card (1:N, max 5)

products ──FK──► product_categories
stock_central ──FK──► products
transfer_orders ──FK──► stores
  └── transfer_order_items ──FK──► products
stock_store ──FK──► stores, products

bills ──FK──► stores, patients, health_cards
  └── bill_items ──FK──► products

commission_entries ──FK──► bills, users
audit_logs ──FK──► users
notifications ──FK──► users
user_sessions ──FK──► users
two_factor_config ──FK──► users (1:1)
system_config ── standalone key-value store
```

---

## Backend Folder Structure

```
src/main/java/com/ask/
├── config/
│   ├── AppConfig.java
│   ├── AsyncConfig.java
│   ├── CorsConfig.java
│   └── HikariConfig.java
├── constants/
│   ├── AppConstants.java
│   ├── ErrorMessages.java
│   ├── ApiPaths.java
│   └── RoleConstants.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── GeographyController.java
│   ├── StoreController.java
│   ├── PatientController.java
│   ├── HealthCardController.java
│   ├── ProductController.java
│   ├── InventoryController.java
│   ├── BillController.java
│   ├── CommissionController.java
│   ├── NotificationController.java
│   ├── MessageController.java
│   ├── ReportController.java
│   └── ProfileController.java
├── dto/
│   ├── request/
│   │   ├── auth/
│   │   ├── user/
│   │   ├── geography/
│   │   ├── patient/
│   │   ├── healthcard/
│   │   ├── product/
│   │   ├── inventory/
│   │   ├── billing/
│   │   ├── commission/
│   │   ├── notification/
│   │   └── messaging/
│   └── response/
│       ├── common/
│       │   ├── ApiResponse.java
│       │   ├── PageResponse.java
│       │   └── ErrorResponse.java
│       ├── auth/
│       ├── user/
│       ├── geography/
│       ├── patient/
│       ├── healthcard/
│       ├── product/
│       ├── inventory/
│       ├── billing/
│       ├── commission/
│       ├── notification/
│       └── messaging/
├── entity/
│   ├── State.java
│   ├── District.java
│   ├── Block.java
│   ├── Store.java
│   ├── Role.java
│   ├── User.java
│   ├── Permission.java
│   ├── UserPermission.java
│   ├── PermissionRequest.java
│   ├── RefreshToken.java
│   ├── UserSession.java
│   ├── TwoFactorConfig.java
│   ├── SystemConfig.java
│   ├── Hospital.java
│   ├── Patient.java
│   ├── HealthCard.java
│   ├── HealthCardMember.java
│   ├── ProductCategory.java
│   ├── Product.java
│   ├── StockCentral.java
│   ├── TransferOrder.java
│   ├── TransferOrderItem.java
│   ├── StockStore.java
│   ├── StockAdjustment.java
│   ├── Bill.java
│   ├── BillItem.java
│   ├── Scheme.java
│   ├── CommissionConfig.java
│   ├── CommissionEntry.java
│   ├── Notification.java
│   ├── StockRequest.java
│   ├── MessageTemplate.java
│   ├── BulkMessage.java
│   └── AuditLog.java
├── enums/
│   ├── UserStatus.java
│   ├── VerificationStatus.java
│   ├── TransferStatus.java
│   ├── BillStatus.java
│   ├── PaymentMode.java
│   ├── Urgency.java
│   ├── MessageChannel.java
│   ├── AudienceType.java
│   └── ... (one enum per domain concept)
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── AccessDeniedException.java
│   ├── GeographicScopeException.java
│   ├── AccountLockedException.java
│   ├── InvalidRequestException.java
│   └── BusinessRuleException.java
├── mapper/
│   ├── UserMapper.java
│   ├── PatientMapper.java
│   ├── ProductMapper.java
│   ├── BillMapper.java
│   └── ... (one MapStruct mapper per domain)
├── repository/
│   ├── UserRepository.java
│   ├── PatientRepository.java
│   ├── ... (one per entity)
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetailsService.java
│   └── SecurityConfig.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── GeographyService.java
│   ├── PatientService.java
│   ├── HealthCardService.java
│   ├── ProductService.java
│   ├── InventoryService.java
│   ├── BillService.java
│   ├── CommissionService.java
│   ├── NotificationService.java
│   ├── MessageService.java
│   ├── ReportService.java
│   ├── AuditService.java
│   ├── ProfileService.java
│   └── impl/
│       ├── AuthServiceImpl.java
│       ├── UserServiceImpl.java
│       └── ... (one impl per interface)
├── util/
│   ├── EncryptionUtil.java
│   ├── DateUtil.java
│   └── SlugUtil.java
└── validator/
    ├── UserValidator.java
    ├── BillValidator.java
    └── GeographicScopeValidator.java
```

## Frontend Folder Structure

```
src/
├── api/
│   ├── axiosInstance.js
│   ├── authApi.js
│   ├── userApi.js
│   ├── geographyApi.js
│   ├── patientApi.js
│   ├── healthCardApi.js
│   ├── productApi.js
│   ├── inventoryApi.js
│   ├── billApi.js
│   ├── commissionApi.js
│   ├── notificationApi.js
│   ├── messageApi.js
│   └── reportApi.js
├── assets/
├── components/
│   ├── common/
│   │   ├── Button.jsx
│   │   ├── Input.jsx
│   │   ├── Modal.jsx
│   │   ├── DataTable.jsx
│   │   ├── Loader.jsx
│   │   ├── ErrorBoundary.jsx
│   │   ├── EmptyState.jsx
│   │   ├── Badge.jsx
│   │   ├── Pagination.jsx
│   │   ├── ConfirmDialog.jsx
│   │   ├── StatusBadge.jsx
│   │   ├── PageHeader.jsx
│   │   └── SkeletonLoader.jsx
│   └── layout/
│       ├── Navbar.jsx
│       ├── Sidebar.jsx
│       ├── PageWrapper.jsx
│       ├── ProtectedRoute.jsx
│       └── RoleGuard.jsx
├── constants/
│   ├── apiPaths.js
│   ├── roles.js
│   ├── routePaths.js
│   └── appConstants.js
├── context/
│   ├── AuthContext.jsx
│   └── ThemeContext.jsx
├── hooks/
│   ├── useAuth.js
│   ├── usePermission.js
│   ├── usePagination.js
│   └── useDebounce.js
├── pages/
│   ├── auth/
│   ├── dashboard/
│   ├── users/
│   ├── patients/
│   ├── health-cards/
│   ├── products/
│   ├── inventory/
│   ├── billing/
│   ├── commissions/
│   ├── notifications/
│   ├── messaging/
│   ├── reports/
│   ├── stores/
│   ├── profile/
│   └── errors/
├── store/
│   ├── useAuthStore.js
│   ├── useNotificationStore.js
│   └── useSidebarStore.js
├── utils/
│   ├── dateUtils.js
│   ├── maskUtils.js
│   ├── validationUtils.js
│   ├── currencyUtils.js
│   └── formatUtils.js
└── styles/
    └── globals.css
```
