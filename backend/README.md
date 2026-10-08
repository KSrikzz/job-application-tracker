# Job & Internship Application Tracker - Backend Guide

A production-style, multi-tenant Spring Boot REST API for tracking full-time job and internship applications, interviews, status histories, and career analytics.

## Architecture & Design Decisions

Requests flow through a clean multi-layer architecture:

```text
HTTP Client (React / Postman)
          ↓
Controller Layer (Validation, Security Context, DTO Mapping)
          ↓
Service Layer (Domain Logic, State Transitions, User Ownership Isolation)
          ↓
Repository Layer (Spring Data JPA, JPQL custom queries)
          ↓
PostgreSQL Database
```

### Key Design Highlights:
1. **Unified Application Model**: Jobs and internships share a unified `JobApplication` table discriminated by `applicationType` (`JOB` or `INTERNSHIP`). Rather than duplicating schemas, common attributes (company, role, location, status, notes) are shared, while flexible fields (`compensation`, `workMode`, `startDate`, `endDate`, `applicationUrl`) accommodate both salaries and stipends without artificial constraints.
2. **Stateless JWT Security**: Spring Security is configured with `SessionCreationPolicy.STATELESS`. Every protected request is intercepted by `JwtAuthenticationFilter`, resolving the authenticated user email into Spring Security's context. Passwords are encrypted with BCrypt (`BCryptPasswordEncoder`).
3. **Multi-Tenant Ownership Isolation**: All application data, interviews, and status logs are strictly scoped to the authenticated user. Cross-user data access is impossible at the query level.
4. **Thin Controllers & Encapsulated DTOs**: JPA entities are never exposed in REST responses or accepted directly from requests.
5. **Automated Status Audit History**: Application state transitions are tracked chronologically in `ApplicationStatusHistory`. When an application is created, its initial status is logged. Subsequent updates only log a new entry when the status actually changes.
6. **Rich Analytics Dashboard**: Dynamic breakdown of total jobs vs. internships, active pipeline counts, interview progression, offers, and conversion rates.

---

## Tech Stack

- **Language & Runtime**: Java 21
- **Framework**: Spring Boot 4.x
- **Security**: Spring Security, JJWT (io.jsonwebtoken 0.12.6), BCrypt
- **Persistence**: Spring Data JPA, Hibernate, PostgreSQL
- **Validation**: Jakarta Bean Validation
- **Testing**: JUnit 5, Mockito, Spring Boot Test, MockMvc
- **Build Tool**: Apache Maven

---

## Project Structure

```text
backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/jobtracker/
│   │   │   ├── BackendApplication.java
│   │   │   ├── config/              # Security filter chain, JWT filter, REST auth handlers
│   │   │   ├── controller/          # REST API endpoints
│   │   │   ├── dto/                 # Request and response models with validation
│   │   │   ├── entity/              # JPA entities (User, JobApplication, Interview, StatusHistory)
│   │   │   ├── exception/           # Custom domain exceptions & GlobalExceptionHandler
│   │   │   ├── repository/          # Spring Data JPA repositories
│   │   │   └── service/             # Business logic & security validation
│   │   └── resources/
│   │       └── application.properties
│   └── test/java/com/jobtracker/
│       ├── SecurityBoundaryTest.java
│       └── service/
│           ├── ApplicationServiceTest.java
│           ├── DashboardServiceTest.java
│           ├── InterviewServiceTest.java
│           ├── JwtServiceTest.java
│           └── UserServiceTest.java
```

---

## Environment Variables & Configuration

Set the following environment variables before starting the backend:

