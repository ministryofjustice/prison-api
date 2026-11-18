# WORKFLOW_FOLDERS and WORKFLOW_SCREENS export plan

## Scope and status

- Branch: `spike/workflow-folders-screens-endpoints`.
- Independent branch from `origin/main` at `c9c95339b`; do not depend on or
  cherry-pick the offender-team-assignment spike.
- Implemented locally with entities, repositories, DTOs, service/controller,
  local schema migrations and focused endpoint/OpenAPI coverage. Oracle access
  and representative execution plans still require an authorised environment.
- Assumption: this is another read-only export of suspected-unused NOMIS data.
  Suspected inactivity is not proof that the source is unchanged.
- Export every row and all columns from the two target tables, including
  creation, modification and audit metadata, to avoid a silently lossy move.
- No writes to NOMIS, source deletion, new production tables, queue access,
  workflow execution, consumer implementation or pull request in this task.

## Evidence and existing support

The supplied `nomis-tables.sql` defines:

- `WORKFLOW_FOLDERS` at lines 5004-5034.
- `WORKFLOW_SCREENS` at lines 10499-10534.
- Supporting parent `OMS_MODULES` at lines 3283-3320.

The export warns that generated DDL may differ from native DDL. Verify these
definitions against an authorised Oracle environment before deployment.
The source file is not a build dependency and must not be copied wholesale
into the repository.

No existing entities, repositories or local DDL for these three tables were
found in `src/main` on the base branch. `WORKFLOW_HISTORY` and the existing
workflow notification services are different functionality; do not extend
them merely because their names contain "workflow".

Follow `README.MD` coding standards: thin Spring REST controller, service-layer
logic, Spring JPA, existing ControllerAdvice and WebTestClient/ResourceTest.
Reuse the existing HSQLDB profile, Spring Data pagination configuration,
authentication and code-first Springdoc setup. No new dependencies or codegen.

## Decision: two endpoints, one controller and one service

| Endpoint | Source | Fixed ascending order |
|---|---|---|
| `GET /api/workflow-folders?page=0&size=500` | WORKFLOW_FOLDERS | WORKFLOW_CODE |
| `GET /api/workflow-screens?page=0&size=500` | WORKFLOW_SCREENS | WORKFLOW_CODE, MODULE_NAME |

Use two endpoints because these are two row sets with different primary keys:

- Folders with no screens must still be exported.
- Multiple screens can belong to one folder. Paging a joined result either
  repeats folders or splits their collections; collection fetch joins can
  also defeat database-level pagination.
- Flat responses give each table independent counts, retries, checkpoints and
  source/destination reconciliation.
- A nested endpoint could work by paging folders and separately fetching their
  screens, but adds aggregation and variable-size responses without helping
  this table-by-table move.

One `WorkflowConfigurationResource` and one `WorkflowConfigurationService`
can expose the two operations without duplicating controller/service classes.
Use separate DTOs and repositories for the two table shapes.
Do not introduce a combined response envelope, nested screens, per-folder
detail route, optional filtering or configurable sorting in this spike.

## Table mappings and response contract

`WORKFLOW_FOLDERS` has primary key `WORKFLOW_CODE`.
`WORKFLOW_SCREENS` has composite primary key `(WORKFLOW_CODE, MODULE_NAME)`.
Both key parts must participate in screen identity, equality and sorting.

Map the following fields explicitly in both the entity/projection and DTO.
Only `MODULE_NAME` is screen-specific; every other row applies to both tables.
Question marks indicate source-nullable fields.

| Source column | JSON property | Kotlin type | Length / constraint |
|---|---|---|---|
| WORKFLOW_CODE | workflowCode | String | 12; folder PK, screen PK/FK |
| MODULE_NAME | moduleName | String | 20; screen PK/FK only |
| DESCRIPTION | description | String? | 100 |
| WORKFLOW_SEQ | workflowSeq | Int? | NUMBER(6,0), default 1 |
| CASELOAD_TYPE | caseloadType | String? | 12 |
| CREATE_DATETIME | createDatetime | LocalDateTime | NOT NULL |
| CREATE_USER_ID | createUserId | String | 32; NOT NULL |
| MODIFY_DATETIME | modifyDatetime | LocalDateTime? | Nullable despite its default |
| MODIFY_USER_ID | modifyUserId | String? | 32; nullable despite its default |
| AUDIT_TIMESTAMP | auditTimestamp | LocalDateTime? | TIMESTAMP |
| AUDIT_USER_ID | auditUserId | String? | 32 |
| AUDIT_MODULE_NAME | auditModuleName | String? | 65 |
| AUDIT_CLIENT_USER_ID | auditClientUserId | String? | 64 |
| AUDIT_CLIENT_IP_ADDRESS | auditClientIpAddress | String? | 39 |
| AUDIT_CLIENT_WORKSTATION_NAME | auditClientWorkstationName | String? | 64 |
| AUDIT_ADDITIONAL_INFO | auditAdditionalInfo | String? | 256 |

