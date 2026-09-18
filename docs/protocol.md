# Application Protocol Specifications (SRAP Protocol v1)

## 1. 12-Byte Fixed Binary Header
```text
┌──────────────┬──────────────┬──────────────┬────────────────────────┐
│ MAGIC / VER  │ PAYLOAD SIZE │ TYPE / CHAN  │        PAYLOAD         │
│   4 bytes    │   4 bytes    │   4 bytes    │  N bytes (0 <= N <=16M)│
└──────────────┴──────────────┴──────────────┴────────────────────────┘
```

- **MAGIC (2 bytes)**: `0x5352` ("SR")
- **VERSION (1 byte)**: `0x01`
- **RESERVED (1 byte)**: `0x00`
- **PAYLOAD SIZE (4 bytes)**: Big-endian integer specifying exact payload length N. Max limit: 16 MB.
- **FRAME TYPE (2 bytes)**: High 16 bits = `CONTROL` (`0x0001`), `DATA` (`0x0002`), `HEARTBEAT` (`0x0003`), `CLOSE` (`0x0004`).
- **COMMAND TYPE (2 bytes)**: Low 16 bits specifying RPC command opcode.

## 2. Channel Architecture
- **Control Channel**: TCP/TLS connection carrying JSON payloads for authentication, heartbeats, process management, and file metadata.
- **Data Channel**: TCP/TLS connection carrying raw binary payloads bound to a session token for file chunks and 64x64 screen delta tiles.

## 3. Session State Machine
`CONNECTING` → `AUTHENTICATED` → `ACTIVE` → `STALE` → `CLOSED`