| Variable | Required | Default | Description |
| --- | --- | --- | --- |
| `DB_USERNAME` | Yes | - | PostgreSQL username |
| `DB_PASSWORD` | Yes | - | PostgreSQL password |
| `JWT_SECRET` | Yes | - | HMAC signing key (minimum 32 bytes) |
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/job_tracker` | Database connection URL |
| `SERVER_PORT` | No | `8080` | Application server port |
| `JWT_EXPIRATION` | No | `86400000` (24h) | Token lifespan in milliseconds |
| `DDL_AUTO` | No | `update` | Hibernate schema tool (`update` / `validate`) |

### Example Setup (PowerShell):
```powershell
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "yourpassword"
$env:JWT_SECRET = "jobtracker-secret-key-at-least-32-bytes-long-123456"
```

---

## Running and Testing

### Build & Run:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Run Automated Tests:
```powershell
cd backend
.\mvnw.cmd test
```

---

## REST API Specification

### 1. Authentication & Profile
- `POST /api/auth/register` - Create a new user account (public)
- `POST /api/auth/login` - Authenticate and obtain JWT bearer token (public)
- `GET /api/users/me` - Fetch authenticated user profile (protected)
- `PUT /api/users/me` - Update profile name (protected)

### 2. Job & Internship Applications
- `POST /api/applications` - Create a job or internship application
- `GET /api/applications` - List applications with search & filter combinations
  - `type`: `JOB` or `INTERNSHIP`
  - `status`: `SAVED`, `APPLIED`, `ONLINE_ASSESSMENT`, `INTERVIEW`, `OFFER`, `REJECTED`, `WITHDRAWN`
  - `company`: Substring match (case-insensitive)
  - `location`: Substring match (case-insensitive)
  - `search`: General search across company, role, and location
- `GET /api/applications/{id}` - Fetch single application
- `PUT /api/applications/{id}` - Update application details
- `DELETE /api/applications/{id}` - Delete application and all associated child entities
- `GET /api/applications/{id}/history` - Retrieve audit log of status transitions

### 3. Interview Tracking
- `POST /api/applications/{id}/interviews` - Schedule an interview
- `GET /api/applications/{id}/interviews` - List all interviews for an application
- `GET /api/interviews/{id}` - Retrieve interview by ID
- `PUT /api/interviews/{id}` - Update interview details
- `DELETE /api/interviews/{id}` - Cancel/delete an interview

Supported Interview Types: `TECHNICAL`, `HR`, `MANAGERIAL`, `BEHAVIORAL`, `SYSTEM_DESIGN`, `OTHER`.

### 4. Dashboard & Analytics
- `GET /api/dashboard` - Retrieve user-scoped pipeline statistics:
  - **Overall**: total applications, active pipeline, interviews, offers, rejected, withdrawn
  - **By Application Type**: total jobs, total internships, job interviews, internship interviews, job offers, internship offers
  - **By Status**: breakdown counts for each stage
  - **Derived Metrics**: overall interview rate, overall offer rate, job conversion rate, internship conversion rate

---

## Example API Payloads

### Create Full-Time Job Application:
```http
POST /api/applications
Authorization: Bearer <token>
Content-Type: application/json

{
  "applicationType": "JOB",
  "companyName": "Google",
  "jobTitle": "Software Development Engineer",
  "location": "Bengaluru, India",
  "compensation": 2400000.00,
  "applicationDate": "2026-10-08",
  "status": "APPLIED",
  "workMode": "Hybrid",
  "applicationUrl": "https://careers.google.com/jobs/123",
  "notes": "Referred by senior software engineer"
}
```

### Create Internship Application:
```http
POST /api/applications
Authorization: Bearer <token>
Content-Type: application/json

{
  "applicationType": "INTERNSHIP",
  "companyName": "Microsoft",
  "jobTitle": "SWE Intern - Summer 2027",
  "location": "Hyderabad, India",
  "compensation": 125000.00,
  "applicationDate": "2026-10-08",
  "status": "APPLIED",
  "workMode": "On-Site",
  "startDate": "2027-05-15",
  "endDate": "2027-07-31",
  "notes": "Applied for summer pre-final year internship"
}
```

### Schedule Interview:
```http
POST /api/applications/1/interviews
Authorization: Bearer <token>
Content-Type: application/json

{
  "interviewDate": "2026-10-25T14:30:00+05:30",
  "interviewType": "TECHNICAL",
  "interviewer": "Engineering Manager",
  "locationOrLink": "https://meet.google.com/xyz-abcd-efg",
  "notes": "DSA + System Design basics round"
}
```

---

## Postman Manual Verification Guide

1. **Environment Setup**: Set `baseUrl` = `http://localhost:8080`.
2. **Register**: `POST {{baseUrl}}/api/auth/register` with user credentials.
3. **Login**: `POST {{baseUrl}}/api/auth/login` to receive JWT. Set collection variable `token`.
4. **Profile**: `GET {{baseUrl}}/api/users/me` with `Bearer {{token}}`.
5. **Create Applications**:
   - Create 1 full-time job application (`JOB`).
   - Create 1 summer internship application (`INTERNSHIP`).
6. **Filter & Search**:
   - `GET {{baseUrl}}/api/applications?type=INTERNSHIP`
   - `GET {{baseUrl}}/api/applications?type=JOB`
   - `GET {{baseUrl}}/api/applications?company=Google`
7. **Status Update & History**:
   - `PUT {{baseUrl}}/api/applications/1` changing status to `INTERVIEW`.
   - `GET {{baseUrl}}/api/applications/1/history` to verify chronological audit trail.
8. **Interview Tracking**:
   - `POST {{baseUrl}}/api/applications/1/interviews`
   - `GET {{baseUrl}}/api/interviews/1`
   - `PUT {{baseUrl}}/api/interviews/1`
9. **Dashboard Analytics**:
   - `GET {{baseUrl}}/api/dashboard` to verify that job and internship metrics are cleanly separated.
10. **Multi-Tenant Isolation**:
    - Register a second account.
    - Confirm that user 2 receives `404 Not Found` when requesting `/api/applications/1` or `/api/interviews/1`.
