# AGENTS.md

This file provides guidance to coding agents working in this repository.

## Scope and precedence

- These instructions apply to the entire repository unless a nested `AGENTS.md` provides more specific guidance for a subdirectory.
- Any database structure diagram or ERD work must also follow `database/AGENTS.md`. That file governs Draw.io diagram artifacts only, not migrations, factories, schemas, or database implementation.
- Direct user instructions take precedence over this file. More specific repository instructions take precedence over general ones.
- Treat examples in this file as illustrations of conventions, not as proof that a class, enum, module, route, or tool exists. Inspect the repository before using an example name.
- When this file disagrees with executable configuration such as `composer.json`, `package.json`, migrations, or CI workflows, follow the executable configuration and report the discrepancy.
- Do not assume an optional tool is available. Use the closest safe repository-native alternative when it is not.

## Working process

1. Read the relevant code, tests, configuration, and any nested `AGENTS.md` files before editing.
2. Check the working tree and preserve existing user changes. Do not revert or overwrite unrelated work.
3. Make the smallest coherent change that satisfies the request and follows nearby patterns.
4. Add or update meaningful tests for behavior changes.
5. Run the narrowest relevant checks first, then broader checks when practical.
6. Report what changed, which checks passed, and anything that could not be verified.

Do not commit, push, rewrite Git history, delete data, or run destructive database commands unless the user explicitly requests it.

## Development Commands

Treat scripts in `composer.json` and `package.json` as the source of truth. If these documented commands drift from the actual scripts, use the actual scripts and update this section.

```bash
# Install PHP dependencies
composer install

# Install frontend dependencies
npm install

# Start development server (runs Laravel, queue worker, Pail logs, and Vite concurrently)
composer dev

# Run all tests (includes lint check)
composer test

# Run specific test file
./vendor/bin/pest tests/Feature/SomeTest.php

# Run specific test by name
./vendor/bin/pest --filter="test name"

# Run root cross-module architecture tests
./vendor/bin/pest tests/Architecture

# Lint PHP code
composer lint

# Lint TypeScript/React
npm run lint

# Format TypeScript/React
npm run format

# Type check TypeScript
npm run types

# Generate TypeScript types from PHP Enums/DTOs
npm run types:generate

# Recreate the local/test database (destructive; only run when explicitly requested)
php artisan migrate:fresh
```

## Code Intelligence

When LSP tools are available, prefer them over text search for code navigation:
- `goToDefinition` / `goToImplementation` to jump to source
- `findReferences` to see all usages across the codebase
- `workspaceSymbol` to find where something is defined
- `documentSymbol` to list all symbols in a file
- `hover` for type info without reading the file
- `incomingCalls` / `outgoingCalls` for call hierarchy

Before renaming or changing a function signature, find all call sites with `findReferences` or, when LSP is unavailable, `rg`.

Use `rg` for text and pattern searches such as comments, strings, configuration values, and as the fallback when LSP is unavailable.

After writing or editing code, check LSP diagnostics when available and run the relevant formatter, linter, type checker, or test command. Fix type errors and missing imports before moving on.

## Tech Stack

- **Backend**: Laravel 12, PHP 8.2+, Pest for testing
- **Frontend**: React 19, Inertia.js 3, TypeScript, Tailwind CSS 4, shadcn/ui (Radix primitives)
- **Mandatory Laravel packages**: `spatie/laravel-activitylog` and `spatie/laravel-translatable`
- **Architecture**: DDD modular monolith with autonomous bounded contexts under `modules/core/`

## Mandatory Baseline Packages

Every project created from this starter must include:

- `spatie/laravel-activitylog` for application audit/activity history
- `spatie/laravel-translatable` for translated Eloquent attributes
- `inertiajs/inertia-laravel` on the latest compatible stable v3 release
- `@inertiajs/react` on the latest compatible stable v3 release
- `@inertiajs/vite` on the latest compatible stable v3 release for module page discovery and Vite/SSR integration

Do not hardcode package versions in this instruction file. On the first project setup, before installing any mandatory package:

1. Read the actual PHP, Laravel, React, Node, and Vite constraints from the root manifests.
2. Look up the latest stable release, requirements, upgrade guide, and migrations on the package's official documentation, repository, Packagist, or npm registry page.
3. Select the newest stable version compatible with all project platform and peer constraints, not merely the highest published version.
4. Use Composer/npm dependency resolution or a dry run to confirm the complete dependency graph and frontend peer requirements before changing lock files.
5. Install the compatible releases, commit the resulting manifest constraints and lock files, and report the resolved versions.

Do not silently raise the project's PHP, Laravel, React, Node, or Vite minimum just to install a newer package release. A platform upgrade is a separate decision. Do not use development branches, abandoned packages, or an older exact version when a newer compatible stable release exists.

Inertia is intentionally pinned to major version 3. Resolve the newest compatible stable `3.x` releases during initial setup; do not install Inertia 2, and do not adopt a later major without an explicit architecture upgrade decision.

### Package-First Implementation

Do not reinvent behavior already provided by an installed package. Before implementing functionality that overlaps with a dependency:

1. Confirm the exact installed package version from `composer.lock` or the frontend lock file.
2. Read the official documentation and upgrade notes for that version. Inspect the package source when the documented behavior or extension point is unclear.
3. Search for existing configuration options, methods, traits, interfaces, events, observers, middleware, validation rules, casts, commands, publishable resources, and supported extension points.
4. Prefer the package's supported API and configuration over custom duplicate services, helpers, traits, database logic, or event plumbing.
5. Use a small Infrastructure adapter only when required to preserve a Domain/Application port, module extractability, or transport independence. The adapter delegates to the package; it must not reimplement the package internally.
6. If the package genuinely lacks the required behavior, document the missing capability and implement only the smallest extension around its public API. Do not copy vendor source or depend on undocumented internals.
7. Test the application's behavior and integration contract rather than duplicating the dependency's own test suite.

This rule applies especially to `spatie/laravel-activitylog` and `spatie/laravel-translatable`: explore their installed-version capabilities before adding custom activity tables, audit observers, translation storage, locale fallback, serialization, or model-attribute helpers.

It also applies to Inertia.js: inspect the installed v3 documentation for visits, forms, standalone HTTP requests, optimistic updates, interceptors, partial reloads, deferred/merged props, prefetching, layout props, SSR, error handling, and page resolution before creating custom request hooks, state containers, navigation abstractions, or Vite discovery code.

### Package Architecture Rules

