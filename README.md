# Patient Registration

## Description

A simple Patient Registration system built with Spring Boot. It allows for the registration and management of patient records.

## Features

- Register new patients
- View patient details
- Search for patients
- Update patient information
- Delete patient records
- JWT-based authentication and authorization
- Redis integration for caching
- MySQL database support
- OpenAPI (Swagger) documentation

## Technologies Used

- Java 21+
- Spring Boot 3.5+
- Maven
- MySQL Database
- Redis
- JWT (JSON Web Token)
- Swagger (OpenAPI) for API documentation

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

### Database & Redis Configuration

Update the `src/main/resources/application.properties` file with your MySQL and Redis credentials and database details as needed.

- MySQL: Set `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password`.
- Redis: Set `spring.data.redis.host`, `spring.data.redis.port`, and `spring.data.redis.password`.
- JWT: Set a strong `app.jwt.secret` (256 characters recommended).

### Running Tests

To run all tests:

```bash
mvn test
```

## Swagger API Documentation

Once the application is running, you can access the Swagger UI for API documentation and testing at:

- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- or [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)

## Usage

Use the Swagger UI to explore and test the available API endpoints for patient registration and management.
