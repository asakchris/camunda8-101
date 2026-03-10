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
│   │   │   ├── ProcessController.java         # REST API controller
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
server:
  port: 8090

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
| `server.port` | HTTP server port | `8090` |
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

## REST API

The application exposes a REST API to interact with the process engine.

### Start a Process Instance

**Endpoint:** `POST http://localhost:8090/api/process/start`

Starts a new instance of the `process1` BPMN process.

#### Request

- **Content-Type**: `application/json`
- **Body** (optional): JSON object with process variables

#### Example: Start with variables

```bash
curl -X POST http://localhost:8090/api/process/start \
  -H "Content-Type: application/json" \
  -d '{"orderId": "12345", "item": "Widget", "customerName": "John Doe"}'
```

#### Example: Start without variables

```bash
curl -X POST http://localhost:8090/api/process/start
```

#### Response

```json
{
  "processInstanceKey": 2251799813685250,
  "bpmnProcessId": "process1",
  "version": 1,
  "processDefinitionKey": 2251799813685249
}
```

| Field | Description |
|-------|-------------|
| `processInstanceKey` | Unique identifier for this process instance |
| `bpmnProcessId` | The BPMN process ID (`process1`) |
| `version` | Version of the deployed process definition |
| `processDefinitionKey` | Unique key for the process definition |

## Process Variables

Process variables are dynamic data that flow through the BPMN process. They do not need to be pre-defined in the BPMN model.

### How Variables Work

1. **Pass variables when starting a process** - Include them in the JSON body of the REST API request:
   ```bash
   curl -X POST http://localhost:8090/api/process/start \
     -H "Content-Type: application/json" \
     -d '{"item": "Laptop", "orderId": "ORD-123", "quantity": 5}'
   ```

2. **Access variables in job workers** - Use the `@Variable` annotation to inject variables:
   ```java
   @JobWorker(type = "check-inventory")
   public Map<String, String> checkInventory(
       final ActivatedJob job, 
       @Variable(name = "item") @Nullable String itemOrdered) {
       // itemOrdered will contain "Laptop" from the process start
   }
   ```

3. **Add or update variables** - Workers can return a `Map` to add/update variables for downstream tasks:
   ```java
   return Map.of("item-allocation", item + " allocated");
   ```

### Variable Flow Example

```
Start Process                    CheckInventoryWorker              ChargePaymentWorker
     │                                  │                                 │
     │  {"item": "Laptop",              │                                 │
     │   "orderId": "ORD-123"}          │                                 │
     │ ─────────────────────────────────>                                 │
     │                                  │                                 │
     │                      Receives: item = "Laptop"                     │
     │                      Returns: item-allocation = "Laptop allocated" │
     │                                  │                                 │
     │                                  │ ───────────────────────────────>│
     │                                  │                                 │
     │                                  │     Receives: item = "Laptop"   │
     │                                  │     item-allocation = "..."     │
     │                                  │     orderId = "ORD-123"         │
```

### Available Variables for This Process

| Variable | Type | Description | Set By |
|----------|------|-------------|--------|
| `item` | String | The item being ordered (optional, defaults to "default-item") | Process start |
| `orderId` | String | Order identifier | Process start |
| `item-allocation` | String | Allocation status of the item | CheckInventoryWorker |

> **Note**: Variables are scoped to the process instance and persist throughout its lifecycle. Any task can read variables set by previous tasks or at process start.

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
