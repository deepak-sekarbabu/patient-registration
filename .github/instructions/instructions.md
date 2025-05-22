# GitHub Copilot Instructions for Java Spring Boot + MySQL Project

## 🧠 Developer Context

You are working on a **Java 21+** Spring Boot application that uses:

- Spring Boot 3.4 +
- MySQL as the relational database
- Spring Data JPA with Hibernate
- Maven for build and dependency management
- Layered architecture: Controller → Service → Repository
- RESTful API design with Models and validation
- Performance optimizations like caching and async processing
- Clean, maintainable, and testable code using modern Java features

---

## ✅ Coding Standards & Expectations

### Java & Spring Boot

- Use **Java 21 features** such as records and pattern matching where appropriate
- Use **Spring Boot annotations** like `@RestController`, `@Service`, `@Repository`, `@Entity`
- Prefer **constructor injection** with `@RequiredArgsConstructor` (Lombok)
- Use **JPA annotations** for entity mapping
- Use **Models** for persistence and API layers
- Implement **global exception handling** using `@ControllerAdvice`
- Add **input validation** using `@Valid`, `@NotBlank`, `@Size`, etc.

---

### Database (MySQL)

- Use Spring Data JPA repositories
- Use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-increment primary keys
- Apply indexing and constraints via JPA annotations
- Support schema evolution via **Flyway** (or Liquibase)

---

### REST API Best Practices

- Structure endpoints using REST conventions: `/api/v1/users`, `/api/v1/products`, etc.
- Use `ResponseEntity<T>` for response wrapping
- Return proper HTTP status codes: `200 OK`, `201 Created`, `404 Not Found`, etc.
- Use `@Valid` in request bodies for input validation

---

### Performance & Optimization

- Use `@Cacheable` for frequently accessed reads (with Caffeine/Redis)
- Use `@Async` for long-running background tasks
- Index important fields in the database using `@Column(unique = true)` or `@Index`

---

### Testing

- Write unit and integration tests for all business logic and API endpoints
- Use **JUnit 5** and **Mockito** for testing
- Use **@SpringBootTest** for integration tests
- Ensure high code coverage and meaningful assertions
- Mock external dependencies in unit tests

---

### Code Formatting & Quality

- Follow standard Java formatting conventions (Google Java Style or similar)
- Use tools like **Checkstyle**, **Spotless**, or **EditorConfig** for consistent formatting
- Use meaningful variable, method, and class names
- Keep methods short and focused
- Avoid code duplication and follow DRY principles

---

### Documentation

- Use **JavaDoc** for public classes and methods
- Document API endpoints using **Swagger/OpenAPI** annotations
- Maintain a **README.md** file with project setup, build, and run instructions
- Use **Markdown** for documentation
