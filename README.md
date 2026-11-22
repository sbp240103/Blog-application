# 📝 Blog Application

A production-style REST backend for a blogging platform, built with **Java 17** and **Spring Boot 4**. Users write and publish posts, organise them with categories and tags, comment and reply, like, bookmark, follow other writers, get notifications and read a personalised feed. Admins moderate reported content and manage accounts. Everything is secured with JWT access and refresh tokens and role-based authorization.

![CI](https://github.com/sbp240103/Blog-application/actions/workflows/ci.yml/badge.svg)

## ✨ Features

**Accounts & security**
- Register, login, refresh token rotation and logout (JWT access token + revocable refresh token)
- Forgot / reset password with one-time, expiring tokens
- BCrypt passwords with strength rules, change password, partial profile updates, avatars
- Account lockout after repeated failed logins and per-IP rate limiting on login
- Roles stored in the database (`ROLE_USER`, `ROLE_ADMIN`), admins can ban, unban, promote and demote
- Ownership rules: only the author (or an admin) can change a post, comment or account
- Personal data export and self-service account deletion

**Content**
- Posts with drafts, publishing, scheduled publishing, SEO-friendly slugs, reading time and excerpts
- Categories, tags (many-to-many), popular tags, featured posts, archive by year and month
- Image upload with type and size validation
- Comments with one level of replies, paginated
- Likes, bookmarks, view counts, trending and related posts
- Case insensitive search over title and content

**Social**
- Follow / unfollow, followers and following lists, public profiles
- Personalised feed, follow suggestions, top authors, user search
- Notifications for likes, comments, replies and follows with unread counts

**Moderation**
- Report posts and comments, admin queue to resolve (optionally removing the content) or dismiss
- Configurable banned-words filter for posts and comments
- Admin dashboard stats

**Engineering**
- Pagination and whitelisted sorting on every listing
- One consistent error format with timestamp, path and request id
- Optimistic locking on posts, caching for categories and tags
- Request id tracing in every log line (`X-Request-Id`)
- Swagger UI, actuator health (database, disk and image storage) and info
- Unit and integration tests on an in-memory H2 database, GitHub Actions CI
- Docker image and docker compose setup with MySQL

## 🛠️ Tech Stack

Java 17 · Spring Boot 4 · Spring MVC · Spring Security · Spring Data JPA / Hibernate · MySQL · H2 (tests) · JJWT · ModelMapper · Lombok · springdoc-openapi · Spring Cache · JUnit 5 · Mockito · Docker · GitHub Actions · Maven

## 🏗️ Architecture

```
Client ─> RequestIdFilter ─> JwtAuthenticationFilter ─> Controller ─> Service ─> Repository ─> MySQL
                                                           │             │
                                                     DTOs + validation   PostMapper, AuthUtil, PageUtil,
                                                                         ContentFilter, schedulers
```

```
src/main/java/com/project/app
├── config/        security, cors, openapi, seeding, schedulers, request id filter, health
├── controller/    rest endpoints
├── entities/      jpa entities and enums
├── exceptions/    ApiException, ResouceNotFoundException, GlobalExceptionHandler
├── impl/          service implementations
├── payloads/      request and response dtos
├── repositories/  spring data repositories
├── security/      jwt filter and helper, AuthUtil, login rate limiter
├── services/      service interfaces
├── utils/         pagination, slugs, post mapping, content filter
└── validation/    custom validation annotations
```

## ⚙️ Getting Started

### Prerequisites
Java 17+, Maven (or the included `mvnw`), MySQL 8 — or just Docker.

### Configure
Secrets are never committed. Create a `.env` file in the project root (it is gitignored):

```properties
MYSQL_DB_USER=root
MYSQL_DB_PASSWORD=your_password
MYSQL_DB_NAME=blog_app
```

The database is created automatically if it does not exist. Optional settings:

| Variable | Purpose | Default |
|---|---|---|
| `SERVER_PORT` | HTTP port | `9090` |
| `MYSQL_DB_HOST` / `MYSQL_DB_PORT` | database location | `localhost` / `3306` |
| `JWT_SECRET` | token signing key, at least 32 characters | development key |
| `JWT_EXPIRATION_MS` | access token lifetime | `18000000` (5 hours) |
| `JWT_REFRESH_EXPIRATION_DAYS` | refresh token lifetime | `7` |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | seed an admin account on startup | not seeded |
| `CORS_ALLOWED_ORIGINS` | comma separated frontend origins | `http://localhost:3000,http://localhost:5173` |
| `RESET_PASSWORD_URL` | link sent in reset mails, token appended | `http://localhost:3000/reset-password?token=` |
| `BANNED_WORDS` | comma separated phrases rejected in content | a few spam phrases |

### Run locally

```bash
./mvnw spring-boot:run
```

### Run with Docker

```bash
docker compose up --build
```

Compose starts MySQL and the app together and reads the same `.env` file.

- API: `http://localhost:9090`
- Swagger UI: `http://localhost:9090/swagger-ui.html`
- Health: `http://localhost:9090/actuator/health`

### Test

```bash
./mvnw test
```

Tests run on an in-memory H2 database, no MySQL needed.

## 🔐 Using the API

```bash
# register
curl -X POST http://localhost:9090/api/v1/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Pushkar","email":"pushkar@example.com","password":"secret123","about":"java dev"}'

# login, returns token, refreshToken and the user
curl -X POST http://localhost:9090/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"email":"pushkar@example.com","password":"secret123"}'

# call a protected endpoint
curl http://localhost:9090/api/users/me -H "Authorization: Bearer <token>"

# get a new token when the old one expires
curl -X POST http://localhost:9090/api/v1/auth/refresh -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refreshToken>"}'
```

Most `GET` endpoints are public. Personal endpoints (`/me`, `/feed`, `/bookmarks`, `/notifications`, `/posts/drafts`) need a token, `/api/admin/**` needs the admin role.

### Errors

Every error has the same shape:

```json
{
  "timestamp": "2026-11-22T14:03:11",
  "status": 400,
  "error": "Bad Request",
  "message": "validation failed",
  "path": "/api/v1/auth/register",
  "requestId": "3f9c2a1b",
  "fieldErrors": { "password": "password must contain at least one letter and one number" }
}
```

Quote the `requestId` when reporting a problem, it appears in every server log line for that request.

## 🔗 API Reference

**Auth**
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
```

**Users**
```
GET    /api/users                          paginated
POST   /api/users
GET    /api/users/search?keyword=
GET    /api/users/top-authors
GET    /api/users/{userId}
GET    /api/users/{userId}/profile
PUT    /api/users/{userId}                 owner or admin
DELETE /api/users/{userId}                 admin
GET    /api/users/avatar/{imageName}
```

**Me**
```
GET    /api/users/me
PATCH  /api/users/me                       name and about
DELETE /api/users/me                       password confirmation
PUT    /api/users/me/password
POST   /api/users/me/avatar                multipart field "image"
GET    /api/users/me/stats
GET    /api/users/me/suggestions
GET    /api/users/me/export
```

**Follows**
```
POST   /api/users/{userId}/follow
DELETE /api/users/{userId}/follow
GET    /api/users/{userId}/followers
GET    /api/users/{userId}/following
GET    /api/users/{userId}/follow-stats
```

**Posts**
```
GET    /api/posts                          paginated, sortable
POST   /api/user/{userId}/category/{categoryId}/posts
GET    /api/posts/{postId}                 counts a view
GET    /api/posts/slug/{slug}
PUT    /api/posts/{postId}                 owner or admin
DELETE /api/posts/{postId}                 owner or admin
PUT    /api/posts/{postId}/publish
PUT    /api/posts/{postId}/unpublish
GET    /api/posts/drafts
GET    /api/posts/featured
GET    /api/posts/trending?limit=5
GET    /api/posts/archive?year=&month=
GET    /api/posts/search/{keyword}
GET    /api/posts/{postId}/related
GET    /api/user/{userId}/posts
GET    /api/category/{categoryId}/posts
GET    /api/feed
POST   /api/post/image/upload/{postId}     multipart field "image"
GET    /api/post/image/{imageName}
```

**Likes & bookmarks**
```
POST   /api/posts/{postId}/like
DELETE /api/posts/{postId}/like
GET    /api/posts/{postId}/likes
POST   /api/posts/{postId}/bookmark
DELETE /api/posts/{postId}/bookmark
GET    /api/bookmarks
```

**Comments**
```
GET    /api/post/{postId}/comments         top level, paginated, with reply counts
POST   /api/post/{postId}/comments
GET    /api/comments/{commentId}/replies
POST   /api/comments/{commentId}/replies
PUT    /api/comments/{commentId}           author
DELETE /api/comments/{commentId}           author, post owner or admin
```

**Categories & tags**
```
GET    /api/categories                     paginated
POST   /api/categories                     admin
GET    /api/categories/{categoryId}
PUT    /api/categories/{categoryId}        admin
DELETE /api/categories/{categoryId}        admin
GET    /api/tags
GET    /api/tags/popular
GET    /api/tags/{tagName}/posts
```

**Notifications**
```
GET    /api/notifications
GET    /api/notifications/unread-count
PUT    /api/notifications/{notificationId}/read
PUT    /api/notifications/read-all
```

**Reports & moderation**
```
POST   /api/reports
GET    /api/admin/reports?status=OPEN
PUT    /api/admin/reports/{reportId}/resolve?removeContent=true
PUT    /api/admin/reports/{reportId}/dismiss
```

**Admin**
```
GET    /api/admin/stats
PUT    /api/admin/users/{userId}/promote
PUT    /api/admin/users/{userId}/demote
PUT    /api/admin/users/{userId}/ban
PUT    /api/admin/users/{userId}/unban
PUT    /api/admin/posts/{postId}/feature
PUT    /api/admin/posts/{postId}/unfeature
```

## 📄 License

This project is intended for educational and development purposes.

## 👨‍💻 Author

**Pushkar Singh** — Java Backend Developer
GitHub: https://github.com/sbp240103
