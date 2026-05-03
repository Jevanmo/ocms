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

## Deploy Online

Use one of these approaches:
- Single VM (AWS EC2/Azure/DigitalOcean): install Docker and run `docker compose up -d --build`
- Render/Railway/Fly.io: deploy backend and frontend as separate services, and use managed MySQL

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
