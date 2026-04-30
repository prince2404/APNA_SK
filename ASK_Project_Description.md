# Apna Swasthya Kendra (ASK) — Complete Project Description

> A web-based healthcare retail management platform serving 200+ stores across Bihar, Uttar Pradesh, and Jharkhand — connecting patients with affordable, genuine health products.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Why ASK Exists — The Business Model](#2-why-apk-exists--the-business-model)
3. [User Hierarchy & Roles](#3-user-hierarchy--roles)
4. [Geographical Structure](#4-geographical-structure)
5. [Module-wise Feature Breakdown](#5-module-wise-feature-breakdown)
   - 5.1 User Management
   - 5.2 Permission Management
   - 5.3 Patient Management
   - 5.4 Health Card System
   - 5.5 Inventory Management
   - 5.6 Billing & Sales
   - 5.7 Commission & Revenue Distribution
   - 5.8 Notification & Request System
   - 5.9 Bulk Messaging
   - 5.10 Reports & Analytics
   - 5.11 Personal Profile & Verification
6. [Complete System Flow](#6-complete-system-flow)
7. [Security Architecture](#7-security-architecture)
8. [Additional Features to Include](#8-additional-features-to-include)

---

## 1. Project Overview

**Project Name:** Apna Swasthya Kendra (ASK)
**Platform:** Web-based (fully responsive, works on mobile and desktop)
**Target Geography:** Bihar, Uttar Pradesh, Jharkhand
**Store Count:** ~200 stores (growing)

ASK is a comprehensive healthcare retail management system. It manages everything from purchasing medicines from companies, distributing them to stores, selling them to patients at low cost, tracking inventory, generating bills, distributing commissions, and communicating with patients — all through a single unified platform.

---

## 2. Why ASK Exists — The Business Model

```
Traditional Medicine Supply Chain (Expensive):
  Company → Distributor → Stockist → Retailer → Patient
  (Each middleman adds margin → Patient pays HIGH price)

ASK Supply Chain (Affordable):
  Company → Sir (ASK) → ASK Stores → Patient
  (No middlemen → Patient pays LOW price)
```

**How Patients Reach ASK Stores:**
1. Patient visits a clinic or hospital (ASK is tied up with these hospitals)
2. The hospital/clinic recommends ASK centers for buying medicines at low cost
3. Hospital/clinic shares patient data with ASK (bulk upload)
4. Patient visits the nearest ASK store
5. Receptionist at the store sells medicines to the patient using the platform
6. A bill is generated, stock is updated automatically

**Key Value Proposition:**
- Genuine, quality medicines directly from companies
- Prices significantly lower than regular pharmacies
- Health card for patient loyalty and family coverage
- Geographically spread across 3 major states

---

## 3. User Hierarchy & Roles

### Hierarchy Diagram

```
                        ┌─────────────────────┐
                        │     SUPER ADMIN      │
                        │       (Sir)          │
                        │  Full system access  │
                        └──────────┬──────────┘
                                   │
                        ┌──────────▼──────────┐
                        │    SYSTEM ADMIN      │
                        │  Platform-wide ops   │
                        └──────────┬──────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
    ┌─────────▼────────┐ ┌─────────▼────────┐ ┌────────▼─────────┐
    │   STATE ADMIN    │ │   STATE ADMIN    │ │   STATE ADMIN    │
    │     (Bihar)      │ │      (UP)        │ │   (Jharkhand)    │
    └─────────┬────────┘ └─────────┬────────┘ └────────┬─────────┘
              │                    │                    │
    ┌─────────▼────────┐           │                    │
    │  DISTRICT ADMIN  │           │  (same structure)  │
    │  (e.g. Patna)    │           │                    │
    └─────────┬────────┘           │                    │
              │
    ┌─────────▼────────┐
    │   BLOCK ADMIN    │
    │  (e.g. Phulwari) │
    └─────────┬────────┘
              │
    ┌─────────┴─────────────────┐
    │                           │
┌───▼──────────┐        ┌───────▼──────┐
│ RECEPTIONIST │        │  VOLUNTEER   │
│  (Salaried)  │        │  (Unpaid)    │
└──────────────┘        └──────────────┘

Also separate from hierarchy:
┌───────────────────┐
│    PHARMACIST     │
│ (Manages supply   │
│  from Sir to      │
│  stores)          │
└───────────────────┘
```

### Role Descriptions

| Role | Description | Paid? |
|------|-------------|-------|
| **Super Admin (Sir)** | Owns the entire system. Has unrestricted access to everything — users, commissions, permissions, reports, notifications. Only person who can delete/deactivate anything. | — |
| **System Admin** | Manages platform-wide technical and operational settings. Helps Super Admin in day-to-day management. | Yes |
| **State Admin** | Manages all operations within one state (Bihar / UP / Jharkhand). Can have more than one per large state. Sees only their state's data. | Yes |
| **District Admin** | Manages operations within a specific district. Sees only their district's data. | Yes |
| **Block Admin** | Manages operations at block level. Closest administrative user above stores. | Yes |
| **Receptionist** | Works at a specific store. Sells medicines to patients, generates bills, checks stock, sends stock requests. | Yes (Salaried) |
| **Volunteer** | Works at a specific store. Assists in store operations. Limited access. | No (Unpaid) |
| **Pharmacist** | Responsible for receiving health items from Sir (after company purchase) and transferring them to respective stores. Manages supply logistics. | Yes |

> **Important:** Every user except Super Admin has geographical restrictions — they can only see and manage data within their assigned geography.

---

## 4. Geographical Structure

The platform is structured geographically in 4 tiers:

```
State  (Bihar, UP, Jharkhand)
  └── District  (e.g., Patna, Lucknow, Ranchi)
        └── Block  (e.g., Phulwari Sharif, Sadar)
              └── Store / Centre  (specific ASK store)
```

- Each user is assigned to one or more geographical zones
- Data visibility is strictly scoped to the user's zone
- Commissions are distributed within the same geographical chain only
- Super Admin can see all states, all districts, all blocks, all stores

---

## 5. Module-wise Feature Breakdown

---

### 5.1 User Management

**Who can create users?**
- Super Admin can create all types of users
- Each user type can create users only below them in the hierarchy, and only within their geographical zone (if Super Admin grants them this permission)

**User Creation Flow:**
1. Go to User Management → Add New User
2. Fill personal details (name, phone, email, address, photo)
3. Assign Role (from dropdown)
4. Assign Geography (State → District → Block → Store depending on role)
5. Assign Permissions (checkboxes — explained in 5.2)
6. Send invite/credentials to the user

**User Actions Available:**
- View user details
- Create new user
- Deactivate a user (not delete)
- Reactivate a deactivated user
- Edit user information
- View assigned geography
- View activity logs of any user

> ⚠️ **No user can be permanently deleted** from the system. Only deactivation is allowed. Only Super Admin has access to deactivation and reactivation of users.

---

### 5.2 Permission Management

This is a **fully flexible, checkbox-based permission system** managed by the Super Admin.

**How it works:**
- When creating or editing a user, Super Admin sees a list of available actions
- Each action can be independently enabled or disabled via checkbox
- Permissions can be updated anytime

**Sample Permission Matrix:**

| Module | Permissions Available |
|--------|-----------------------|
| Users | View, Create, Deactivate, Reactivate |
| Patients | View, Create (Single), Bulk Upload, Edit |
| Health Cards | View, Issue, Add Family Member |
| Inventory | View Stock, Add Stock, Transfer Stock |
| Billing | View Bills, Create Bill, Cancel Bill |
| Reports | View, Download |
| Commissions | View, Edit Percentage |
| Notifications | View, Send |
| Messaging | Send SMS, Send Email, Send WhatsApp |
| Stores | View, Create, Edit |

**Permission Request Flow:**
- A user can request additional permissions from within the platform
- The request goes as a notification to Super Admin
- Super Admin reviews and enables/disables the permission from the user's profile
- User gets notified of approval or rejection

> Permissions are **geography-bound** — even if a user has "View Reports" permission, they only see reports for their assigned geography.

---

### 5.3 Patient Management

**How patients enter the system:**

```
Option A: Bulk Upload (from hospital/clinic tie-up)
  Hospital shares patient data → Excel/CSV file →
  System Admin or State Admin uploads → Data mapped and verified →
  Patients added to system

Option B: Single Patient Entry (at store level)
  Receptionist adds patient manually at the time of visit →
  Fill: Name, Age, Gender, Phone, Address, Medical History (optional)
```

**Patient Profile contains:**
- Full name, age, gender
- Contact number, address (with state/district/block auto-tagged)
- Linked hospital/clinic (if referred)
- Health Card number (once issued)
- Purchase history (all bills linked to patient)
- Family members linked to the health card
- Messaging preferences (SMS/WhatsApp/Email)

**Duplicate Prevention:**
- System checks phone number / Aadhaar (if provided) before creating a new patient to avoid duplicate entries

---

### 5.4 Health Card System

Each patient visiting an ASK store can be issued a **Health Card**.

**Health Card Rules:**
- One card per patient (primary holder)
- Up to **6 family members** can be added to one card
- Card has a unique ID / barcode / QR code
- Card is linked to the patient's purchase history

**Health Card Flow:**

```
Patient visits store
    ↓
Receptionist checks if patient already has a card
    ↓
If No → Issue new Health Card
    ↓
Enter primary holder details
    ↓
Option to add family members (up to 5 more)
    ↓
Card generated with unique ID
    ↓
Patient can now buy medicines using card
```

**Health Card contains:**
- Card ID (unique)
- Primary holder name, photo (optional), contact
- Family members list (name, relation, age)
- Date of issue
- Store of issue
- Purchase history

---

### 5.5 Inventory Management

This module tracks health items from the moment Sir buys them from companies to when they are sold at stores.

**Inventory Flow:**

```
Sir purchases from Company
       ↓
Items received by Pharmacist at central warehouse
       ↓
Pharmacist logs items into system (product name, quantity, batch, expiry, MRP, ASK price)
       ↓
Pharmacist creates Transfer Order → assigns items to specific stores
       ↓
Store receives items → Block Admin / Receptionist confirms receipt
       ↓
Stock updated at store level
       ↓
Receptionist sells items to patients → stock reduced per sale
       ↓
When stock falls below threshold → Receptionist sends Stock Request
       ↓
Notification sent to Super Admin / Pharmacist
       ↓
Pharmacist prepares and transfers new stock
```

**Product Catalogue includes:**
- Product name, brand, category (Medicine / Baby Food / Cosmetics / Other)
- HSN/SAC code (for billing)
- MRP, ASK selling price
- GST percentage
- Batch number, manufacturing date, expiry date
- Minimum stock threshold (for low-stock alerts)

**Stock Management Features:**
- View current stock at any store (geography-scoped)
- View stock transfer history
- Low stock alerts (automatic notification)
- Expiry tracking (items expiring within 30/60/90 days flagged)
- Stock adjustment (for damages, returns, corrections — with reason)

---

### 5.6 Billing & Sales

**Billing Flow at Store:**

```
Patient visits store with Health Card
       ↓
Receptionist opens New Bill
       ↓
Searches patient by Name / Phone / Card ID
       ↓
Adds items from store's current stock
       ↓
System auto-calculates: MRP, ASK Price, GST, Discount, Total
       ↓
Bill preview shown
       ↓
Receptionist confirms → Bill generated
       ↓
Stock automatically reduced for sold items
       ↓
Bill linked to patient's purchase history
       ↓
Commission entry created for revenue distribution
```

**Bill contains:**
- Bill number (unique, auto-generated)
- Date & time
- Store name and address
- Patient name and Health Card ID
- List of items (product, qty, MRP, ASK price, GST, subtotal)
- Total MRP, Total ASK Price, Total savings for patient
- Payment mode (Cash / UPI / Card)
- Receptionist name

**Bill Management:**
- View all bills (scoped by geography and date range)
- Cancel a bill (with reason — limited permission)
- Return/refund a bill item
- Download/print bill as PDF

---

### 5.7 Commission & Revenue Distribution

When a store generates revenue, a portion is distributed **upward through the hierarchy within the same geography only.**

**Commission Structure:**

```
Store Sale Revenue
       │
       ├── % stays at Store level (operational cost)
       │
       ├── % goes to Block Admin
       │
       ├── % goes to District Admin
       │
       ├── % goes to State Admin
       │
       └── % goes to Super Admin
```

**Rules:**
- Commission percentages are **set by Super Admin** and can be changed anytime
- Each hierarchy level has its own percentage
- Commission is geography-locked — a Bihar store's revenue only distributes to Bihar's hierarchy
- Monthly commission reports are generated per store, per district, per state
- Commission is tracked in the system but actual payout is managed externally (or can be set up with bank integration later)

**Commission Management Panel (Super Admin):**
- Set/edit commission percentage per hierarchy level
- View monthly commission summary
- Download commission reports (store-wise, district-wise, state-wise)

---

### 5.8 Notification & Request System

**Notification Center** is a central panel visible to Super Admin (and scoped versions for other admins).

**Types of Notifications:**
- 📦 Stock Request from Receptionist (low stock at a store)
- 🔐 Permission Request from any user (requesting access to a feature)
- 👤 New user pending verification
- 💊 Expiry alert (products expiring soon)
- 📊 Monthly revenue/commission summary ready
- ⚠️ Low stock threshold breached
- ✅ Stock transferred successfully

**Notification Flow:**

```
Receptionist notices low stock
       ↓
Clicks "Send Stock Request" → selects product, quantity needed, urgency
       ↓
Notification appears in Super Admin's panel
       ↓
Super Admin views request details
       ↓
Forwards to Pharmacist OR approves transfer directly
       ↓
Pharmacist prepares stock and transfers
       ↓
Receptionist receives confirmation notification
```

**Permission Request Flow:**

```
User wants additional permission (e.g., Download Reports)
       ↓
User clicks "Request Permission" from their profile
       ↓
Selects permission needed, writes reason
       ↓
Super Admin gets notified
       ↓
Super Admin reviews → Approves / Rejects
       ↓
User is notified with result
```

---

### 5.9 Bulk Messaging

ASK can communicate directly with patients for offers, discounts, health tips, and reminders.

**Messaging Channels:**
- 📧 Email
- 📱 SMS
- 💬 WhatsApp

**Messaging Options:**

| Type | Description |
|------|-------------|
| **Broadcast** | Send to all patients across all stores |
| **State-wise** | Send to all patients in a specific state |
| **District-wise** | Send to all patients in a specific district |
| **Store-wise** | Send to patients of a specific store |
| **Filter-based** | Send to patients who bought specific product, specific age group, etc. |
| **Individual** | Send to a single patient |

**Message Templates:**
- Pre-built templates for common messages (discount offers, health reminders)
- Custom message creation with variable fields (e.g., {Patient Name}, {Store Name})
- Schedule messages for a future date/time

**Who can send messages?**
- Only users who have "Send Message" permission enabled
- Geography-scoped: State Admin can only message patients in their state

---

### 5.10 Reports & Analytics

**Available Reports:**

| Report | Description | Who Can Access |
|--------|-------------|----------------|
| Sales Report | Total sales by store / district / state / date range | Super Admin, State Admin (own state) |
| Stock Report | Current stock levels at each store | All admins (scoped) |
| Commission Report | Commission earned by each level | Super Admin |
| Patient Report | Total patients registered, new this month | All admins (scoped) |
| Bill Report | All bills generated in a period | All admins (scoped) |
| Expiry Report | Products expiring within 30/60/90 days | All admins (scoped) |
| User Activity Report | Actions taken by any user | Super Admin, System Admin |
| Revenue Report | Total revenue (store/district/state) | Super Admin |
| Low Stock Report | Stores with stock below threshold | Super Admin, Pharmacist |

**Report Features:**
- Filter by date range, geography, product category
- Export as PDF or Excel
- Scheduled auto-reports (e.g., monthly report emailed to Super Admin)
- Visual dashboards with charts and graphs

---

### 5.11 Personal Profile & Verification

Every user has a personal profile page within the platform.

**Profile contains:**
- Profile photo
- Full name, date of birth, gender
- Contact number, email address
- Permanent address (state / district / block)
- Assigned role and geography
- Bank account details (for salary / commission)
- Government ID (Aadhaar / PAN) for KYC verification
- Verification status badge

**What users can do in their profile:**
- Upload/change profile photo
- Update contact information (requires re-verification)
- Add / update bank account (requires document upload + admin approval)
- Upload government ID for identity verification
- Change password
- Request permissions

**Verification Flow:**
```
User submits new information (e.g., bank account)
       ↓
System marks it as "Pending Verification"
       ↓
Super Admin / System Admin is notified
       ↓
Admin reviews and approves / rejects
       ↓
User is notified
       ↓
Information becomes active in the system
```

---

## 6. Complete System Flow

### End-to-End Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         SUPPLY SIDE                             │
│                                                                 │
│  Company → Sir buys medicines → Pharmacist receives at HQ       │
│                                      ↓                          │
│                         Pharmacist logs in inventory            │
│                                      ↓                          │
│                    Pharmacist transfers to stores                │
│                                      ↓                          │
│                    Store confirms stock received                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DEMAND SIDE                              │
│                                                                 │
│  Patient visits Hospital/Clinic                                 │
│       ↓ (Hospital recommends ASK / shares data)                 │
│  Patient data bulk uploaded to ASK platform                     │
│       OR Patient walks into ASK store directly                  │
│       ↓                                                         │
│  Receptionist searches or adds patient                          │
│       ↓                                                         │
│  Health Card issued (if new patient)                            │
│       ↓                                                         │
│  Receptionist creates bill → adds items                         │
│       ↓                                                         │
│  Stock auto-reduces → Bill generated → Patient pays             │
│       ↓                                                         │
│  Revenue recorded for the store                                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   COMMISSION DISTRIBUTION                        │
│                                                                 │
│  Store Revenue                                                  │
│       ↓                                                         │
│  Block Admin gets % commission (same state)                     │
│       ↓                                                         │
│  District Admin gets % commission (same state)                  │
│       ↓                                                         │
│  State Admin gets % commission (same state)                     │
│       ↓                                                         │
│  Super Admin gets % commission                                  │
│                                                                 │
│  (Percentages set by Super Admin, changeable anytime)           │
└─────────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    COMMUNICATION LOOP                           │
│                                                                 │
│  Platform sends bulk messages to patients (SMS/Email/WhatsApp)  │
│  about offers, discounts, health tips                           │
│                                                                 │
│  Patients return to store → Cycle repeats                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. Security Architecture

Security is a core, non-negotiable part of this platform.

### Access Control Rules

| Rule | Details |
|------|---------|
| **No Deletion** | No record can be permanently deleted — only deactivated |
| **Geo-Scoping** | Every user can only see/manage data within their assigned geography |
| **Role Hierarchy** | Users can only manage roles below their own |
| **Permission Control** | Each feature is permission-gated, enabled by Super Admin |
| **Only Super Admin** | Can deactivate/reactivate users, change commission %, access all data |
| **Audit Logs** | Every action by every user is timestamped and logged |
| **Session Management** | Auto-logout after inactivity, one session per user (optional) |

### Authentication & Security

- Login via email + password (or phone OTP option)
- Two-Factor Authentication (2FA) for Admin roles
- Password policy: minimum length, complexity, expiry
- Failed login attempt lockout (after 5 attempts)
- HTTPS enforced on all pages
- All sensitive data (bank accounts, Aadhaar) encrypted at rest
- Role and permission validated on every API request (server-side)
- Activity logs retained for minimum 1 year

### Audit Trail

Every action in the system is recorded:
```
[Timestamp] [User] [Role] [Geography] [Action] [Affected Record]

Example:
2024-11-15 10:32 | Ramesh Kumar | Receptionist | Store-Patna-01 |
Created Bill #B00432 | Patient: Suresh Sharma
```

---

## 8. Additional Features to Include

These features were not in the original brief but are strongly recommended for a complete system:

### 8.1 Dashboard (Role-specific)
Each user sees a personalized dashboard on login:
- Super Admin: Complete system-wide overview — total revenue   (all states), state-wise & district-wise revenue breakdown,  active stores count, total registered patients, new patients  this month, total users by role, active vs deactivated users,  inventory summary (low stock stores, expiring products),  pending notifications (stock requests, permission requests,  verification requests), commission distribution summary, top  performing stores, monthly sales trend chart, bulk messaging  activity, and pharmacist transfer status.
- State Admin: State-level stats (stores, revenue, patients)
- Receptionist: Today's sales, low stock alerts, pending tasks
- Pharmacist: Pending transfers, stock levels at stores

### 8.2 Store / Centre Management
- Create and manage store profiles
- Store address, operating hours, assigned receptionist & volunteer
- Store-wise performance metrics

### 8.3 Product Return & Refund
- Patient can return a product within X days
- Receptionist raises return request
- Stock re-added on approval
- Refund recorded

### 8.4 Hospital/Clinic Partner Management
- Maintain a directory of tied-up hospitals and clinics
- Track how many patients came through each hospital
- Manage bulk data upload from each hospital

### 8.5 Scheme & Discount Management
- Super Admin creates discount schemes (e.g., 10% off on baby food in July)
- Schemes can be geography-specific, product-category-specific, or time-limited
- Applied automatically at billing

### 8.6 Mobile Responsiveness
- All modules must work on mobile browsers (for field staff)
- Receptionist should be able to create bills on a tablet or smartphone

### 8.7 Multi-language Support (Future)
- Hindi + English as primary languages
- Support for regional languages (Bhojpuri, Maithili) in future

### 8.8 Data Backup & Recovery
- Automated daily database backups
- Point-in-time recovery option
- Super Admin can request data export anytime

### 8.9 Help & Support Section
- In-platform user manual / documentation
- Super Admin can raise support tickets for technical issues
- FAQ section for common user questions

---

## Summary — What This Platform Manages

| Area | What the Platform Does |
|------|----------------------|
| **People** | Manages 8 types of users across 200+ stores in 3 states |
| **Products** | Tracks medicines and health items from company purchase to patient sale |
| **Patients** | Registers lakhs of patients, issues health cards, manages families |
| **Billing** | Generates bills at stores, updates stock, saves patient purchase history |
| **Money** | Distributes commissions geographically up the hierarchy |
| **Communication** | Sends bulk messages (SMS/Email/WhatsApp) to patients |
| **Security** | Geo-scoped permissions, no deletion, full audit trail |
| **Decisions** | Gives Super Admin full control over commissions, permissions, reports |

---

*Document prepared for: Apna Swasthya Kendra (ASK) Internal Project Planning*
*Version: 1.0 — Initial Comprehensive Description*