- Both Spatie packages are Infrastructure concerns. Domain and Application code must not import Spatie package classes or traits.
- `spatie/laravel-activitylog` is host-application audit infrastructure, not routine debug logging. Root composition/listener adapters record audit-worthy module Integration Events without coupling modules to the package.
- The shared `activity_log` table is owned by the host application's technical Infrastructure. Modules must not add cross-module foreign keys to it or query it from Domain/Application code.
- Never place secrets, credentials, tokens, or unnecessary sensitive/regulated data in activity-log descriptions or properties. Define explicit logged fields and redact sensitive changes.
- `spatie/laravel-translatable` is for translated Eloquent attributes stored by Infrastructure models. It does not replace Laravel/frontend translation of static user-interface copy.
- A module that uses `spatie/laravel-translatable` on one of its Infrastructure Eloquent models must also declare the package in its own `composer.json`; relying only on the root dependency would break standalone extraction.
- Module migrations own their translated JSON columns. Infrastructure maps translated persistence values to Domain values and Application DTOs without exposing the package trait outside Infrastructure.
- Inertia's Laravel adapter is a Presentation dependency. Module controllers and frontend packages that use it must declare the appropriate backend dependency or frontend peer dependency so standalone extraction does not rely on an undeclared root installation.
- Inertia types, hooks, router objects, responses, and page props must not enter Domain or Application code.
- Test project behavior around audit entries, redaction, locale fallback, validation, and translated persistence. Do not duplicate the packages' own implementation tests.

---

## General code instructions

- Don't generate comments above methods or code blocks when the code is obvious. Add variable docblocks only when static analysis cannot infer a necessary type. Comments should explain a non-obvious reason or constraint, not restate the code.
- For new features, you MUST generate Pest automated tests.
- For library documentation, use Laravel Boost `search-docs` when available. Otherwise use Context7 when available, then fall back to the library's official documentation. Verify guidance against the installed dependency version.
- Avoid unnecessary defensive coding: no redundant null checks, no fallback values for things that can't be null, no extra if-statements for impossible conditions. Trust the type system and database constraints. Don't sprinkle `??` on every array access — if the schema guarantees the key exists, access it directly. Use `Arr::get()` instead of `??` chains whenever the array itself may be null or the key may be absent. Common cases: nullable JSON-cast model columns (`Arr::get($model->context, 'deviation_type')` not `($model->context ?? [])['deviation_type'] ?? null`), nested access (`Arr::get($data, 'nested.deep')` not `$data['nested']['deep'] ?? null`), and external data where structure isn't guaranteed.
- Use `null` for absent or optional domain values; do not use an empty string as a sentinel. Preserve an empty string only when it is a meaningful validated value. Remember that Laravel normally converts empty request strings to `null`.
- **No magic-value sentinels**: Never use `?? 'unknown'`, `?? 0`, `?? ''`, `?? []`, or any magic-string/empty-object fallback that silently changes runtime meaning. If the value must exist, access it directly and let the error surface. If the value is genuinely optional, use `null`. If a default is needed, define it once in the schema (migration default, config, or enum) — not inline at every access site.
- **Enum boundary parsing**: Infrastructure and Presentation adapters convert external strings from database JSON, requests, and APIs into Domain or Application Enums. Use `from()` when invalid input is exceptional, `tryFrom()` when absence is a valid boundary result, or an Enum named constructor when an explicit `Unknown` case is part of the domain. Do not scatter inline fallback defaults or provider-specific parsing through Domain code.
- User-facing text in Presentation and frontend code should be wrapped in `__('...')` for translation support. Use the actual text as the key (e.g., `__('Save changes')`), not translation keys like `__('buttons.save')`. Domain and Application code must not translate or choose presentation copy.

---

## PHP instructions

- In PHP, use `match` operator over `switch` whenever possible. But when selecting multiple related values based on a type, use a config array lookup instead of multiple separate `match` expressions:

```php
// GOOD - Single lookup for related values
$config = [
    ExportFormat::Csv->value => ['serializer' => 'serializeCsv', 'message' => __('CSV export created.')],
    ExportFormat::Json->value => ['serializer' => 'serializeJson', 'message' => __('JSON export created.')],
][$format->value];

// BAD - Multiple match expressions for related values
$serializer = match ($format) { ExportFormat::Csv => 'serializeCsv', ExportFormat::Json => 'serializeJson' };
$message = match ($format) { ExportFormat::Csv => __('CSV...'), ExportFormat::Json => __('JSON...') };
```

- **Never use `default` in `match` when matching on enums.** List all cases explicitly so that adding a new enum case causes an `UnhandledMatchError` — forcing you to handle it everywhere. A `default` silently swallows new cases:

```php
// GOOD - Explicit cases, fails loudly when new case added
$serialized = match ($format) {
    ExportFormat::Csv => $this->serializer->serializeCsv($data),
    ExportFormat::Json => $this->serializer->serializeJson($data),
    ExportFormat::Xml => throw new \InvalidArgumentException('XML export is not yet supported.'),
};

// BAD - default hides unhandled cases
$serialized = match ($format) {
    ExportFormat::Csv => $this->serializer->serializeCsv($data),
    default => throw new \InvalidArgumentException('Unsupported type.'),
};
```
- Always use an Enum value as the default in a migration when column values come from that Enum. Always cast the column to the Enum type in the model.
- In Presentation code, don't create temporary variables like `$currentUser = auth()->user()` if that variable is used only one time.
- Always use Enum where possible instead of hardcoded string values, if Enum class exists. For example, in Blade files, and in the tests when creating data if field is casted to Enum then use that Enum instead of hardcoding the value.
- When multiple cases need to be grouped (e.g., checking if status is one of several values), add static grouping methods to the Enum instead of hardcoding case lists throughout the codebase. Example:
```php
enum OrderStatus: string
{
    case Pending = 'pending';
    case Processing = 'processing';
    case Shipped = 'shipped';
    case Delivered = 'delivered';
    case Cancelled = 'cancelled';
    case Refunded = 'refunded';

    /** @return array<self> */
    public static function paidStates(): array
    {
        return [self::Processing, self::Shipped, self::Delivered];
    }

    /** @return array<self> */
    public static function terminalStates(): array
    {
        return [self::Delivered, self::Cancelled, self::Refunded];
    }

    public function isTerminal(): bool
    {
        return in_array($this, self::terminalStates(), true);
    }
}
```
- **Application DTOs should use constructor parameters, not `fromArray()` static methods. Use `readonly class` with constructor promotion:**
```php
// Good
readonly class BulkDataUploadDTO
{
    public function __construct(
        public string $userId,
        public array  $dataGroups,
    ) {}
}

// Bad - don't create fromArray() methods
readonly class BulkDataUploadDTO
{
    public function __construct(...) {}

    public static function fromArray(array $data): self
    {
        return new self(...);
    }
}
```
- Avoid unnecessary logging. Domain code never logs, and Application code exposes outcomes rather than using Laravel logging. Log only at Infrastructure or Presentation boundaries when it provides meaningful debugging value, such as errors or important state changes; do not log routine starts or request receipt.
- **Never create database seeders** for test data. Test data should be created in tests using factories. Seeders bloat the codebase, go stale, and create data format mismatches with the actual schema expectations.
- **Never use `php artisan tinker` to verify browser test results.** When testing via browser, use the browser itself — screenshots, snapshots, clicking through the UI. Tinker bypasses the actual user flow and misses UI bugs, state issues, and rendering problems.

