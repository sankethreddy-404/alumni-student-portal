# Alumni–Student Portal

A full-stack platform connecting alumni and students through profiles, job opportunities, resume-based job matching, mentorship, events, learning materials, messaging, and an admin console.

Built with **Spring Boot 3, Java 17, React 18, Vite, and MySQL**.

---

## 1. Features

* Student and alumni registration/login
* JWT-based authentication
* Role-based authorization for Admin, Alumni, and Student
* Alumni and student profiles
* Profile completeness tracking
* Job and internship posting
* Job application management
* Resume upload and parsing
* Resume-based job matching and shortlisting
* Mentorship request and approval workflow
* Mentorship scheduling and session tracking
* Gated messaging between mentors and students
* Event creation and registration
* Learning materials associated with the platform
* Admin dashboard and analytics
* Alumni approval workflow
* Job approval workflow
* User activation/deactivation
* Profile verification reminders
* Contribution tracking
* File upload support

---

## 2. Tech Stack

### Backend

* Java 17
* Spring Boot 3
* Spring Security
* JWT
* Spring Data JPA
* Hibernate
* MySQL
* Maven
* Apache Tika

### Frontend

* React 18
* Vite
* JavaScript
* Axios
* Recharts
* CSS

### Development Tools

* Git
* GitHub
* Postman
* IntelliJ IDEA / VS Code

---

## 3. Project Structure

```text
alumni-portal/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/alumniportal/
│   │       │   ├── config/
│   │       │   ├── controller/
│   │       │   ├── dto/
│   │       │   ├── entity/
│   │       │   ├── exception/
│   │       │   ├── repository/
│   │       │   ├── scheduler/
│   │       │   ├── security/
│   │       │   └── service/
│   │       └── resources/
│   │           └── application.properties
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── context/
│   │   ├── pages/
│   │   └── styles/
│   ├── package.json
│   └── vite.config.js
│
├── database/
│   └── schema.sql
│
├── .gitignore
└── README.md
```

---

## 4. Architecture

```text
                    ┌─────────────────────┐
                    │    React + Vite     │
                    │      Frontend       │
                    └──────────┬──────────┘
                               │
                         REST API / JWT
                               │
                               ▼
                    ┌─────────────────────┐
                    │    Spring Boot      │
                    │      REST API       │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
        Controllers        Services       Spring Security
              │                │                │
              └────────────────┼────────────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Spring Data JPA     │
                    │    / Hibernate      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │       MySQL         │
                    └─────────────────────┘
```

The frontend communicates with the Spring Boot REST API using Axios.

The backend follows a **Controller → Service → Repository** structure. Spring Security and JWT handle authentication and role-based authorization, while JPA/Hibernate manages persistence to MySQL.

---

## 5. Prerequisites

Install the following before running the project:

| Tool         | Version | Check             |
| ------------ | ------- | ----------------- |
| Java JDK     | 17+     | `java -version`   |
| Maven        | 3.8+    | `mvn -version`    |
| Node.js      | 18+     | `node -v`         |
| npm          | 9+      | `npm -v`          |
| MySQL Server | 8.x     | `mysql --version` |

---

## 6. Database Setup

The application uses MySQL.

Create the database:

```sql
CREATE DATABASE alumni_portal CHARACTER SET utf8mb4;
```

The backend uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

so Hibernate creates and updates the required tables based on the JPA entities.

The `database/schema.sql` file is provided as a reference for the database structure.

The JDBC URL also includes:

```text
createDatabaseIfNotExist=true
```

so the database can be created automatically when the configured MySQL user has sufficient permissions.

---

## 7. Backend Setup

Navigate to the backend:

```bash
cd backend
```

### 7.1 Configure Environment Variables

The application reads database credentials and the JWT secret from environment variables.

Set the following variables according to your local environment.

### Windows PowerShell

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
$env:JWT_SECRET="your_development_jwt_secret"
```

### Windows Command Prompt

```cmd
set DB_USERNAME=root
set DB_PASSWORD=your_mysql_password
set JWT_SECRET=your_development_jwt_secret
```

The corresponding properties in `application.properties` are:

```properties
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
app.jwt.secret=${JWT_SECRET}
```

Do not commit real passwords, API keys, or production secrets to GitHub.

### 7.2 Optional Email Configuration

Email reminders are disabled by default:

```properties
app.mail.enabled=false
```

When disabled, the application logs reminder information instead of sending real emails.

To enable Gmail SMTP, configure:

```properties
app.mail.enabled=true
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

