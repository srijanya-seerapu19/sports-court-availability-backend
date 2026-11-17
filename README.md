# 🏟️ Courtside Backend — Sports Court Availability & Booking API

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat&logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg?style=flat&logo=mysql)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg?style=flat&logo=docker)](https://www.docker.com/)

A robust, enterprise-grade RESTful API engineered for **Anurag University's Campus Sports Complex**. It enables students to view real-time court and ground availability across 9 sports disciplines, register student accounts, authenticate securely, and reserve courts with automated session lifecycle management.

---

## 🚀 Key Highlights & Architecture

- **Concurrency-Safe Court Reservations:** Employs JPA pessimistic write locking (`SELECT ... FOR UPDATE`) in `CourtRepository.findByIdForBooking` to guarantee strict serializability and eliminate double-booking race conditions during high-demand peak hours.
- **Automated Lifecycle Management:** Background scheduled worker (`BookingExpirationScheduler`) sweeps every 10 seconds to automatically transition expired sessions to `COMPLETED` and immediately release courts for other students.
- **Session-Based Authentication:** Integrated with **Spring Security** utilizing BCrypt password hashing, session state persistence via `HttpSessionSecurityContextRepository`, and support for secure cross-origin cookies (`SameSite=None`, `Secure=true`) for decoupled cloud deployments.
- **Cloud-Native & Production Ready:** Multi-stage Docker build, pre-configured HikariCP connection pooling, and flexible environment variable injection supporting local MySQL as well as serverless cloud providers (TiDB Cloud, Aiven, Railway).

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Java 21 LTS (Eclipse Temurin) |
| **Framework** | Spring Boot 3 (Web, Security, Data JPA, Validation) |
| **Persistence & ORM** | Hibernate 7 / Spring Data JPA |
| **Database** | MySQL 8.0 / Compatible MySQL Cloud (TiDB Cloud, Aiven) |
| **Connection Pool** | HikariCP (optimized timeouts & keepalive) |
| **Containerization** | Multi-stage Dockerfile (JRE 21 minimal runtime) |
| **Build Tool** | Apache Maven 3.9+ / Maven Wrapper (`mvnw`) |

---

## 📡 REST API Reference

### Health & System
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/` or `/api/health` | Public | System status, service health, and server timestamp |

### Courts & Live Availability
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/courts` | Public | Returns all 18 courts with active status, availability flag, and live booking summaries |

### Student Authentication
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register a new student (`fullName`, `email`, `password`) |
| `POST` | `/api/auth/login` | Public | Sign in and establish authenticated session cookie |
| `GET` | `/api/auth/me` | Authenticated | Fetch details of the currently signed-in student |
| `POST` | `/api/auth/logout` | Authenticated | Invalidate active session and clear security context |

### Court Reservations
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/bookings` | Authenticated | Create a court reservation (`courtId`, `durationMinutes`: 15, 30, 45, or 60) |
| `GET` | `/api/bookings/mine` | Authenticated | Retrieve session history for the logged-in student |
| `DELETE` | `/api/bookings/{id}` | Authenticated | End active session early and instantly release the court |

---

## ⚙️ Configuration & Environment Variables

The application can be configured via `src/main/resources/application.properties` or standard environment variables:

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `PORT` | `8080` | HTTP port the Tomcat server listens on |
| `DB_URL` | `jdbc:mysql://localhost:3306/sports_court?...` | JDBC connection URL |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | `root` | Database password |
| `COOKIE_SAME_SITE` | `lax` (`none` in production) | SameSite cookie policy |
| `COOKIE_SECURE` | `false` (`true` in production) | Secure HTTPS flag for session cookies |

---

## 💻 Local Setup & Execution

### Prerequisites
- **JDK 21** installed (`java -version`)
- **MySQL 8.0** running on `localhost:3306` with database `sports_court` created:
  ```sql
  CREATE DATABASE sports_court CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```

### Build and Run
```bash
# Clone the repository
git clone https://github.com/srijanya-seerapu19/sports-court-availability-backend.git
cd sports-court-availability-backend

# Build the JAR (skipping tests if DB not yet configured)
./mvnw clean package -DskipTests

# Run the packaged Spring Boot application
java -jar target/backend-0.0.1-SNAPSHOT.jar
```
Starter data (18 courts across Badminton, Basketball, Cricket, Football, Tennis, Volleyball, Table Tennis, Carrom, and Chess) will be automatically initialized on first boot via `CourtDataInitializer`.

---

## ☁️ Production Deployment (Render + Cloud MySQL)

1. **Cloud Database:** Create a free MySQL database on [TiDB Cloud](https://tidbcloud.com) or [Aiven](https://aiven.io).
2. **Render Web Service:**
   - Link this repository on [Render](https://render.com).
   - Environment: **Docker**
   - Configure Environment Variables:
     ```env
     DB_URL=jdbc:mysql://<host>:<port>/sports_court?useSSL=true&requireSSL=true
     DB_USERNAME=<username>
     DB_PASSWORD=<password>
     COOKIE_SAME_SITE=none
     COOKIE_SECURE=true
     ```
3. Deploy! Render will build the lightweight Docker container and start serving requests on port 10000.
