# Audit Framework

This project is an audit logging framework built with Spring Boot and Maven. It utilizes the `audit4j-core` library for handling audit logs and provides various appenders for different logging mechanisms, including database, file, and Kafka.

## Project Structure

The project follows a standard Maven structure and includes the following key components:

- **src/main/java/com/example/audit/**: Contains the main Java code for the audit framework.
   - **config/**: Configuration classes for setting up the audit framework.
   - **annotation/**: Custom annotations for marking auditable methods.
   - **aspect/**: Aspect-oriented programming classes for intercepting method calls.
   - **service/**: Service classes for handling business logic related to audit logs.
   - **controller/**: REST controllers for exposing audit log endpoints.
   - **repository/**: Repository interfaces for CRUD operations on audit logs.
   - **entity/**: Entity classes representing the audit log structure.

- **src/main/resources/**: Contains configuration files, including `application.yml` for application settings.

- **pom.xml**: Maven configuration file that includes all necessary dependencies.

## Setup Instructions

1. **Clone the Repository**
    ```bash
    git clone <repository-url>
    cd audit-framework
    ```

2. **Build the Project**
    Use Maven to build the project:
    ```bash
    mvn clean install
    ```

3. **Run the Application**
    You can run the Spring Boot application using:
    ```bash
    mvn spring-boot:run
    ```

4. **Configuration**
    Update the `src/main/resources/application.properties` file with your database connection details and any other necessary configurations for the audit logging.

## Usage

- Annotate methods or classes with `@Auditable` to enable auditing.
- Use the `AuditController` to interact with the audit logs via REST endpoints.
- The audit logs will be stored according to the configured appenders (database, file, or Kafka).

## Dependencies

This project includes the following key dependencies:
- Spring Boot
- Spring Data JPA (for database interactions)

## License

This project is licensed under the MIT License. See the LICENSE file for more details.