---

## DDD Modular Architecture

This project uses a modular monolith organized as autonomous Domain-Driven Design bounded contexts.

- **Bounded-context modules** live under `modules/core/`. Each module owns one business capability and all of its backend, frontend, persistence, configuration, and tests.
- **Reusable technical packages** live under `packages/`. Packages are not bounded contexts and must not contain application-specific domain concepts.
- **Application composition** lives under `app/Composition/`. It is the only place allowed to coordinate more than one module.
- Do not create a shared domain kernel. Domain entities, enums, value objects, DTOs, and business rules belong to exactly one bounded context.

### Module Structure

Use this structure for each bounded context. Create only the subdirectories the module actually needs.

```text
modules/core/{module-name}/
├── composer.json
├── package.json
├── src/
│   ├── {ModuleName}ServiceProvider.php
│   ├── Domain/
│   │   ├── Entities/
│   │   ├── ValueObjects/
│   │   ├── Enums/
│   │   ├── Events/
│   │   ├── Exceptions/
│   │   ├── Contracts/
│   │   └── Services/
│   ├── Application/
│   │   ├── Contracts/
│   │   ├── Actions/
│   │   ├── Commands/
│   │   ├── Queries/
│   │   ├── DTOs/
│   │   ├── Enums/
│   │   └── Events/
│   ├── Infrastructure/
│   │   ├── Persistence/
│   │   │   ├── Models/
│   │   │   └── Repositories/
│   │   ├── Integrations/
│   │   ├── Messaging/
│   │   │   └── LaravelEvents/
│   │   ├── Queue/
│   │   │   └── Jobs/
│   │   └── Observers/
│   └── Presentation/
│       ├── Http/
│       │   ├── Controllers/
│       │   ├── Requests/
│       │   └── Middleware/
│       └── Console/
│           └── Commands/
├── database/
│   ├── migrations/
│   └── factories/
├── config/
├── routes/
│   ├── web.php
│   ├── api.php
│   └── console.php
├── resources/
│   ├── js/
│   │   ├── pages/
│   │   ├── components/
│   │   └── hooks/
│   ├── views/
│   └── lang/
└── tests/
    ├── Pest.php
    ├── TestCase.php
    ├── Unit/
    ├── Feature/
    └── Architecture/
```

Each module-level `composer.json` owns its `Modules\{ModuleName}\` PSR-4 mapping for `src/` and its test namespace mapping for `tests/`. A module with frontend code also owns a `package.json` registered through the root workspace. Reusable packages under `packages/` own their namespaces and dependencies through their Composer and/or frontend package manifests; do not impose a project-specific namespace on every package.

Application composition and reusable technical packages use these smaller structures:

```text
app/Composition/
├── Workflows/
├── Listeners/
├── DTOs/
└── Providers/

packages/{package-name}/
├── composer.json and/or package.json
├── src/ and/or resources/
└── tests/
```

The root application may register module packages and service providers, but implementation classes remain inside the owning module. `{ModuleName}ServiceProvider` is the module's wiring boundary: it may bind ports to adapters and load that module's configuration, migrations, routes, views, and translations, but it must not contain business logic.

### Layer Responsibilities and Dependency Direction

```text
Presentation ─────► Application ─────► Domain
Infrastructure ───► Application ─────► Domain
Infrastructure ──────────────────────► Domain

