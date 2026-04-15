# Budget Management App

A REST API for personal budget and expense management built with Java 21, Spring Boot 3.5.1 and PostgreSQL.

## Prerequisites

Make sure you have the following installed:

- [Java 21](https://adoptium.net/)
- [Maven](https://maven.apache.org/download.cgi)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Rares9912/budget-management-app.git
cd budget-management-app
```

### 2. Start the database

```bash
docker compose up -d
```

This will start a PostgreSQL 18 container with the following configuration:

| Property | Value |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | budget_db |
| Username | postgres |
| Password | postgres |

### 3. Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

Database migrations will run automatically via Flyway on startup.