Return standard `Page<WorkflowFolderResponse>` and
`Page<WorkflowScreenResponse>`, with `content`, `number`, `size`,
`numberOfElements`, `totalElements`, `totalPages` and `last`.

Preserve stored values, including nulls, numeric zero and fractional seconds.
Do not replace null sequences with the database default, derive flags, trim
strings or convert timestamps to dates. Oracle TIMESTAMP has no timezone:
use ISO local date-times without inventing UTC or another offset, and document
them as strings with the `local-date-time` OpenAPI format. Always
include nullable JSON properties explicitly as null, so replay/upserts can
clear a previously populated value rather than treating it as omitted.

`WORKFLOW_SEQ` is display order, not identity; it can be null or duplicated.
Do not use it as pagination order or a migration checkpoint.

## Implementation components

| Component | Planned change |
|---|---|
| Entities | Add `repository/jpa/model/WorkflowFolder.java` and `WorkflowScreen.java`, following the existing Java entity pattern around the package-private AuditableEntity base. |
| Composite ID | Define nested `@Embeddable WorkflowScreen.PK` in the screen entity file, with scalar workflowCode/moduleName, both `@Column` mappings, `Serializable` and value equality; use `@EmbeddedId`. |
| Audit mapping | Reuse `AuditableEntity` for its four CREATE/MODIFY fields; map the remaining audit columns explicitly without redeclaring inherited columns. |
| Folder repository | Add `WorkflowFolderRepository` with Spring Data paging and a flat DTO projection, explicit full-key ordering and matching count query. |
| Screen repository | Add `WorkflowScreenRepository` with the equivalent paged projection/count, ordered by both embedded-ID properties. |
| DTOs | Add `api/model/WorkflowFolderResponse.kt` and `WorkflowScreenResponse.kt`, documenting every field, nullability and timestamp format. |
| Service | Add `service/WorkflowConfigurationService.kt`, with read-only transactional methods for folders and screens. |
| Controller | Add `api/resource/WorkflowConfigurationResource.kt`, with both GET routes, parameter validation, role/scope checks and OpenAPI responses. |
| Local schema | Add new Flyway DDL migrations for OMS_MODULES, WORKFLOW_FOLDERS, then WORKFLOW_SCREENS, respecting dependency order. |
| Tests | Add `api/resource/impl/WorkflowConfigurationResourceTest.kt`, focused SQL fixtures/cleanup, and generated-contract assertions in existing `OpenApiDocsTest.kt`. |

Paths in this table are relative to the existing main or test Java package,
except local DDL under `src/main/resources/db/migration/nomis/ddl`.
Allocate unused migration versions at implementation time; do not edit
existing applied migrations.

Store `WORKFLOW_SCREENS.WORKFLOW_CODE` and `MODULE_NAME` as scalar ID fields.
No ORM association or joins to folders/modules are required for an export.
Do not introduce a collection on folders or an `OmsModule` entity/repository.
DTO constructor projections can read inherited audit fields directly in JPQL,
without exposing entity serialization or requiring public audit setters.
Verify that each page produces bounded SQL and no relationship-loading/N+1
queries. Do not fetch all rows and paginate in application memory.

## Pagination, authentication and errors

- Zero-based `page`, default 0; `size`, default 500.
- Reject negative pages, non-positive sizes, non-numeric values and integer
  overflow using existing validation/ControllerAdvice behaviour.
- Cap size using `spring.data.web.pageable.max-page-size`, currently 5000.
  Construct the effective PageRequest using the capped size.
- Reject offsets beyond the JPA integer offset limit with HTTP 400, not 500.
- Reject page number 2147483647 even when the offset fits: Spring Data's
  next-page calculation would overflow and return incorrect `last` metadata.
- Return HTTP 200 with empty content for an empty table or representable
  beyond-end page. Do not return 404 for an empty collection.
- Ordering is always the full source PK; ignore and document supplied `sort`.
- Require both `VIEW_PRISONER_DATA` and `SCOPE_read`, matching the chosen global
  export convention. Confirm API-owner approval for this permission choice
  before deployment, including access to the exported audit metadata.
