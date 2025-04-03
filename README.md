# Audit Framework

This project is an audit logging framework built with Spring Boot and Maven. It provides various appenders for different logging mechanisms, including database and Kafka.

## Project Structure

The project follows a standard Maven structure and includes the following key components:

- **src/main/java/com/example/audit/**: Contains the main Java code for the audit framework.
    - **config/**: Configuration classes for setting up the audit framework.
    - **annotation/**: Custom annotations for marking auditable methods.
    - **service/**: Service classes for handling business logic related to audit logs.
    - **controller/**: REST controllers for exposing audit log endpoints.
    - **repository/**: Repository interfaces for CRUD operations on audit logs.
    - **entity/**: Entity classes representing the audit log structure.
    - **interceptor/**: Hibernate interceptor for tracking entity changes.
    - **producer/**: Kafka producer for sending audit logs.
    - **consumer/**: Kafka consumer for processing audit logs.

- **src/main/resources/**: Contains configuration files, including `application.properties` for environment-based settings.
- **pom.xml**: Maven configuration file that includes all necessary dependencies.

## Setup Instructions

### Clone the Repository
```sh
git clone <repository-url>
cd audit-framework
```

### Build the Project
Use Maven to build the project:
```sh
mvn clean install
```

### Run the Application
You can run the Spring Boot application using:
```sh
mvn spring-boot:run
```

### Configuration
The framework reads its configuration from environment variables, making it easily configurable for different environments. Ensure that the following environment variables are set:

#### **Database Configuration**
```sh
export AUDIT_DB_NAME=audit_logs
export AUDIT_DB_USER=audit_user
export AUDIT_DB_PASSWORD=audit_password
```

#### **Kafka Configuration**
```sh
export KAFKA_BOOTSTRAP_SERVERS=kafka1:9092
export KAFKA_AUDIT_TOPIC=audit_topic_test
export KAFKA_CONSUMER_GROUP=audit_log_group
```

#### **Kafka SSL Configuration (If Using SSL)**
```sh
export TRUSTSTORE_LOCATION=/path/to/truststore.p12
export KEYSTORE_LOCATION=/path/to/keystore.p12
export TRUSTSTORE_PASSWORD=your-truststore-password
export KEYSTORE_PASSWORD=your-keystore-password
export KEY_PASSWORD=your-key-password
```

## Usage

- The framework automatically captures entity changes and logs them according to the configured appender (database or Kafka).
- REST endpoints are available via the `AuditController` to fetch audit logs.

### Fetch Audit Logs (Paginated)
```http
GET /api/audit-logs?page=0&size=10&sort=timestamp,desc
```

## Dependencies
This project includes the following key dependencies:

- **Spring Boot**
- **Spring Data JPA** (for database interactions)
- **Hibernate Interceptor** (for tracking entity changes)
- **Kafka** (for event-driven auditing)
- **Resilience4j** (for circuit breaker & retry mechanisms)

## Customization
- The `AuditLog` entity structure can be modified based on requirements.
- Additional appenders or configurations can be introduced via environment variables.

## License
This project is licensed under the MIT License. See the `LICENSE` file for more details.

