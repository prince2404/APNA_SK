# Apna Swasthya Kendra (ASK) — Retail & Inventory ERP Platform

Apna Swasthya Kendra (ASK) is a state-of-the-art pharmacy and chain inventory management system. It provides dynamic dashboards, patient KYC registration, digital health cards (with dynamic QR code generation), automated POS billing, stock transfer orders, commission tracking, notifications, and bulk template-driven dispatches.

---

## 🛠️ Technology Stack
* **Backend**: Java 21, Spring Boot 3.x, JPA/Hibernate, Spring Security (Stateless JWT, BCrypt, Rate Limiting), Flyway Migrations, MySQL.
* **Frontend**: React 19, Vite, Tailwind CSS v4, Lucide Icons, Axios.
* **Orchestration**: Docker, Docker Compose.

---

## 🚀 Quick Start (Local Run)

### Option 1: Docker Compose (Recommended)
Orchestrate the entire platform (MySQL + Spring Boot Backend) with a single command:
```bash
docker-compose up --build
```
* **Database**: Runs on `localhost:3306` (seeds schema automatically).
* **Backend API**: Runs on `localhost:8080/api/` (seeds Super Admin on startup).
* **Frontend**: Navigate to the `frontend/` directory and run `npm run dev` to view the UI.

---

### Option 2: Bare Metal Local Execution

#### 1. Database Setup
Ensure you have MySQL running and create the database:
```sql
CREATE DATABASE ask_db;
```

#### 2. Backend Boot
Configure your environmental parameters (copy `backend/.env.example` to `backend/.env` if applicable) and run:
```bash
cd backend
mvn clean spring-boot:run
```

#### 3. Frontend Boot
Install dependencies and trigger Vite development server:
```bash
cd frontend
npm install
npm run dev
```

---

## 📋 Environment Variables Checklist

Ensure these variables are defined in your deployment environment or local system variables:

| Component | Env Variable | Purpose |
|---|---|---|
| **Database** | `DB_URL` | JDBC Connection String |
| | `DB_USERNAME` | Database username |
| | `DB_PASSWORD` | Database password |
| **Security** | `JWT_SECRET` | 256-bit+ HMAC signature key for JWT tokens |
| | `AES_SECRET_KEY` | 32-character key for patient bank details encryption |
| **System** | `SUPER_ADMIN_EMAIL` | Default credentials generated on startup |
| | `SUPER_ADMIN_PASSWORD` | Default credentials password |
| | `CORS_ALLOWED_ORIGINS` | Permitted client origins (comma-separated) |
| **Email/SMS**| `BREVO_API_KEY` | API Key for Brevo email dispatch |
| | `BREVO_SENDER_EMAIL`| From-address for system emails |
| | `MSG91_AUTH_KEY` | Auth Token for SMS broadcasting |

---

## 🛡️ Production Deployment Notes

Refer to **[DEPLOY.md](file:///c:/APNA_SK/DEPLOY.md)** for a step-by-step deployment guide. Key production configurations include:

### 1. Security Checkpoints
* Enable CORS on the backend mapping strictly to your production domain: `ask.cors.allowed-origins=https://app.yourdomain.com`
* Enforce SSL/HTTPS on all API calls and frontend traffic.
* Restrict sensitive endpoint access using `@PreAuthorize("hasAnyRole(...)")`.

### 2. Database Backup Routine
Set up a daily cron job to run backups using `mysqldump`:
```bash
mysqldump -h <host> -u <user> -p<password> ask_db > ask_backup_$(date +%F).sql
```

### 3. Monitoring & Health Checks
Enable Spring Boot Actuator health checks at `/api/actuator/health` and monitor server availability using service monitors (e.g. UptimeRobot, Prometheus).
