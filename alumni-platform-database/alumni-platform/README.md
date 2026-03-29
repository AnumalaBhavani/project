# AlumniConnect — Alumni Management & Career Networking Platform

A full-stack web application for college alumni networking, mentorship, career opportunities, and engagement.

---

## Tech Stack

| Layer      | Technology                        |
|------------|-----------------------------------|
| Frontend   | React 18, React Router v6, Axios, Recharts |
| Backend    | Java 17, Spring Boot 3.2          |
| Database   | MySQL 8.x                         |
| Auth       | JWT (HS256) + Spring Security     |
| File Parse | Apache PDFBox, Apache POI         |

---

## Project Structure

```
alumni-platform/
├── database/
│   └── schema.sql              ← Full DB schema + sample data
├── backend/                    ← Spring Boot project
│   ├── pom.xml
│   └── src/main/java/com/alumni/
│       ├── entity/             ← JPA entities
│       ├── repository/         ← Spring Data repositories
│       ├── service/            ← Business logic
│       ├── controller/         ← REST controllers
│       ├── security/           ← JWT filter & utils
│       ├── config/             ← SecurityConfig
│       └── dto/                ← Request/response DTOs
└── frontend/                   ← React app
    └── src/
        ├── context/            ← AuthContext
        ├── services/           ← Axios API calls
        ├── components/shared/  ← Layout, UIComponents
        └── pages/
            ├── alumni/         ← Alumni dashboard & profile
            ├── student/        ← Student dashboard
            └── admin/          ← Admin dashboard
```

---

## Setup Instructions

### 1. Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.x

---

### 2. Database Setup

```sql
-- In MySQL client or Workbench:
mysql -u root -p < database/schema.sql
```

This creates the `alumni_platform` database, all tables, and sample data.

**Default test accounts (all passwords encoded for `Admin@123` / `Alumni@123` / `Student@123`):**

| Role    | Email                           | Password    |
|---------|---------------------------------|-------------|
| Admin   | admin@alumni.edu                | Admin@123   |
| Alumni  | rahul.sharma@alumni.edu         | Alumni@123  |
| Alumni  | priya.patel@alumni.edu          | Alumni@123  |
| Student | aman.kumar@student.edu          | Student@123 |

> **Note:** The bcrypt hashes in schema.sql are generated for `Admin@123`. Update the password hash for production.

---

### 3. Backend Setup

```bash
cd backend

# Configure database credentials
# Edit: src/main/resources/application.properties
# Change: spring.datasource.password=your_password_here

# Build and run
mvn clean install -DskipTests
mvn spring-boot:run
```

Backend starts at: **http://localhost:8080**

---

### 4. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

Frontend starts at: **http://localhost:3000**

The `proxy` field in `package.json` forwards `/api` calls to `http://localhost:8080`.

---

## REST API Reference

### Authentication (`/api/auth`)

| Method | Endpoint               | Access | Description              |
|--------|------------------------|--------|--------------------------|
| POST   | /auth/login            | Public | Login, returns JWT       |
| POST   | /auth/register/alumni  | Public | Register as alumni       |
| POST   | /auth/register/student | Public | Register as student      |

### Alumni (`/api/alumni`)

| Method | Endpoint                        | Access        | Description                    |
|--------|---------------------------------|---------------|--------------------------------|
| GET    | /alumni/directory               | Public        | Browse alumni with filters     |
| GET    | /alumni/profile                 | ALUMNI        | Get own profile                |
| GET    | /alumni/profile/{id}            | Auth          | Get profile by ID              |
| PUT    | /alumni/profile                 | ALUMNI        | Update profile                 |
| POST   | /alumni/profile/verify          | ALUMNI        | Confirm profile is up-to-date  |
| PATCH  | /alumni/profile/mentorship      | ALUMNI        | Toggle mentorship availability |
| POST   | /alumni/profile/parse-resume    | ALUMNI        | Upload & parse resume          |
| GET    | /alumni/contributions           | ALUMNI        | Get contribution history       |
| GET    | /alumni/notifications           | Auth          | Get notifications              |
| POST   | /alumni/notifications/read-all  | Auth          | Mark all notifications read    |

### Jobs (`/api/jobs`)

