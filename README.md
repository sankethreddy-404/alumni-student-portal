# Alumni–Student Portal

A full-stack platform connecting alumni and students: profiles, a job portal with
AI-assisted resume shortlisting, mentorship workflows with gated chat, events,
materials, and an admin console — built with **Spring Boot 3 (Java 17)**,
**React 18 (Vite)**, and **MySQL**.

---

## 1. Project Structure

```
alumni-portal/
├── backend/          Spring Boot REST API (Java 17, Maven)
├── frontend/          React 18 + Vite SPA
├── database/
│   └── schema.sql     Reference SQL schema (optional — see note below)
└── README.md          You are here
```

---

## 2. Prerequisites

Install these before you start:

| Tool | Version | Check with |
|---|---|---|
| Java JDK | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Node.js | 18+ | `node -v` |
| npm | 9+ | `npm -v` |
| MySQL Server | 8.x | `mysql --version` |

---

## 3. Database Setup

You do **not** need to run `schema.sql` manually — the backend has
`spring.jpa.hibernate.ddl-auto=update`, so Hibernate creates/updates every
table automatically on first boot, based on the JPA entities. `database/schema.sql`
is provided purely as a readable reference of the resulting structure (useful if
you want to inspect it, pre-create the DB elsewhere, or hand it to a DBA).

All you need to do is make sure a MySQL server is running and create an empty database:

```sql
CREATE DATABASE alumni_portal CHARACTER SET utf8mb4;
```

(The backend's `createDatabaseIfNotExist=true` connection flag will actually
create it for you too, as long as your MySQL user has permission — so this
step is a safety net, not strictly required.)

---

## 4. Backend Setup (Spring Boot)

```bash
cd backend
```

### 4.1 Configure `src/main/resources/application.properties`

Open the file and update these three lines to match **your** MySQL credentials:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

If your MySQL isn't on `localhost:3306`, update the URL too:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/alumni_portal?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
```

### 4.2 (Optional) Enable real email reminders

By default `app.mail.enabled=false`, so the 6-month profile verification
reminder just **logs** the email to the console instead of sending it —
this means the app runs correctly out of the box with zero email setup.

To send real emails, set:

```properties
app.mail.enabled=true
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
```

(For Gmail you need an **App Password**, not your normal password — enable
2FA on the Google account first, then generate one under Security → App Passwords.)

### 4.3 Run the backend

```bash
mvn clean install
mvn spring-boot:run
```

The API starts on **http://localhost:8080**. On first run, `DataSeeder` populates
sample data automatically (admin, alumni, students, jobs, an event) — see
Section 6 for login credentials.

Uploaded resumes/materials are stored under `backend/uploads/` (created
automatically) and served at `http://localhost:8080/uploads/...`.

---

## 5. Frontend Setup (React + Vite)

Open a **new terminal** (keep the backend running):

```bash
cd frontend
cp .env.example .env      # on Windows: copy .env.example .env
npm install
npm run dev
```

The app starts on **http://localhost:5173** and is already configured to talk
to the backend at `http://localhost:8080/api` (see `.env`).

> If you changed the backend port, update `VITE_API_BASE_URL` in `frontend/.env` to match.

### About CORS

The backend's `SecurityConfig` explicitly allows `http://localhost:5173` and
`http://localhost:3000` as CORS origins (`app.cors.allowed-origins` in
`application.properties`), and OPTIONS preflight requests are permitted through
without JWT checks. If you serve the frontend from a different origin/port,
add it to that comma-separated property and restart the backend — otherwise
the browser will block requests with a CORS error.

---

## 6. Demo Accounts (seeded automatically)

| Role | Email | Password | Notes |
|---|---|---|---|
| Admin | `admin@alumniportal.com` | `admin123` | Full admin console |
| Alumni | `priya.sharma@example.com` | `alumni123` | Fully complete profile, mentorship **enabled** |
| Alumni | `rahul.verma@example.com` | `alumni123` | Partially complete profile, mentorship disabled |
| Alumni (pending) | `sana.iyer@example.com` | `alumni123` | Awaiting admin approval — log in fails until approved |
| Student | `ananya.gupta@example.com` | `student123` | |
| Student | `vikram.nair@example.com` | `student123` | |

You can also register new accounts from the app — new **Alumni** signups require
admin approval (visible under Admin → Alumni Approvals) before they can log in;
**Student** signups are approved instantly.

---

## 7. Walking Through the Core Features

### Profile completeness
Alumni → My Profile shows a live "Profile XX% Complete" bar, calculated from
7 fields (Company, Skills, Location, Domain, Experience, Bio, LinkedIn URL),
with tags for whatever is still missing.

