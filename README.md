# WebBills

WebBills is a web application for managing bills, payments, entries, and users. It is built with Java (Spring Boot), uses Maven for build management, and supports containerization with Docker.

## Features

- Bill management (CRUD)
- Payment tracking and management
- Entry management with filtering, sorting, and statistics
- User authentication, registration, and profile management
- Recycle bin for soft-deleted bills, entries, and payments
- RESTful API endpoints secured with JWT authentication
- Dockerized deployment for easy setup

## Prerequisites

- Java 17+ (or your project’s required version)
- Maven 3.6+
- Docker (optional, for containerized deployment)
- Python 3 (for utility scripts, if needed)

## Getting Started

### Clone the repository

```sh
git clone https://github.com/yourusername/WebBills.git
cd WebBills
```

### Build the project

```sh
./mvnw clean install
```

### Run locally

```sh
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.

### Run with Docker

```sh
docker-compose up --build
```

### Configuration

Edit `src/main/resources/application.properties` or `application-dev.properties` for environment-specific settings, such as database connection and JWT secrets.

### API Documentation

API endpoints are organized under `/api/v1/`:
- `/auth` – User registration, login, and token refresh
- `/bills` – Bill CRUD operations
- `/entries` – Entry CRUD, filtering, and statistics
- `/payments` – Payment CRUD operations
- `/recycle` – View recycle bin contents
- `/user` – User profile and update

See the `bruno/web-bills/` folder for example requests and environments.

## Testing

Run all tests with:

```sh
./mvnw test
```

Test reports are available in the `target/surefire-reports/` directory.

## Deployment

See `deploy-bills-app.sh` and `task-def-template.json` for deployment scripts and templates.

## License

[MIT](LICENSE)