app/Composition ──► public Application contracts of multiple modules
```

- **Domain** contains business invariants, entities, value objects, domain services, domain events, repository ports, and domain exceptions. It must not depend on Laravel, Eloquent, HTTP, queues, the database, external-service SDKs, another layer, or another module.
- **Application** contains use cases and orchestration for one module. Actions, commands, queries, public use-case contracts, and DTOs belong here. It may depend on Domain but never on Infrastructure or Presentation. Keep it framework-independent.
- **Infrastructure** contains adapters: Eloquent models, repository implementations, external API clients, filesystem implementations, queue jobs, and observers. It may implement Domain or Application contracts. It must not contain business decisions.
- **Presentation** contains delivery mechanisms such as controllers, form requests, middleware, console commands, Inertia responses, and API resources. It may depend on Application contracts and DTOs, but not on Domain entities, Eloquent models, or Infrastructure implementations.
- **Module Service Provider** performs dependency injection and resource registration for its own module only. It is the permitted internal wiring boundary between layers.
- **Application Composition** may import the public Application contracts, DTOs, and integration events of multiple modules. It contains cross-module workflow orchestration only, not domain rules or persistence logic.

Use Actions for ordinary use cases. Add Commands, Queries, and dedicated handlers only when the use case benefits from that separation; do not add CQRS ceremony to simple behavior.

### Domain Modeling Rules

- Name modules and Domain types using the bounded context's ubiquitous language. Do not use generic names such as `Manager`, `Helper`, or `Common` for domain concepts.
- Aggregate roots enforce invariants and control mutations to the aggregate. Do not expose public setters or let callers update aggregate state piecemeal.
- Value objects are immutable and validate themselves at construction. Use them when a primitive has domain meaning or invariants; do not wrap every scalar mechanically.
- Domain behavior must not read the current user, clock, random generator, environment, or global state. Pass required values explicitly or depend on a Domain/Application port implemented by Infrastructure.
- Domain services contain stateless business behavior that does not naturally belong to one entity or value object. Application orchestration is not a Domain service.
- Domain events describe facts that already happened and use past-tense names. Keep internal Domain events inside the module.
- Repository ports are defined around aggregates and use Domain language. Do not expose Eloquent builders, collections, pagination objects, or persistence records through a Domain contract.
- Eloquent models are persistence records, not Domain entities. Do not place business invariants, cross-aggregate workflows, or transport serialization on them.
- Do not create interfaces for every class. Interfaces are for Domain/Application ports and intentionally public Application contracts.

### Events and Message Broker Portability

Events are documented at two different boundaries and must not be conflated:

- **Domain Events** live in `Domain/Events/`. They are pure, immutable PHP facts used only inside the owning bounded context. They contain no Laravel event traits, Eloquent models, queue metadata, broker routing keys, or serialization concerns.
- **Integration Events** live in `Application/Events/`. They are immutable, transport-neutral public contracts emitted when another application workflow may need to react. Domain Events may be translated into Integration Events by the Application layer.

Application code publishes Integration Events only through a module-owned port such as `Application/Contracts/Messaging/IntegrationEventPublisher`. It must not call Laravel's dispatcher, a queue facade, AMQP, RabbitMQ, or a broker client directly.

```php
interface IntegrationEventPublisher
{
    public function publish(IntegrationEvent $event): void;
}
```

The current/default transport is a **[Laravel Events](https://laravel.com/docs/12.x/events) Infrastructure adapter**. The module Service Provider binds `IntegrationEventPublisher` to that adapter, which uses Laravel's event dispatcher for delivery inside the monolith. Laravel event classes, listeners, queue interfaces, discovery, and framework traits remain inside `Infrastructure/Messaging/LaravelEvents/` or the root composition layer; they must not leak into Domain or Application.

RabbitMQ is not a current dependency or runtime requirement. Do not add an AMQP client, exchanges, queues, RabbitMQ configuration, outbox/inbox tables, or RabbitMQ-specific code until a module is extracted into another Laravel application and that deployment actually requires a broker.

Every Integration Event has a stable envelope containing:

- `eventId`: globally unique message identifier
- `eventName`: stable domain-facing name that is not a PHP class name
- `schemaVersion`: positive integer version of the serialized contract
- `occurredAt`: immutable timestamp serialized in an explicit standard format
- `correlationId`: nullable identifier connecting one workflow
- `causationId`: nullable identifier of the command or event that caused it
- `payload`: documented scalar, list, or nested DTO data with stable camelCase keys

Never serialize Eloquent models, Domain entities, service-container objects, closures, PHP object serialization, credentials, or undocumented arbitrary arrays into an Integration Event. Prefer identifiers and immutable snapshots required by consumers.

#### Future RabbitMQ Extraction

When an extracted module needs RabbitMQ, preserve its Domain, Application Actions, public contracts, and Integration Events unchanged. The extraction work should be limited to:

1. Add a RabbitMQ implementation of `IntegrationEventPublisher` under the extracted application's Infrastructure layer, or install a generic RabbitMQ technical package.
2. Replace the Service Provider binding from the Laravel Events adapter to the RabbitMQ adapter.
3. Add broker connection, exchange, queue, routing, retry, and dead-letter configuration through environment-backed Infrastructure config.
4. Add an outbox relay when a database change and message publication must be reliable as one local operation.
5. Add inbox/idempotency storage for consumers and deduplicate by `eventId`.
6. Add RabbitMQ adapter, serialization-contract, publisher-confirm, consumer-acknowledgement, retry, and failure-path tests.

For RabbitMQ delivery:

- Assume at-least-once delivery. Consumers must be idempotent; do not promise exactly-once delivery.
- Store an outgoing Integration Event in the extracted module's outbox within the same local database transaction as the business mutation when atomic publication is required. Publish pending records after commit.
- Use durable delivery where required and [publisher confirms plus explicit consumer acknowledgements](https://www.rabbitmq.com/docs/confirms). Consumers acknowledge only after successful processing.
- Retries, backoff, negative acknowledgements, dead-letter routing, connection recovery, prefetch, exchange/queue declarations, and routing keys remain Infrastructure concerns.
- Preserve ordering only when a documented business invariant requires it, using an explicit aggregate or partition key. Do not assume global broker ordering.
- Breaking payload changes create a new `schemaVersion`. Consumers tolerate documented additive changes and explicitly support the versions they accept.

The RabbitMQ migration is not considered minimal if it changes Domain behavior, Application use-case signatures, public contracts, or event meaning. Only transport, reliability Infrastructure, bindings, deployment configuration, and transport tests should change.

Another module must never type-hint or register a listener for an Integration Event class owned by a sibling module. In the monolith, `app/Composition/` receives the event and invokes the consumer module's public Application contract. After extraction, the new host application's composition/messaging adapter performs the same translation.

### Module Autonomy and Public APIs

- Modules MUST NOT depend on one another. This includes PHP imports, type hints, inheritance, container resolution, static calls, event listeners, Eloquent relationships, table queries, migrations, foreign keys, frontend imports, and runtime class-name strings.
- A module exposes only its `Application/Contracts`, `Application/DTOs`, transport-safe `Application/Enums`, and documented integration events to `app/Composition/`. Domain and Infrastructure types are never cross-module APIs.
- A module owns its database tables. Store another module's identifier as an opaque scalar only when an application workflow requires correlation; do not add a database foreign key or Eloquent relationship across modules.
- A module owns its feature frontend. Module frontend code must not import another module's pages, components, hooks, state, or generated internal types.
- Root `resources/js/` is limited to the application shell, composition-level pages, and shell layouts. Reusable UI primitives belong in a neutral package under `packages/`; feature-specific frontend code belongs to its module.
- Modules may depend on framework and third-party packages declared in their own Composer or frontend manifest. A generic technical package may live under `packages/`, but it must not import a bounded-context module or contain shared domain concepts.
- Do not hide coupling behind the service container, global helpers, polymorphic class strings, database views, or untyped event payloads.
- Every module must boot and pass its own tests without loading another module. If a use case needs another module, orchestrate it from `app/Composition/`.

### Extractability and Standalone Installation

Every bounded-context module must be installable in a fresh compatible Laravel application as an independent Composer package without copying code from another module or the current host application.

- The module's `composer.json` declares its package name, PHP/Laravel constraints, runtime dependencies, PSR-4 mappings, test mappings, and Laravel package discovery metadata when discovery is supported.
- A frontend-owning module ships its own frontend manifest and public entry points. It must not require root-only aliases, unpublished root components, or another module's build configuration.
- Module source must not import the host application's `App\` namespace. Host capabilities such as the authenticated principal, tenancy, authorization context, clock, filesystem, messaging, and external services are represented by module-owned Domain/Application ports.
- The host Laravel application supplies adapters and bindings for optional host capabilities. Required default adapters that belong to the module ship inside its Infrastructure layer.
- Configuration keys, route names, translation namespaces, view namespaces, cache keys, queue names, and database tables must be prefixed or namespaced by the module to avoid collisions after installation.
- The module Service Provider resolves all module resources relative to the package directory and registers only that module's bindings, config, migrations, routes, commands, views, and translations.
- Installation must require only declaring the Composer package, registering or discovering its Service Provider, publishing/configuring optional resources, running its migrations, and wiring explicitly documented host ports.
- Public Application contracts, Application DTOs/Enums, integration-event schemas, configuration keys, and migrations are versioned compatibility surfaces. Change them deliberately and document breaking changes.
- Each module has a standalone test harness that boots a minimal Laravel application with only the module provider and declared dependencies. It must not rely on root application factories, helpers, middleware, routes, models, or test fixtures.
- CI should run the module test suite from the module package and include an installation smoke test against a minimal compatible Laravel skeleton.

Extraction is incomplete if moving the module requires editing its Domain/Application code, importing another bounded context, copying files from `app/`, or reproducing undocumented container bindings.

### Persistence and Repositories

- Eloquent models belong only in `Infrastructure/Persistence/Models/`. Controllers, Application use cases, and Domain code must never query Eloquent directly.
- Repository interfaces are ports owned by Domain when they persist aggregates or Domain entities. Infrastructure repository classes implement those interfaces and perform mapping between persistence models and Domain objects.
- Application query ports may return Application DTOs for read-only projections that do not need Domain entities. Their Infrastructure implementations may use Eloquent or the query builder.
- Do not create a generic base repository or one repository per database table. Define a port around a use case or aggregate boundary.
- Transactions are controlled by Application through a framework-neutral transaction port or by an Infrastructure decorator around a use case. Application code must not call Laravel's database facade directly. Transactions must not span modules; cross-module workflows use explicit orchestration and eventual consistency.
- Migrations and factories live in the owning module. Test data comes from factories; do not add seeders for tests.

Example Application action using a Domain repository port:

```php
final readonly class ApproveLoanApplication
{
    public function __construct(
        private LoanApplicationRepository $applications,
    ) {}

    public function handle(string $applicationId, Money $approvedAmount): void
    {
        $application = $this->applications->get($applicationId);
        $application->approve($approvedAmount);

        $this->applications->save($application);
    }
}
```

### DTOs and Boundaries

- Public Application contracts return DTOs or `void`, never Domain objects or unstructured arrays. Internal Application code may use Domain objects while executing a use case.
- DTOs live in `Application/DTOs/`, use `readonly class` with constructor promotion, and contain no persistence or business logic.
- Application Enums are boundary representations for transports and composition. Map them explicitly from Domain Enums; do not expose Domain Enums directly to HTTP, Inertia, or another module.
- Convert DTOs with `->toArray()` only at a Presentation boundary such as an Inertia render or JSON response.
- Presentation validates and normalizes transport input before constructing an Application command or invoking an Action.
- Domain entities and Eloquent models must never be serialized directly into Inertia or API responses.

### Architecture Tests

Module independence and DDD layer direction are enforced with [Pest architecture tests](https://pestphp.com/docs/arch-testing), not code review alone.

- Root `tests/Architecture/ModuleBoundariesTest.php` enforces boundaries between all modules.
- Each module's `tests/Architecture/` enforces its internal Domain, Application, Infrastructure, and Presentation boundaries.
- Configure the root Pest/PHPUnit test suite to discover every module's `tests/` directory so `composer test` executes module tests automatically.
- A module Feature `TestCase` should boot the smallest Laravel application needed and register only that module's Service Provider. Load multiple module providers only in root composition tests.
- Define `moduleNamespaces()` once in root `tests/Pest.php`. It derives namespaces from module Composer manifests or the authoritative module registry, without a hand-maintained duplicate list.
- Adding or renaming a module must update its manifest or registry entry so architecture coverage discovers it automatically.
- Architecture tests run in `composer test` and CI. A module or boundary change is incomplete until they pass.
- Never add `ignoring()` exceptions for cross-module dependencies or layer violations. Redesign the dependency instead.
- Pest architecture tests cover PHP dependencies. Add equivalent checks for cross-module frontend imports and database references.
- Architecture tests must reject imports from the host `App\` namespace anywhere inside a module.
- Domain and Application architecture tests must reject Laravel event/queue namespaces and any broker-client namespace. Broker libraries are allowed only in Infrastructure after the broker is actually introduced.

Use this pattern in the root architecture test:

```php
$moduleNamespaces = moduleNamespaces();

