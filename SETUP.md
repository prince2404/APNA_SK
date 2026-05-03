# Apna Swasthya Kendra (ASK) — Local Setup Guide

Welcome to the ASK project! This guide will walk you through setting up the complete application (Frontend + Backend) on your local machine from scratch.

---

## 1. Prerequisites
Before you begin, ensure you have the following tools installed on your system. Please install the exact or newer versions specified.

| Tool | Version | Download Link |
|------|---------|---------------|
| **Java (JDK)** | 21 | [Download JDK 21](https://adoptium.net/temurin/releases/?version=21) |
| **Apache Maven** | 3.9+ | [Download Maven](https://maven.apache.org/download.cgi) |
| **Node.js** | 20+ | [Download Node.js](https://nodejs.org/) |
| **Bun** | 1.1+ | [Download Bun](https://bun.sh/) |
| **MySQL** | 8.0+ | [Download MySQL](https://dev.mysql.com/downloads/mysql/) |

*Note: Verify your installations by running `java -version`, `mvn -v`, `node -v`, `bun -v`, and `mysql -V` in your terminal.*

---

## 2. Clone the Repository
Open your terminal and clone the repository to your local machine:

```bash
git clone <your-repository-url>
cd apna-swasthya-kendra
```

---

## 3. Environment Setup

The project uses environment variables (`.env` files) to keep secrets secure. You need to configure these for both the backend and frontend.

### Backend Setup
Navigate to the backend folder and copy the example environment file:
```bash
cd backend
cp .env.example .env
```

Open `backend/.env` in your code editor and fill in the following values:

| Variable | What to put for local development |
|----------|-----------------------------------|
| `DB_USERNAME` | Your MySQL username (usually `root`) |
| `DB_PASSWORD` | Your MySQL password |
| `JWT_SECRET` | Any long random string (min 64 characters) |
| `AES_SECRET_KEY` | Any random string of **exactly 32 characters** |
| `MAIL_USERNAME` | Your Gmail address (for testing emails) |
| `MAIL_PASSWORD` | Your Gmail **App Password** (not your real password) |
| `SUPER_ADMIN_EMAIL` | An email for your admin account (e.g. `admin@local.com`) |
| `SUPER_ADMIN_PASSWORD` | A password for your admin account (e.g. `Admin123!`) |

### Frontend Setup
Open a new terminal, navigate to the frontend folder, and copy the example environment file:
```bash
cd frontend
cp .env.example .env
```
For local development, the default value in `.env.example` (`VITE_API_BASE_URL=/api`) is perfectly fine. You don't need to change anything unless you are deploying.

---

## 4. Database Setup

You need to create an empty database in MySQL. Open MySQL Workbench or your MySQL command line and run:

```sql
CREATE DATABASE ask_db;
```

**That's it!** You do **not** need to run any SQL scripts manually. When you start the backend, **Flyway** will automatically run all database migrations, create the tables, and insert default data (like states and roles).

---

## 5. Running the Backend

Open a terminal, navigate to the `backend` folder, and run the Spring Boot application using Maven:

```bash
cd backend
mvn clean spring-boot:run
```

*Wait until you see `Started AskApplication in ... seconds` in the console.*

---

## 6. Running the Frontend

Open a second terminal, navigate to the `frontend` folder, install the dependencies, and start the development server:

```bash
cd frontend
bun install
bun run dev
```

*The console will show a local URL (usually `http://localhost:5173/`). Click it to open the app in your browser.*

---

## 7. First Login

When you successfully start the backend for the first time, a special process called `SuperAdminSeeder` runs automatically.

It looks at the `SUPER_ADMIN_EMAIL` and `SUPER_ADMIN_PASSWORD` you provided in your `backend/.env` file and creates the Super Admin account in the database for you.

To log in:
1. Open the frontend URL (`http://localhost:5173/`)
2. Enter the **Email** and **Password** you set in `backend/.env`
3. You will have full Super Admin access to the system!

---

## 8. Common Errors and Fixes

If you run into issues, check this list of common developer problems:

#### ❌ Error: `Web server failed to start. Port 8080 was already in use.`
* **Cause**: Another process (or a previous crashed run) is using port 8080.
* **Fix**: Stop the process using the port, or restart your computer. Alternatively, change `server.port=8081` in `application.properties`.

#### ❌ Error: `Access denied for user 'root'@'localhost'`
* **Cause**: Incorrect MySQL username or password in your backend `.env` file.
* **Fix**: Double-check `DB_USERNAME` and `DB_PASSWORD` in `backend/.env`.

#### ❌ Error: `Could not resolve placeholder 'JWT_SECRET' in value "${JWT_SECRET}"`
* **Cause**: Spring Boot cannot find your environment variables.
* **Fix**: You forgot to create the `backend/.env` file, or you named it incorrectly (e.g. `.env.txt`). Run `cp .env.example .env` in the backend folder.

#### ❌ Error: `Process terminated with exit code: 1` during `mvn spring-boot:run`
* **Cause**: Stale generated files (like MapStruct mappers) in your `target` folder are causing compilation conflicts.
* **Fix**: Always include the `clean` command. Run: `mvn clean spring-boot:run`.

#### ❌ Error: Frontend shows `Network Error` or `502 Bad Gateway`
* **Cause**: The backend server is not running or is still starting up.
* **Fix**: Ensure the backend terminal shows "Started AskApplication" before trying to log in on the frontend.
