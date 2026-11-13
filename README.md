# DhanushApp Backend (Dhanushappbe)

A modern, robust REST API backend for the **DhanushApp** personal productivity, fitness, nutrition, and personal vault suite. Built with **Spring Boot 4**, **Java 21**, **Spring Data JPA**, and **PostgreSQL**.

---

## 🚀 Key Features

* **Authentication & Security**:
  * Cookie-based HttpOnly JWT authentication (`SameSite=Lax`).
  * User registration, secure login, and session validation (`/api/auth/me`).
  * Secure Password Reset flow with cryptographically random SHA-256 hashed tokens and a 15-minute expiration.
  * In-memory IP + email rate limiting to prevent brute-force attacks.
* **Workout & Fitness Tracking**:
  * Full catalog of exercises with categorization and muscle groups.
  * Weekly workout schedule planner with position ordering.
* **Nutrition & Food Planning**:
  * Food items catalog with full macronutrient tracking (Calories, Protein, Carbs, Fats).
  * Daily and weekly nutrition plans.
* **Personal Dashboard**:
  * Unified metrics, weekly compliance statistics, and quick-access data.
* **Secure Vault**:
  * Zero-knowledge secret and credential storage encrypted using **AES-256-GCM**.
* **Personal Notes**:
  * Rich personal notes management with tag filtering and search.
* **Media & File Storage**:
  * Integrated Cloudinary image storage with local filesystem fallback for profile photos and attachments.
* **Cloud Native & Containerized**:
  * Multi-stage `Dockerfile` optimized for memory-constrained cloud environments (e.g., Render free tier with 512MB RAM).

---

## 🛠 Tech Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Java 21 (Eclipse Temurin) |
| **Framework** | Spring Boot 4.0.x |
| **Persistence** | Spring Data JPA, Hibernate |
| **Database** | PostgreSQL (Tested with local PostgreSQL and Neon DB) |
| **Security** | Spring Security, JJWT (0.12.6), BCrypt, AES-256 |
| **Cloud Storage** | Cloudinary Java SDK |
| **Utilities** | Apache Commons CSV, Project Lombok |
| **Containerization** | Docker (Multi-stage build) |

---

## 📂 Project Architecture

```
Dhanushappbe/
├── src/
│   ├── main/
│   │   ├── java/com/developersfab/Dhanushappbe/
│   │   │   ├── auth/         # Authentication, JWT, Rate Limiting, Password Reset
│   │   │   ├── common/       # Global exception handlers, Error codes, Base DTOs
│   │   │   ├── food/         # Food catalog, Categories, Nutrition tracking
│   │   │   ├── notes/        # Personal notes, Tags, Categorization
│   │   │   ├── plan/         # Workout & Diet weekly planner, Dashboard aggregation
│   │   │   ├── profile/      # User profile management
│   │   │   ├── security/     # Spring Security config, JWT filter, Token provider
│   │   │   ├── system/       # Health checks and system monitoring
│   │   │   ├── upload/       # Cloudinary and local media upload services
│   │   │   ├── vault/        # Encrypted credential and secret vault (AES-256)
│   │   │   └── workout/      # Exercises, Workout categories, Sets/Reps
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── db/           # Database migrations and seed scripts
├── .dockerignore
├── Dockerfile
├── pom.xml
└── README.md
```

---

## ⚙️ Environment Variables

The application can be configured via environment variables or directly inside `src/main/resources/application.yaml`:

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `PORT` | HTTP port the server binds to | `8080` |
| `DB_URL` | PostgreSQL JDBC Connection URL | `jdbc:postgresql://localhost:5432/dhanushdb` |
| `DB_USER` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `root` |
| `JWT_SECRET` | Secret key used to sign JWTs (min 32 characters) | *Random 256-bit secret string* |
| `VAULT_ENCRYPTION_KEY` | 32-byte Base64-encoded key for AES-256 Vault | *32-byte Base64 string* |
| `FRONTEND_BASE_URL` | Base URL of the client app for links/CORS | `http://localhost:5173` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed origins | `http://localhost:5173,http://127.0.0.1:5173` |
| `COOKIE_SECURE` | Set `true` in production (HTTPS) | `false` |
| `CLOUDINARY_CLOUD_NAME`| Cloudinary cloud account name | *(Optional)* |
| `CLOUDINARY_API_KEY`   | Cloudinary API key | *(Optional)* |
| `CLOUDINARY_API_SECRET`| Cloudinary API secret | *(Optional)* |