Use a Gmail **App Password** rather than your normal Gmail password.

### 7.3 Run the Backend

```bash
mvn clean install
mvn spring-boot:run
```

The backend starts at:

```text
http://localhost:8080
```

On the first run, the application seeds sample data for demonstration.

Uploaded resumes and materials are stored under:

```text
backend/uploads/
```

---

## 8. Frontend Setup

Open a new terminal while the backend is running:

```bash
cd frontend
```

Copy the environment example file.

### Windows

```cmd
copy .env.example .env
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend starts at:

```text
http://localhost:5173
```

The frontend communicates with the backend through:

```text
http://localhost:8080/api
```

If the backend URL or port is changed, update:

```text
VITE_API_BASE_URL
```

in `frontend/.env`.

---

## 9. Demo Accounts

The application seeds demo accounts for local development and demonstration.

| Role    | Email                      | Password     | Description                          |
| ------- | -------------------------- | ------------ | ------------------------------------ |
| Admin   | `admin@alumniportal.com`   | `admin123`   | Full admin console                   |
| Alumni  | `priya.sharma@example.com` | `alumni123`  | Complete profile, mentorship enabled |
| Alumni  | `rahul.verma@example.com`  | `alumni123`  | Partial profile, mentorship disabled |
| Alumni  | `sana.iyer@example.com`    | `alumni123`  | Pending admin approval               |
| Student | `ananya.gupta@example.com` | `student123` | Student account                      |
| Student | `vikram.nair@example.com`  | `student123` | Student account                      |

> These accounts are seeded for local development and demonstration only.

New users can also register through the application.

* Student registrations are approved immediately.
* Alumni registrations require admin approval before login.

---

## 10. Core Features

### Profile Management

Alumni and students can manage their profiles.

Alumni profile completeness is calculated using fields such as:

* Company
* Skills
* Location
* Domain
* Experience
* Bio
* LinkedIn URL

The application displays the completion percentage and identifies missing information.

### Resume Auto-Fill

Alumni can upload PDF, DOCX, or TXT resumes.

The backend uses **Apache Tika** to extract text from uploaded documents.

A keyword-based matching process identifies skills, while regular expressions are used for selected profile information such as company, role, and years of experience.

Only empty profile fields are populated automatically, so existing user-entered information is preserved.

LinkedIn URLs are stored directly; LinkedIn profile scraping is not implemented.

### Resume-Based Job Matching

Students can apply for jobs with a resume.

The backend extracts skills from the resume and calculates a match score:

```text
Match Score = (Matched Skills / Required Skills) × 100
```

Applications are categorized as:

* **HIGH** — 75% or higher
* **MEDIUM** — 40% to 74%
* **LOW** — below 40%

Alumni can view applicants ranked by match score and perform actions such as:

* Auto-shortlist top candidates
* Refer
* Reject

### Mentorship Workflow

Students can send mentorship requests to alumni.

The workflow is:

```text
Student sends request
        ↓
Alumni reviews request
        ↓
Accept / Reject
        ↓
If accepted
        ↓
Messaging becomes available
```

Mentorship sessions can also be scheduled and tracked.

Messaging is restricted to users with an accepted mentorship relationship.

The application uses simple polling for chat updates rather than WebSockets.

### Events

Alumni and administrators can create events.

Students can browse available events and register for them.

An `EventRegistration` relationship is used to associate students with events.

### Learning Materials

The platform supports learning materials that can be uploaded and accessed through the application.

Uploaded materials are stored using the backend file-storage functionality.

### Admin Console

Administrators can:

* View dashboard statistics
* Approve alumni accounts
* Approve jobs
* Activate/deactivate users
* Monitor incomplete profiles
* Monitor profile verification
* View contribution information
* View top contributors

The dashboard uses charts for selected analytics.

---

## 11. Authentication & Authorization

The application uses **Spring Security with JWT-based authentication**.

Authentication flow:

```text
User Login
    ↓
