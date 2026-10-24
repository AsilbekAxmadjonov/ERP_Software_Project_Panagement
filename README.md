# ERP

ERP system built by Team Kaizen. It covers accounting (with period lock), sales (with credit notes), purchasing, inventory (with low-stock alerts), partners and user login with roles. The UI follows Odoo's look and workflow: app launcher, list/kanban views with search, filters and grouping, and form sheets with status bars. The planned scope is in `ERP_Requirements_Team_Kaizen.pdf`, and the sprint plan is in [`docs/sprint-1-2-plan.md`](docs/sprint-1-2-plan.md).

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3.5 (Web, Data JPA, Security, Validation) |
| Auth | JWT (jjwt 0.12.6) |
| Database | PostgreSQL 16 in Docker. H2 file database (PostgreSQL mode) when run without Docker |
| Frontend | React 19, TypeScript, Vite 6, Tailwind CSS 4, React Router 7 |
| Tests | JUnit 5, Spring Security Test, ArchUnit |

## Project structure

```text
.
├── pom.xml                      Maven build (backend)
├── src/main/java/org/example/erp
│   ├── ErpApplication.java
│   ├── composition/             Cross-module workflows (adapters, listeners, controllers)
│   └── modules/
│       ├── identity/            Users, login, roles
│       ├── partners/            Customers and suppliers
│       ├── accounting/          Chart of accounts, journal, ledgers, trial balance
│       ├── inventory/           Products and stock
│       ├── sales/               Invoices, credit notes, customer payments, sales report
│       └── purchasing/          Purchase orders, goods receipts, vendor payments
├── src/main/resources           application.yml, application-postgres.yml
├── src/test                     ArchitectureTest, SalesFlowIntegrationTest, module tests
├── frontend/                    React app (one folder per module under src/modules)
├── docs/                        Planning documents
└── tools/jira.py                Jira helper script
```

### Architecture

The backend is a modular monolith. Each module under `modules/` is split into four layers:

```text
presentation  ->  application  ->  domain
infrastructure ->  application  ->  domain
```

- **domain**: business rules and entities, with no framework code.
- **application**: actions (use cases), commands, DTOs, enums and contracts.
- **infrastructure**: persistence and other adapters.
- **presentation**: REST controllers.

Modules do not depend on each other. Anything that needs more than one module lives in `composition/`. For example, a sales invoice reduces stock and posts to the ledger through adapters and listeners there. `ArchitectureTest` (ArchUnit) enforces these rules, so it must stay green.

The frontend mirrors the backend: `frontend/src/modules/<module>/` holds that module's `api.ts`, `types.ts` and `pages/`, and `frontend/src/shell/` holds the app shell.

## Quick start with Docker (recommended)

Requires only Docker. On macOS, Linux or Windows (Git Bash/WSL):

```bash
./setup.sh
```

This creates `.env` on first run, then builds and starts PostgreSQL, the backend and the frontend. It also creates the basic users and starter data. It is safe to run again. Open <http://localhost:8088> and sign in as `finadmin` / `kok-7070`. The script also creates `manager` / `Manager123!` and `accountant` / `Account123!`. Change these in `.env` before any real deployment.

- Stop: `docker compose down`
- Wipe all data: `docker compose down -v`
- If the script is not executable on macOS or Linux, run `chmod +x setup.sh`.

## Getting started (without Docker)

### Prerequisites

- JDK 21
- Maven 3.9+
- Node.js 20+ and npm
- PostgreSQL (only for the `postgres` profile)

### Run the backend

```bash
mvn spring-boot:run
```

The API starts on <http://localhost:8080>. By default it uses an H2 file database at `./data/erp`, with the schema created automatically (`ddl-auto: update`).

To use PostgreSQL instead:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The app starts on <http://localhost:5173>. Vite proxies `/api` to `http://localhost:8080`.

## Configuration

| Variable | Purpose | Default |
|---|---|---|
| `JWT_SECRET` | JWT signing secret, at least 32 characters | Dev-only value. **Always override outside local development.** |
| `ERP_ADMIN_USERNAME` | Bootstrap admin username | empty |
| `ERP_ADMIN_PASSWORD` | Bootstrap admin password | empty |
| `DB_URL` | PostgreSQL URL (`postgres` profile) | `jdbc:postgresql://localhost:5432/erp` |
| `DB_USER` | PostgreSQL user (`postgres` profile) | `erp` |
| `DB_PASSWORD` | PostgreSQL password (`postgres` profile) | `erp` |

Other settings are in `src/main/resources/application.yml`: JWT lifetime is `app.jwt.ttl-hours` (12 hours) and the allowed CORS origin is `app.cors-origins`.

## Testing and checks

```bash
# Backend tests, including architecture rules
mvn test

# Frontend
cd frontend
npm run types     # TypeScript check
npm run lint      # ESLint
npm run build     # type check + production build
```

## Conventions

- Keep module boundaries intact. Cross-module logic goes in `composition/`.
- Controllers call application actions and return DTOs, never domain entities or JPA entities.
- Do not commit secrets, `.env` files, the `data/` database or log files.
- Coding-agent guidance is in [`AGENTS.md`](AGENTS.md). It was written for a Laravel/PHP starter, so its stack-specific parts (Composer, Pest, Inertia) do not apply to this Java/React codebase. The architecture principles do.
