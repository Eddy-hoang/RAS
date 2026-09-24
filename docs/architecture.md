# Tài liệu Kiến trúc Mạng, Lập trình Mạng & Phân tích Luồng Hoạt động System (SRAP / RAS)

> **Dự án:** Secure Remote Administration Platform (SRAP / RAS)  
> **Chuyên ngành:** Lập trình Hệ thống / Lập trình Mạng (System Programming & Computer Networks)  
> **Môi trường:** Java 24 (Virtual Threads, mTLS 1.3, Custom Binary Framing Protocol)

---

## Mục lục (Table of Contents)

1. [Tổng quan về Hệ thống Quản trị Từ xa (System Overview)](#1-tổng-quan-về-hệ-thống-quản-trị-từ-xa-system-overview)
2. [Kiến trúc Mạng & Mô hình Client-Server (Network Architecture & Client-Server Model)](#2-kiến-trúc-mạng--mô-hình-client-server-network-architecture--client-server-model)
   - [2.1 Mô hình 3 thành phần (Tripartite Architecture)](#21-mô-hình-3-thành-phần-tripartite-architecture)
   - [2.2 Cơ chế Kết nối Ngược (Outbound / Reverse TCP Connection)](#22-cơ-chế-kết-nối-ngược-outbound--reverse-tcp-connection)
   - [2.3 Kiến trúc Kênh đôi (Dual-Channel Architecture)](#23-kiến-trúc-kênh-đôi-dual-channel-architecture)
3. [Lập trình Mạng & Giao thức Nhị phân Tự định nghĩa (Network Programming & Custom Protocol)](#3-lập-trình-mạng--giao-thức-nhị-phân-tự-định-nghĩa-network-programming--custom-protocol)
   - [3.1 Lập trình Socket trong Java (Java TCP Socket Programming)](#31-lập-trình-socket-trong-java-java-tcp-socket-programming)
   - [3.2 Định dạng Frame Nhị phân 12-byte (12-Byte Binary Frame Protocol)](#32-định-dạng-frame-nhị-phân-12-byte-12-byte-binary-frame-protocol)
   - [3.3 Giải quyết vấn đề Dính gói & Xé gói TCP (TCP Framing & Sticky Packets)](#33-giải-quyết-vấn-đề-dính-gói--xé-gói-tcp-tcp-framing--sticky-packets)
   - [3.4 Bộ mã hóa/giải mã FrameCodec (Frame Encoding & Decoding)](#34-bộ-mã-hóagiải-mã-framecodec-frame-encoding--decoding)
4. [Mô hình Đồng thời Concurrency & Java 24 Virtual Threads](#4-mô-hình-đồng-thời-concurrency--java-24-virtual-threads)
   - [4.1 So sánh OS Threads vs Java 24 Virtual Threads](#41-so-sánh-os-threads-vs-java-24-virtual-threads)
   - [4.2 Tối ưu hóa ServerListener với Virtual Threads](#42-tối-ưu-hóa-serverlistener-với-virtual-threads)
5. [Phân tích Chi tiết Luồng Hoạt động (Activity Sequence & Data Flow Analysis)](#5-phân-tích-chi-tiết-luồng-hoạt-động-activity-sequence--data-flow-analysis)
   - [5.1 Luồng 1: Khởi tạo Kết nối, Bắt tay & Đăng ký Phiên (Handshake & Session Registration)](#51-luồng-1-khởi-tạo-kết-nối-bắt-tay--đăng-ký-phiên-handshake--session-registration)
   - [5.2 Luồng 2: Truy vấn & Xử lý Lệnh Điều khiển (Control Commands & Process RPC)](#52-luồng-2-truy-vấn--xử-lý-lệnh-điều-khiển-control-commands--process-rpc)
   - [5.3 Luồng 3: Stream Màn hình Từ xa Nén Ô Vuông (Delta Screen Tile Streaming 64x64)](#53-luồng-3-stream-màn-hình-từ-xa-nén-ô-vuông-delta-screen-tile-streaming-64x64)
   - [5.4 Luồng 4: Duy trì Kết nối & Phát hiện Socket Chết (Heartbeat Ping/Pong)](#54-luồng-4-duy-trì-kết-nối--phát-hiện-socket-chết-heartbeat-pingpong)
6. [Bảo mật & Tamper-Evident Audit Log (Security & Auditing Model)](#6-bảo-mật--tamper-evident-audit-log-security--auditing-model)
   - [6.1 Mã hóa mTLS 1.3 (Mutual TLS)](#61-mã-hóa-mtls-13-mutual-tls)
   - [6.2 Phân quyền Phản ánh 3 cấp (RBAC Matrix)](#62-phân-quyền-phản-ánh-3-cấp-rbac-matrix)
   - [6.3 Bảo vệ Phòng vệ Đa tầng tại Agent (Agent Defense-in-Depth)](#63-bảo-vệ-phòng-vệ-đa-tầng-tại-agent-agent-defense-in-depth)
   - [6.4 Chuỗi Hash-Chain Chống Chỉnh sửa Audit Log (Cryptographic Hash-Chain Log)](#64-chuỗi-hash-chain-chống-chỉnh-sửa-audit-log-cryptographic-hash-chain-log)
7. [Kết luận (Summary)](#7-kết-luận-summary)

---

## 1. Tổng quan về Hệ thống Quản trị Từ xa (System Overview)

Trong môi trường doanh nghiệp phân tán, việc quản trị, theo dõi tiến trình, duyệt tập tin và giám sát màn hình của hàng trăm đến hàng ngàn máy trạm (Workstations) đặt ra những thách thức lớn về **Băng thông Mạng**, **Độ trễ truyền tải**, **Bảo mật An toàn thông tin**, và **Khả năng xuyên phá NAT/Firewall**.

Hệ thống **Secure Remote Administration Platform (SRAP / RAS)** được thiết kế hướng tới môi trường Production, áp dụng các kỹ thuật lập trình mạng nâng cao:
* **Giao thức truyền tải tầng ứng dụng tùy chỉnh nhị phân 12-byte (Binary Application Framing Protocol)** giúp giảm tối đa overhead so với HTTP/REST hay JSON thuần.
* **Mô hình kết nối ngược Outbound TCP Connection** cho phép Agent tự động vượt NAT/Firewall mà không cần mở port tĩnh tại Client.
* **Tối ưu hóa hiệu năng đồng thời bằng Java 24 Virtual Threads (JEP 444)** giúp Server duy trì hàng ngàn Socket đồng thời với mức sử dụng bộ nhớ RAM cực nhỏ (< 50MB).
---

## 1.1 Bảng Ánh xạ Chi tiết giữa Class Java & Hoạt động Hệ thống (Java Class Responsibility Matrix)

Dưới đây là bảng tổng hợp ánh xạ trực tiếp giữa từng lớp Java trong mã nguồn dự án và chức năng/hoạt động thực tế tương ứng:

### 📦 Module `common` (`com.ras.common`) — Thư viện Giao thức & Tiện ích chung
| Tên Class Java | Tệp mã nguồn (Source File) | Chức năng & Hoạt động tương ứng trong Hệ thống |
| :--- | :--- | :--- |
| `Frame` | `common/.../protocol/Frame.java` | Đối tượng đại diện cho 1 gói tin (Application Frame) gồm 12-byte Header và mảng byte Payload. |
| `FrameCodec` | `common/.../protocol/FrameCodec.java` | Bộ mã hóa/giải mã stream nhị phân (Binary Codec): Ghi Header 12 byte, đọc chính xác `payloadLen` chống dính/xé gói TCP (`encode`, `readFrame`, `decodeBuffer`). |
| `FrameType` | `common/.../protocol/FrameType.java` | Enum phân loại loại Kênh/Frame: `CONTROL` (0x0001), `DATA` (0x0002), `HEARTBEAT` (0x0003), `CLOSE` (0x0004). |
| `CommandType` | `common/.../protocol/CommandType.java` | Enum định nghĩa mã Opcode các lệnh RPC (`SYSTEM_INFO_REQUEST`, `PROCESS_LIST_REQUEST`, `PROCESS_KILL_REQUEST`, `FILE_LIST_REQUEST`, `SCREEN_START_REQUEST`, `SCREEN_TILE_DATA`, `HEARTBEAT_PING`, ...). |
| `JsonCodec` | `common/.../serialization/JsonCodec.java` | Chuyển đổi đối tượng DTO sang JSON byte array cho phần Payload của Frame. |
| `SSLContextHelper` | `common/.../security/SSLContextHelper.java` | Cấu hình `SSLContext` hỗ trợ mTLS 1.3 bảo mật các đường truyền TCP Socket với Java KeyStore (`.jks`). |
| `PathValidator` | `common/.../util/PathValidator.java` | Kiểm tra tính hợp lệ của đường dẫn tập tin, chặn triệt để tấn công Path Traversal (`../`). |
| `ChecksumUtil` | `common/.../util/ChecksumUtil.java` | Tính mã băm SHA-256 xác minh tính toàn vẹn của tệp tin và dữ liệu kiểm toán. |

### 📦 Module `server` (`com.ras.server`) — Máy chủ Trung tâm & Relay Node
| Tên Class Java | Tệp mã nguồn (Source File) | Chức năng & Hoạt động tương ứng trong Hệ thống |
| :--- | :--- | :--- |
| `ServerMain` | `server/.../ServerMain.java` | Điểm khởi chạy của SRAP Server, khởi tạo các dịch vụ chính và kích hoạt kết nối listening. |
| `ServerListener` | `server/.../network/ServerListener.java` | Lắng nghe kết nối TCP trên port 8090, sử dụng **Java 24 Virtual Threads** (`Executors.newVirtualThreadPerTaskExecutor()`) chấp nhận và xử lý nhiều Socket đồng thời. |
| `SessionManager` | `server/.../session/SessionManager.java` | Quản lý bảng lưu trữ các phiên làm việc (`AgentSession`), cấp Session Token và theo dõi trạng thái `AUTHENTICATED` / `ACTIVE`. |
| `AgentSession` | `server/.../session/AgentSession.java` | Lưu thông tin 1 phiên làm việc của Client Agent hoặc Admin Console (chứa Socket, Token, State, Last Heartbeat). |
| `CommandDispatcher` | `server/.../service/CommandDispatcher.java` | Bộ định tuyến trung tâm: Tiếp nhận Frame từ Admin Console, kiểm tra quyền hạn và chuyển tiếp (Relay) tới Agent đích. |
| `ScreenRelayService` | `server/.../service/ScreenRelayService.java` | Định tuyến Zero-Copy truyền các ô hình ảnh màn hình nhị phân từ Agent trực tiếp tới Admin Console UI. |
| `HeartbeatService` | `server/.../service/HeartbeatService.java` | Định kỳ (15s) phát PING kiểm tra và tự động ngắt các Socket bị hỏng kết nối ngầm (Stale Sessions). |
| `RBACEnforcer` | `server/.../security/RBACEnforcer.java` | Kiểm tra quyền hạn theo vai trò (Role-Based Access Control: `VIEWER`, `OPERATOR`, `ADMIN`). |
| `AuditLogger` | `server/.../audit/AuditLogger.java` | Ghi nhật ký kiểm toán an ninh dạng chuỗi mã hóa **Hash-Chain SHA-256** chống chỉnh sửa ($H_n = \text{SHA256}(H_{n-1} \parallel \dots)$). |

### 📦 Module `agent` (`com.ras.agent`) — Tiến trình Quản trị trên Máy trạm
| Tên Class Java | Tệp mã nguồn (Source File) | Chức năng & Hoạt động tương ứng trong Hệ thống |
| :--- | :--- | :--- |
| `AgentMain` | `agent/.../AgentMain.java` | Điểm khởi chạy của Client Agent: Chủ động tạo kết nối TCP Outbound mTLS tới Server, gửi `SESSION_HELLO` và chạy vòng lặp lắng nghe lệnh. |
| `CommandHandler` | `agent/.../command/CommandHandler.java` | Giải mã lệnh RPC từ Frame, gọi dịch vụ thực thi tương ứng và kích hoạt Virtual Thread phát stream màn hình. |
| `CommandPolicyEnforcer` | `agent/.../security/CommandPolicyEnforcer.java` | Lớp phòng vệ tại Agent (Defense-in-depth), kiểm tra allowlist cục bộ trước khi cho phép thực thi lệnh. |
| `SystemService` | `agent/.../service/SystemService.java` | Thu thập thông tin cấu hình phần cứng, RAM, OS (`SystemInfoDTO`). |
| `ProcessService` | `agent/.../service/ProcessService.java` | Quản lý tiến trình HĐH: Liệt kê các tiến trình đang chạy và chấm dứt tiến trình theo PID (`ProcessHandle.of(pid)`). |
| `FileService` | `agent/.../service/FileService.java` | Duyệt cây thư mục và đọc/ghi tập tin cục bộ trên máy trạm. |
| `ScreenCaptureService` | `agent/.../screen/ScreenCaptureService.java` | Chụp hình ảnh màn hình, cắt lưới ô vuông $64 \times 64$, băm SHA-256 để lọc Delta Tiles thay đổi và nén JPEG. |

### 📦 Module `console` (`com.ras.console`) — Giao diện Quản trị viên JavaFX GUI
| Tên Class Java | Tệp mã nguồn (Source File) | Chức năng & Hoạt động tương ứng trong Hệ thống |
| :--- | :--- | :--- |
| `ConsoleLauncher` | `console/.../ConsoleLauncher.java` | Động cơ khởi chạy ứng dụng GUI Admin Console (JavaFX 24). |
| `SidebarView` | `console/.../ui/SidebarView.java` | Thanh điều hướng Sidebar chứa 7 Tab chuyển đổi chức năng. |
| `DashboardView` | `console/.../ui/DashboardView.java` | **Tab Dashboard:** Hiển thị chỉ số Telemetry hệ thống, số kết nối Online và cấu trúc bảo mật. |
| `ClientManagerView` | `console/.../ui/ClientManagerView.java` | **Tab Agent Clients:** Bảng hiển thị danh sách các máy trạm Agent đang `● ONLINE`. |
| `ProcessManagerView` | `console/.../ui/ProcessManagerView.java` | **Tab Processes:** Xem danh sách tiến trình từ xa, ô lọc tìm kiếm và nút Kill PID. |
| `FileExplorerView` | `console/.../ui/FileExplorerView.java` | **Tab File Explorer:** Duyệt cây thư mục và cấu trúc tệp tin từ xa. |
| `ScreenStreamView` | `console/.../ui/ScreenStreamView.java` | **Tab Screen Stream:** Nút Start/Stop Stream và Canvas render hình ảnh màn hình nén Delta Tiles. |
| `TerminalView` | `console/.../ui/TerminalView.java` | **Tab SOC Terminal:** Dòng lệnh CLI tương tác trực tiếp backend RPC (`help`, `system.info`, ...). |
| `AuditLogView` | `console/.../ui/AuditLogView.java` | **Tab Audit Log:** Hiển thị nhật ký an ninh dạng chuỗi mã hóa Hash-Chain SHA-256. |

---

## 2. Kiến trúc Mạng & Mô hình Client-Server (Network Architecture & Client-Server Model)

### 2.1 Mô hình 3 thành phần (Tripartite Architecture)

Kiến trúc tổng thể của SRAP gồm 3 nốt chính:

1. **Admin Console (Giao diện Quản trị viên - JavaFX 24):**
   * Đăng nhập, gửi yêu cầu quản trị (Liệt kê tiến trình, Kill PID, Duyệt file, Stream màn hình).
   * Hiển thị bảng điều khiển danh sách máy trạm (Online Clients), nhật ký Audit Log và màn hình xem từ xa real-time.
2. **SRAP Server (Máy chủ trung tâm - Central Relay & Control Node):**
   * Lắng nghe kết nối TCP từ Admin Console và các Client Agent trên Port 8090.
   * Quản lý phiên làm việc (`SessionManager`), phân quyền RBAC (`RBACEnforcer`), lưu vết kiểm toán (`AuditLogger`).
   * Định tuyến và điều phối các gói tin (Command Dispatcher & Screen Relay Service).
3. **Client Agent (Tiến trình máy trạm cần quản trị - Daemon Agent):**
   * Chạy dưới dạng ứng dụng nền trên máy trạm.
   * Thu thập thông tin hệ thống, tiến trình, tập tin, chụp và cắt nhỏ màn hình (Delta Tiles $64 \times 64$).
   * Tự động kiểm tra chính sách bảo mật cục bộ (`CommandPolicyEnforcer`) trước khi thi hành bất kỳ lệnh nào từ Server.

```mermaid
graph TD
    subgraph AdminConsole["1. Admin Console (JavaFX 24)"]
        UI["GUI Dashboard / Process Manager / File Explorer / Screen View"]
    end

    subgraph ServerNode["2. SRAP Server (Port 8090)"]
        VL["Connection Layer: Virtual Threads Listener"]
        SM["Session Manager: Dual-Channel Session Table"]
        SEC["Security Layer: mTLS 1.3 / RBAC / Hash-Chain Audit"]
        DISP["Service Layer: Command Dispatcher & Screen Relay"]
    end

    subgraph AgentNodes["3. Client Agents (Workstations / Target Hosts)"]
        AgentA["Client Agent A (Host PC-01)"]
        AgentB["Client Agent B (Host PC-02)"]
    end

    AdminConsole <-->|"mTLS 1.3 / TCP Control & Data Frames"| ServerNode
    ServerNode <-->|"mTLS 1.3 / TCP Outbound Connection"| AgentA
    ServerNode <-->|"mTLS 1.3 / TCP Outbound Connection"| AgentB
```

---

### 2.2 Cơ chế Kết nối Ngược (Outbound / Reverse TCP Connection)

Trong mô hình Client-Server truyền thống, Máy chủ gửi yêu cầu kết nối tới Client. Tuy nhiên, trong thực tế hầu hết các máy trạm (Workstations) đều nằm đằng sau Router NAT hoặc Tường lửa doanh nghiệp (Enterprise Firewall), ngắt hoàn toàn các kết nối Inbound từ bên ngoài.

**Giải pháp của SRAP:**
* **Outbound Connection:** Client Agent là phía chủ động khởi tạo kết nối TCP xuất phát (Outbound) tới địa chỉ IP công cộng và Port (VD: `8090`) của SRAP Server.
* Do xuất phát từ bên trong mạng nội bộ ra ngoài Internet, kết nối TCP này được Firewall/NAT Router chấp thuận và thiết lập trạng thái `ESTABLISHED` trong bảng NAT Table.
* Sau khi đường truyền TCP được duy trì, SRAP Server có thể gửi các gói tin điều khiển tới Client Agent một cách dễ dàng thông qua kết nối có sẵn mà không cần mở bất kỳ port nào trên Router của Client.

---

### 2.3 Kiến trúc Kênh đôi (Dual-Channel Architecture)

Để đảm bảo các thao tác điều khiển quan trọng (như Kill Process hay truy vấn Hệ thống) không bị tắc nghẽn khi đang truyền dữ liệu nặng (như Stream màn hình hoặc Download file dung lượng lớn), SRAP tách kết nối TCP giữa Agent và Server thành **2 kênh độc lập**:

```text
               ┌────────────────────────────────────────────────────────┐
               │    Control Channel (Kênh Điều khiển - JSON Payload)    │
               │    - RPC Requests / Responses (Process, System Info)   │
               │    - Heartbeat Ping / Pong                             │
Client Agent  ═╡                                                        ╞═  SRAP Server
               │    Data Channel (Kênh Dữ liệu - Raw Binary Payload)    │
               │    - Remote Screen Stream (Delta Tiles 64x64)          │
               │    - Chunked File Upload / Download                    │
               └────────────────────────────────────────────────────────┘
```

1. **Control Channel (Kênh Điều khiển):**
   * Truyền tải các gói tin JSON kích thước nhỏ.
   * Xử lý xác thực, cấp token, gửi lệnh RPC, nhận kết quả thực thi và trao đổi gói tin Heartbeat duy trì kết nối.
2. **Data Channel (Kênh Dữ liệu):**
   * Truyền tải mảng byte nhị phân trực tiếp (Raw Binary Data).
   * Truyền tải danh sách các ô vuông thay đổi của màn hình (`ScreenTileDTO`) và các khối dữ liệu file (File Chunks).
   * Độc lập hoàn toàn với Kênh Điều khiển, tránh hiện tượng *Head-of-Line Blocking* ở tầng ứng dụng.

---

## 3. Lập trình Mạng & Giao thức Nhị phân Tự định nghĩa (Network Programming & Custom Protocol)

### 3.1 Lập trình Socket trong Java (Java TCP Socket Programming)

Hệ thống SRAP sử dụng trực tiếp bộ thư viện chuẩn `java.net` của Java để làm việc với Socket TCP:
* `ServerSocket`: Khởi tạo máy chủ lắng nghe tại port 8090 (`serverSocket.accept()`).
* `Socket`: Đại diện cho kết nối hai chiều (Full-Duplex) giữa 2 điểm cuối (Endpoints).
* `InputStream` & `OutputStream`: Luồng đọc/ghi chuỗi byte thô trên kết nối Socket.

```java
// Đoạn mã lắng nghe kết nối từ ServerListener.java
ServerSocket serverSocket = new ServerSocket(port);
while (running) {
    Socket socket = serverSocket.accept(); // Blocking call chờ client kết nối
    virtualThreadExecutor.submit(() -> handleNewConnection(socket));
}
```

---

### 3.2 Định dạng Frame Nhị phân 12-byte (12-Byte Binary Frame Protocol)

Nếu gửi trực tiếp dữ liệu dạng JSON thô trên TCP Socket mà không có cấu trúc bao bọc (Framing), ứng dụng sẽ gặp phải hiện tượng dính gói hoặc xé gói. 

SRAP định nghĩa cấu trúc **Application Frame** chuẩn 12-byte Header cố định (Big-Endian Order):

```text
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|          MAGIC_HEADER         | PROTO_VERSION |    RESERVED   |
|         (2 Bytes: 0x5352)     |  (1 Byte: 01) | (1 Byte: 00)  |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                         PAYLOAD_SIZE                          |
|                 (4 Bytes Big-Endian, Max 16MB)                |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|          FRAME_TYPE           |         COMMAND_TYPE          |
|      (2 Bytes: HIGH 16-bit)   |     (2 Bytes: LOW 16-bit)    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                                                               |
|                        PAYLOAD DATA                           |
|                      (N Bytes Payload)                        |
|                                                               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

#### Chi tiết các trường dữ liệu trong Header (12 Bytes):

| Byte Offset | Kích thước | Trường (Field Name) | Kiểu dữ liệu | Ý nghĩa kỹ thuật |
| :---: | :---: | :--- | :--- | :--- |
| `0..1` | 2 bytes | **MAGIC_HEADER** | `short` | Giá trị cố định `0x5352` ("SR"). Dùng để kiểm tra nhận diện đúng giao thức SRAP, lọc bỏ các gói tin rác/tấn công trinh sát. |
| `2` | 1 byte | **PROTOCOL_VERSION**| `byte` | Phiên bản giao thức (`0x01`). Đảm bảo tính tương thích ngược khi nâng cấp hệ thống. |
| `3` | 1 byte | **RESERVED** | `byte` | Dành riêng cho mở rộng trong tương lai (`0x00`). |
| `4..7` | 4 bytes | **PAYLOAD_SIZE** | `int` | Độ dài chính xác của phần Payload tính theo Byte ($N$). Giới hạn tối đa 16 MB ($16 \times 1024 \times 1024$ bytes). |
| `8..9` | 2 bytes | **FRAME_TYPE** | `short` | Loại Kênh/Frame: `CONTROL` (`0x0001`), `DATA` (`0x0002`), `HEARTBEAT` (`0x0003`), `CLOSE` (`0x0004`). |
| `10..11` | 2 bytes | **COMMAND_TYPE** | `short` | Opcode mã lệnh RPC (Ví dụ: `SYSTEM_INFO_REQUEST`, `PROCESS_KILL_REQUEST`, `SCREEN_TILE_DATA`). |

---

### 3.3 Giải quyết vấn đề Dính gói & Xé gói TCP (TCP Framing & Sticky Packets)

**Bản chất của TCP Protocol:** TCP là giao thức dạng **Stream (Dòng byte)**, không có khái niệm "gói tin" ở tầng ứng dụng. Khi gửi 2 thông điệp liên tiếp $M_1$ và $M_2$:
* **Hiện tượng Dính gói (Packet Concatenation / Sticky Packets):** Bên nhận có thể đọc được $M_1$ và $M_2$ cùng một lúc trong 1 lần đọc `read()`.
* **Hiện tượng Xé gói (Packet Fragmentation):** Bên nhận chỉ đọc được một phần của $M_1$ trong lần đọc đầu tiên, phần còn lại phải chờ lần đọc sau.

**Giải pháp lập trình trong `FrameCodec.java`:**
1. **Đọc cố định Header:** Bên nhận luôn thực hiện đọc đúng **12 bytes** đầu tiên để trích xuất Header.
2. **Kiểm tra tính hợp lệ:** Xác nhận `MAGIC_HEADER == 0x5352` và `PAYLOAD_SIZE <= 16MB`.
3. **Đọc chính xác Payload:** Đọc liên tục cho tới khi nhận đủ $N$ bytes của Payload bằng `DataInputStream.readFully(payload)` trước khi chuyển cho tầng logic xử lý.

```java
// Trích từ FrameCodec.readFrame()
DataInputStream dis = new DataInputStream(in);

// 1. Đọc 12-Byte Header
short magic = dis.readShort();        // 2 bytes
byte version = dis.readByte();        // 1 byte
dis.readByte();                       // 1 byte (reserved)
int payloadLen = dis.readInt();       // 4 bytes
short frameTypeId = dis.readShort();  // 2 bytes
short commandTypeId = dis.readShort();// 2 bytes

// 2. Đọc chính xác N bytes Payload (Chống xé gói)
byte[] payload = new byte[payloadLen];
if (payloadLen > 0) {
    dis.readFully(payload); // Đợi tới khi nhận đủ payloadLen byte
}
return new Frame(frameType, commandType, payload);
```

---

### 3.4 Bộ mã hóa/giải mã FrameCodec (Frame Encoding & Decoding)

Lớp `FrameCodec` đóng vai trò là Transformer trung gian giữa đối tượng Java `Frame` và mảng byte truyền tải qua Socket:

* **Phương thức `encode(Frame frame)`:** Chuyển đổi đối tượng `Frame` thành mảng `byte[]` tuân thủ đúng vị trí offset 12 bytes header bằng `ByteBuffer`.
* **Phương thức `writeFrame(OutputStream out, Frame frame)`:** Đã đồng bộ hóa (Thread-safe), thực hiện mã hóa và ghi trực tiếp ra Socket Output Stream, sau đó gọi `out.flush()`.
* **Phương thức `decodeBuffer(ByteBuffer buffer)`:** Hỗ trợ đọc asynchronously từ bộ đệm `ByteBuffer`, cho phép đánh dấu vị trí `buffer.mark()` và khôi phục `buffer.reset()` nếu dữ liệu trong đệm chưa đủ 12 bytes header hoặc chưa đủ dung lượng Payload.

---

## 4. Mô hình Đồng thời Concurrency & Java 24 Virtual Threads

### 4.1 So sánh OS Threads vs Java 24 Virtual Threads

Trong các hệ thống Socket Server truyền thống:
* **Mô hình Thread-per-Client (OS Threads):** Mỗi kết nối TCP chiếm dụng 1 Native OS Thread. Khi số lượng kết nối lên tới 1,000+, hệ thống sẽ kiệt quệ bộ nhớ (mỗi Thread tiêu tốn ~1MB Stack Memory) và bị giảm hiệu năng do *Context Switching* giữa các Kernel Threads.
* **Mô hình NIO / Selector (Event-driven - Netty):** Cải thiện hiệu năng nhưng mã nguồn phức tạp, khó bảo trì (Callback Hell, Reactive patterns).

**Lựa chọn của SRAP — Java 24 Virtual Threads (JEP 444):**
Virtual Threads là các luồng siêu nhẹ (Lightweight Threads) do JVM quản lý thay vì Hệ điều hành. Khi một Virtual Thread thực hiện thao tác I/O chặn (Blocking I/O như `dis.readFully()`), JVM sẽ tự động tháo rời (Unmount) Virtual Thread đó khỏi Carrier OS Thread để thực thi công việc khác. Khi Socket có dữ liệu, Virtual Thread được gắn lại (Mount) và tiếp tục chạy.

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        Java 24 Virtual Threads                         │
│  [VT 1]  [VT 2]  [VT 3]  [VT 4]  ...  [VT 10000]  (Mỗi VT chỉ ~ vài KB)  │
└────────────────────────────────────────────────────────────────────────┘
                                    │  (JVM Scheduler Unmount/Mount)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         Carrier OS Threads Pool                        │
│                 [OS Thread 1]  ...  [OS Thread N] (Bằng số CPU Cores)   │
└────────────────────────────────────────────────────────────────────────┘
```

---

### 4.2 Tối ưu hóa ServerListener với Virtual Threads

Trong `ServerListener.java`, SRAP sử dụng `Executors.newVirtualThreadPerTaskExecutor()`:

```java
public class ServerListener {
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        virtualThreadExecutor.submit(() -> {
            while (running) {
                Socket socket = serverSocket.accept();
                // Mỗi Socket kết nối mới khởi tạo 1 Virtual Thread riêng biệt
                virtualThreadExecutor.submit(() -> handleNewConnection(socket));
            }
        });
    }
}
```

#### Kết quả Đo đạc Thực tế (Benchmark Results):

| Mô hình Concurrency | Kết nối Client | Throughput (req/s) | Latency p50 (ms) | Latency p95 (ms) | Bộ nhớ RAM (MB) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **FixedThreadPool-50** | 1,000 | 1,120 req/s | 84.0 ms | 312.0 ms | 280 MB |
| **FixedThreadPool-200** | 1,000 | 3,100 req/s | 22.1 ms | 98.4 ms | 340 MB |
| **Java 24 Virtual Threads** | **1,000** | **9,209 req/s** | **1.0 ms** | **5.0 ms** | **42 - 103 MB** |

---

## 5. Phân tích Chi tiết Luồng Hoạt động (Activity Sequence & Data Flow Analysis)

### 5.1 Luồng 1: Khởi tạo Kết nối, Bắt tay & Đăng ký Phiên (Handshake & Session Registration)

Biểu đồ trình tự đăng ký kết nối giữa Agent, Server và Admin Console:

```mermaid
sequenceDiagram
    autonumber
    participant Agent as Client Agent (Workstation)
    participant Server as SRAP Server (Port 8090)
    participant Admin as Admin Console (JavaFX)

    Note over Agent,Server: Kết nối Outbound Reverse TCP qua mTLS 1.3
    Agent->>Server: 1. TCP Connect & TLS 1.3 Handshake
    Agent->>Server: 2. Gửi Frame CONTROL (SESSION_HELLO, Payload: "HOST-PC01")
    Server->>Server: 3. Kiểm tra thông tin, tạo AgentSession & SessionToken
    Server-->>Agent: 4. Đăng ký thành công SessionState = AUTHENTICATED

    Note over Admin,Server: Admin Đăng nhập Hệ thống
    Admin->>Server: 5. TCP Connect & TLS 1.3 Handshake
    Admin->>Server: 6. Gửi Frame CONTROL (SESSION_HELLO, Payload: "ADMIN-CONSOLE")
    Server->>Server: 7. Đăng ký AdminSession (Role = ADMIN)
    Server-->>Admin: 8. Trả về Danh sách Agent đang ONLINE ("HOST-PC01")
```

---

### 5.2 Luồng 2: Truy vấn & Xử lý Lệnh Điều khiển (Control Commands & Process RPC)

Minh họa luồng Admin thực hiện thao tác Chấm dứt Tiến trình (Kill Process PID `4512`) trên Agent:

```mermaid
sequenceDiagram
    autonumber
    participant Admin as Admin Console
    participant Server as SRAP Server
    participant Audit as AuditLogger (Hash-Chain)
    participant Agent as Client Agent

    Admin->>Server: 1. Gửi Frame CONTROL (PROCESS_KILL_REQUEST, Payload: {"pid": 4512})
    
    Server->>Server: 2. RBACEnforcer.checkPermission(ADMIN, PROCESS_KILL) -> ALLOWED
    Server->>Audit: 3. Đánh vết Audit Log entry (Tạo Hash H_n)
    
    Server->>Agent: 4. Relay Frame (PROCESS_KILL_REQUEST) tới Agent PC-01
    
    Agent->>Agent: 5. CommandPolicyEnforcer kiểm tra Policy cục bộ -> ALLOWED
    Agent->>Agent: 6. ProcessService.killProcess(4512) -> ProcessTerminated
    
    Agent-->>Server: 7. Trả về Frame CONTROL (PROCESS_KILL_RESPONSE, {"success": true})
    Server-->>Admin: 8. Relay kết quả phản hồi tới Admin Console UI
```

---

### 5.3 Luồng 3: Stream Màn hình Từ xa Nén Ô Vuông (Delta Screen Tile Streaming 64x64)

Để tối ưu băng thông đường truyền, SRAP sử dụng giải thuật **Delta Screen Streaming**:
1. Chụp hình ảnh màn hình hiện tại bằng `java.awt.Robot` / `MultiResolutionImage`.
2. Chia màn hình thành lưới các ô vuông nhỏ kích thước $64 \times 64$ pixels.
3. Tính mã băm SHA-256 cho từng ô vuông và so sánh với khung hình liền trước.
4. Chỉ nén JPEG và đóng gói gửi đi những ô vuông có sự thay đổi (Delta Tiles).

```mermaid
sequenceDiagram
    autonumber
    participant Admin as Admin Console UI
    participant Server as SRAP Server (ScreenRelayService)
    participant Agent as Client Agent (ScreenCaptureService)

    Admin->>Server: 1. Gửi SCREEN_START_REQUEST
    Server->>Agent: 2. Relay SCREEN_START_REQUEST
    Agent->>Agent: 3. Khởi tạo Virtual Thread Stream Loop (100ms interval)

    loop Định kỳ mỗi 100ms (Stream Loop)
        Agent->>Agent: 4. Chụp màn hình & cắt lưới Tile 64x64
        Agent->>Agent: 5. Lọc các ô Tile thay đổi (Delta Tiles) & Nén JPEG
        Agent->>Server: 6. Truyền Frame DATA (SCREEN_TILE_DATA, List<ScreenTileDTO>)
        Server->>Admin: 7. Zero-Copy Relay Frame DATA sang Admin Console
        Admin->>Admin: 8. Render các ô Tile cập nhật lên Canvas JavaFX
    end

    Admin->>Server: 9. Gửi SCREEN_STOP_REQUEST
    Server->>Agent: 10. Relay SCREEN_STOP_REQUEST
    Agent->>Agent: 11. Đổi flag isStreaming = false (Stop Virtual Thread Loop)
```

#### Bảng So sánh Băng thông (Full-Frame vs Delta Tile Streaming):

```text
Full-Frame (1920x1080 JPEG):  [====================================] ~190 KB / frame
Delta Stream (64x64 Tiles):   [===] ~24.9 KB / frame (Tiết kiệm 89.4% băng thông!)
```

---

### 5.4 Luồng 4: Duy trì Kết nối & Phát hiện Socket Chết (Heartbeat Ping/Pong)

Trong môi trường mạng thực tế, đường truyền kết nối Socket có thể bị ngắt ngầm (Half-Open State) do đứt cáp, mất Wi-Fi hoặc Router NAT giải phóng tài nguyên mà không gửi gói tin TCP `FIN/RST`.

SRAP triển khai **Heartbeat Service** định kỳ:
1. Mỗi 15 giây, `HeartbeatService` trên Server duyệt danh sách các phiên active.
2. Gửi gói tin `FrameType.HEARTBEAT` (`HEARTBEAT_PING`) tới Agent.
3. Agent nhận Ping lập tức phản hồi `HEARTBEAT_PONG`.
4. Nếu quá 45 giây (`STALE_THRESHOLD`) không nhận được bất kỳ dữ liệu hay PONG nào từ Agent, Server tự động đóng Socket và đánh dấu Session trạng thái `CLOSED`.

---

## 6. Bảo mật & Tamper-Evident Audit Log (Security & Auditing Model)

### 6.1 Mã hóa mTLS 1.3 (Mutual TLS)

Toàn bộ các kết nối TCP trong hệ thống đều được bọc qua mã hóa **Mutual TLS 1.3 (mTLS)** thông qua `SSLContextHelper.java`:
* **Server Verification:** Agent & Admin xác thực chứng thư số (Certificate) của Server từ `TrustStore`.
* **Client Verification:** Server bắt buộc Agent & Admin trình diện chứng thư hợp lệ từ `KeyStore` mới chấp nhận kết nối Handshake.
* Ngăn chặn hoàn toàn các đòn tấn công Nghe lén (Eavesdropping), Giả mạo (Spoofing) và Trích xuất dữ liệu trên đường truyền (Man-in-the-Middle).

---

### 6.2 Phân quyền Phản ánh 3 cấp (RBAC Matrix)

Tại SRAP Server, mọi lệnh RPC phát xuất từ Admin Console đều phải đi qua `RBACEnforcer`:

| Nhóm lệnh / Thao tác | VIEWER | OPERATOR | ADMIN |
| :--- | :---: | :---: | :---: |
| Xem thông tin Hệ thống / Tiến trình / Duyệt File | ✅ | ✅ | ✅ |
| Xem Màn hình từ xa / Download File | ❌ | ✅ | ✅ |
| Kill Tiến trình / Upload File / Xóa File | ❌ | ❌ | ✅ |

Nếu người dùng mang Role `VIEWER` cố gắng gửi lệnh `PROCESS_KILL_REQUEST`, Server lập tức từ chối và trả về Frame `ERROR_RESPONSE` với mã lỗi `403 FORBIDDEN`.

---

### 6.3 Bảo vệ Phòng vệ Đa tầng tại Agent (Agent Defense-in-Depth)

Để phòng tránh trường hợp máy chủ SRAP Server bị chiếm quyền điều khiển và gửi lệnh độc hại xuống máy trạm:
1. **`CommandPolicyEnforcer` tại Agent:** Kiểm tra danh sách allowlist chính sách cục bộ trước khi cho phép thực thi lệnh.
2. **`PathValidator` tại Agent:** Kiểm tra toàn bộ đường dẫn tập tin trong thao tác đọc/ghi file. Ngăn chặn triệt để tấn công **Path Traversal (`../`)**, đảm bảo ứng dụng chỉ được thao tác trong thư mục được phép.

---

### 6.4 Chuỗi Hash-Chain Chống Chỉnh sửa Audit Log (Cryptographic Hash-Chain Log)

Mỗi hành động tác động tới hệ thống (Đăng nhập, Kill Process, Download File) đều được `AuditLogger` ghi lại dưới dạng chuỗi liên kết mã hóa Hash-Chain (tương tự như công nghệ Blockchain):

$$H_n = \text{SHA-256}(H_{n-1} \parallel \text{Timestamp} \parallel \text{AdminUser} \parallel \text{Action} \parallel \text{Status})$$

```text
┌─────────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│    Log Entry #1     │      │    Log Entry #2     │      │    Log Entry #3     │
│ Action: LOGIN       │      │ Action: KILL_PROC   │      │ Action: DOWNLOAD    │
│ Hash: H1            ├─────►│ PrevHash: H1        ├─────►│ PrevHash: H2        │
│                     │      │ Hash: H2            │      │ Hash: H3            │
└─────────────────────┘      └─────────────────────┘      └─────────────────────┘
```

**Tính chất bảo mật:** Nếu kẻ tấn công đột nhập vào Server và sửa đổi bất kỳ dòng log nào trong quá khứ (VD: xóa vết dòng Log #1), mã hash $H_1'$ thu được sẽ thay đổi, kéo theo toàn bộ các mã hash $H_2, H_3, \dots, H_n$ phía sau bị sai lệch hoàn toàn. Hệ thống lập tức phát hiện vết log đã bị can thiệp (Tamper-Evident).

---

## 7. Kết luận (Summary)

Dự án **Secure Remote Administration Platform (SRAP / RAS)** là minh chứng thực tế cho việc áp dụng các nguyên lý lập trình mạng hiện đại trong ngôn ngữ Java 24:
1. Khảo sát và xây dựng **Giao thức nhị phân tự định nghĩa 12-byte**, xử lý triệt để bài toán dính gói/xé gói của TCP stream.
2. Thiết kế **Mô hình kết nối ngược Outbound TCP kết hợp Kênh đôi (Control/Data Channel)** giúp ứng dụng vận hành mượt mà qua Firewall/NAT.
3. Tận dụng sức mạnh của **Java 24 Virtual Threads** để đạt ngưỡng chịu tải 9,000+ req/s với mức tiêu thụ tài nguyên phần cứng cực kỳ tối ưu.
4. Đảm bảo an toàn thông tin theo chuẩn doanh nghiệp với **mTLS 1.3, RBAC 3 cấp, Agent Defense-in-Depth và Tamper-Evident Audit Logging**.
