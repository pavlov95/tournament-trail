# TournamentTrail

TournamentTrail is a Spring Boot web application for managing chess tournaments, player registrations, and shared travel groups. The project was developed as a portfolio application and as part of a Java/Spring learning path.

The application allows players to discover tournaments, register for events, track registration and payment status, and coordinate travel with other participants. Organisers can create and manage tournaments, while administrators can review organiser applications and manage platform activity.

## Main Features

### User and Security

- User registration and login
- Email verification before account activation
- Password encryption with BCrypt
- Role-based access control with Spring Security
- Roles: `PLAYER`, `ORGANISER`, `ADMIN`
- Permission-based page visibility using Spring Security and Thymeleaf

### Tournament Management

- Tournament creation by approved organisers
- Draft, published, cancelled, started, registration closed, and completed statuses
- Tournament search and filtering
- Tournament details pages
- Manual tournament publishing/cancellation by authorised users
- Automatic tournament status updates using scheduled jobs

### Tournament Registrations

- Players can register for tournaments
- Registration and payment status tracking
- Pending payment expiration through scheduled processing
- Organiser/admin registration management

### Travel Groups

- Users can create travel groups connected to tournaments
- Players can request to join travel groups
- Travel group owners can approve or reject requests
- Travel group members can coordinate travel details
- Travel cost tracking support

### Reviews and Platform Trust

- Tournament reviews
- Organiser approval workflow
- Restricted organiser permissions until admin approval

### Internationalisation

- English and Bulgarian language support
- Message bundles using Spring internationalisation
- Locale switching through request parameters

### Caching

- Spring Cache integration
- Caffeine as the cache provider
- Cached tournament reads, search results, tournament option labels, and autocomplete options
- Cache eviction on tournament changes and scheduled status updates

### Scheduling

- Scheduled tournament status updates
- Scheduled expiration of pending payment registrations

### Results Microservice

The project also contains a separate tournament results service under:

```text
tournament-results-service/
```

This service is intended to manage tournament games and results separately from the main TournamentTrail application.

Important: the results microservice should be extracted from the main project folder and started as a separate Spring Boot project/application. The main application and the results service should not be started together from the same project configuration. Each service should run separately on its own port.

The main application communicates with the results service through the configured service URL:

```properties
tournament-results.service.url=http://localhost:8081
```

## Tech Stack

### Backend

- Java 17
- Spring Boot 3.4.0
- Spring MVC
- Spring Security
- Spring Data JPA
- Spring Validation
- Spring Mail
- Spring Cache
- Spring Scheduling
- OpenFeign

### Frontend

- Thymeleaf
- HTML
- CSS
- Thymeleaf Spring Security extras

### Database

- MySQL
- Hibernate/JPA

### Caching

- Caffeine

### Build Tool

- Maven

## Project Structure

```text
TournamentTrail/
├── src/
│   ├── main/
│   │   ├── java/tournament_trail/demo/
│   │   └── resources/
│   └── test/
├── tournament-results-service/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## Running the Main Application

### 1. Requirements

Install:

- Java 17+
- Maven
- MySQL

### 2. Database Setup

The application expects a MySQL database named:

```text
tournament_trail
```

The database can be created automatically if the MySQL user has permission, because the JDBC URL uses:

```properties
createDatabaseIfNotExist=true
```

### 3. Environment Variables

Set the following environment variables before starting the app:

```text
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
MAIL_USERNAME=your_email_username
MAIL_PASSWORD=your_email_app_password
```

### 4. Example `application.properties`

```properties
spring.application.name=tournament-trail
spring.datasource.url=jdbc:mysql://localhost:3306/tournament_trail?createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

spring.mvc.hiddenmethod.filter.enabled=true

spring.messages.basename=messages
spring.messages.encoding=UTF-8

tournament-results.service.url=http://localhost:8081

spring.cache.type=caffeine
spring.cache.cache-names=upcomingTournaments,tournamentSearch,tournamentById,tournamentOptions,tournamentOptionLabel
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=10m
```

### 5. Start the Main Application

From the main project root:

```bash
mvn spring-boot:run
```

or run the main Spring Boot application class from IntelliJ IDEA.

The main application runs on:

```text
http://localhost:8080
```

## Running the Results Microservice

The results microservice should be run separately from the main application.

Recommended approach:

1. Move `tournament-results-service/` outside the main project folder.
2. Open it as a separate project in IntelliJ IDEA.
3. Run it as a separate Spring Boot application.
4. Make sure it uses port `8081`, because the main app expects:

```properties
tournament-results.service.url=http://localhost:8081
```

The main application should run on port `8080`, and the results service should run on port `8081`.

## Testing

Run tests with:

```bash
mvn test
```

The project includes tests for service logic, repository methods, and parts of the tournament results microservice.

## Current Limitations

- The frontend is currently built with Thymeleaf, not as a separate SPA.
- The results service is still located inside the main repository folder and should be separated before further development.
- Database schema management currently relies on Hibernate `ddl-auto=update`; production-style migration tooling is not yet added.
- The project is designed primarily as a learning and portfolio application, not as a production deployment.

## Future Improvements

Planned improvements include:

- Replace or extend the Thymeleaf frontend with an Angular frontend
- Split the results microservice into a fully separate repository or standalone project
- Add REST APIs for frontend and microservice communication
- Add Docker Compose for the main app, MySQL, and the results service
- Add Flyway or Liquibase database migrations
- Add Redis as an optional distributed cache provider
- Add pagination for tournaments, registrations, users, and travel groups
- Add more integration tests and controller tests
- Add CI/CD with GitHub Actions
- Add better error handling for unavailable microservices
- Add more email notifications for registration, payment, travel group, and tournament updates
- Add richer admin tools for user and content moderation
- Add Angular-based dashboard pages for players, organisers, and admins
- Deploy the application to a cloud platform

## Possible Future Architecture

A future version of the system could be split into:

```text
Angular frontend
        |
TournamentTrail REST API
        |
MySQL database
        |
Tournament Results Service
```

The results service would then run independently and communicate with the main application through REST APIs.

## GitHub Notes

Recommended files and folders to avoid committing:

```text
target/
.env
*.log
real credentials
IDE-specific sensitive configuration
```

Recommended commit message for documentation updates:

```text
docs: add project README
```
