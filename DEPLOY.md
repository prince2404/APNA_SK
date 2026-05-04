# Apna Swasthya Kendra — Production Deployment Guide

This guide takes you from a working local project to a live application on the internet, step by step.

---

## 1. Recommended Platforms

| Component | Platform | Why |
|-----------|----------|-----|
| **Backend** (Spring Boot JAR) | [Railway](https://railway.com) | Free tier includes 500 hours/month, auto-deploys from GitHub, built-in Java support, easy env vars |
| **Database** (MySQL) | [Railway](https://railway.com) | One-click MySQL setup, same network as backend (fast), free tier included |
| **Frontend** (React build) | [Vercel](https://vercel.com) | Free forever for personal projects, instant global CDN, auto-deploys from GitHub, built for React |
| **File Storage** (documents/images) | [Cloudinary](https://cloudinary.com) | Free tier: 25GB storage + 25GB bandwidth, easy API, image transformations built-in |
| **Domain Name** | [Namecheap](https://namecheap.com) or [GoDaddy](https://godaddy.com) | Domains start at ₹199/year for `.in`, good DNS management |

> **Total cost to start: ₹0** (all platforms have free tiers that are sufficient for launch)

---

## 2. What You Need Before Starting

### Accounts to Create (all free)

| # | Account | Sign Up Link | What it's for |
|---|---------|-------------|---------------|
| 1 | **GitHub** | https://github.com/signup | Host your code, auto-deploy to Railway + Vercel |
| 2 | **Railway** | https://railway.com | Backend server + MySQL database |
| 3 | **Vercel** | https://vercel.com/signup | Frontend hosting |
| 4 | **Gmail** | You already have this | SMTP email for sending OTPs |

### Before You Start Checklist

- [ ] Your code is pushed to a **GitHub repository** (private is fine)
- [ ] You have a **Gmail App Password** for SMTP (not your regular password)
- [ ] You have decided on a **Super Admin email and password** for production

### How to Get a Gmail App Password

1. Go to https://myaccount.google.com/security
2. Enable **2-Step Verification** if not already enabled
3. Go to https://myaccount.google.com/apppasswords
4. Select **Mail** → **Other** → type "ASK ERP"
5. Click **Generate** → copy the 16-character password
6. This is your `MAIL_PASSWORD` for production

---

## 3. Prepare the Project for Deployment

### 3.1 Push Code to GitHub

If you haven't already:

```bash
cd c:\APNA_SK
git init
git add .
git commit -m "Initial commit - ASK ERP"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/APNA_SK.git
git push -u origin main
```

### 3.2 Add a Production CORS Configuration

Your backend currently has no CORS config (works locally because Vite proxy handles it). In production, frontend and backend are on different domains, so you **must** add CORS support.

Create this file:

**`backend/src/main/java/com/ask/config/CorsConfig.java`**

```java
package com.ask.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${ask.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

Then update `SecurityConfig.java` to enable CORS:

Add this import at the top:
```java
import org.springframework.web.cors.CorsConfigurationSource;
```

Add this field:
```java
private final CorsConfigurationSource corsConfigurationSource;
```

Add this line inside `filterChain()`, right after `.csrf(AbstractHttpConfigurer::disable)`:
```java
.cors(cors -> cors.configurationSource(corsConfigurationSource))
```

Add the environment variable to your `.env.example`:
```
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

And add this to `application.properties`:
```
ask.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:5173}
```

### 3.3 Add Production Logging Profile

Create **`backend/src/main/resources/application-prod.properties`**:

```properties
# =============================================================================
# ASK — Production Profile
# =============================================================================

# Reduce logging in production
logging.level.root=WARN
logging.level.com.ask=INFO
logging.level.org.springframework.security=WARN
logging.level.org.hibernate.SQL=WARN

# Show SQL in production? No.
spring.jpa.show-sql=false
```

### 3.4 Build the Backend JAR (for testing locally)

```bash
cd backend
mvn clean package -DskipTests
```

This creates `backend/target/apna-swasthya-kendra-1.0.0.jar`.  
Railway does this automatically, but this verifies your project builds.

### 3.5 Build the Frontend (for testing locally)

```bash
cd frontend
bun run build
```

This creates the `frontend/dist/` folder.  
Vercel does this automatically, but this verifies your project builds.

---

## 4. Deploy the Database (Railway)

### Step 1: Create a Railway Project

1. Go to https://railway.com and log in
2. Click **"New Project"**
3. Select **"Empty Project"**
4. Name it `ask-production`

### Step 2: Add MySQL Database

1. Inside your project, click **"+ New"** → **"Database"** → **"MySQL"**
2. Railway instantly creates a MySQL instance
3. Click on the MySQL service → go to **"Variables"** tab
4. You will see these auto-generated variables:

| Variable | Example Value |
|----------|---------------|
| `MYSQL_HOST` | `roundhouse.proxy.rlwy.net` |
| `MYSQL_PORT` | `39145` |
| `MYSQL_DATABASE` | `railway` |
| `MYSQL_USER` | `root` |
| `MYSQL_PASSWORD` | `aBcDeFgHiJkLmNoP` |
| `MYSQL_URL` | `mysql://root:aBcD...@roundhouse.proxy.rlwy.net:39145/railway` |

5. **Copy these values** — you need them for the backend deployment.

### Step 3: Build Your Production DB_URL

Using the values above, construct your `DB_URL`:

```
jdbc:mysql://MYSQL_HOST:MYSQL_PORT/MYSQL_DATABASE?useSSL=true&serverTimezone=Asia/Kolkata
```

Example:
```
jdbc:mysql://roundhouse.proxy.rlwy.net:39145/railway?useSSL=true&serverTimezone=Asia/Kolkata
```

> **Note:** Flyway will automatically create all tables and seed data on the first backend startup. You do NOT need to run any SQL manually.

---

## 5. Deploy the Backend (Railway)

### Step 1: Add Backend Service

1. In your Railway project, click **"+ New"** → **"GitHub Repo"**
2. Connect your GitHub account if not already connected
3. Select your `APNA_SK` repository
4. Railway will detect the project — it may ask which folder to use

### Step 2: Configure the Build

1. Click on the newly created service → go to **"Settings"** tab
2. Set these build settings:

| Setting | Value |
|---------|-------|
| **Root Directory** | `backend` |
| **Build Command** | `mvn clean package -DskipTests` |
| **Start Command** | `java -jar -Dspring.profiles.active=prod target/apna-swasthya-kendra-1.0.0.jar` |

### Step 3: Set Environment Variables

Go to the **"Variables"** tab and add every variable:

| Variable | Value |
|----------|-------|
| `DB_URL` | `jdbc:mysql://MYSQL_HOST:MYSQL_PORT/MYSQL_DATABASE?useSSL=true&serverTimezone=Asia/Kolkata` |
| `DB_USERNAME` | Your Railway MySQL username (from step 4) |
| `DB_PASSWORD` | Your Railway MySQL password (from step 4) |
| `JWT_SECRET` | A random 64+ character string (generate at https://randomkeygen.com) |
| `AES_SECRET_KEY` | A random exactly 32 character string |
| `MAIL_HOST` | `smtp.gmail.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | Your Gmail address |
| `MAIL_PASSWORD` | Your Gmail App Password (the 16-char one) |
| `SUPER_ADMIN_EMAIL` | The admin email for production |
| `SUPER_ADMIN_PASSWORD` | A strong admin password |
| `CORS_ALLOWED_ORIGINS` | `https://your-app.vercel.app` (update after deploying frontend) |
| `SPRING_PROFILES_ACTIVE` | `prod` |

> **Tip:** Generate `JWT_SECRET` by running this in your terminal:
> ```bash
> node -e "console.log(require('crypto').randomBytes(48).toString('hex'))"
> ```
>
> Generate `AES_SECRET_KEY` (exactly 32 chars):
> ```bash
> node -e "console.log(require('crypto').randomBytes(16).toString('hex'))"
> ```

### Step 4: Deploy

1. Click **"Deploy"** — Railway builds and deploys automatically
2. Wait 2-3 minutes for the build and startup

### Step 5: Get Your Backend URL

1. Go to **"Settings"** → **"Networking"**
2. Click **"Generate Domain"**
3. Railway gives you a URL like: `ask-production-backend.up.railway.app`
4. **Your production API base URL is:** `https://ask-production-backend.up.railway.app/api`

### Step 6: Verify Backend is Running

Open this in your browser:
```
https://YOUR-BACKEND.up.railway.app/api/actuator/health
```

If you see a response (even a 401), the server is running.

### Step 7: Check Logs

1. In Railway, click on your backend service
2. Go to the **"Logs"** tab
3. Look for:
   - `Started AskApplication in X seconds` → ✅ Backend started
   - `Super Admin account created successfully` → ✅ Admin seeded
   - `Schema ask_db is up to date` → ✅ Flyway migrations ran
4. If you see errors, the logs tell you exactly what's wrong

---

## 6. Deploy the Frontend (Vercel)

### Step 1: Log in to Vercel

1. Go to https://vercel.com
2. Click **"Add New Project"**
3. Connect your GitHub account
4. Select your `APNA_SK` repository

### Step 2: Configure the Build

| Setting | Value |
|---------|-------|
| **Framework Preset** | Vite |
| **Root Directory** | `frontend` |
| **Build Command** | `bun run build` (or `npm run build`) |
| **Output Directory** | `dist` |

### Step 3: Set the Environment Variable

Add this environment variable:

| Variable | Value |
|----------|-------|
| `VITE_API_BASE_URL` | `https://YOUR-BACKEND.up.railway.app/api` |

Replace `YOUR-BACKEND` with your actual Railway backend URL from Step 5.5 above.

### Step 4: Deploy

1. Click **"Deploy"**
2. Vercel builds and deploys in under 1 minute
3. You get a live URL like: `https://ask-erp.vercel.app`

### Step 5: Verify Frontend is Live

Open the Vercel URL in your browser. You should see the ASK login page.

---

## 7. Connect Everything

### 7.1 Update Backend CORS with Frontend URL

Now that you have your Vercel URL, go back to Railway:

1. Click on your backend service → **"Variables"** tab
2. Update `CORS_ALLOWED_ORIGINS` to your Vercel URL:
   ```
   https://ask-erp.vercel.app
   ```
   If you have multiple domains (e.g., custom domain too), separate with commas:
   ```
   https://ask-erp.vercel.app,https://app.askhealth.in
   ```
3. Railway will automatically redeploy with the new variable

### 7.2 Test the Connection

1. Open your Vercel URL in a browser
2. Open browser DevTools → Network tab
3. Try logging in with your Super Admin credentials
4. Check that the login request goes to your Railway backend URL
5. If you see CORS errors, double-check the `CORS_ALLOWED_ORIGINS` variable

### 7.3 Verify API Communication

Login should work end-to-end:
1. Enter Super Admin email and password
2. If 2FA is enabled, check your email for OTP
3. Enter OTP → redirected to dashboard
4. Dashboard loads stats from the backend API

If the login works, **frontend ↔ backend ↔ database** are all connected. 🎉

---

## 8. Custom Domain (Optional)

### 8.1 Buy a Domain

1. Go to https://namecheap.com
2. Search for your domain (e.g., `askhealth.in`)
3. Purchase it (`.in` domains start at ₹199/year)

### 8.2 Connect Domain to Frontend (Vercel)

1. In Vercel, go to your project → **"Settings"** → **"Domains"**
2. Click **"Add Domain"**
3. Enter your domain: `app.askhealth.in`
4. Vercel shows you DNS records to add. Go to your domain registrar:

| Type | Name | Value |
|------|------|-------|
| CNAME | `app` | `cname.vercel-dns.com` |

5. Wait 5-10 minutes for DNS propagation
6. Vercel automatically provisions an SSL certificate (HTTPS)

### 8.3 Connect Domain to Backend (Railway)

1. In Railway, go to backend service → **"Settings"** → **"Networking"**
2. Click **"Custom Domain"**
3. Enter: `api.askhealth.in`
4. Add this DNS record at your registrar:

| Type | Name | Value |
|------|------|-------|
| CNAME | `api` | Your Railway domain (e.g., `ask-production-backend.up.railway.app`) |

5. Wait for DNS propagation

### 8.4 Update Environment Variables

After custom domains are live, update these:

**Railway (Backend):**
```
CORS_ALLOWED_ORIGINS=https://app.askhealth.in,https://ask-erp.vercel.app
```

**Vercel (Frontend):**
```
VITE_API_BASE_URL=https://api.askhealth.in/api
```

Redeploy both after updating.

---

## 9. Verify Deployment Checklist

After everything is deployed, go through this checklist:

| # | Check | How to Test | Expected Result |
|---|-------|-------------|-----------------|
| 1 | Frontend loads | Open Vercel URL in browser | Login page appears |
| 2 | Login works | Enter Super Admin credentials | Redirected to OTP or dashboard |
| 3 | 2FA works | Enter OTP from email | Redirected to dashboard |
| 4 | Dashboard loads data | Check stat cards on dashboard | Numbers appear (states, stores, etc.) |
| 5 | API calls reach backend | Browser DevTools → Network tab | Requests go to Railway URL, return 200 |
| 6 | Database works | Create a new user from the dashboard | User appears in the user list |
| 7 | Emails send | Login to trigger OTP email | OTP email arrives in your inbox |
| 8 | No CORS errors | Browser DevTools → Console tab | No red CORS error messages |
| 9 | HTTPS works | Check the padlock icon in browser | Green padlock on both frontend and backend URLs |
| 10 | Mobile works | Open Vercel URL on your phone | Login page loads and is responsive |

---

## 10. Common Deployment Errors and Fixes

### ❌ `Build failed` on Railway

**Cause:** Maven can't build the project.  
**Fix:** Make sure `Root Directory` is set to `backend` in Railway settings. Run `mvn clean package -DskipTests` locally to verify it builds.

---

### ❌ `Communications link failure` or `Unable to connect to database`

**Cause:** Backend can't reach the MySQL database.  
**Fix:** Double-check `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in Railway variables. Make sure you're using the Railway MySQL host/port, not `localhost:3306`.

---

### ❌ `CORS error: No 'Access-Control-Allow-Origin' header`

**Cause:** Backend is rejecting requests from the frontend domain.  
**Fix:** Check `CORS_ALLOWED_ORIGINS` in Railway variables. It must exactly match your frontend URL including `https://`. No trailing slash.

```
✅ https://ask-erp.vercel.app
❌ https://ask-erp.vercel.app/
❌ http://ask-erp.vercel.app
```

---

### ❌ `404 Not Found` when refreshing a page on Vercel

**Cause:** React Router uses client-side routing. Vercel doesn't know about your routes.  
**Fix:** Create `frontend/vercel.json`:

```json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
```

Commit and push — Vercel will auto-redeploy.

---

### ❌ `Could not resolve placeholder 'JWT_SECRET'`

**Cause:** Environment variables are not set in Railway.  
**Fix:** Go to Railway → your backend service → Variables tab → add all missing variables from section 5.3 above.

---

### ❌ `Flyway migration checksum mismatch`

**Cause:** A migration file was modified after it already ran.  
**Fix:** In the Railway MySQL instance, connect via the provided connection string and run:

```sql
DELETE FROM flyway_schema_history WHERE success = 0;
```

Then redeploy the backend.

---

### ❌ Emails not sending (OTP not received)

**Cause:** Gmail is blocking the SMTP connection.  
**Fix:**
1. Make sure you're using a Gmail **App Password**, not your regular password
2. Make sure `MAIL_HOST` is `smtp.gmail.com` and `MAIL_PORT` is `587`
3. Check Railway logs for the exact email error

---

### ❌ Frontend shows `Network Error` on all API calls

**Cause:** `VITE_API_BASE_URL` is wrong or backend is down.  
**Fix:**
1. Check if backend is running: visit `https://YOUR-BACKEND.up.railway.app/api/v1/auth/login` — you should get a 405 Method Not Allowed (not a connection error)
2. Verify `VITE_API_BASE_URL` in Vercel includes `/api` at the end
3. **Important:** After changing a `VITE_` variable, you must redeploy on Vercel (Vite bakes env vars into the build at build time)

---

### ❌ Railway deploy works but app crashes on startup

**Cause:** Java heap memory issue on free tier.  
**Fix:** Add this environment variable in Railway:

```
JAVA_OPTS=-Xmx256m -Xms128m
```

And update your start command to:

```
java $JAVA_OPTS -jar -Dspring.profiles.active=prod target/apna-swasthya-kendra-1.0.0.jar
```

---

## 11. After Deployment

### View Live Logs

**Backend logs (Railway):**
1. Go to https://railway.com → your project → backend service
2. Click the **"Logs"** tab
3. Logs stream in real-time

### Redeploy When Code is Updated

**Both Railway and Vercel auto-deploy when you push to GitHub:**

```bash
git add .
git commit -m "Fix: description of change"
git push origin main
```

Railway rebuilds the backend automatically (takes ~2-3 minutes).  
Vercel rebuilds the frontend automatically (takes ~30 seconds).

### Take a Database Backup

**Option 1 — From Railway UI:**
1. Click on your MySQL service in Railway
2. Go to **"Data"** tab
3. Use the query editor to export data

**Option 2 — Using mysqldump from your local machine:**

```bash
mysqldump -h RAILWAY_HOST -P RAILWAY_PORT -u root -pRAILWAY_PASSWORD railway > backup_$(date +%Y%m%d).sql
```

Replace `RAILWAY_HOST`, `RAILWAY_PORT`, and `RAILWAY_PASSWORD` with values from your Railway MySQL Variables tab.

### Monitor If the Server Goes Down

**Free uptime monitoring with UptimeRobot:**

1. Go to https://uptimerobot.com and create a free account
2. Click **"Add New Monitor"**
3. Set:
   - **Monitor Type:** HTTP(s)
   - **Friendly Name:** ASK Backend
   - **URL:** `https://YOUR-BACKEND.up.railway.app/api/actuator/health`
   - **Monitoring Interval:** 5 minutes
4. Add your email for alerts
5. Repeat for the frontend URL

You will get an email notification within 5 minutes if either server goes down.

---

## Quick Reference Card

Once deployed, bookmark these URLs:

| What | URL |
|------|-----|
| **Frontend (live app)** | `https://ask-erp.vercel.app` |
| **Backend API** | `https://YOUR-BACKEND.up.railway.app/api` |
| **Railway Dashboard** | `https://railway.com/dashboard` |
| **Vercel Dashboard** | `https://vercel.com/dashboard` |
| **Backend Logs** | Railway → Project → Backend → Logs tab |
| **Database Admin** | Railway → Project → MySQL → Data tab |

---

**Congratulations!** Your Apna Swasthya Kendra ERP is now live on the internet. 🚀
