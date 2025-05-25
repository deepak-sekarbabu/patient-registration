# Patient Registration

A modern, secure, and high-performance Patient Registration system built with Spring Boot 3.5+, Java 21, and MySQL. It supports robust patient management, JWT-based authentication, Redis caching, and comprehensive API documentation via Swagger/OpenAPI.

## Features

- Register, view, search, update, and delete patient records
- JWT-based authentication and authorization
- Redis integration for caching
- MySQL database support
- OpenAPI (Swagger) documentation
- Global exception handling
- Input validation with annotations
- Layered architecture (Controller → Service → Repository)
- Performance optimizations (caching, async processing)
- Unit and integration tests with JUnit 5 & Mockito

## Technologies Used

- Java 21+
- Spring Boot 3.5+
- Spring Data JPA (Hibernate)
- Maven
- MySQL
- Redis
- JWT (JSON Web Token)
- Swagger/OpenAPI
- Lombok
- Flyway (for DB migrations)
- JUnit 5, Mockito

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven
- MySQL
- Redis

### Setup and Installation

```bash
# Clone the repository
git clone [repository_url]

# Navigate to the project directory
cd patient-registration

# Build the project using Maven
mvn clean install

# Run the application
mvn spring-boot:run
```

### Database, Redis & JWT Configuration

Update `src/main/resources/application.properties` with your credentials:

- **MySQL:**
  - `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`
- **Redis:**
  - `spring.data.redis.host`, `spring.data.redis.port`, `spring.data.redis.password`
- **JWT:**
  - `app.jwt.secret` (256+ chars recommended)

### Running Tests

To run all tests:

```bash
mvn test
```

## API Documentation

Once running, access Swagger UI at:

- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- or [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)

## Usage

Use Swagger UI to explore and test all API endpoints for patient registration and management.

---

## Project Structure & Best Practices

- **Layered architecture:** Controller → Service → Repository
- **Validation:** Use `@Valid`, `@NotBlank`, `@Size`, etc. for input validation
- **Exception Handling:** Centralized with `@ControllerAdvice`
- **Caching:** Use `@Cacheable` for frequently accessed reads
- **Async:** Use `@Async` for long-running tasks
- **Testing:** High coverage with unit/integration tests
- **Code Quality:** Follows Google Java Style, uses Spotless/Checkstyle
- **Documentation:** JavaDoc for public classes/methods, Swagger annotations for APIs

---


