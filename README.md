# Camunda 8 - Getting Started Example

This project is a multi-module Spring Boot application that demonstrates how to build and deploy a process application using **Camunda 8**. It was created by following the [Camunda Getting Started Guide](https://docs.camunda.io/docs/next/guides/getting-started-example).

## Overview

This is a multi-module Maven project with the following modules:

### Order Module
The main application module implementing a simple **Process Order** workflow with the following service tasks:

- **Check Inventory** - Verifies item availability
- **Charge Payment** - Processes payment for the order
- **Ship Items** - Handles shipping of ordered items
- **Send Email** - Sends email notification to users (handled by Util module)

### Util Module
A utility module containing additional job workers:

- **Send Email** - Sends email notifications to users

## Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **Camunda 8 Run** (for local development)
  - Download from [Camunda 8 Run](https://github.com/camunda/camunda/releases) or install via [Camunda Desktop Modeler](https://camunda.com/download/modeler/) starter package

## Project Structure

```
camunda8-101/
├── pom.xml                                    # Parent Maven configuration
├── order/                                     # Order module
│   ├── pom.xml                               # Order module Maven configuration
│   └── src/
│       ├── main/
│       │   ├── java/com/camunda/academy/
│       │   │   ├── ProcessOrderApplication.java   # Spring Boot main class with @Deployment
│       │   │   ├── ProcessController.java         # REST API controller
│       │   │   ├── CheckInventoryWorker.java      # Worker for check-inventory task
│       │   │   ├── ChargePaymentWorker.java       # Worker for charge-payment task
│       │   │   └── ShipItemsWorker.java           # Worker for ship-items task
│       │   └── resources/
│       │       ├── application.yml                # Application configuration
│       │       └── diagram_1.bpmn                 # BPMN process definition
│       └── test/
│           └── java/                              # Test classes
└── util/                                      # Util module
    ├── pom.xml                               # Util module Maven configuration
    └── src/
        ├── main/
        │   ├── java/com/camunda/academy/
        │   │   ├── UtilApplication.java           # Spring Boot main class
        │   │   └── SendEmailWorker.java           # Worker for send-email task
        │   └── resources/
        │       └── application.yml                # Application configuration
        └── test/
            └── resources/                         # Test resources
```

## Configuration

The applications are configured to connect to a local Camunda 8 Run instance. Update the respective `application.yml` files to match your environment:

### Order Module (`order/src/main/resources/application.yml`)
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

### Util Module (`util/src/main/resources/application.yml`)
```yaml
spring:
  application:
    name: Util Service

camunda:
  client:
    mode: self-managed
    grpc-address: http://127.0.0.1:26500
    rest-address: http://127.0.0.1:8080
    execution-threads: 4
```

> **Note**: The Util module doesn't expose any REST endpoints, so no `server.port` is configured.

### Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | HTTP server port (Order module only) | `8090` |
| `camunda.client.mode` | Connection mode (`self-managed` or `saas`) | `self-managed` |
| `camunda.client.grpc-address` | Zeebe gRPC gateway address | `http://127.0.0.1:26500` |
| `camunda.client.rest-address` | Camunda REST API address | `http://127.0.0.1:8080` |
| `camunda.client.execution-threads` | Number of threads for job worker execution | `4` |

### Job Worker Thread Pool

All job workers in each module share a single thread pool. By default, the pool size is 4. You can adjust this using the `camunda.client.execution-threads` property for better throughput when:

- Running multiple process instances concurrently
- Using parallel gateways in BPMN that create concurrent jobs
- Handling high job throughput with many queued jobs

> **Note**: Jobs from a single sequential process instance will still execute one at a time, even with multiple threads configured. The thread pool helps when there are multiple concurrent jobs available.

## Building the Application

Build all modules:
```bash
mvn clean install
```

Build a specific module:
```bash
mvn clean install -pl order
mvn clean install -pl util
```

## Running the Application

### Step 1: Start Camunda 8 Run

Before running the application, you need to start Camunda 8 Run:

1. **Download and Install**: If you haven't already, download [Camunda 8 Run](https://github.com/camunda/camunda/releases) or install via the Camunda Desktop Modeler starter package for Windows.

2. **Start Camunda 8 Run**:
   - **Windows**: Navigate to the Camunda 8 Run installation directory and run:
     ```cmd
     c8run.exe start
     ```
   - Or double-click `c8run.exe` and select "Start"

3. **Wait for initialization**: Camunda 8 Run may take 30-60 seconds to fully initialize all components.

4. **Verify it's running**: Open [http://localhost:8080](http://localhost:8080) in your browser. You should see the Camunda dashboard.

> **Important**: Ensure Camunda 8 Run is fully initialized before starting your Spring Boot application. If you see connection errors, restart Camunda 8 Run and wait for it to be ready.

### Step 2: Run the Order Module

The Order module will automatically deploy the BPMN process (`diagram_1.bpmn`) on startup:

```bash
mvn spring-boot:run -pl order
```

You should see logs indicating:
- The BPMN deployment: `Configuring deployments: [DeploymentValue{resources=[classpath:diagram_1.bpmn], tenantId='null'}]`
- Job workers registered for `check-inventory`, `charge-payment`, and `ship-items`

### Step 3: Run the Util Module (Optional)

If your BPMN process includes the `send-email` service task, run the Util module:

```bash
mvn spring-boot:run -pl util
```

This registers the `SendEmailWorker` to handle `send-email` tasks.

### Step 4: Start a Process Instance

Use the REST API to start a process instance:

```bash
curl -X POST http://localhost:8090/api/process/start \
  -H "Content-Type: application/json" \
  -d '{"orderId": "12345", "item": "Widget", "customerName": "John Doe"}'
```

## Job Workers

The application includes job workers that handle service tasks in BPMN processes:

### Order Module Workers

#### CheckInventoryWorker
- **Task Type**: `check-inventory`
- **Description**: Checks inventory for the ordered item and allocates it
- **Input Variables**: `item` (optional)
- **Output Variables**: `item` (with " allocated" suffix)

#### ChargePaymentWorker
- **Task Type**: `charge-payment`
- **Description**: Processes payment for the order

#### ShipItemsWorker
- **Task Type**: `ship-items`
- **Description**: Ships the ordered items

### Util Module Workers

#### SendEmailWorker
- **Task Type**: `send-email`
- **Description**: Sends email notification to users
- **Input Variables**: `name` (optional, defaults to "User")
- **Output**: Logs a message indicating email was sent to the user

## REST API

The order module exposes a REST API to interact with the process engine.


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

4. **Access all variables at once** - Use `getVariablesAsMap()` on the `ActivatedJob` object:
   ```java
   @JobWorker(type = "ship-items")
   public void shipItems(final ActivatedJob job) {
       // Get all process variables as a Map
       Map<String, Object> allVariables = job.getVariablesAsMap();
       LOG.info("All process variables: {}", allVariables);
       
       // Other options:
       // String json = job.getVariables();                    // As JSON string
       // MyClass vars = job.getVariablesAsType(MyClass.class); // As custom object
   }
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
| `customerName` | String | Customer name for notifications | Process start |
| `name` | String | User name for email notifications (optional, defaults to "User") | Process start |
| `item-allocation` | String | Allocation status of the item | CheckInventoryWorker |

> **Note**: Variables are scoped to the process instance and persist throughout its lifecycle. Any task can read variables set by previous tasks or at process start.

## Testing

Run all tests:

```bash
mvn test
```

Run tests for a specific module:

```bash
mvn test -pl order
mvn test -pl util
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