foreach ($moduleNamespaces as $moduleNamespace) {
    $otherModuleNamespaces = array_values(array_diff($moduleNamespaces, [$moduleNamespace]));
    $domain = "{$moduleNamespace}\\Domain";
    $application = "{$moduleNamespace}\\Application";
    $infrastructure = "{$moduleNamespace}\\Infrastructure";
    $presentation = "{$moduleNamespace}\\Presentation";

    if ($otherModuleNamespaces !== []) {
        arch("{$moduleNamespace} does not depend on other modules")
            ->expect($moduleNamespace)
            ->not->toUse($otherModuleNamespaces);
    }

    arch("{$moduleNamespace} domain is independent")
        ->expect($domain)
        ->not->toUse([$application, $infrastructure, $presentation, 'Illuminate', 'Laravel']);

    arch("{$moduleNamespace} application depends inward")
        ->expect($application)
        ->not->toUse([$infrastructure, $presentation, 'Illuminate', 'Laravel']);

    arch("{$moduleNamespace} infrastructure does not use presentation")
        ->expect($infrastructure)
        ->not->toUse($presentation);

    arch("{$moduleNamespace} presentation uses application boundaries")
        ->expect($presentation)
        ->not->toUse([$domain, $infrastructure]);
}

arch('modules use strict types and contain no debug calls')
    ->expect($moduleNamespaces)
    ->toUseStrictTypes()
    ->not->toUse(['die', 'dd', 'dump']);

arch('modules do not depend on the host application')
    ->expect($moduleNamespaces)
    ->not->toUse('App');
