# WebBills

WebBills is a secure, user-centric web application for managing bills, payments, financial entries, and user accounts. Built with Java (Spring Boot), it features robust authentication, fine-grained access control, and a modern RESTful API. The application is designed for easy deployment and scalability, supporting Docker and cloud-native workflows.

## Key Features

- **User Authentication & Security**: JWT-based authentication, secure password hashing, and refresh token support.
- **Bill Management**: Create, update, and view bills. Bills can be archived (recycled) and restored.
- **Entry Management**: Track financial entries with advanced filtering, sorting, and statistics (including top payees/payers, overpaid entries, and more).
- **Payment Tracking**: Record, update, and view payments linked to entries. Automatic calculation of paid/overpaid status.
- **Recycle Bin**: Soft-delete (recycle) bills, entries, and payments, with endpoints to view and restore recycled items.
- **User Profile**: View and update user details, change email/password, and soft-delete (recycle) user accounts.
- **Comprehensive API**: All features are accessible via RESTful endpoints under `/api/v1/`.
- **Dockerized Deployment**: Easily build and run the app in a containerized environment. Includes a deployment script for streamlined setup.

## Prerequisites

- Java 17+
- Maven 3.6+
- Docker (for containerized deployment)
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

Or use the deployment script for only the spring app:

```sh
./deploy-bills-app.sh
```

This script will:
- Build the Spring Boot application using Maven
- Build a Docker image for the app
- Run the container locally

You can customize the script as needed for your environment or CI/CD pipeline.

### Configuration

Edit `src/main/resources/application.properties` or `application-dev.properties` for environment-specific settings, such as database connection and JWT secrets.

See the [`bruno/web-bills/`](bruno/web-bills/) folder for example requests and environments.

## Repository

Find the latest source code and updates at: [github.com/mhackett909/WebBills](https://github.com/mhackett909/WebBills/)

## Testing

Run all tests with:

```sh
./mvnw test
```

Test reports are available in the `target/surefire-reports/` directory.

## Deployment

- Use `deploy-bills-app.sh` for automated Maven build, Docker image creation, and container startup.
- See `task-def-template.json` for cloud/container orchestration templates.

## License

[MIT](LICENSE)

