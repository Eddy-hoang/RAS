# Security & Defense-in-Depth Architecture

## 1. mTLS Transport Encryption
All TCP sockets use JDK JSSE `SSLContext` configured with TLS 1.3 and mutual certificate verification using Java KeyStores (`.jks`).

## 2. Server-side RBAC Matrix
| Command Type | VIEWER | OPERATOR | ADMIN |
|---|:---:|:---:|:---:|
| System / Process / File Read | ✅ | ✅ | ✅ |
| Screen Stream / File Download | ❌ | ✅ | ✅ |
| Process Kill / File Upload / Delete | ❌ | ❌ | ✅ |

## 3. Agent Defense-in-Depth Policy
The Client Agent maintains an independent local `CommandPolicyEnforcer`. Even if a compromised server sends a malicious process termination or system power command, the agent checks its local policy before executing.

## 4. Tamper-Evident Hash-Chain Audit Logging
Audit log lines follow a cryptographically linked hash chain:
$$H_n = \text{SHA256}(H_{n-1} \parallel \text{Timestamp} \parallel \text{AdminUser} \parallel \text{Action} \parallel \text{Status})$$
Any retroactive editing of past log entries invalidates all subsequent hashes.