```

### Creating a Module

1. Identify a business capability with its own language, rules, data ownership, and lifecycle. Do not create a module merely for a technical layer.
2. Create `modules/core/{module-name}/composer.json` with complete runtime constraints, dependencies, Laravel discovery metadata, PSR-4 mappings, and test mappings. Add a module `package.json` and root workspace entry when the module owns frontend code.
3. Create the required DDD layers, module Service Provider, resources, and tests. Do not create empty directories for unused patterns.
4. Register the module as a Composer path package and register its Service Provider through `bootstrap/providers.php` or the repository's module-discovery mechanism.
5. Add the module to frontend and route discovery without moving its feature files into the root application.
6. If the module emits Integration Events, define transport-neutral Application event contracts and an `IntegrationEventPublisher` port; bind the current Laravel Events Infrastructure adapter.
7. Add module-level layer tests and confirm the root cross-module architecture test discovers it.
8. Run `composer validate` for the module, `composer dump-autoload`, the standalone module suite, architecture tests, installation smoke test, and relevant frontend checks.

### File Placement Guidelines

- **Domain entities/value objects/enums**: `modules/core/{module}/src/Domain/`
- **Application Actions/commands/queries/DTOs**: `modules/core/{module}/src/Application/`
- **Eloquent models/repository implementations**: `modules/core/{module}/src/Infrastructure/Persistence/`
- **External integrations and queue adapters**: `modules/core/{module}/src/Infrastructure/`
- **Current Laravel Events adapter**: `modules/core/{module}/src/Infrastructure/Messaging/LaravelEvents/`
- **Future broker adapter after extraction**: the extracted application's Infrastructure layer, without modifying the module's Domain or Application contracts
- **Controllers/requests/commands**: `modules/core/{module}/src/Presentation/`
- **Migrations/factories**: `modules/core/{module}/database/`
- **Routes/configuration**: `modules/core/{module}/routes/` and `modules/core/{module}/config/`
- **Feature frontend/views/translations**: `modules/core/{module}/resources/`
- **Module tests**: `modules/core/{module}/tests/`
- **Cross-module orchestration**: `app/Composition/`
- **Reusable technical libraries**: `packages/{package}/`

### Eloquent Table Names

When an Infrastructure Eloquent model contains an acronym (e.g., `MLShadowPrediction`), explicitly set `protected $table` to avoid Laravel's snake-case conversion issues:

```php
class MLShadowPrediction extends Model
{
    protected $table = 'ml_shadow_predictions';
}
```

---

## Laravel instructions

- Controllers invoke Application Actions, Queries, or public Application contracts. If a use case is needed by only one controller method, inject it into that method; if multiple methods use it, inject it through the constructor.
- Controllers must remain transport adapters: authorize and validate the request, invoke one Application use case, then transform its DTO into an Inertia or HTTP response. Put no business or persistence logic in controllers.
- Do not inject Eloquent models into controllers through route-model binding. Pass validated scalar identifiers to the Application layer so Presentation does not depend on Infrastructure.
- **Eloquent Observers** live in the owning module's Infrastructure layer and should be registered on Infrastructure Eloquent models with PHP Attributes, not in `AppServiceProvider`. Example: `#[ObservedBy([UserObserver::class])]` with `use Illuminate\Database\Eloquent\Attributes\ObservedBy;` on top.
- In Laravel-aware Presentation and Infrastructure code, use Laravel helpers instead of facade imports. Examples: use `auth()->id()` instead of `Auth::id()`, `redirect()->route()` instead of `Redirect::route()`, or `str()->slug()` instead of `Str::slug()`. Domain and Application code must not call Laravel helpers.
- In Infrastructure persistence code, don't use `whereKey()` or `whereKeyNot()`; use specific fields like `id`. Example: instead of `->whereKeyNot($currentUser->getKey())`, use `->where('id', '!=', $currentUser->id)`.
- In Infrastructure persistence code, don't add `::query()` when running Eloquent `create()` statements. Example: instead of `User::query()->create()`, use `User::create()`.
- In Livewire projects, don't use Livewire Volt. Only Livewire class components.
- When adding columns in a module migration, update the owning Infrastructure Eloquent model's `$fillable` array to include those new attributes.
- Never chain multiple migration-creating commands (e.g., `make:model -m`, `make:migration`) with `&&` or `;` — they may get identical timestamps. Run each command separately and wait for completion before running the next.
- Enums: If a Domain Enum exists for a concept, always use its cases (or their `->value` at persistence/transport boundaries) instead of raw strings — routes, middleware, migrations, configs, UI defaults, and especially `match` branch keys.
- Controllers: Single-method Controllers should use `__invoke()`; multi-method RESTful controllers should use `Route::resource()->only([])`.
- Don't create Controllers with just one method which just returns `view()`. Instead, use `Route::view()` with Blade file directly.
- Always use Laravel's @session() directive instead of @if(session()) for displaying flash messages in Blade templates.
- In Blade files always use `@selected()` and `@checked()` directives instead of conditional `selected` and `checked` HTML strings. Example: `@selected(old('status') === Modules\Project\Application\Enums\ProjectStatus::Pending->value)`.

---

## Testing instructions

### Before Writing Tests

1. **Check the owning module's database schema** - Use the `database-schema` tool when available; otherwise inspect the module migrations, schema dump, Infrastructure model casts, and database constraints to understand:
    - Which columns have defaults
    - Which columns are nullable
    - Foreign key relationship names

2. **Verify relationship names** - Read the owning Infrastructure model to confirm:
    - Exact relationship method names (not assumed from column names)
    - Return types and related models
    - That every relationship remains inside the same module

3. **Test realistic states** - Don't assume:
    - Empty model = all nulls (check for defaults)
    - `user_id` foreign key = `user()` relationship (could be `author()`, `employer()`, etc.)
    - When testing form submissions that redirect back with errors, assert that old input is preserved using `assertSessionHasOldInput()`.

### DDD Testing Strategy