---

## 🏃 Local Setup & Running

### 1. Prerequisites
* **Java 21 JDK** installed (`java -version`)
* **Maven 3.9+** (or use the included `./mvnw`)
* **PostgreSQL** running locally or a cloud database (e.g. Neon)

### 2. Configure Database
Set up your local PostgreSQL database or configure Neon:
```bash
# Example local database creation
psql -U postgres
CREATE DATABASE dhanushdb;
```

### 3. Build & Run with Maven
```bash
# Clone the repository
git clone https://github.com/<your-username>/Dhanushappbe.git
cd Dhanushappbe

# Run the application
./mvnw spring-boot:run
# On Windows PowerShell:
.\mvnw.cmd spring-boot:run
```

The API will start at: `http://localhost:8080/api`

---

## 🐳 Docker Setup

### Build and Run with Docker
```bash
# Build the Docker image
docker build -t dhanushappbe:latest .

# Run the container
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://<host>:5432/<db>?sslmode=require" \
  -e DB_USER="<user>" \
  -e DB_PASSWORD="<password>" \
  -e JWT_SECRET="your-32-char-min-secure-jwt-secret-here" \
  -e VAULT_ENCRYPTION_KEY="your-base64-aes-key" \
  dhanushappbe:latest
```

---

## ☁️ Deploying to Render

This repository is ready for deployment on **Render** using Docker:

1. Push your repository to **GitHub**.
2. In the [Render Dashboard](https://dashboard.render.com/), click **New +** -> **Web Service**.
3. Connect this GitHub repository.
4. Render will automatically detect the `Dockerfile`.
5. Under the **Environment** settings, configure the following variables:
   * `DB_URL`: `jdbc:postgresql://<neon-host>/neondb?sslmode=require`
   * `DB_USER`: `<neon-user>`
   * `DB_PASSWORD`: `<neon-password>`
   * `JWT_SECRET`: *(A secure random 32+ character string)*
   * `VAULT_ENCRYPTION_KEY`: *(Your 32-byte Base64 key)*
   * `CORS_ALLOWED_ORIGINS`: `https://<your-frontend-app>.onrender.com`
   * `FRONTEND_BASE_URL`: `https://<your-frontend-app>.onrender.com`
   * `COOKIE_SECURE`: `true`
6. Click **Create Web Service**.

---

## 📡 API Reference Overview

All API endpoints are served under the `/api` context path.

### Authentication (`/api/auth`)
* `POST /api/auth/signup` - Register a new user
* `POST /api/auth/login` - Authenticate and obtain HttpOnly session cookie
* `POST /api/auth/logout` - Clear session cookie
* `GET  /api/auth/me` - Fetch authenticated user details
* `POST /api/auth/forgot-password` - Request a password reset link
* `POST /api/auth/reset-password` - Reset password using verified token

### Workouts & Exercises (`/api/workouts`)
* `GET  /api/workouts` - List all workouts (supports search & category filters)
* `POST /api/workouts` - Create custom workout entry
* `GET  /api/workouts/categories` - List workout categories

### Food & Nutrition (`/api/foods`)
* `GET  /api/foods` - List food items with nutritional macros
* `POST /api/foods` - Add a new food item
* `GET  /api/foods/categories` - List food categories

### Plans & Dashboard (`/api/plans`)
* `GET  /api/plans/workouts/week` - Fetch weekly workout plan
* `POST /api/plans/workouts/day` - Add workout entry to a specific day
* `DELETE /api/plans/workouts/{id}` - Remove workout entry
* `GET  /api/plans/foods/week` - Fetch weekly meal plan
* `GET  /api/plans/dashboard` - Retrieve aggregated dashboard statistics

### Encrypted Vault (`/api/vault`)
* `GET  /api/vault` - List encrypted secret items
* `POST /api/vault` - Store an encrypted item (AES-256)
* `GET  /api/vault/{id}/reveal` - Decrypt and reveal secret content

### Notes (`/api/notes`)
* `GET  /api/notes` - List notes (with pagination and search)
* `POST /api/notes` - Create a note
* `PUT  /api/notes/{id}` - Update a note
* `DELETE /api/notes/{id}` - Delete a note

### System
* `GET  /api/system/health` - Health check endpoint

---

## 📄 License
This project is proprietary and developed by **DevelopersFab**. All rights reserved.

