# SRAP Presentation & Demo Script

1. **Architecture & Scope Overview (1-2 mins)**
   - Explain the 12-byte binary protocol framing (`[4b Magic/Ver][4b Length][4b Type]`).
   - Highlight the outbound Agent -> Server connection (NAT/Firewall friendly).

2. **mTLS & Session Establishment (1 min)**
   - Start Server (`server`).
   - Launch Client Agent (`agent`) and Admin Console (`console`). Show instant session binding.

3. **Administration Features (2 mins)**
   - Demonstrate System Info & Process List retrieval.
   - Show Process Termination (PID kill) and show immediate entry in Audit Log.
   - Show File Explorer with SHA-256 checksum verification.

4. **Security & Defense-in-Depth (2 mins)**
   - Log in as VIEWER role and attempt a Process Kill command -> Show `403 FORBIDDEN` response.
   - Inspect `audit.log` and explain the SHA-256 Hash Chain.

5. **Highlight 1: Concurrency Benchmark (2 mins)**
   - Run `benchmark` module comparing **Java 24 Virtual Threads** vs **Fixed Thread Pools (50, 200)**.
   - Show throughput (req/sec) and memory footprint stability.

6. **Highlight 2: Delta Screen Streaming (2 mins)**
   - Start Delta Screen Stream on Admin Console.
   - Show tile grid differential capture (64x64) and report the **~89.4% bandwidth reduction metric**.