- No caseload, booking, prison, current-only or workflow-type filtering.
- Document 200 with the correct Page schema; 400 and 500 with ErrorResponse;
  401 with explicitly empty content; 403 with ErrorResponse. Assert the
  generated contract so Springdoc cannot infer a success schema for errors.
- Keep authentication enabled locally. No new roles, auth bypass or embedded
  tokens/credentials.
- Use the default primary database initially. A read-only transaction does
  not select the replica; do not add SlowReportQuery routing without approval.

## Local database support

The Oracle profile disables Flyway. These new DDL migrations support the
existing local/test schema; they must not create tables in a deployed NOMIS
database or require a change to the deployed profile.

Create the two target tables faithfully from the supplied definitions,
including nullability, defaults, PKs and the folder FK. Add supporting
`OMS_MODULES` DDL from its actual definition so the module FK can also be
enforced. This supporting table needs no API, DTO, entity or repository.
Avoid copying generated duplicate PK indexes or environment-specific schema
owners/constraint names that are unnecessary locally.

Use test-scoped fixtures, not global repeatable production-like seed data.
Insert modules, folders, then screens; clean up screens, folders, then modules.
Use dedicated fixture keys and targeted cleanup, never delete all shared rows.
Supply valid CREATE_DATETIME/CREATE_USER_ID values in fixtures.

Local development remains:

```sh
./gradlew bootRun --args='--spring.profiles.active=nomis-hsqldb'
```

Test fixtures do not automatically load during bootRun. If a manual demo needs
data, use a separate opt-in local seed, not a change to normal startup data.

## Consumer and consistency rules

1. Establish a stable source window covering extraction of BOTH tables.
2. Read all folder pages with a fixed size, staging rows by workflowCode.
3. Independently read all screen pages with a fixed size, staging rows by
   (workflowCode, moduleName). Retain moduleName as the external reference.
4. For each endpoint, advance only after a page is durably saved. Stop on
   last=true; retry transient failures on the same page with bounded backoff.
5. Make destination upserts replay-safe. Maintain separate checkpoints for the
   two streams and isolate incomplete extraction runs before restarting.
6. Reconcile source/destination counts, distinct keys, every exported field
   and folder-to-screen references before publishing the complete run.

Neither endpoint provides a snapshot, including across requests to the same
endpoint. Read-only transactions and full-key ordering do not fix concurrent
inserts/deletes/updates. Matching counts or modification timestamps alone do
not prove completeness. If the source changes, pause the move and agree a
fresh extract/reconciliation or a consistent export mechanism.

OMS_MODULES data is outside these two exports. A relational destination must
already contain the referenced modules or arrange their separate migration
before enforcing the module FK; do not claim these two endpoints alone are a
self-contained copy of every related table.

No source deletion or exactly-once downstream delivery is provided.
No SCN/cursor/signing infrastructure is needed unless changing-data
requirements are confirmed.

## Focused acceptance checks

- Folders with zero, one and several screens are exported.
- Two screens in one folder remain distinct; the same module in different
  folders remains distinct. Exercise both parts of the composite ID.
- Concatenating small pages from stable fixtures returns all rows once, in
  full-key order, for each endpoint independently.
- Null/duplicated workflowSeq values do not change pagination ordering.
- Cover empty tables, partial and exactly-full final pages, beyond-end pages,
  default size, configured size cap, invalid/overflow input and sort override.
- Round-trip all source columns, nulls and non-midnight fractional timestamps.
  Required and nullable fields match the source, not default-value guesses.
- Responses contain flat DTO fields, not folders-with-screens collections,
  JPA IDs/proxies, module objects or framework entity internals.
- Unauthenticated, missing-role and missing-read-scope requests fail;
  permitted callers can read both complete row sets.
- Verify actual Page and ErrorResponse shapes and the generated OpenAPI
  200/400/401/403/500 contracts, including the empty 401 body.
- Validate local FK constraints with focused repository/fixture cases where
  needed; no independent OMS_MODULES API or entity is required.
- Inspect captured page SQL using existing test logging to confirm database
  pagination, no collection fetch and no extra selects per result row.

Run the narrow endpoint/contract checks and existing formatter:

```sh
./gradlew test \
  --tests '*WorkflowConfigurationResourceTest' \
  --tests '*OpenApiDocsTest' \
  ktlintCheck --console=plain
```

Run a broader suite only if shared changes or failures justify it. Verify the
local/test profile can start with the new DDL. In an authorised Oracle
environment, separately verify API-account SELECT access to both tables,
actual column/constraint compatibility, generated Oracle pagination SQL and
representative projection/count execution plans. HSQLDB cannot establish
Oracle permissions, deployed connectivity or production query cost.
