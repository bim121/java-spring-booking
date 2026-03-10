# Java Spring Booking Service

A comprehensive booking service application built with Spring Boot for managing accommodations, bookings, users, and payments.

## Features

- **Accommodation Management**: CRUD operations for rental accommodations
- **User Management**: User registration, authentication, and profile management
- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control**: Admin, Manager, and Customer roles
- **Payment Integration**: Stripe payment processing (to be implemented)
- **Notifications**: Telegram notifications for bookings, accommodations, and payments
- **API Documentation**: Swagger/OpenAPI documentation
- **Database Migrations**: Liquibase for database version control
- **Docker Support**: Containerized deployment with docker-compose

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA
- **Security**: Spring Security with JWT
- **API Documentation**: Swagger/OpenAPI
- **Database Migrations**: Liquibase
- **Build Tool**: Maven
- **Java Version**: 17

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- PostgreSQL 15+ (or use Docker)
- Docker and Docker Compose (optional)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd java-spring-booking
```

### 2. Configure Environment Variables

Create a `.env` file in the root directory based on `.env.sample`:

```bash
cp .env.sample .env
```

Edit `.env` with your configuration:

```env
DB_URL=jdbc:postgresql://localhost:5432/booking_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your-secret-key-min-256-bits
JWT_EXPIRATION=86400000
STRIPE_API_KEY=sk_test_your_stripe_key
STRIPE_PUBLIC_KEY=pk_test_your_stripe_key
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
TELEGRAM_CHAT_ID=your_telegram_chat_id
TELEGRAM_API_URL=https://api.telegram.org/bot
SERVER_PORT=8080
```

### 3. Run with Docker Compose (Recommended)

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Spring Boot application

### 4. Run Locally

#### Start PostgreSQL

```bash
# Using Docker
docker run -d --name booking-postgres \
  -e POSTGRES_DB=booking_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine
```

#### Build and Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## API Documentation

Once the application is running, access the Swagger UI at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token

### Users
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update current user profile
- `PUT /api/users/{id}/role` - Update user role (Admin only)

### Accommodations
- `GET /api/accommodations` - List all accommodations (public)
- `GET /api/accommodations/{id}` - Get accommodation details (public)
- `POST /api/accommodations` - Create accommodation (Admin/Manager only)
- `PUT /api/accommodations/{id}` - Update accommodation (Admin/Manager only)
- `DELETE /api/accommodations/{id}` - Delete accommodation (Admin/Manager only)

### Health Check
- `GET /actuator/health` - Application health status

## Database Schema

The application uses Liquibase for database migrations. Migration files are located in:
`src/main/resources/db/changelog/`

### Tables
- `users` - User accounts and authentication
- `accommodations` - Rental accommodation inventory
- `accommodations_amenities` - Accommodation amenities (many-to-many)

## Testing

Run tests with:

```bash
mvn test
```

Check test coverage:

```bash
mvn jacoco:report
```

Coverage reports are generated in `target/site/jacoco/index.html`

## Code Quality

Check code style:

```bash
mvn checkstyle:check
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── org/example/
│   │       ├── BookingApplication.java
│   │       ├── config/          # Configuration classes
│   │       ├── controller/      # REST controllers
│   │       ├── dto/             # Data Transfer Objects
│   │       ├── entity/          # JPA entities
│   │       ├── exception/       # Exception handlers
│   │       ├── model/           # Enums and models
│   │       ├── repository/      # Data repositories
│   │       ├── security/        # Security configuration
│   │       └── service/         # Business logic
│   └── resources/
│       ├── application.yml      # Application configuration
│       └── db/
│           └── changelog/       # Liquibase migrations
└── test/                        # Test files
```

## Development

### Adding New Features

1. Create database migration in `src/main/resources/db/changelog/changes/`
2. Create entity classes in `entity/` package
3. Create repository interfaces in `repository/` package
4. Create service classes in `service/` package
5. Create DTOs in `dto/` package
6. Create controllers in `controller/` package
7. Add tests in `test/` package

## License

This project is licensed under the Apache License 2.0.

## Demo backend app
https://www.loom.com/share/c3e1bcd3bf0e4fffaa5ece05db57d2f5