# GitHub Copilot Instructions for Java Spring Boot + MySQL Project

## 🧠 Developer Context

You are working on a **Java 21+** Spring Boot application with:

- Spring Boot 3.5+
- MySQL (via Spring Data JPA/Hibernate)
- Maven for build/dependency management
- Layered architecture: Controller → Service → Repository
- RESTful API design with Models and validation
- Performance optimizations: caching (Redis), async processing
- Clean, maintainable, and testable code using modern Java features

---

## ✅ Coding Standards & Expectations

### Java & Spring Boot

- Use **Java 21 features** (records, pattern matching, etc.)
- Use **Spring Boot annotations**: `@RestController`, `@Service`, `@Repository`, `@Entity`
- Prefer **constructor injection** with `@RequiredArgsConstructor` (Lombok)
- Use **JPA annotations** for entity mapping
- Use **DTOs/Models** for persistence and API layers
- Implement **global exception handling** with `@ControllerAdvice`
- Add **input validation**: `@Valid`, `@NotBlank`, `@Size`, etc.

---

### Database (MySQL)

- Use Spring Data JPA repositories
- Use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-increment primary keys
- Apply indexing/constraints via JPA annotations
- Support schema evolution via **Flyway**

---

### REST API Best Practices

- Structure endpoints as `/api/v1/patients`, etc.
- Use `ResponseEntity<T>` for responses
- Return proper HTTP status codes: `200 OK`, `201 Created`, `404 Not Found`, etc.
- Use `@Valid` in request bodies for validation
- Document endpoints with Swagger/OpenAPI annotations

---

### Performance & Optimization

- Use `@Cacheable` for frequently accessed reads (Redis)
- Use `@Async` for long-running tasks
- Index important fields in the database

---

### Testing

- Write unit/integration tests for all business logic and API endpoints
- Use **JUnit 5** and **Mockito**
- Use **@SpringBootTest** for integration tests
- Ensure high code coverage and meaningful assertions
- Mock external dependencies in unit tests

---

### Code Formatting & Quality

- Follow Google Java Style (or similar)
- Use **Checkstyle**, **Spotless**, or **EditorConfig**
- Use meaningful names
- Keep methods short and focused
- Avoid code duplication (DRY)

---

### Documentation

- Use **JavaDoc** for public classes/methods
- Document API endpoints with **Swagger/OpenAPI**
- Maintain a **README.md** with setup, build, and run instructions
- Use **Markdown** for documentation

---

## Additional Notes

- Use Lombok to reduce boilerplate
- Use Flyway for DB migrations
- Ensure security best practices for JWT and sensitive data
- Keep dependencies up to date