| Method | Endpoint                   | Access         | Description                      |
|--------|----------------------------|----------------|----------------------------------|
| GET    | /jobs/public/list          | Public         | List approved jobs (with filters)|
| POST   | /jobs/post                 | ALUMNI         | Post a new job                   |
| GET    | /jobs/my-jobs              | ALUMNI         | Get alumni's own job postings    |
| POST   | /jobs/{id}/apply           | STUDENT        | Apply to a job (with resume)     |
| GET    | /jobs/{id}/applications    | ALUMNI, ADMIN  | Get ranked applications          |
| GET    | /jobs/my-applications      | STUDENT        | Student's own applications       |

### Mentorship (`/api/mentorship`)

| Method | Endpoint                           | Access  | Description              |
|--------|------------------------------------|---------|--------------------------|
| POST   | /mentorship/request/{alumniId}     | STUDENT | Send mentorship request  |
| PATCH  | /mentorship/request/{id}/respond   | ALUMNI  | Accept/Reject/Schedule   |
| GET    | /mentorship/alumni/requests        | ALUMNI  | Get all incoming requests|
| GET    | /mentorship/student/requests       | STUDENT | Get own requests         |

### Admin (`/api/admin`)

| Method | Endpoint                        | Access | Description                    |
|--------|---------------------------------|--------|--------------------------------|
| GET    | /admin/dashboard                | ADMIN  | Platform analytics stats       |
| GET    | /admin/alumni/pending           | ADMIN  | List pending alumni approvals  |
| PATCH  | /admin/alumni/{userId}/approve  | ADMIN  | Approve alumni account         |
| GET    | /admin/jobs/pending             | ADMIN  | List pending job approvals     |
| PATCH  | /admin/jobs/{id}/moderate       | ADMIN  | Approve or reject job posting  |
| GET    | /admin/alumni/incomplete-profiles| ADMIN | Profiles below threshold       |
| GET    | /admin/contributors/top         | ADMIN  | Top alumni contributors        |

---

## Key Features — Implementation Details

### Profile Completeness
`ProfileCompletenessService.calculate()` scores 7 fields: company (15%), skills (20%), location (10%), experience (15%), domain (15%), bio (15%), role (10%). Missing fields generate auto-suggestions shown on the profile page.

### Resume Parsing & Auto-Fill
`ResumeParserService` uses Apache PDFBox to extract text from PDF resumes. A dictionary of 40+ known skills is matched via substring search. Regex patterns extract job title, company, and years of experience. The extracted data is returned to the frontend which pre-fills the profile form.

### Resume Shortlisting / Match Score
Formula: `MatchScore = (matchedSkills / requiredSkills) × 100`
- HIGH: ≥ 70%
- MODERATE: 40–69%
- LOW: < 40%
Applications are auto-ranked by match score when alumni view applicants.

### 6-Month Verification Scheduler
`VerificationScheduler` runs daily at 9 AM via Spring `@Scheduled`. Alumni whose `lastVerifiedAt` is null or > 6 months ago receive an in-app notification. A banner is shown on the frontend dashboard with a one-click verify button.

### Mentorship Workflow
1. Student sends request → notification sent to alumni
2. Alumni can: Accept → Schedule (with datetime + meeting link) → Complete
3. Completing records a contribution (+20 points)
4. Student gets notified at each status change

### JWT Auth
Tokens are HS256-signed with a 24-hour expiry. A refresh token (7 days) is also issued. Role-based access is enforced at Spring Security method level via `@PreAuthorize`.

---

## Contribution Points System

| Action                | Points |
|-----------------------|--------|
| Post a job            | +10    |
| Complete mentorship   | +20    |
| Organize event        | +15    |
| Upload material       | +10    |
| Attend event          | +5     |

Admin dashboard shows top contributors ranked by total points.

---

## Production Checklist

- [ ] Change `app.jwt.secret` to a long random string
- [ ] Configure real SMTP credentials in `application.properties`
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (not create-drop)
- [ ] Configure S3 or cloud storage instead of local file storage
- [ ] Enable HTTPS (SSL/TLS certificate)
- [ ] Set `app.cors.allowed-origins` to your production domain
- [ ] Add rate limiting to auth endpoints
- [ ] Remove demo auto-activation in `AuthService.registerAlumni()` — require admin approval
- [ ] Add email verification on registration
