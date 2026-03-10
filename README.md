# Camunda 8 - Getting Started Example

This project is a Spring Boot application that demonstrates how to build and deploy a process application using **Camunda 8**. It was created by following the [Camunda Getting Started Guide](https://docs.camunda.io/docs/next/guides/getting-started-example).

## Overview

The application implements a simple **Process Order** workflow with the following service tasks:

- **Check Inventory** - Verifies item availability
- **Charge Payment** - Processes payment for the order
- **Ship Items** - Handles shipping of ordered items

## Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- **Camunda 8** (Self-Managed or SaaS)
  - For local development, you can use [Camunda Desktop Modeler](https://camunda.com/download/modeler/) and a local Camunda 8 instance

## Project Structure

```
camunda8-101/
├── pom.xml                                    # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/com/camunda/academy/
│   │   │   ├── ProcessOrderApplication.java   # Spring Boot main class
│   │   │   ├── CheckInventoryWorker.java      # Worker for check-inventory task
│   │   │   ├── ChargePaymentWorker.java       # Worker for charge-payment task
│   │   │   └── ShipItemsWorker.java           # Worker for ship-items task
│   │   └── resources/
│   │       ├── application.yml                # Application configuration
│   │       └── diagram_1.bpmn                 # BPMN process definition
│   └── test/
│       └── java/                              # Test classes
└── target/                                    # Build output
```

## Configuration

The application is configured to connect to a self-managed Camunda 8 instance. Update `src/main/resources/application.yml` to match your environment:

```yaml
spring:
  application:
    name: Process Order

camunda:
  client:
    mode: self-managed
    grpc-address: http://127.0.0.1:26500
    rest-address: http://127.0.0.1:8080
    execution-threads: 4

logging:
  level:
    io.camunda.client: DEBUG
    io.camunda.zeebe.spring.client: DEBUG
    io.camunda.client.impl.CamundaCallCredentials: ERROR
```

### Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `camunda.client.mode` | Connection mode (`self-managed` or `saas`) | `self-managed` |
| `camunda.client.grpc-address` | Zeebe gRPC gateway address | `http://127.0.0.1:26500` |
| `camunda.client.rest-address` | Camunda REST API address | `http://127.0.0.1:8080` |
| `camunda.client.execution-threads` | Number of threads for job worker execution | `1` |

### Job Worker Thread Pool

All job workers in this application share a single thread pool. By default, the pool size is 1 (single-threaded). You can increase this using the `camunda.client.execution-threads` property for better throughput when:

- Running multiple process instances concurrently
- Using parallel gateways in BPMN that create concurrent jobs
- Handling high job throughput with many queued jobs

> **Note**: Jobs from a single sequential process instance will still execute one at a time, even with multiple threads configured. The thread pool helps when there are multiple concurrent jobs available.

## Building the Application

```bash
mvn clean install
```

## Running the Application

1. **Start Camunda 8** - Ensure your Camunda 8 instance is running (either locally or in the cloud)

2. **Deploy the BPMN process** - The process definition (`diagram_1.bpmn`) will be automatically deployed when the application starts

3. **Run the Spring Boot application**:
   ```bash
   mvn spring-boot:run
   ```

## Job Workers

The application includes three job workers that handle service tasks in the BPMN process:

### CheckInventoryWorker
- **Task Type**: `check-inventory`
- **Description**: Checks inventory for the ordered item and allocates it
- **Input Variables**: `item` (optional)
- **Output Variables**: `item` (with " allocated" suffix)

### ChargePaymentWorker
- **Task Type**: `charge-payment`
- **Description**: Processes payment for the order

### ShipItemsWorker
- **Task Type**: `ship-items`
- **Description**: Ships the ordered items

## Testing

Run the tests with:

```bash
mvn test
```

The project includes `camunda-process-test-spring` for process testing with JUnit 5.

## Dependencies

| Dependency | Version | Description |
|------------|---------|-------------|
| Spring Boot | 3.5.9 | Application framework |
| Camunda Spring Boot Starter | 8.8.14 | Camunda 8 integration |
| Camunda Process Test Spring | 8.8.14 | Testing support |
| JUnit Jupiter | 5.14.1 | Testing framework |

## Resources

- [Camunda 8 Documentation](https://docs.camunda.io/)
- [Getting Started Guide](https://docs.camunda.io/docs/next/guides/getting-started-example)
- [Camunda Spring Boot Starter](https://docs.camunda.io/docs/apis-tools/spring-boot-starter/)
- [Job Workers Documentation](https://docs.camunda.io/docs/components/concepts/job-workers/)
