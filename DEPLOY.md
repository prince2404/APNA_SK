# Production Deployment Guide — AWS Free Tier

This guide takes you through deploying the ASK application to a production-grade infrastructure using the **AWS 12-Month Free Tier** for the backend and database, combined with **Vercel** (free forever) for the frontend.

> [!WARNING]
> **AWS Free Tier Notice**
> AWS is the industry gold standard for hosting, but it is **only free for the first 12 months**. After 12 months, or if you exceed the free tier usage limits, you will be billed. You must provide a credit card during AWS sign-up. 
> To keep the setup 100% free, ensure you choose the **"Free Tier Eligible"** options during database and server creation as detailed below.

---

## Production Architecture

```mermaid
graph TD
    User[Web Browser] -->|Accesses Frontend over HTTPS| Vercel[Vercel (React Frontend)]
    Vercel -->|Secure HTTPS API Requests| Nginx[Nginx Reverse Proxy & SSL]
    subgraph AWS EC2 Instance (Ubuntu t2.micro)
        Nginx -->|Decrypts & Forwards to port 8080| DockerContainer[Docker Container (Spring Boot API)]
    end
    DockerContainer -->|Database Queries on port 3306| AWS_RDS[AWS RDS (MySQL db.t3.micro)]
```

---

## Prerequisites
Before starting, ensure you have:
1. An **AWS Account** ([Sign Up here](https://aws.amazon.com/free/)).
2. A **GitHub Account** holding your repository `APNA_SK`.
3. A **Vercel Account** ([Sign Up here](https://vercel.com/)).
4. A **Custom Domain** (e.g., `apnask.com`) purchased from GoDaddy, Namecheap, etc. (Required to configure SSL/HTTPS on the backend to avoid browser Mixed Content security blocks).
5. A **Brevo Account** (free-forever email provider) to send OTPs.

---

## 1. Setting Up MySQL on AWS RDS (Free Tier)

AWS RDS (Relational Database Service) is a managed database service. Under the Free Tier, you get 750 hours/month of a single `db.t2.micro` or `db.t3.micro` MySQL instance, with 20GB of storage.

### Step-by-Step Setup:
1. Log into the **AWS Management Console** and search for **RDS**.
2. Click **"Create database"**.
3. Choose **"Standard create"** and select **"MySQL"** as the Engine type.
4. Under **Templates**, select **"Free Tier"** (this is critical to avoid charges).
5. Under **Settings**:
   - **DB instance identifier**: `ask-production-db`
   - **Master username**: `admin`
   - **Master password**: *Choose a strong password and save it somewhere secure.*
6. Under **Instance configuration**:
   - **DB instance class**: Verify `db.t3.micro` (or `db.t2.micro`) is selected.
7. Under **Storage**:
   - **Storage type**: `General Purpose SSD (gp3)`
   - **Allocated storage**: `20 GiB`
   - **Disable storage autoscaling** (uncheck "Enable storage autoscaling" to prevent costs from scaling up).
8. Under **Connectivity**:
   - **VPC**: Select the Default VPC.
   - **Public access**: Select **"Yes"** (This allows your EC2 backend to connect to it).
   - **VPC security group**: Choose **"Create new"** and name it `ask-rds-sg`.
9. Under **Additional configuration**:
   - **Initial database name**: Enter `ask_db`.
10. Click **"Create database"**. This will take 5-10 minutes.
11. **Configure Database Firewalls**:
    - Go to the RDS Dashboard, click your database, and look under **"Connectivity & security"**.
    - Click on the security group link under **"VPC security groups"** (e.g., `ask-rds-sg`).
    - Select the Security Group, go to the **"Inbound rules"** tab, and click **"Edit inbound rules"**.
    - Add a rule:
      - **Type**: `MYSQL/Aurora` (Port 3306)
      - **Source**: `Anywhere-IPv4` (`0.0.0.0/0`) or select your EC2 instance's private IP once created.
      - Click **"Save rules"**.
12. Copy the **Endpoint** (e.g., `ask-production-db.xxxxx.us-east-1.rds.amazonaws.com`) listed under Connectivity.

---

## 2. Deploying Backend on AWS EC2 (Free Tier)

AWS EC2 (Elastic Compute Cloud) provides virtual servers. The Free Tier includes 750 hours/month of a `t2.micro` (or `t3.micro` depending on region) instance running Linux.

### Step 1: Launch the EC2 Instance
1. Go to the **EC2 Dashboard** and click **"Launch instance"**.
2. **Name**: `ask-backend-server`.
3. **Application and OS Image (AMI)**: Select **"Ubuntu"** (Ubuntu Server 24.04 LTS — Free Tier Eligible).
4. **Instance Type**: Select **"t2.micro"** (or `t3.micro` if in a region where it is free tier).
5. **Key Pair**: Click **"Create new key pair"**. Name it `ask-ssh-key`, format `.pem`, and download it. Keep this file safe (we need it to SSH into the server).
6. **Network Settings**:
   - Check **"Allow SSH traffic from Anywhere"**.
   - Check **"Allow HTTP traffic from the internet"**.
   - Check **"Allow HTTPS traffic from the internet"**.
7. Click **"Launch instance"**.

---

### Step 2: Configure Public DNS
1. Go to your Domain Registrar's DNS settings panel (GoDaddy, Namecheap, etc.).
2. Copy your EC2 instance's **Public IPv4 Address** from the AWS EC2 dashboard.
3. Create an **A Record** pointing a subdomain to your EC2 IP:
   - **Type**: `A`
   - **Host / Name**: `api` (creates `api.yourdomain.com`)
   - **Value / Points to**: *Your EC2 Public IPv4 Address*
   - **TTL**: `Automatic` or `1 Hour`

---

### Step 3: Install Docker, Nginx, and SSL Certificate
1. Open your terminal and connect to your EC2 instance via SSH (replace the key path and IP with your own):
   ```bash
   chmod 400 ask-ssh-key.pem
   ssh -i ask-ssh-key.pem ubuntu@YOUR_EC2_PUBLIC_IP
   ```
2. **Install Docker**:
   ```bash
   sudo apt-get update
   sudo apt-get install -y docker.io
   sudo systemctl start docker
   sudo systemctl enable docker
   sudo usermod -aG docker ubuntu
   newgrp docker
   ```
3. **Install Nginx & Certbot**:
   ```bash
   sudo apt-get install -y nginx certbot python3-certbot-nginx
   ```
4. **Configure Nginx Reverse Proxy**:
   Create a new configuration file:
   ```bash
   sudo nano /etc/nginx/sites-available/ask-backend
   ```
   Paste the following config (replace `api.yourdomain.com` with your actual domain):
   ```nginx
   server {
       listen 80;
       server_name api.yourdomain.com;

       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
   }
   ```
   Enable the site and remove default Nginx pages:
   ```bash
   sudo ln -s /etc/nginx/sites-available/ask-backend /etc/nginx/sites-enabled/
   sudo rm /etc/nginx/sites-enabled/default
   sudo nginx -t
   sudo systemctl restart nginx
   ```
5. **Acquire Let's Encrypt SSL Certificate**:
   Run Certbot to generate and configure the SSL certificate automatically:
   ```bash
   sudo certbot --nginx -d api.yourdomain.com --non-interactive --agree-tos -m your-email@example.com --redirect
   ```
   *Certbot will obtain the certificate, configure HTTPS on port 443, and set up automatic HTTP-to-HTTPS redirection.*

---

### Step 4: Run the Backend Docker Container
1. While connected to the EC2 server, clone your repository and navigate to the backend:
   ```bash
   git clone https://github.com/YOUR_USERNAME/APNA_SK.git
   cd APNA_SK/backend
   ```
2. Create a production environment variable file:
   ```bash
   nano prod.env
   ```
   Paste and configure the variables (ensure no spaces around `=`):
   ```env
   DB_URL=jdbc:mysql://YOUR_RDS_ENDPOINT:3306/ask_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
   DB_USERNAME=admin
   DB_PASSWORD=YOUR_RDS_MASTER_PASSWORD
   JWT_SECRET=YOUR_64_CHARACTER_RANDOM_SECRET_KEY
   AES_SECRET_KEY=YOUR_EXACTLY_32_CHARACTER_ENCRYPTION_KEY
   BREVO_API_KEY=YOUR_BREVO_API_KEY
   BREVO_SENDER_EMAIL=YOUR_BREVO_VERIFIED_SENDER_EMAIL
   BREVO_SENDER_NAME=Apna Swasthya Kendra
   SUPER_ADMIN_EMAIL=admin@yourdomain.com
   SUPER_ADMIN_PASSWORD=YOUR_SECURE_ADMIN_PASSWORD
   CORS_ALLOWED_ORIGINS=https://ask-frontend.vercel.app
   ```
3. **Build the Docker Image**:
   ```bash
   docker build -t ask-backend .
   ```
4. **Start the Container**:
   ```bash
   docker run -d --name ask-api --env-file prod.env -p 8080:8080 --restart always ask-backend
   ```
5. **Verify Running State**:
   - Check container logs: `docker logs -f ask-api`
   - Test locally: `curl http://localhost:8080/api/actuator/health`
   - Test publicly over HTTPS in your browser: `https://api.yourdomain.com/api/actuator/health` (should return `{"status":"UP"}`)

---

## 3. Deploying Frontend on Vercel (Free Forever)

1. Go to [Vercel](https://vercel.com/) and log in with GitHub.
2. Click **"Add New"** → **"Project"** and import your `APNA_SK` repository.
3. Configure:
   - **Framework Preset**: `Vite`
   - **Root Directory**: Choose **`frontend`**.
4. **Environment Variables**: Add the variable pointing to your secure EC2 endpoint:

| Name | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://api.yourdomain.com/api` |

5. Click **"Deploy"**. Vercel will build the frontend and provide a URL (e.g., `https://ask-frontend.vercel.app`).

---

## 4. Connecting Everything Together

### Step 1: Update CORS Allowed Origins on EC2
1. Log back into your EC2 server via SSH.
2. Navigate to your project backend directory: `cd APNA_SK/backend`.
3. Open `prod.env` and update `CORS_ALLOWED_ORIGINS` to point to your live Vercel URL:
   ```env
   CORS_ALLOWED_ORIGINS=https://ask-frontend.vercel.app
   ```
4. Re-run your Docker container to load the updated origins:
   ```bash
   docker stop ask-api
   docker rm ask-api
   docker run -d --name ask-api --env-file prod.env -p 8080:8080 --restart always ask-backend
   ```

### Step 2: Test End-to-End
1. Visit your Vercel URL (`https://ask-frontend.vercel.app`).
2. Log in with the `SUPER_ADMIN_EMAIL` and `SUPER_ADMIN_PASSWORD` you set in `prod.env`.
3. You should log in successfully and be able to manage the dashboard.

---

## 5. Custom Domain Setup for Frontend (Optional)

1. Go to Vercel Project → **"Settings"** → **"Domains"**.
2. Add your root custom domain (e.g., `yourdomain.com`).
3. Add the suggested DNS records in your domain registrar panel:
   - **A Record** (Host: `@`, Value: `76.76.21.21`)
   - **CNAME Record** (Host: `www`, Value: `cname.vercel-dns.com`)
4. **Update CORS on EC2**:
   - Go to your EC2 server, open `prod.env`, and update `CORS_ALLOWED_ORIGINS` to allow your custom domain:
     ```env
     CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
     ```
   - Restart the Docker container:
     ```bash
     docker restart ask-api
     ```
