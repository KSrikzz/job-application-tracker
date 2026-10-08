# Job & Internship Application Tracker

A production-style, multi-tenant Full-Stack web application designed to track and manage both **full-time job applications** and **internship applications**, upcoming interviews, status transitions, and career progression analytics.

---

## Key Features

- **Unified Application Tracking**: Seamlessly manages both full-time jobs and internships under a unified, extensible data model.
- **Internship-Specific Domain Support**: Custom fields for compensation (salary vs. stipend), internship duration (`startDate`, `endDate`), work mode (`Remote`, `Hybrid`, `On-Site`), and application URLs.
- **Search & Multi-Dimensional Filtering**: Real-time filtering by `type` (JOB / INTERNSHIP), `status`, `company`, `location`, and general keyword queries.
- **Chronological Status History**: Automated transition auditing tracking every status change from initial creation (`SAVED`, `APPLIED`, `ONLINE_ASSESSMENT`, `INTERVIEW`, `OFFER`, `REJECTED`, `WITHDRAWN`).
- **Comprehensive Interview Tracking**: Schedule and manage multiple rounds (`TECHNICAL`, `HR`, `BEHAVIORAL`, `MANAGERIAL`, `SYSTEM_DESIGN`) with date/time, meeting links, and interviewer notes.
- **Analytics Dashboard**: Aggregated metrics separating full-time jobs from internships, active pipeline tracking, and derived conversion rates.
- **Enterprise-Grade Security**: Stateless JWT authentication, BCrypt password hashing, Jakarta Bean Validation, and multi-tenant user isolation.

---

## Technology Stack

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 4.x
- **Security**: Spring Security 6.x, JJWT 0.12.6, BCrypt
- **Data & Persistence**: Spring Data JPA, Hibernate, PostgreSQL
- **Build Tool**: Apache Maven
- **Testing**: JUnit 5, Mockito, Spring Boot Test, MockMvc

### Frontend
- **Framework**: React, JavaScript (ES6+), HTML5, CSS3

---

## Project Structure

```text
job-application-tracker/
├── backend/
│   ├── pom.xml
│   ├── README.md               # Detailed backend documentation & API spec
│   ├── src/
│   │   ├── main/java/com/jobtracker/
│   │   │   ├── config/         # Security & JWT filters
│   │   │   ├── controller/     # Auth, User, Application, Interview, Dashboard
│   │   │   ├── dto/            # Validated request and response schemas
│   │   │   ├── entity/         # User, JobApplication, Interview, StatusHistory
│   │   │   ├── exception/      # Domain exceptions & GlobalExceptionHandler
│   │   │   ├── repository/     # Spring Data JPA repositories
│   │   │   └── service/        # Business logic & security validation
│   │   └── test/java/com/jobtracker/
├── frontend/                   # React frontend application
├── docs/                       # Project documentation & architecture notes
├── .gitignore
└── README.md
```

---

## Getting Started

### Prerequisites
- Java 21 JDK
- PostgreSQL 15+
- Node.js & npm (for frontend)

### Quick Start (Backend)

1. **Configure Environment Variables**:
   ```powershell
   $env:DB_USERNAME = "postgres"
   $env:DB_PASSWORD = "yourpassword"
   $env:JWT_SECRET = "jobtracker-secret-key-at-least-32-bytes-long-123456"
   ```

2. **Run Tests**:
   ```powershell
   cd backend
   .\mvnw.cmd test
   ```

3. **Start Application**:
   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

For detailed API specifications, sample payloads, and Postman testing instructions, see [backend/README.md](backend/README.md).