AuthController
    ↓
AuthService
    ↓
Credential Validation
    ↓
JWT Generation
    ↓
Token returned to frontend
    ↓
Frontend sends Bearer token
    ↓
JwtAuthFilter validates token
    ↓
Spring Security establishes authentication
    ↓
Protected Controller
```

API requests use:

```http
Authorization: Bearer <token>
```

Role-based authorization restricts functionality based on:

* `ADMIN`
* `ALUMNI`
* `STUDENT`

---

## 12. API Overview

All REST endpoints are prefixed with:

```text
/api
```

| Area           | Base Path         | Description                                                 |
| -------------- | ----------------- | ----------------------------------------------------------- |
| Authentication | `/api/auth`       | Registration and login                                      |
| Alumni         | `/api/alumni/**`  | Alumni profiles, jobs, mentorship and contributions         |
| Student        | `/api/student/**` | Student profiles, jobs, applications, mentorship and events |
| Jobs           | `/api/jobs`       | Job listing and details                                     |
| Messages       | `/api/messages`   | Messaging and conversations                                 |
| Events         | `/api/events`     | Event listing, creation and registration                    |
| Admin          | `/api/admin/**`   | Dashboard, approvals and user management                    |

JWT authentication is required for protected endpoints.

Detailed endpoint implementations can be found in:

```text
backend/src/main/java/com/alumniportal/controller/
```

---

## 13. CORS Configuration

The backend allows the default frontend development origins:

```text
http://localhost:5173
http://localhost:3000
```

These are configured through:

```properties
app.cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

If the frontend runs on another origin or port, update this property and restart the backend.

---

## 14. File Uploads

The application supports file uploads for resumes and learning materials.

Default configuration:

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

Uploaded files are stored under:

```text
backend/uploads/
```

The upload directory is created automatically when required.

---

## 15. Design Decisions & Limitations

### Resume Matching

The resume matching feature uses deterministic keyword and regular-expression matching rather than a trained machine-learning or NLP model.

Apache Tika handles document text extraction.

This approach keeps the implementation:

* Dependency-light
* Explainable
* Deterministic
* Suitable for local development

### LinkedIn Integration

The application stores LinkedIn profile URLs but does not scrape LinkedIn profiles.

Profile information must be entered manually or populated through resume processing.

### Chat

Chat uses simple polling rather than WebSockets.

The frontend periodically refreshes conversation data, avoiding the need for additional WebSocket infrastructure.

### Event Registration

Event registration uses a dedicated `EventRegistration` relationship to associate students with events.

---

## 16. Troubleshooting

### CORS Error

Check that the frontend origin is included in:

```properties
app.cors.allowed-origins
```

The default frontend URL is:

```text
http://localhost:5173
```

If Vite starts on another port such as `5174`, update the CORS configuration accordingly and restart the backend.

### 401 Unauthorized After Login

JWT tokens are stored on the client side.

If the JWT secret changes, previously issued tokens become invalid.

Log out and log in again to obtain a new token.

### Alumni Account Pending Approval

New alumni registrations require administrator approval.

Log in using the admin account and approve the alumni account through:

```text
Admin → Alumni Approvals
```

### File Upload Fails

The default maximum upload size is:

```text
10MB
```

Increase the following properties if larger files are required:

```properties
spring.servlet.multipart.max-file-size
spring.servlet.multipart.max-request-size
```

### MySQL Connection Refused

Verify that:

1. MySQL Server is running.
2. The database exists or the configured MySQL user can create it.
3. `DB_USERNAME` is correct.
4. `DB_PASSWORD` is correct.
5. The database URL points to the correct host and port.

---

## 17. Future Improvements

Possible future enhancements include:

* WebSocket-based real-time messaging
* More advanced NLP-based resume matching
* Cloud-based file storage
* Production deployment
* Automated CI/CD pipeline
* Comprehensive automated integration testing
* Refresh-token based authentication
* Password reset and email verification
* Advanced job recommendation algorithms
* More detailed admin analytics

---

## 18. License

This project is intended for educational and portfolio purposes.
