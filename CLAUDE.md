# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**优答·中小学答疑网** — An online learning community platform for K-12 students, teachers, and parents. Features: community posts, study resources library, AI Q&A (stub, pending real API integration), video courses, and admin management.

Tech stack:
- **Backend**: Spring Boot 2.7.18, Java 11, MyBatis-Plus 3.5.5, JWT (jjwt 0.11.5), Hutool, MySQL 8.0
- **Frontend**: Vue 3 + Ant Design Vue + Pinia + Vue Router (directory exists but not yet scaffolded)
- **Storage**: Local file system (`./uploads/`)

## Backend Commands

```bash
cd youda-backend

# Run the application
mvn spring-boot:run

# Build (skip tests)
mvn clean package -DskipTests

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName
```

## Database Setup

```sql
-- Run init script
source youda-backend/src/main/resources/sql/init.sql
-- Or create manually:
CREATE DATABASE IF NOT EXISTS youda DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

Default DB config in `application.yml`: `localhost:3306/youda`, user `root`, password `root`. Update before running.

## Architecture

### Backend Package Structure (`com.youda`)

- `controller/` — REST controllers, all routes prefixed `/api/`
- `service/` + `service/impl/` — Business logic (services extend MyBatis-Plus `ServiceImpl`)
- `mapper/` — MyBatis-Plus mapper interfaces
- `entity/` — JPA/MyBatis-Plus entities with Lombok `@Data`
- `dto/` — Request input objects (validation annotations)
- `vo/` — Response output objects
- `config/` — `WebConfig` (CORS, interceptors, static resources), `MybatisPlusConfig` (pagination plugin), `JwtInterceptor`
- `common/` — `Result<T>` unified response wrapper, `BusinessException`, `GlobalExceptionHandler`
- `utils/` — `JwtUtils`, `UserContext`, `FileUtils`

### Request Flow

1. `JwtInterceptor` parses `Authorization: Bearer <token>` header and stores user ID in request attribute via `UserContext.setCurrentUserId()`
2. Controllers call `UserContext.getCurrentUserId()` / `getCurrentUserIdOrNull()` for auth context — throws 401 `BusinessException` if not authenticated
3. Admin-only endpoints call a local `checkAdmin()` helper that verifies `user.role == 1`
4. All responses use `Result.success(data)` / `Result.error(code, message)` wrapper

### Authentication

- JWT token lifetime: 7 days (`jwt.expiration: 604800000`)
- Passwords hashed with BCrypt via Hutool (`BCrypt.hashpw` / `BCrypt.checkpw`)
- User roles: `0` = regular user, `1` = admin
- User status: `0` = disabled, `1` = active

### Public endpoints (no auth required)

`/api/user/login`, `/api/user/register`, `/api/category/list`, `/api/subject/list`, `/api/grade/list`, `/api/post/list`, `/api/post/*`, `/api/resource/list`, `/api/resource/*`, `/api/course/list`, `/api/course/*`, `/api/announcement/list`

### MyBatis-Plus Conventions

- All entities use `IdType.AUTO` (auto-increment)
- Soft delete via `isDeleted` field (0 = not deleted, 1 = deleted) — configured globally in `application.yml`
- `createTime` / `updateTime` use `@TableField(fill = FieldFill.INSERT)` / `INSERT_UPDATE`
- Underscore-to-camelCase mapping enabled
- Custom SQL queries in `src/main/resources/mapper/*.xml`

### AI Chat Module

`ChatServiceImpl.sendMessage()` currently uses a stub `generateAIResponse()` method. Real OpenAI (or compatible) API integration should replace this method. Chat sessions are identified by a UUID-based `chatId` string (not the DB primary key).

### File Upload

Uploaded files stored at `./uploads/` (relative to working directory). Served via `/uploads/**` static resource handler. Max upload size: 50MB per file / 50MB per request.

## Key Documentation

Detailed specs are in `docs/`:
- `01-需求文档.md` — Requirements and user roles
- `02-功能文档.md` — Feature descriptions and business flows
- `03-接口文档.md` — Full REST API reference with request/response examples
- `04-数据库设计文档.md` — Full DB schema, ER diagram, indexes, seed data