- **Domain unit tests** exercise invariants, value objects, entities, and Domain services as pure PHP. They must not boot Laravel or touch a database.
- **Application unit tests** exercise Actions, Commands, and Queries with in-memory fakes for Domain/Application ports. Use a fake `IntegrationEventPublisher` to assert emitted Integration Events without invoking Laravel Events or a broker.
- **Infrastructure feature tests** verify repository mappings, Eloquent scopes, integrations, queue adapters, the current Laravel Events adapter, and module Service Provider bindings using Laravel test facilities.
- **Presentation feature tests** verify authorization, validation, redirects, Inertia props, and API responses through the real transport boundary.
- **Composition tests** live under root `tests/Feature/Composition/` and cover workflows involving multiple modules. Individual module tests must not load sibling module providers.
- **Messaging contract tests** verify stable event names, envelope metadata, schema versions, camelCase serialization, and the absence of Eloquent/Domain objects from Integration Event payloads.
- **Extraction tests** install or boot the module in a minimal compatible Laravel harness with no sibling providers or host `App\` implementations.
- **Future RabbitMQ tests** are added only when RabbitMQ is introduced in an extracted application. They cover serialization, publisher confirms, manual acknowledgements, idempotency, outbox/inbox behavior, retries, and failure routing.
- **Architecture tests** enforce both DDD layer direction and module autonomy. Run them whenever a module class, namespace, manifest, migration, or frontend boundary changes.
- Keep module behavior tests in `modules/core/{module}/tests/`. Root tests are reserved for application-shell and composition behavior.

### Don't Write Trivial Tests

Don't write tests for obvious implementations that provide no value. Examples of tests to avoid:

- **Enum label/color/icon tests** - Testing that `Status::Pending->label()` returns a string or `->color()` returns 'gray'
- **Enum grouping method tests** - Testing that `terminalStates()` contains certain cases or `isTerminal()` returns true/false
- **Simple getter/setter tests** - Testing that a DTO returns what you passed to its constructor
- **Interface implementation tests** - Testing that a class implements HasLabel, HasColor, etc.

These are implementation details, not behavior. Only test meaningful business logic.

---

## TypeScript Type Generation

This project uses `spatie/laravel-typescript-transformer` to generate frontend types from module Application Enums and DTOs.

### Adding Types for Generation

Add the `#[TypeScript]` attribute only to Application Enums and DTOs that form part of a module's frontend contract. Domain types stay private and must not carry frontend-generation attributes. The attribute is build metadata; it must not introduce transformer behavior into Application code.

```php
use Spatie\TypeScriptTransformer\Attributes\TypeScript;

#[TypeScript]
enum JobStatus: string
{
    case Created = 'created';
    case Running = 'running';
    // ...
}

#[TypeScript]
readonly class FileItemDTO
{
    public function __construct(
        public string $id,
        public FileType $type,
    ) {}
}
```

### DTO `toArray()` must use camelCase keys

The TypeScript transformer generates camelCase property names from PHP constructor properties. `toArray()` MUST use the same camelCase keys so the generated types match the runtime JSON exactly. Never use snake_case in `toArray()`.

```php
// Good — matches generated TypeScript type { numberMin: string | null }
public function toArray(): array {
    return [
        'numberMin' => $this->numberMin,
        'isDynamic' => $this->isDynamic,
    ];
}

// Bad — snake_case mismatches generated TypeScript type
public function toArray(): array {
    return [
        'number_min' => $this->numberMin,
        'is_dynamic' => $this->isDynamic,
    ];
}
```

### Use generated DTO types in TypeScript

In TypeScript pages/components, use the generated DTO type aliases instead of re-defining local interfaces that duplicate the shape:

```typescript
// Good
type Project = Modules.Project.Application.DTOs.ProjectDTO;

// Bad — duplicates the DTO shape, goes stale when DTO changes
interface Project {
    id: string;
    name: string;
    // ...
}
```

After adding `#[TypeScript]` to a DTO, run `npm run types:generate` before writing frontend code.

### Generating Types

Run `npm run types:generate` (or `php artisan typescript:transform`) to regenerate `resources/js/types/generated.d.ts`.

### Using Generated Enums in Frontend

Generated enums are available via global namespaces. Assign them to local constants at the top of the file:

Before using a generated enum as a runtime value, verify that the project's generation setup emits runtime values rather than declarations only. If it emits declarations only, use the repository's existing generated TypeScript value module or pass the values from the backend; do not invent a runtime global.

```typescript
// At top of file
const ProjectStatus = Modules.Project.Application.Enums.ProjectStatus;
const JobStatus = Modules.Jobs.Application.Enums.JobStatus;

// Use in comparisons (NOT hardcoded strings)
if (project.status === ProjectStatus.Active) { ... }

// Use in switch statements
switch (job.status) {
    case JobStatus.Pushed:
        return 'success';
    case JobStatus.Failed:
        return 'error';
}

// Use as Record keys
const STATUS_CONFIG: Record<Modules.Project.Application.Enums.ProjectStatus, { color: string }> = {
    [ProjectStatus.Active]: { color: 'green' },
    [ProjectStatus.Archived]: { color: 'gray' },
};
```

**IMPORTANT**: Never use hardcoded magic strings like `'running'`, `'error'`, `'pending'` for comparisons. Always use the generated enum values.

---

## Inertia + React Rules

### Version and Native Capabilities

- Inertia.js v3 is mandatory. During initial setup, resolve and install the latest compatible stable `3.x` releases of the Laravel, React, and Vite adapters.
- Before adding custom frontend infrastructure, inspect the installed v3 documentation and use Inertia's native primitives where they fit.
- Prefer native v3 capabilities such as `Link`, `router`, `useForm`, `<Form>`, `useHttp`, HTTP interceptors, optimistic updates, cancellation, file uploads, validation/Precognition, remembered state, partial reloads, deferred/optional/merged props, prefetching, layout props, async visits, view transitions, SSR integration, and exception pages over custom replacements.
- Inertia v3 has a built-in XHR client. Do not add Axios merely to replace functionality already provided by Inertia. If a verified requirement needs Axios, use Inertia's supported adapter centrally instead of scattered Axios instances.
- Do not build wrappers that hide standard Inertia APIs without a concrete module-boundary or repeated domain-specific need. A feature hook may compose an Inertia primitive but must preserve its lifecycle, cancellation, errors, and types.

### Page Components

- Module page components live in `modules/core/{module}/resources/js/pages/` and follow that module's route structure.
- Root `resources/js/pages/` is reserved for application-composition pages that genuinely coordinate multiple modules.
- Use the official `@inertiajs/vite` v3 plugin for page resolution and SSR integration. Extend its supported page resolution configuration to discover module-owned pages; do not duplicate its behavior with a custom Vite plugin or copy feature pages into the root frontend.
- Use `Head` from `@inertiajs/react` for page titles.
- Props must match the Application DTOs serialized by the module's Presentation controller. Never pass Domain entities or Infrastructure Eloquent models to Inertia.

```typescript
import { Head } from '@inertiajs/react';

type Project = Modules.Project.Application.DTOs.ProjectDTO;

interface Props {
    projects: Project[];
}

export default function ProjectsIndex({ projects }: Props) {
    return (
        <>
            <Head title={__('Projects')} />
            {/* ... */}
        </>
    );
}
```

### Forms and Navigation

