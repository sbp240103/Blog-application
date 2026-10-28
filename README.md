# 📝 Blog Application

A production-style REST backend for a blogging platform, built with **Java 17** and **Spring Boot 4**. Users can write posts, organise them with categories and tags, comment, like, bookmark, follow other writers and read a personalised feed. Access is secured with JWT and role-based authorization.

## ✨ Features

**Authentication & security**
- Register and log in with JWT (stateless bearer tokens), plus form login and HTTP Basic
- Passwords hashed with BCrypt, change password for the logged in user
- Roles stored in the database (`ROLE_USER`, `ROLE_ADMIN`), admin endpoints protected with `@PreAuthorize`
- Ownership rules: only the author (or an admin) can edit or delete a post, comment or account
- Optional admin account seeded from environment variables
- CORS configuration for browser frontends

**Content**
- Posts with categories, tags (many to many), image upload and view counts
- Comments with author and timestamp
- Likes, bookmarks and "who liked this"
- Follow / unfollow users, followers and following lists
- Personalised feed with posts from followed users
- Trending posts by views
- Case insensitive search over title and content

**API quality**
- Pagination and sorting on every listing (`pageNumber`, `pageSize`, `sortBy`, `sortDirection`)
- Request validation with clear field level error messages
- Consistent JSON errors (400, 401, 403, 404, 409, 413) from a global exception handler
- Image upload validation (jpg / jpeg / png, max 5 MB) and safe file serving
- Swagger UI / OpenAPI documentation with JWT support
- Actuator health and info endpoints
- Unit tests for services and JWT handling

## 🛠️ Tech Stack

Java 17 · Spring Boot 4 · Spring MVC · Spring Security · Spring Data JPA / Hibernate · MySQL · JJWT · ModelMapper · Lombok · springdoc-openapi · JUnit 5 · Mockito · Maven

## 🏗️ Architecture

```
Client ──> Controller ──> Service ──> Repository ──> MySQL
             │              │
             │              └── PostMapper / AuthUtil / PageUtil
             └── DTOs + validation      JwtAuthenticationFilter runs before every request
```

```
src/main/java/com/project/app
├── config/        security, cors, openapi, role and admin seeding
├── controller/    rest endpoints
├── entities/      jpa entities
├── exceptions/    ApiException, ResouceNotFoundException, GlobalExceptionHandler
├── impl/          service implementations
├── payloads/      request and response dtos
├── repositories/  spring data repositories
├── security/      jwt filter, token helper, AuthUtil
├── services/      service interfaces
└── utils/         pagination and mapping helpers
```

## ⚙️ Getting Started

### Prerequisites
Java 17+, Maven (or the included `mvnw`), MySQL 8

### Configure
Secrets are never committed. Create a `.env` file in the project root (it is gitignored):

```properties
MYSQL_DB_USER=root
MYSQL_DB_PASSWORD=your_password
MYSQL_DB_NAME=blog_app
```

The database is created automatically if it does not exist. Optional variables:

| Variable | Purpose | Default |
|---|---|---|
| `SERVER_PORT` | HTTP port | `9090` |
| `MYSQL_DB_HOST` / `MYSQL_DB_PORT` | database location | `localhost` / `3306` |
| `JWT_SECRET` | token signing key, at least 32 characters | development key |
| `JWT_EXPIRATION_MS` | token lifetime | `18000000` (5 hours) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | seed an admin account on startup | not seeded |
| `CORS_ALLOWED_ORIGINS` | comma separated frontend origins | `http://localhost:3000,http://localhost:5173` |

### Run

```bash
./mvnw spring-boot:run
```

- API: `http://localhost:9090`
- Swagger UI: `http://localhost:9090/swagger-ui.html`
- Health: `http://localhost:9090/actuator/health`

### Test

```bash
./mvnw test
```

## 🔐 Using the API

```bash
# register
curl -X POST http://localhost:9090/api/v1/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Pushkar","email":"pushkar@example.com","password":"secret123","about":"java dev"}'

# login, the response contains a token and the user
curl -X POST http://localhost:9090/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"email":"pushkar@example.com","password":"secret123"}'

# call a protected endpoint
curl http://localhost:9090/api/users/me -H "Authorization: Bearer <token>"
```

All `GET` endpoints are public except the personal ones (`/me`, `/feed`, `/bookmarks`) and `/api/admin/**`.

## 🔗 API Reference

**Auth**
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
```

**Users**
```
GET    /api/users                          paginated
POST   /api/users
GET    /api/users/me                       logged in user
PUT    /api/users/me/password              change password
GET    /api/users/{userId}
PUT    /api/users/{userId}                 owner or admin
DELETE /api/users/{userId}                 admin
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
PUT    /api/posts/{postId}                 owner or admin
DELETE /api/posts/{postId}                 owner or admin
GET    /api/user/{userId}/posts
GET    /api/category/{categoryId}/posts
GET    /api/posts/search/{keyword}
GET    /api/posts/trending?limit=5
GET    /api/feed                           posts from followed users
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
GET    /api/post/{postId}/comments
POST   /api/post/{postId}/comments
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
GET    /api/tags/{tagName}/posts
```

**Admin**
```
GET    /api/admin/stats
PUT    /api/admin/users/{userId}/promote
PUT    /api/admin/users/{userId}/demote
```

## 📄 License

This project is intended for educational and development purposes.

## 👨‍💻 Author

**Pushkar Singh** — Java Backend Developer
GitHub: https://github.com/sbp240103
