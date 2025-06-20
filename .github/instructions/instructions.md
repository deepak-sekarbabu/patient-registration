---
trigger: model_decision
description: Java 21 + Spring Boot 3.5+ Code Style Guide
---

# Java Spring Boot Code Style Guide

## 📋 Table of Contents

- [1. Code Style & Formatting](#1-code-style--formatting)
- [2. Project Structure](#2-project-structure)
- [3. Java & Spring Boot](#3-java--spring-boot)
- [4. Database](#4-database)
- [5. REST API](#5-rest-api)
- [6. Testing](#6-testing)
- [7. Documentation](#7-documentation)
- [8. Security](#8-security)
- [9. Performance](#9-performance)
- [10. CI/CD & Build](#10-cicd--build)
- [11. Git & Version Control](#11-git--version-control)

---

## 1. Code Style & Formatting

### General

- Follow **Google Java Style Guide** with 4-space indentation
- Use **UTF-8** file encoding
- Maximum line length: **120 characters**
- Use Unix-style line endings (LF)
- Remove trailing whitespace
- End files with a single newline
- Use Spotless Maven plugin for code formatting

### Naming Conventions

- **Classes**: `PascalCase` (e.g., `PatientController`)
- **Methods**: `camelCase` (e.g., `findPatientById`)
- **Variables**: `camelCase` (e.g., `patientName`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_ATTEMPTS`)
- **Packages**: `lowercase` (e.g., `com.deepak.patient.registration`)
- **Test Classes**: Suffix with `Test` (e.g., `PatientServiceTest`)
- **DTOs**: Suffix with `Request`/`Response` (e.g., `PatientRequest`, `AppointmentResponse`)
- **Converters**: Suffix with `Converter` (e.g., `PatientConverter`)

### Code Organization

- Group related code together
- Keep classes focused and single-responsibility
- Limit file size to **400 lines** maximum
- Use `@Order` annotation for configuration classes when load order matters
- Place configuration classes in the `config` package
- Keep controller methods focused on HTTP concerns only
- Move business logic to service layer

## 2. Project Structure

```
src/
├── main/
│   ├── java/com/deepak/
│   │   ├── appointment/registration/    # Appointment management
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── converter/        # DTO <-> Entity converters
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── repository/       # JPA repositories
│   │   │   └── service/          # Business logic
│   │   │
│   │   └── patient/registration/   # Patient management
│   │       ├── config/           # Configuration classes
│   │       ├── controller/       # REST controllers
│   │       ├── model/            # Domain models
│   │       ├── repository/       # JPA repositories
│   │       └── service/          # Business logic
│   │
│   └── resources/
│       ├── db/migration/     # Flyway migrations (V{version}__{description}.sql)
│       ├── static/           # Static resources
│       ├── templates/        # Server-side templates (if using Thymeleaf)
│       ├── application.yml   # Main configuration
│       └── application-{profile}.yml  # Profile-specific configs
│
└── test/                     # Test code mirrors main structure
    └── java/com/deepak/
        └── ...
```

## 3. Java & Spring Boot

### General

- Use **Java 21** features:
  - Records for immutable data carriers
  - Text blocks for multi-line strings
  - Pattern matching for `instanceof`
  - Sealed classes for restricted hierarchies
  - Local variable type inference (`var`) where it improves readability
- Prefer **immutable** objects using `record` or `@Value`
- Use `final` for method parameters and local variables when they shouldn't be reassigned
- Avoid `null` where possible - use `Optional` for return types that might be empty
- Use `@Data` for JPA entities (with `@EqualsAndHashCode` and `@ToString` excluded where needed)
- Use `@Builder` for complex object creation

### Spring Specific

- Use constructor injection (via `@RequiredArgsConstructor` from Lombok)
- Use `@Slf4j` for logging
- Prefer `@ConfigurationProperties` over `@Value`
- Use `@Profile` for environment-specific configurations
- Implement `CommandLineRunner` or `ApplicationRunner` for startup tasks
- Use `@Transactional` at service layer (not repository)
- Use `@EnableCaching` with appropriate cache configurations
- Leverage Spring's `@Async` for non-blocking operations

### Validation

- Use Jakarta Bean Validation 3.0
- Create custom validators when needed (e.g., `@ValidEmail`)
- Use validation groups for different validation scenarios
- Always validate input at controller level with `@Valid`
- Use `@Validated` at class level for method validation
- Return meaningful error messages with `@ExceptionHandler`

## 4. Database

### JPA Entities

- Use `@Entity` for all persistent classes
- Use `@Id` with `@GeneratedValue(strategy = IDENTITY)` for MySQL
- Use appropriate column types and constraints (`@Column`, `@Size`, `@NotNull`)
- Implement `equals()` and `hashCode()` consistently (be careful with collections)
- Use `@Version` for optimistic locking
- Prefer `java.time` types (`LocalDate`, `LocalDateTime`, etc.)
- Use `@CreationTimestamp` and `@UpdateTimestamp` for audit fields
- Document entity relationships clearly

### Repository

- Extend `JpaRepository` or `PagingAndSortingRepository`
- Use `@Query` with JPQL for complex queries
- Use `@EntityGraph` for fetching strategies
- Implement custom repository interfaces when needed
- Use `@Modifying` for update/delete queries
- Use `@Lock` for pessimistic locking when needed
- Consider using Querydsl for type-safe queries

### Migrations

- Use **Flyway** for database migrations
- Name migrations as `V{version}__{description}.sql` (e.g., `V1__create_patient_table.sql`)
- Keep migrations idempotent
- Include rollback scripts when applicable
- Test migrations in non-production environments first
- Document breaking changes in migration files

## 5. REST API

### General

- Use plural nouns for resources (`/api/v1/patients`)
- Use HTTP methods correctly:
  - `GET` - Retrieve resources (200 OK)
  - `POST` - Create resources (201 Created)
  - `PUT` - Replace resources (200 OK/204 No Content)
  - `PATCH` - Partially update resources (200 OK/204 No Content)
  - `DELETE` - Remove resources (204 No Content)
- Use proper HTTP status codes
- Version your API in the URL path (`/api/v1/...`)
- Use `@RestControllerAdvice` for global exception handling
- Document all endpoints with OpenAPI/Swagger

### DTOs

- Use separate DTOs for request/response
- Use `record` for immutable DTOs
- Use `@JsonView` for different representations
- Document DTOs with `@Schema` for OpenAPI
- Use `@JsonInclude(JsonInclude.Include.NON_NULL)`
- Consider using `@JsonNaming` for consistent property naming
- Use `@JsonFormat` for date/time formatting

### Pagination & Sorting

- Implement pagination for collections
- Use `Pageable` from Spring Data
- Support sorting via query parameters
- Include pagination metadata in responses

## 6. Testing

### Unit Tests

- Use JUnit 5 with `@ExtendWith(MockitoExtension.class)`
- Follow Arrange-Act-Assert pattern
- Use `@Mock` for dependencies
- Use `@InjectMocks` for class under test
- Use AssertJ for fluent assertions
- Use `@DisplayName` for descriptive test names
- Test edge cases and error conditions
- Use `@Nested` for grouping related tests
- Use parameterized tests when applicable
- Mock external dependencies

### Integration Tests

- Use `@SpringBootTest` with minimal configuration
- Use `@Testcontainers` for database tests
- Use `@Sql` for test data setup
- Use `@AutoConfigureMockMvc` for web layer tests
- Use `@TestPropertySource` for test properties
- Clean up test data after tests
- Consider using Testcontainers for integration testing
- Test security configurations

### Test Naming

- Follow `methodName_StateUnderTest_ExpectedBehavior`
- Example: `findById_WhenPatientExists_ReturnsPatient`
- Use descriptive test names that explain the test case
- Group related tests with `@Nested`

## 7. Documentation

### JavaDoc

- Document all public APIs
- Include `@param`, `@return`, and `@throws` where applicable
- Document thread safety and nullability
- Keep documentation up-to-date
- Document edge cases and constraints

### API Documentation

- Use SpringDoc OpenAPI 3.0
- Document all endpoints with `@Operation`
- Document models with `@Schema`
- Include example requests/responses
- Document error responses with `@ApiResponse`
- Include security requirements
- Document query parameters and request/response schemas
- Group related endpoints with tags

### Project Documentation

- Keep `README.md` up-to-date
- Document setup instructions
- Include environment variables
- Document API usage examples
- Include troubleshooting guide
- Document deployment procedures

## 8. Security

### Authentication & Authorization

- Use JWT for stateless authentication
- Implement proper password hashing (BCrypt)
- Use role-based access control (RBAC)
- Implement proper CORS configuration
- Use CSRF protection for state-changing operations
- Implement rate limiting
- Use HTTPS in production

### Input Validation

- Validate all user inputs
- Use Bean Validation constraints
- Sanitize inputs to prevent XSS
- Use parameterized queries to prevent SQL injection
- Implement proper error handling

### Secure Headers

- Add security headers (CSP, XSS-Protection, etc.)
- Use `Content-Security-Policy`
- Implement `X-Content-Type-Options: nosniff`
- Use `X-Frame-Options: DENY`
- Implement `X-XSS-Protection: 1; mode=block`

## 9. Performance

### Caching

- Use `@Cacheable` for expensive operations
- Configure appropriate TTL for caches
- Use cache eviction strategies
- Consider distributed caching for clustered environments
- Monitor cache hit/miss ratios

### Database Optimization

- Create appropriate indexes
- Use `@EntityGraph` or `JOIN FETCH` to avoid N+1 problems
- Use pagination for large result sets
- Consider read replicas for read-heavy workloads
- Monitor slow queries

### Asynchronous Processing

- Use `@Async` for non-blocking operations
- Use `CompletableFuture` for async operations
- Consider using Spring WebFlux for reactive programming
- Use appropriate thread pool configurations

## 10. CI/CD & Build

### Maven

- Use Maven Wrapper (`mvnw`)
- Define all dependencies with versions in `<dependencyManagement>`
- Use Maven profiles for different environments
- Configure Maven plugins for code quality checks
- Use Maven enforcer plugin for dependency convergence

### Code Quality

- Use Spotless for code formatting
- Configure Checkstyle for code style validation
- Use SpotBugs for static analysis
- Configure JaCoCo for code coverage
- Set up pre-commit hooks

### CI/CD Pipeline

- Run tests on every push
- Build and test on multiple JDK versions
- Run code quality checks
- Generate code coverage reports
- Deploy to staging on merge to main
- Use semantic versioning for releases
- Automate changelog generation

## 11. Git & Version Control

### Branching Strategy

- Use Git Flow or GitHub Flow
- Create feature branches from `main`
- Use descriptive branch names (`feature/`, `bugfix/`, `hotfix/`)
- Keep commits small and focused
- Write meaningful commit messages

### Commit Messages

Follow Conventional Commits specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

Types:

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code changes that neither fix bugs nor add features
- `perf`: Performance improvements
- `test`: Adding or modifying tests
- `chore`: Changes to build process or auxiliary tools

### Pull Requests

- Keep PRs small and focused
- Include a clear description
- Reference related issues
- Include screenshots for UI changes
- Request reviews from relevant team members
- Address all review comments
- Ensure all tests pass before merging

### Code Review

- Review for code quality and best practices
- Check for security vulnerabilities
- Ensure proper test coverage
- Verify documentation is updated
- Check for performance implications
- Ensure backward compatibility
- Verify error handling and logging
