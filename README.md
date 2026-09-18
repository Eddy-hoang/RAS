# Secure Remote Administration Platform (SRAP)

A secure, distributed, enterprise-oriented remote administration system built in Java 24, demonstrating low-level socket programming, binary protocol framing, concurrency models using Virtual Threads, mTLS transport security, defense-in-depth authorization, tamper-evident audit logging, and bandwidth-optimized delta screen streaming.

---

## 1. Project Overview

Secure Remote Administration Platform (SRAP) is an authorized remote administration system designed for IT administrators to manage multiple workstation endpoints within an enterprise network. 

Key Architectural Distinctions:
- Protocol-First Design: Transport is strictly governed by a custom 12-byte fixed binary header protocol.
- Outbound Connection Model: Client Agents initiate outbound connections to the central Server, ensuring full compatibility with NAT and firewall environments.
- Separation of Control and Data Channels: Asynchronous RPC commands (JSON) and heavy binary transfers (file chunks, screen stream tiles) run on isolated TCP channels bound by a Session Token.
- Scalable Concurrency: Built on Java 24 Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`), handling high-density client connections with minimal OS thread overhead.

---

## 2. Technical Specifications

### Protocol Framing (12-Byte Fixed Header)
```text
+----------------+----------------+----------------+------------------+
|   MAGIC / VER  |  PAYLOAD SIZE  |  TYPE / CHAN   |     PAYLOAD      |
|    4 bytes     |    4 bytes     |    4 bytes     | N bytes (<=16MB) |
+----------------+----------------+----------------+------------------+
```
- MAGIC (2 bytes): `0x5352` ("SR")
- VERSION (1 byte): `0x01`
- RESERVED (1 byte): `0x00`
- PAYLOAD SIZE (4 bytes): Big-endian 32-bit integer (Max limit: 16 MB)
- FRAME TYPE (2 bytes): High 16 bits (`CONTROL`, `DATA`, `HEARTBEAT`, `CLOSE`)
- COMMAND TYPE (2 bytes): Low 16 bits specifying the RPC command opcode

### Security Architecture
- Transport Security: Mutual TLS 1.3 (mTLS) via Java JSSE `SSLServerSocket` and `SSLSocket`.
- Role-Based Access Control (RBAC): Server-side enforcement for `VIEWER`, `OPERATOR`, and `ADMIN` roles.
- Agent Command Policy: Client Agents locally validate incoming commands against a local allowlist before execution.
- Tamper-Evident Audit Log: Asynchronous file logger maintaining a SHA-256 Hash Chain:
  $$H_n = \text{SHA256}(H_{n-1} \parallel \text{Timestamp} \parallel \text{AdminUser} \parallel \text{Action} \parallel \text{Status})$$

### Highlight Features
- Delta Screen Streaming: Captures screen changes in 64x64 grid tiles, hashes tile payloads, compresses modified tiles with JPEG, and transmits delta frames. Achieves up to 90% bandwidth reduction compared to full-frame capture.
- Concurrency Benchmarking: Dedicated module evaluating Java 24 Virtual Threads vs Fixed Thread Pools (50, 200) under synthetic loads of 100 to 1,000 concurrent client connections.

---

## 3. Repository Structure

The project is organized as a Maven Multi-Module repository:

```text
RemoteAdministrationSystem/
|-- pom.xml                 Root Parent POM (Dependency management and compiler settings)
|-- common/                 Shared binary frame codec, protocol enums, DTOs, PathValidator
|-- server/                 RAS Server, SessionManager, Virtual Thread listener, AuditLogger
|-- agent/                  Headless Client Agent, System, Process, File, and Screen services
|-- console/                JavaFX Admin Console GUI (Client, Process, File, Screen, Audit tabs)
|-- benchmark/              Automated load generator and latency/throughput benchmark runner
`-- docs/                   Technical documentation and demonstration scripts
    |-- protocol.md         Full application protocol specifications
    |-- security.md         Security architecture, RBAC matrix, and audit hash-chain details
    |-- benchmark.md        Performance benchmark report and CSV metric analysis
    `-- demo-script.md      Step-by-step presentation and live demo guide
```

---

## 4. Prerequisites

- Java Development Kit (JDK): Version 24 or higher
- Build Tool: Apache Maven 3.8.0 or higher

---

## 5. Build and Installation

To compile and package all sub-modules, run the following command from the root directory:

```bash
mvn clean package
```

This generates compiled JAR artifacts in the `target/` directory of each sub-module.

---

## 6. Running the Application

### 1. Start the RAS Server
```bash
java -jar server/target/server-1.0-SNAPSHOT.jar
```

### 2. Start a Client Agent
```bash
java -jar agent/target/agent-1.0-SNAPSHOT.jar
```

### 3. Launch the JavaFX Admin Console
```bash
mvn -pl console javafx:run
```

### 4. Run Concurrency Benchmarks
```bash
java -jar benchmark/target/benchmark-1.0-SNAPSHOT.jar
```

---

## 7. Verification and Testing

Run unit tests across all modules (including `FrameCodecTest` for stream fragmentation and packet coalescing):

```bash
mvn test
```

---

## 8. Documentation Links

- [Protocol Specifications](docs/protocol.md)
- [Security Architecture](docs/security.md)
- [Benchmark Report](docs/benchmark.md)
- [Presentation Demo Script](docs/demo-script.md)