- Use `router` from `@inertiajs/react` for programmatic page visits.
- Use `Link` for navigation links.
- Use `useForm` or `<Form>` for submissions participating in the Inertia page lifecycle, redirects, validation bags, and page-state handling.

```typescript
import { router, Link, useForm } from '@inertiajs/react';

// Navigation
router.visit(route('projects.show', project.id));
router.post(route('projects.store'), data, { preserveScroll: true });

// Links
<Link href={route('projects.show', project.id)}>{__('View Project')}</Link>

// Forms
const { data, setData, post, processing, errors } = useForm({
    name: '',
    description: '',
});

const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    post(route('projects.store'));
};
```

### Standalone HTTP Requests

Not every request should trigger an Inertia page visit. Use the v3 `useHttp` hook for standalone JSON requests to non-Inertia endpoints, including background actions, searches, and external APIs.

```typescript
import { useHttp } from '@inertiajs/react';

const search = useHttp({ query: '' });

const handleSearch = () => {
    search.get(route('projects.search'), {
        onSuccess: (data) => {
            // Handle the parsed JSON response.
        },
    });
};
```

- `useHttp` supports `get`, `post`, `put`, `patch`, `delete`, and generic `submit` methods without navigation or the Inertia page lifecycle.
- Use its native `processing`, validation errors, progress, lifecycle callbacks, Promise result, cancellation, typed request/response support, and optimistic updates with rollback.
- Use a separate `useHttp` instance for independent concurrent request state.
- Return JSON from standalone endpoints. Do not return an Inertia page response to `useHttp`.
- Use Inertia's global HTTP interceptors for shared headers, response handling, and network behavior instead of duplicating setup in every request.
- Raw `fetch()` is a last resort for behavior unsupported by `useHttp`, such as a streaming response or specialized binary download. Document the reason at the call site. Do not use raw `fetch()` for ordinary JSON requests, page visits, or form submissions.

### Shared Data and Types

- `HandleInertiaRequests` may share application-shell concerns such as the authenticated principal, flash data, locale, and navigation state. It must not expose module Domain objects or query module persistence directly.
- A module's page-specific props come from that module's Presentation controller and Application DTOs.
- Module-local handwritten types live in `modules/core/{module}/resources/js/types/`.
- Generated PHP contract types remain in `resources/js/types/generated.d.ts` as a root build artifact and retain their module namespaces.

### Component Organization

- **Module pages/components/hooks/types**: `modules/core/{module}/resources/js/`
- **Module-specific layouts**: `modules/core/{module}/resources/js/layouts/`
- **Application shell and composition UI**: `resources/js/`
- **Reusable UI primitives**: a declared neutral package under `packages/`, or module-local components when reuse is not justified

Module frontend code must not import from another module. Enforce this with ESLint `no-restricted-imports` or an equivalent boundary rule generated from the same module registry used by PHP architecture tests.

### Best Practices

- **Type-based logic must use lookup objects, not ternary chains.** When selecting values based on a type/enum, use a `Record<Type, Config>` lookup instead of `isFoo ? ... : ...` ternaries. Ternaries break when new types are added and don't scale.

```typescript
// GOOD - Lookup object, extensible
const ExportFormat = Modules.Reports.Application.Enums.ExportFormat;

const CONFIG: Record<Modules.Reports.Application.Enums.ExportFormat, { title: string; hint: string }> = {
    [ExportFormat.Csv]: { title: __('CSV export'), hint: __('Best for spreadsheets') },
    [ExportFormat.Json]: { title: __('JSON export'), hint: __('Best for integrations') },
};
const { title, hint } = CONFIG[data.format];

// BAD - Ternary chains break when another format is added
const isCsv = data.format === ExportFormat.Csv;
const title = isCsv ? __('CSV export') : __('JSON export');
const hint = isCsv ? __('Best for spreadsheets') : __('Best for integrations');
```

- Choose the native Inertia primitive by request intent: `Link`/`router` for page visits, `useForm`/`<Form>` for page-lifecycle forms, and `useHttp` for standalone JSON requests.

```typescript
// GOOD - Inertia page-lifecycle submission
router.post(route('projects.store'), data, {
    preserveScroll: true,
    onSuccess: () => { /* handle success */ },
    onError: (errors) => { /* handle validation errors */ },
});

// BAD - Bypasses Inertia's native request and form features
const response = await fetch(route('projects.store'), {
    method: 'POST',
    headers: { 'X-CSRF-TOKEN': '...' },
    body: JSON.stringify(data),
});
```

- Keep module page components focused on layout and data flow; extract complex UI to components owned by the same module.
- Use the project's `cn()` utility from a declared neutral UI package for conditional class names.
- Wrap user-facing text in `__('...')` for translation support
- Use `route()` helper (from Ziggy) for generating URLs, never hardcode paths
- Prefer controlled components with React state over uncontrolled forms
- Use TypeScript strictly. Reuse generated Application DTO and Application Enum types; define module-local interfaces only for shapes that are not already represented.

---

## Security and data safety

- Never commit secrets, credentials, tokens, private keys, `.env` files, or production data.
- Keep environment-specific values in configuration and environment variables. Do not hardcode credentials or deployment-specific URLs.
- Preserve authorization, validation, tenancy, and ownership boundaries when changing controllers, jobs, services, or queries.
- Do not weaken security checks to make a test pass. Fix the setup or behavior instead.
- Treat migrations, bulk updates, queue jobs, external API calls, and file operations as potentially destructive. Verify scope and use the safest environment available.

## Definition of done

A change is complete when:

- The requested behavior is implemented without unrelated changes.
- New or changed behavior has meaningful automated coverage.
- Relevant PHP formatting/tests and frontend lint/type checks pass.
- Architecture tests pass whenever module code or module boundaries change.
- A changed module passes its standalone minimal-Laravel test suite and installation smoke test without sibling modules or host `App\` code.
- Module manifests fully declare runtime and frontend dependencies; extraction requires no undocumented files or bindings.
- Integration Events remain transport-neutral and are covered by schema/serialization tests. The current monolith uses the Laravel Events adapter without introducing RabbitMQ dependencies.
- Inertia remains on the latest compatible stable v3 adapters, and implementations use native v3 capabilities before custom HTTP, form, navigation, state, SSR, or page-resolution code.
- Generated types or other derived artifacts are regenerated when their sources change.
- User-facing text is translatable and accessibility is preserved.
- No secrets, debug output, temporary files, or accidental generated artifacts were introduced.
- The final report states what changed, which checks ran, and any remaining verification gaps.
