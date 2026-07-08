# PackX

A full-stack file packing/unpacking platform, built on top of a much smaller
original: two Java console programs (`program606` for packing, `program614`
for unpacking) that combined `.txt` files into a single archive using a
100-byte header per file and a single-byte XOR cipher.

PackX keeps that core algorithm and its on-disk format byte-for-byte
compatible, and wraps it in a Spring Boot REST API, MongoDB-backed operation
history, and a React frontend.

---

## 1. Project Overview

- **Pack** one or more `.txt` files into a single `.pack` archive.
- **Unpack** a `.pack` archive back into its original files (zipped for
  download when there's more than one).
- Every operation — success or failure — is recorded in MongoDB with
  metadata: type, files, sizes, timestamps, processing time.
- A dashboard summarizes activity; a history page lists every operation;
  an operation detail page shows the full record for one.

## 2. Features

- Drag-and-drop multi-file upload with per-file removal before packing
- Streaming pack/unpack (no whole-file buffering in memory)
- MongoDB operation history + dashboard statistics
- Centralized error handling with consistent JSON error responses
- Path-traversal and filename-sanitization protection on extraction
- Duplicate-filename, empty-file, and extension validation
- ZIP bundling when an unpack operation produces multiple files
- Docker Compose setup for backend + frontend + MongoDB

## 3. Original Algorithm — What It Actually Does

**Packing (`program606`):** for each `.txt` file in a chosen folder, write a
100-byte header (`"<filename> <filesize>"`, space-padded) followed by the
file's bytes XORed against the fixed key `0x11`, repeated for every file, all
into one output file.

**Unpacking (`program614`):** read 100 bytes, parse them as
`"<filename> <filesize>"` by splitting on spaces, read that many bytes,
XOR them back, write them to a new file, and repeat until the input is
exhausted.

### Bugs and limitations in the original implementation

| # | Issue | Where | Fix in PackX |
|---|-------|-------|--------------|
| 1 | `Header.split(" ")` breaks for filenames containing spaces (`Tokens[0]` is only the first word) | unpacking | `PackFormat.parseHeader` splits on the **last** space instead, so `"my file.txt 120"` parses correctly. Byte layout is unchanged, so old archives (with space-free names) still unpack correctly. |
| 2 | `fiobj.read(Buffer, 0, FileSize)` is trusted to read the full length in one call, which `InputStream` never guarantees | unpacking | `PackFormat`/`UnpackingService` loop until the requested length is fully read, or fail loudly if the stream ends early. |
| 3 | Extracted files' `FileOutputStream`s are never explicitly closed | unpacking | Every stream is opened in `try-with-resources`. |
| 4 | `new byte[FileSize]` allocates the entire file in memory at once | both | PackX streams in fixed 8 KB chunks in both directions; memory use is constant regardless of file size. |
| 5 | The XOR key is hardcoded inline wherever encryption happens | both | Isolated behind `EncryptionService`, a one-method interface that can be swapped for AES without touching packing/unpacking logic. |

None of these fixes change the on-wire format. A file packed by the original
Java console program can be unpacked by PackX, and a file packed by PackX
(with space-free filenames) can be unpacked by the original program.

## 4. Architecture

```
Browser (React)
     │  REST / multipart
     ▼
Spring Boot API
 ├── controller/   HTTP layer only — no business logic
 ├── service/      PackingService, UnpackingService, EncryptionService,
 │                 FileStorageService, OperationService
 ├── repository/   MongoRepository for Operation metadata
 ├── model/        Operation document
 ├── dto/          Response shapes returned to the frontend
 ├── exception/    Typed exceptions + @RestControllerAdvice handler
 └── util/         PackFormat — header encode/decode, readFully, XOR
     │
     ├── MongoDB          → operation metadata only
     └── Filesystem        → uploaded / packed / extracted file bytes
         (storage/uploads, storage/output, storage/extracted)
```

Large binaries never go into MongoDB — only paths and metadata do, per the
spec's requirement.

## 5. Technology Stack

**Backend:** Java 17, Spring Boot 3.3, Spring Web, Spring Data MongoDB,
Maven, JUnit 5 + Mockito.
**Frontend:** React 18, Vite, React Router, Axios, plain CSS (no UI
framework — a small hand-built design system in `src/index.css`).
**Database:** MongoDB (local via Docker, or MongoDB Atlas).
**Infra:** Docker, Docker Compose, Nginx (frontend static hosting + API
reverse proxy in the container build).

## 6. Project Structure

```
PackX/
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/packx/backend/
│       ├── PackxApplication.java
│       ├── config/        StorageProperties, CorsConfig
│       ├── controller/    PackController, UnpackController,
│       │                  OperationController, DashboardController
│       ├── service/       PackingService, UnpackingService,
│       │                  EncryptionService, FileStorageService,
│       │                  OperationService
│       ├── model/         Operation, OperationType, OperationStatus
│       ├── repository/    OperationRepository
│       ├── dto/           OperationResponse, DashboardStatsResponse,
│       │                  ErrorResponse
│       ├── exception/     GlobalExceptionHandler + typed exceptions
│       └── util/          PackFormat
│   └── src/test/java/…    Unit + round-trip tests
├── frontend/
│   ├── package.json
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       ├── components/    FileDropzone, FileList, Navbar, StatsCard,
│       │                  OperationCard, LoadingSpinner
│       ├── pages/          Dashboard, Pack, Unpack, History,
│       │                  OperationDetails, About
│       ├── services/api.js
│       ├── hooks/useToast.jsx
│       ├── utils/format.js
│       ├── App.jsx, main.jsx, index.css
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

## 7. REST API

All endpoints are under `/api`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/pack` | multipart: `files` (one or more), `packName` (string) → `OperationResponse` |
| POST | `/api/unpack` | multipart: `file` (one `.pack`) → `OperationResponse` |
| GET | `/api/operations` | list all operations, newest first |
| GET | `/api/operations/{id}` | one operation's full detail |
| GET | `/api/operations/{id}/download` | streams the result file (packed archive, single extracted file, or ZIP) |
| DELETE | `/api/operations/{id}` | delete an operation record |
| GET | `/api/dashboard/stats` | aggregate counts + 5 most recent operations |

Example error response (from `GlobalExceptionHandler`):

```json
{
  "success": false,
  "message": "Corrupted packed file: malformed header \"???\"",
  "timestamp": "2026-09-03T10:12:00Z"
}
```

Example pack request (curl):

```bash
curl -X POST http://localhost:8080/api/pack \
  -F "files=@notes.txt" \
  -F "files=@todo.txt" \
  -F "packName=my-archive"
```

## 8. MongoDB Configuration

One collection, `operations`, indexed on `operationType`, `status`, and
`createdAt`. Example document:

```json
{
  "_id": "66f...",
  "operationType": "PACK",
  "inputFileName": "notes.txt, todo.txt",
  "outputFileName": "a1b2c3d4_my-archive.pack",
  "storedFilePath": "/app/storage/output/a1b2c3d4_my-archive.pack",
  "numberOfFiles": 2,
  "totalSize": 2457600,
  "status": "SUCCESS",
  "createdAt": "2026-09-03T10:00:00Z",
  "completedAt": "2026-09-03T10:00:01Z",
  "processingTimeMs": 812
}
```

Point `MONGODB_URI` at either a local container or an Atlas cluster — the
application code doesn't change either way.

## 9. Environment Variables

See `.env.example` for the full list with defaults. Nothing is hardcoded:
MongoDB URI, storage paths, CORS origins, upload size limits, and the
allowed file extension list are all configurable.

## 10. Local Development (without Docker)

**Start MongoDB** (skip if you're pointing at Atlas):

```bash
docker run -d --name packx-mongo -p 27017:27017 mongo:7
```

**Configure environment variables:**

```bash
cp .env.example .env
# edit .env if you're using Atlas or non-default ports
```

**Start the backend:**

```bash
cd backend
export MONGODB_URI=mongodb://localhost:27017/packx
mvn spring-boot:run
```

The API comes up on `http://localhost:8080`.

**Start the frontend:**

```bash
cd frontend
npm install
npm run dev
```

The app comes up on `http://localhost:5173` and proxies `/api` calls to
`http://localhost:8080` (see `vite.config.js`).

## 11. Using Pack

1. Open the **Pack Files** page.
2. Drag `.txt` files onto the dropzone, or click **Browse files**.
3. Remove any file you don't want with its **remove** link.
4. Give the archive a name and click **Pack Files**.
5. Click **Download Packed File** once it completes.

## 12. Using Unpack

1. Open the **Unpack Files** page.
2. Drag a `.pack` file onto the dropzone (or browse for it).
3. Click **Unpack**.
4. Click **Download** — a single file downloads directly; multiple files
   download as a ZIP.

## 13. Running Tests

```bash
cd backend
mvn test
```

Covers: XOR symmetry, header build/parse (including filenames with
spaces), corrupted-header rejection, `readFully` behavior on truncated
streams, and a full pack → unpack round trip verifying byte-identical
recovery of two files (one with a space in its name).

> Note: this sandbox environment could not reach Maven Central to actually
> execute `mvn test`, since the build tool network allowlist only covers
> a handful of package registries. The test suite is written and ready to
> run in a normal development environment with internet access.

## 14. Running with Docker

```bash
cp .env.example .env
docker compose up --build
```

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`
- MongoDB: `localhost:27017`

To use MongoDB Atlas instead of the bundled container, set `MONGODB_URI` in
`.env` to your Atlas connection string and remove (or ignore) the `mongodb`
service in `docker-compose.yml`.

## 15. Security Considerations

- **The XOR cipher is not secure encryption.** It's a single-byte key
  applied byte-by-byte — trivially reversible, and kept only for
  compatibility with the original console program. Do not use PackX in its
  current form to protect sensitive or confidential data. `EncryptionService`
  exists specifically so this can be replaced with AES-256-GCM later without
  touching the packing or unpacking logic.
- **Path traversal:** every filename read from a packed-file header or an
  upload is sanitized to its base name and re-resolved against the target
  directory in `FileStorageService.resolveSafely`, which refuses anything
  that would land outside that directory.
- **Uploads are treated as untrusted input** throughout: extension
  allow-listing, duplicate-name rejection, empty-file rejection, and header
  validation all happen before any file is written to disk.
- **No secrets are hardcoded.** MongoDB URI, storage paths, and CORS origins
  all come from environment variables (see `.env.example`).

## 16. What's Deliberately Not Included Yet

Authentication (Spring Security + JWT + per-user history) was scoped out of
this first build to keep the initial delivery focused and correct, per the
spec's own fallback ("if authentication makes the initial implementation
excessively complex, structure the project so it can be added cleanly").
The architecture supports it directly:

- Add a `User` document + `UserRepository`.
- Add a `userId` field to `Operation` and filter all `OperationRepository`
  queries by the authenticated user.
- Add Spring Security + a JWT filter; hash passwords with BCrypt.
- Add `/api/auth/register` and `/api/auth/login` controllers.

## 17. Future Improvements

- AES-256-GCM encryption (swap-in via `EncryptionService`)
- Password-protected archives
- Support for additional file types beyond `.txt`
- Cloud storage backend (S3-compatible) via a `FileStorageService`
  implementation swap
- Real-time processing progress via Server-Sent Events
- File expiration and shareable download links
- Admin dashboard, storage quotas, audit logs
- Background job queue for large archives instead of synchronous processing
- ZIP/GZIP compression before XOR (or instead of it, once AES lands)

---

Built from an original console-based packing/unpacking implementation,
preserving its exact wire format while replacing the console I/O with a
proper layered web application.
