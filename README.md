# Online Course Management System (OCMS)

Full-stack project with:
- Backend: Spring Boot + JWT + MySQL
- Frontend: React (Vite)
- Features: student/instructor login, course creation/enrollment, content upload, assignment submission, grading

## Project Structure

- `backend` - Spring Boot REST API
- `frontend` - React app
- `docker-compose.yml` - MySQL + backend + frontend deployment stack

## Local Development

### 1) Start MySQL

Use local MySQL and ensure credentials match `backend/src/main/resources/application.yml`:
- DB: `ocms_db`
- User: `root`
- Password: `root`

### 2) Run Backend

```bash
cd backend
mvn spring-boot:run
```

Backend URL: `http://localhost:8080`

### 3) Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

## Docker Deployment (Recommended)

```bash
docker compose up --build
```

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- MySQL: `localhost:3306`

## API Overview

### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`

### Courses
- `GET /api/courses`
- `POST /api/courses` (instructor)
- `POST /api/courses/{courseId}/enroll` (student)
- `GET /api/courses/my`

### Course Content
- `POST /api/courses/{courseId}/contents` (instructor, multipart)
- `GET /api/courses/{courseId}/contents`
- `GET /api/files/{storedName}`

### Assignments
- `POST /api/courses/{courseId}/assignments` (instructor)
- `GET /api/courses/{courseId}/assignments`
- `POST /api/assignments/{assignmentId}/submit` (student, multipart)
- `GET /api/assignments/{assignmentId}/submissions` (instructor)
- `PUT /api/submissions/{submissionId}/grade` (instructor)

## Push to GitHub

```bash
cd "d:/jevan proj/bank"
git init
git add .
git commit -m "Build OCMS full-stack app with JWT auth, courses, content, assignments, and grading"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

## Deploy everything on Railway (recommended if you want one platform)

Use **three pieces** in one Railway project: **MySQL**, **backend** (Spring Boot), **frontend** (static UI). The API listens on Railway’s `PORT`; the UI image uses `serve` on the same `PORT` pattern.

### 1) Repo + project

1. Push this repo to GitHub.
2. [Railway](https://railway.app) → **New Project** → **Deploy from GitHub repo** → select it.

### 2) MySQL

- **New** → **Database** → **MySQL** (or the MySQL template Railway shows).
- Open the database → **Variables** / **Connect** and note host, port, database name, user, password (or any `DATABASE_URL` / JDBC hints Railway provides).

### 3) Backend service

1. **New** → deploy the **same** GitHub repo again (or add a service and connect the repo).
2. **Settings** → **Root Directory** → `backend` (uses `backend/Dockerfile` + `backend/railway.toml`).
3. **Variables** (examples — names on your MySQL service may differ slightly):
   - `SPRING_DATASOURCE_URL` — JDBC URL for Railway MySQL, e.g. `jdbc:mysql://HOST:PORT/railway?createDatabaseIfNotExist=true&useSSL=true&allowPublicKeyRetrieval=true`
   - `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`
   - `APP_JWT_SECRET` — long random string (never commit real secrets).
   - `APP_UPLOAD_DIR` — e.g. `/app/uploads` (disk is **ephemeral** unless you add storage; for serious production use object storage).
   - `APP_CORS_ALLOWED_ORIGINS` — set **after** step 4: your **frontend** public origin(s), comma-separated, e.g. `https://your-ui.up.railway.app,http://localhost:5173`
4. **Settings** → **Networking** → **Generate Domain**. Copy the API base, e.g. `https://ocms-api.up.railway.app` (no `/api` suffix in the hostname).

The JVM app uses `server.port: ${PORT:8080}` so it binds correctly on Railway. **Health check:** `GET /api/health` returns `{"status":"ok"}` (public, no auth). Railway can use this path in **Settings → Healthcheck** if you set one manually; `backend/railway.toml` also references it for deploy health checks where supported.

### 4) Frontend service

1. **New** → same repo again.
2. **Root Directory** → `frontend` (uses `frontend/Dockerfile` + `frontend/railway.toml`).
3. Add variable **`VITE_API_URL`** = `https://YOUR-BACKEND-DOMAIN/api` (**no trailing slash**). In Railway’s variable UI, enable **available at build time** / **Build** (so Docker `npm run build` sees it and Vite bakes the URL into the bundle).
4. Deploy, then **Generate Domain** for the frontend (e.g. `https://ocms-ui.up.railway.app`).

### 5) CORS round-trip

1. Edit the **backend** service → set `APP_CORS_ALLOWED_ORIGINS` to include your **frontend** Railway URL exactly (scheme + host, no path).
2. Redeploy the backend if it already deployed without the right CORS value.

If the UI still cannot log in or load data, check: `VITE_API_URL` ends with `/api`, backend domain is public, and CORS lists the frontend origin.

### Local “production-like” stack (Docker)

On your machine, **start Docker Desktop**, then from the repo root:

```bash
docker compose up -d --build
```

- UI: `http://localhost:5173`
- API: `http://localhost:8080`

## Deploy Online (other options)

Use one of these approaches:
- Single VM (AWS EC2/Azure/DigitalOcean): install Docker and run `docker compose up -d --build`
- Render/Fly.io: deploy backend and frontend as separate services; use managed MySQL or an external MySQL host (Render’s default managed DB is PostgreSQL, so MySQL is usually a separate provider).

For production:
- change DB/JWT secrets via env vars
- use HTTPS + domain
- configure CORS for your frontend domain

### Deploy frontend to Vercel

Vercel hosts the **React (Vite) app only**. It does **not** run Spring Boot or MySQL. Deploy the API on Railway, Render, Fly.io, a VPS, etc., with a public HTTPS URL, then point the UI at it.

1. In the Vercel dashboard: **Add New Project** → import your Git repo.
2. Set **Root Directory** to `frontend`.
3. Under **Environment Variables** (Production + Preview), add:
   - `VITE_API_URL` = `https://YOUR-BACKEND-HOST/api` (no trailing slash; must match where Spring Boot is served).
4. Deploy.

On the backend, allow your Vercel origin in CORS. Either edit `app.cors-allowed-origins` in `application.yml` or set an env var when running the JAR (comma-separated list), for example:

`APP_CORS_ALLOWED_ORIGINS=https://your-app.vercel.app,http://localhost:5173`

CLI (from `frontend` after `npm i -g vercel` or `npx vercel`):

```bash
cd frontend
npx vercel
```

Follow the prompts; set the same `VITE_API_URL` in the Vercel project settings if the CLI does not prompt for it.