### Resume auto-fill
Alumni → My Profile → "Auto-fill from Resume" — upload a PDF/DOCX/TXT resume.
Apache Tika extracts the text; a keyword-matching pass pulls out skills, and
regex heuristics look for company/role/years of experience. Only **empty**
fields get filled — anything you've already typed is preserved.
(LinkedIn URL auto-fill just stores the URL; we don't scrape LinkedIn, since
that requires their API/auth and would violate their terms — the app is honest
about this rather than faking it.)

### Resume-based job shortlisting
Student → Job Portal → Apply, attaching a resume. The backend re-parses the
resume, extracts skills, and computes:

```
Match Score = (Matched Skills / Required Skills) × 100
```

categorized as **HIGH** (≥75%), **MEDIUM** (≥40%), or **LOW** (below 40%).
Alumni → Job Portal → "View Applicants" shows every applicant ranked by score,
with a one-click "Auto-shortlist Top 3" and per-candidate **Refer** / **Reject** actions.

### Mentorship → chat unlock
Student sends a mentorship request from the Alumni Directory. Once the alumnus
**Accepts** it (Alumni → Mentorship Requests), the Messages tab unlocks a chat
between exactly those two users — no contact info is ever exposed directly, and
you cannot message someone without an accepted mentorship request between you.
Chat refreshes every 4 seconds while a conversation is open (simple polling —
no WebSocket infrastructure needed).

### Admin console
Dashboard charts (bar + pie, via Recharts), alumni approval queue, job approval
queue, user activation/deactivation, incomplete-profile and stale-verification
monitoring, and a top-contributors leaderboard (weighted: 3× jobs posted +
2× mentorship sessions + 1× events attended).

---

## 8. API Overview

All endpoints are prefixed `/api`. JWT auth: send `Authorization: Bearer <token>`
after login/register.

| Area | Base path | Notes |
|---|---|---|
| Auth | `/api/auth` | `/register`, `/login` — public |
| Alumni | `/api/alumni/**` | Profile, jobs, mentorship, contributions — role `ALUMNI`/`ADMIN` |
| Student | `/api/student/**` | Profile, jobs, applications, mentorship, events — role `STUDENT`/`ADMIN` |
| Jobs (shared) | `/api/jobs` | Read-only list/detail, any authenticated role |
| Messages | `/api/messages` | Send / conversation / inbox, any authenticated role |
| Events | `/api/events` | List/create/register, any authenticated role (creation restricted to Alumni/Admin) |
| Admin | `/api/admin/**` | Dashboard, approvals, user management — role `ADMIN` |

Full endpoint-by-endpoint detail is visible directly in the controller classes
under `backend/src/main/java/com/alumniportal/controller/`.

---

## 9. Troubleshooting

**"CORS error" in the browser console**
Confirm the frontend origin (`http://localhost:5173` by default) is listed in
`app.cors.allowed-origins` in `application.properties`, and that you restarted
the backend after any change. Also double check you didn't accidentally start
the frontend on a different port (Vite will auto-increment to 5174, 5175... if
5173 is taken) — if so, either free up 5173 or add the new port to the CORS list.

**401 Unauthorized right after logging in**
The JWT is stored in `localStorage`. If you're testing against a rebuilt
backend with a new `app.jwt.secret`, old tokens become invalid — just log out
and back in.

**"Your alumni account is pending admin approval"**
Expected behavior for new alumni signups (and for the seeded `sana.iyer@example.com`
account). Log in as `admin@alumniportal.com` and approve it under
Admin → Alumni Approvals.

**File upload fails / 413 Payload Too Large**
Max upload size is 10MB (`spring.servlet.multipart.max-file-size`). Increase it
in `application.properties` if you need larger resumes.

**MySQL connection refused**
Make sure MySQL is running and the credentials/URL in `application.properties`
match your local setup.

**`mvn` or `npm` commands fail with dependency errors**
This project was built in an offline sandbox without internet access, so the
code could not be compiled or `npm install`-ed here before packaging. The Java
and JS were written carefully against known-correct, standard framework
patterns, but you're the first to actually build it — if you hit a real
compile/runtime error, copy the exact error message back and it can be fixed
immediately.

---

## 10. Known Simplifications (by design, not oversights)

- **"AI" resume parsing** is deterministic keyword/regex matching (via Apache
  Tika for text extraction), not a trained ML/NLP model — this keeps the
  feature dependency-light, fully offline, and its behavior fully explainable,
  while still implementing the exact match-score formula requested.
- **LinkedIn auto-fill** stores the URL rather than scraping LinkedIn, since
  scraping would need LinkedIn's paid API or violate their ToS.
- **Chat** uses simple 4-second polling rather than WebSockets — no extra
  infrastructure (STOMP/SockJS broker) required, at the cost of slightly
  less "real-time" delivery.
- **Event registration** required an `EventRegistration` join table that
  wasn't in the original table list but is necessary to support "students can
  register for events" — added for correctness.
