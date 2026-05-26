# Production Deployment Guide — Free Forever Stack

This guide takes you through the deployment of the ASK application using **120% free-forever** hosting platforms that require **no credit card** and will **never expire**.

- **Frontend** → **Vercel** (Free forever)
- **Backend** → **Render (Docker)** (Free forever)
- **Database** → **TiDB Cloud Serverless (MySQL)** (Free forever, 5GB storage)
- **Pinger** → **UptimeRobot** (Free forever, keeps Render backend awake)

> [!IMPORTANT]
> **PlanetScale Free Tier Notice**
> PlanetScale discontinued their free hobby tier in April 2024 (now costs minimum $39/month). To keep your deployment **100% free forever**, we will use **TiDB Cloud (Serverless)**. It is fully MySQL-compatible, completely free, and requires no credit card.

---

## 1. Set Up MySQL on TiDB Cloud (Free Forever)

1. **Sign Up**: Create an account on [TiDB Cloud](https://tidbcloud.com/) (no card required).
2. **Create Cluster**: Click **"Create Cluster"** and select **"Serverless"** (Free tier, includes 5GB storage).
3. **Configure**: Name it `ask-db`, select a region near your users, and click **"Create"**.
4. **Get Connection Credentials**:
   - Once the cluster is active, click **"Connect"** in the top right.
   - Set connection method to **"Spring Boot (JDBC)"** or **"General Connection"**.
   - Copy the following details:
     - **Host** (e.g., `gateway01.us-east-1.prod.aws.tidbcloud.com`)
     - **Port** (e.g., `4000`)
     - **Database** (e.g., `ask_db` or default `test`)
     - **User** (e.g., `3x7K2...root`)
     - **Password** (auto-generated during cluster creation)
   - Connection URL format:
     `jdbc:mysql://YOUR_TIDB_HOST:4000/YOUR_DB_NAME?sslMode=VERIFY_IDENTITY&useSSL=true&allowPublicKeyRetrieval=true`

---

## 2. Deploy Backend on Render (Free Forever)

We will deploy our backend on Render using the **Docker** runtime. Render will automatically read the `backend/Dockerfile` in our project, build the image, and run it.

1. **Sign Up**: Log into [Render](https://render.com/).
2. **Create Service**: Click **"New +"** → **"Web Service"**.
3. **Connect Repo**: Select your `APNA_SK` GitHub repository.
4. **Configure Service**:
   - **Name**: `ask-backend`
   - **Region**: Select your preferred region.
   - **Branch**: `main`
   - **Root Directory**: **`backend`**
   - **Runtime**: **`Docker`** (Build/Start command fields will disappear as Render builds from the Dockerfile).
   - **Instance Type**: `Free` (512MB RAM).
5. **Environment Variables**: Click **"Advanced"** and add:

| Name | Value |
|---|---|
| `DB_URL` | `jdbc:mysql://YOUR_TIDB_HOST:4000/YOUR_DB_NAME?sslMode=VERIFY_IDENTITY&useSSL=true&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | *Your TiDB User string* |
| `DB_PASSWORD` | *Your TiDB Password string* |
| `JWT_SECRET` | *A random string of 64+ characters* |
| `AES_SECRET_KEY` | *A random string of **exactly 32 characters*** |
| `BREVO_API_KEY` | *Your Brevo API Key* |
| `BREVO_SENDER_EMAIL`| *Your Brevo verified sender email* |
| `BREVO_SENDER_NAME` | `Apna Swasthya Kendra` |
| `SUPER_ADMIN_EMAIL` | *e.g., `admin@yourdomain.com`* |
| `SUPER_ADMIN_PASSWORD`| *Choose a strong password* |
| `CORS_ALLOWED_ORIGINS`| `http://localhost:5173` *(We will update this in Step 4)* |

6. Click **"Create Web Service"**.
7. **Verify**: Wait for build logs to show `Started AskApplication`. Copy the Render Web Service URL (e.g. `https://ask-backend.onrender.com`) and verify `https://ask-backend.onrender.com/api/actuator/health` returns `{"status":"UP"}`.

---

## 3. Deploy Frontend on Vercel (Free Forever)

1. **Sign Up**: Log into [Vercel](https://vercel.com/) with GitHub.
2. **Import Project**: Click **"Add New"** → **"Project"** and import `APNA_SK`.
3. **Configure**:
   - **Framework Preset**: `Vite`
   - **Root Directory**: Choose **`frontend`**.
4. **Environment Variables**: Add:

| Name | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://ask-backend.onrender.com/api` (use your Render URL) |

5. Click **"Deploy"**. Vercel will build the frontend and provide a URL (e.g., `https://ask-frontend.vercel.app`).

---

## 4. Connecting Everything Together

### Step 1: Configure CORS on Render
1. Go to Render → select `ask-backend` → **"Environment"**.
2. Change `CORS_ALLOWED_ORIGINS` to your Vercel URL:
   - Value: `https://ask-frontend.vercel.app` (no trailing slash).
3. Save changes. Render will redeploy.

### Step 2: Sync API Base URL on Vercel
1. Go to Vercel → select `ask-frontend` → **"Settings"** → **"Environment Variables"**.
2. Verify `VITE_API_BASE_URL` matches your Render URL + `/api` suffix.
3. Go to the **"Deployments"** tab, click the three dots next to your top deployment, and select **"Redeploy"** to re-compile Vite with the corrected environment variable.

### Step 3: Test Login
1. Go to your Vercel URL.
2. Log in using the `SUPER_ADMIN_EMAIL` and `SUPER_ADMIN_PASSWORD` you set in Render.

---

## 5. Keeping Backend Awake with UptimeRobot (Free Forever)

Render's Free Web Services go to sleep after 15 minutes of inactivity. We will use UptimeRobot to ping the service every 14 minutes, keeping it awake 24/7.

1. **Sign Up**: Create an account on [UptimeRobot](https://uptimerobot.com/).
2. **Add Monitor**: Click **"Add New Monitor"**.
3. **Configure**:
   - **Monitor Type**: `HTTPS`
   - **Friendly Name**: `ASK Backend Pinger`
   - **URL (or IP)**: Enter `https://ask-backend.onrender.com/api/actuator/health`
   - **Monitoring Interval**: Set to **`Every 14 minutes`**.
4. Click **"Create Monitor"**.
