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

## Database Schema

The primary entity in this application is `Patient`. The `patients` table stores patient registration information. Key fields include:

- `id` (BIGINT, Primary Key, Auto-incremented): Unique identifier for the patient.
- `phone_number` (VARCHAR, Unique): Patient's 10-digit phone number, also used for login.
- `password_hash` (VARCHAR): Hashed password for the patient.
- `using_default_password` (BOOLEAN): Flag indicating if the patient is currently using the default password (which is their phone number).
- `personal_details` (JSON): Stores personal information like name, date of birth, gender, address.
- `medical_info` (JSON): Stores medical history, allergies, current medications.
- `emergency_contact` (JSON): Stores emergency contact details.
- `insurance_details` (JSON): Stores insurance provider information.
- `clinic_preferences` (JSON): Stores communication preferences and preferred clinic location.
- `updated_at` (TIMESTAMP): Timestamp of the last update to the patient record.

The JSON fields are mapped to corresponding Java classes (`PersonalDetails`, `MedicalInfo`, etc.) within the application using JPA converters.

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

## Deployment

To deploy the application:

1.  Build the application JAR:
    ```bash
    mvn clean package
    ```
2.  Run the JAR file:
    `bash
java -jar target/registration-0.0.1-SNAPSHOT.jar
`
    Ensure that your MySQL and Redis instances are running and accessible by the application. You might need to configure environment variables for database credentials, Redis connection, and JWT secret for a production environment instead of using the `application.properties` file directly.

### Building with Docker

To build the application using Docker, run the following command from the project root:

```bash
docker build -f Dockerfile -t registration:1.0.0 .
```

This command uses the multi-stage Dockerfile to first build the application using Maven and then creates a lightweight final image with only the necessary runtime dependencies.

### Database, Redis & JWT Configuration

Update `src/main/resources/application.properties` with your credentials:

- **MySQL:**
  - `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`
- **Redis:**
  - `spring.data.redis.host`, `spring.data.redis.port`, `spring.data.redis.password`
- **JWT:**
  - `app.jwt.secret` (256+ chars recommended)

## Running Tests

To run all tests:

```bash
mvn test
```

## API Documentation

Once running, access Swagger UI at:

- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- or [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)

You can find the OpenAPI specification in the [`swagger.yaml`](swagger.yaml) file at the project root.

## Usage

Use Swagger UI to explore and test all API endpoints for patient registration and management.

## Security Considerations

This application incorporates several security best practices:

- **JWT-Based Authentication:** Secure stateless authentication using JSON Web Tokens. Ensure your `app.jwt.secret` is strong and kept confidential.
- **HTTPS:** While not enforced by the application itself, it is strongly recommended to run the application behind a reverse proxy (e.g., Nginx, Apache) configured with SSL/TLS to ensure all communication is encrypted.
- **Password Hashing:** Passwords are securely hashed using BCrypt before being stored in the database.
- **Input Validation:** Spring Validation annotations (`@Valid`, `@NotBlank`, etc.) are used to validate incoming request data, preventing common injection flaws at the model level.
- **Global Exception Handling:** Centralized exception handling helps prevent exposing sensitive stack traces to the client.
- **CSRF Protection:** For web applications that use sessions and cookies for authentication, Spring Security provides CSRF protection by default. Since this application primarily uses JWT for API authentication (often via Authorization header), traditional CSRF might be less of a concern for API endpoints if cookies are not the primary method for session tracking. However, if cookies are used for authentication (e.g. refresh tokens), ensure SameSite cookie attributes are appropriately set (as done for refresh and access tokens in this application - `SameSite=Lax`).
- **ORM and Parameterized Queries:** Spring Data JPA (Hibernate) uses parameterized queries by default, which helps protect against SQL injection vulnerabilities.
- **Regular Dependency Updates:** Keep dependencies (Spring Boot, Java, Maven plugins, etc.) updated to patch known vulnerabilities.

---

## Project Structure & Best Practices

- **Layered architecture:** Controller → Service → Repository
- **Validation:** Use `@Valid`, `@NotBlank`, `@Size`, etc. for input validation
- **Exception Handling:** Centralized with `@ControllerAdvice`
- **Caching:** Use `@Cacheable` for frequently accessed reads (Redis)
- **Async:** Use `@Async` for long-running tasks
- **Testing:** High coverage with unit/integration tests
- **Code Quality:** Follows Google Java Style, uses Spotless/Checkstyle
- **Documentation:** JavaDoc for public classes/methods, Swagger annotations for APIs

---

## Contributing

Contributions are welcome! If you'd like to contribute to this project, please follow these general guidelines:

1.  **Fork the repository.**
2.  **Create a new branch** for your feature or bug fix: `git checkout -b feature-name` or `git checkout -b bugfix-name`.
3.  **Make your changes** and ensure they follow the existing code style.
4.  **Add tests** for any new functionality or bug fixes.
5.  **Ensure all tests pass:** `mvn test`.
6.  **Commit your changes** with a clear and descriptive commit message.
7.  **Push your changes** to your forked repository.
8.  **Create a Pull Request** to the main repository's `main` or `develop` branch.

Please provide a clear description of the changes in your pull request.

## License

This project is licensed under the terms of the LICENSE file.
