# OFFENDER_TEAM_ASSIGNMENTS consistent-export spike

## Status and scope

- Repository: ministryofjustice/prison-api.
- Branch: spike/offender-team-assignments-endpoint.
- Base: origin/main at c9c95339b.
- Plan only: no endpoint implementation, database access or permission changes.
- Export every row of OFFENDER_TEAM_ASSIGNMENTS at one database snapshot,
  returning flat objects without related-table expansion.
- No writes to NOMIS, new NOMIS tables, unbounded responses or production access.
- Consumer implementation and permanent DPS storage are separate deliverables;
  their required contract is specified below, not assumed to be implemented.

## Success guarantee and limits

A successfully completed, sequential cursor traversal returns exactly the rows
and selected field values visible at the initial Oracle SCN. This is a snapshot
extract, not a stream of subsequent changes, all audit columns or an exactly-once
delivery mechanism. Repeated requests may repeat records.

The guarantee depends on verified Oracle capabilities and an available snapshot.
Undo loss, relevant DDL, database replacement or expired cursors must cause an
explicit failure, never a successful partial result or fallback to current data.
No promise is made that an arbitrarily long extraction can finish.

## Gate 0: establish feasibility before endpoint implementation

Confirm with Tim/the NOMIS DBA team:

1. The preferred authorised Oracle development/test environment and database
   version. Do not assume Docker Oracle or T3 access has been arranged.
2. The actual API database principal/proxy session can capture an SCN and query
   the table AS OF SCN. Verify using read-only queries under that principal.
   Ask for narrowly scoped privileges through the DBA process if required;
   never request broad DBA privileges or use a personal login as proof.
3. Representative table size, primary-key index, expected extraction duration,
   query plan and acceptable query timeout/load.
4. Undo capacity and a practical maximum extraction lifetime, including retry
   time. Configured UNDO_RETENTION alone is not a guarantee of availability.
5. A safe place and approval to create/change isolated test fixtures for
   concurrent-change tests. Do not mutate unrelated rows in shared T3.

Record the environment, principal category (no credentials), capabilities,
measured timings, approved lifetime and permission outcome in implementation
evidence. If any essential capability is unavailable, stop this implementation
path and report the blocker. A separately agreed write-freeze or managed
snapshot export is an alternative; ordinary pagination is not an equivalent.

## Existing patterns to preserve

- OffenderTeamAssignment.java and OffenderTeamAssignmentRepository.java already
  map this table; the composite PK is (OFFENDER_BOOK_ID, FUNCTION_TYPE).
- V1_267__OFFENDER_TEAM_ASSIGNMENTS.sql already creates the local HSQLDB table.
- OffenderTeamAssignmentBuilder.kt and existing booking/team builders provide
  test fixture patterns.
- Follow README: thin Spring REST controller, service logic, Spring JPA,
  ControllerAdvice, OpenAPI and WebTestClient integration tests.
- Preserve existing entity mappings, assignment writes and transfer workflows.
- Deliberate deviation: cursor DTO and native scalar projection, not Page or
  entity serialisation. Oracle flashback and stable historical values require
  an explicit table query; current-state JPA associations must not be loaded.

## API contract

Initial request:

`GET /api/offender-team-assignments?size=500`

Continuation:

`GET /api/offender-team-assignments?cursor=<opaque-nextCursor>`

Example response:

```json
{
  "content": [
    {
      "bookingId": 12345,
      "functionType": "AUTO_TRN",
      "teamId": 678,
      "assignmentDate": "2026-11-17"
    }
  ],
  "nextCursor": null
}
```

- Initial size defaults to 500; accept only integers in 1..5000. Validate this
  explicitly: Spring's Pageable cap does not apply to a custom cursor API.
- For continuation, reject a supplied size rather than changing/defaulting it;
  size is carried in the authenticated cursor.
- Reject unsupported page, offset or sort parameters; ordering is fixed.
- nextCursor=null is the only success-completion signal. Empty snapshots
  return content=[] and nextCursor=null.
- No count/start/booking-ID endpoint is required. No total-count query.
- Include all bookings and teams, including historical/inactive ones. Query
  only the assignment table; joins must not accidentally omit rows.
- Require authentication, read scope and existing VIEW_PRISONER_DATA authority
  on every call; follow the repository's authority naming conventions.
  Obtain API-owner approval before enabling this global export permission.
- Do not mark the endpoint as reference data or apply a per-booking/caseload
  filter. A valid cursor never substitutes for authorisation.
- Mark responses Cache-Control: no-store; do not log response bodies/cursors.
- Document that audit/user metadata is excluded and DATE mapping follows the
  existing assignment entity's LocalDate semantics. This is not a byte-for-byte
  database backup; verify whether any time-of-day data matters before expansion.

## Snapshot query and routing

1. Capture the SCN before the first data query, including for empty snapshots.
2. Read all four response fields from OFFENDER_TEAM_ASSIGNMENTS AS OF SCN using
   a bounded native projection in the existing repository. Bind all values.
3. Use ascending order by OFFENDER_BOOK_ID, FUNCTION_TYPE. For continuation use:
   booking ID greater than the last ID, OR the same booking ID with function
   type greater than the last type.
4. Match comparison and ordering collation on every pooled connection. Verify
   session NLS behaviour for function codes; no Java-side reordering.
5. Fetch at most size+1 rows, return size, and build the next cursor from the
   last RETURNED row, never the look-ahead row. Order before applying row limits.
   Use syntax supported by the verified Oracle version, including 11g if needed.
6. Use a short read-only transaction per request; capture/query on the same
   primary datasource. No transaction/connection is retained between requests.

Use the existing default primary routing initially. Do not annotate this
endpoint with SlowReportQuery: ReadOnlyRouteInterceptor routes that annotation
to the replica, whereas readOnly=true alone does not choose a replica. Verify
the route in tests and Oracle evidence. Replica support is out of scope until
SCN compatibility and availability are separately proven.

Apply a configured database query timeout agreed in Gate 0. Read-only does not
mean no load: inspect plans and bound result size; do not run a full-table
COUNT per batch. Do not rely on transaction readOnly to create a snapshot.

## Cursor format and operation across pods

Use a stateless, versioned, authenticated cursor. Its protected payload contains:

- Endpoint/purpose identifier and source identifier.
- Snapshot SCN, last composite key and immutable batch size.
- Extraction UUID, original issue time and absolute expiry.
- Format version and signing-key ID.

The source identifier binds this environment to its logical database/restore
epoch, configured consistently on every pod. It must change after a database
replacement/restore that could make an old SCN refer to different data.

Use an existing supported signing facility if suitable; otherwise standard
HMAC-SHA-256 from the existing Java platform, with a cryptographically random
secret of at least 256 bits. Verify authenticity before trusting payload fields.
Use bounded parsing, a fixed allowed algorithm and constant-time MAC comparison.
Do not treat base64 encoding as integrity protection.

- Provision keys through existing deployment secret mechanisms, never source
  code, logs, query strings in examples or a randomly generated per-pod key.
- Every pod shares the current signing key and any still-valid verification
  keys. Old unexpired cursors must survive pod restarts and rolling deployments.
- Rotate by distributing the new verification key first, then switching signing,
  retaining the old key through the maximum cursor lifetime plus clock skew.
  An emergency revocation can invalidate runs and must be operationally explicit.
- Configure and require a positive maximum extraction lifetime established in
  Gate 0; expiry is not extended by requests. Fail-fast configuration checks run
  when the export endpoint is enabled.
- Limit cursor input length to 4096 characters and validate key lengths/types,
  source, purpose, version, timestamps and batch bounds. Use the existing Clock
  injection pattern for expiry tests. Do not expose cursor contents in logs.
- Preserve the same extraction UUID/SCN/expiry in every continuation; clients
  treat cursors as opaque. Exact token bytes need not be identical on retries.

## Error contract

Use existing ControllerAdvice/ErrorResponse conventions with a stable,
documented machine-readable discriminator compatible with the current schema.
Do not introduce a conflicting application-wide error format.

| Condition | HTTP | Consumer action |
|---|---|---|
| Invalid size/parameters or malformed/tampered/wrong-source cursor | 400 | Fix input; do not retry unchanged |
| Missing authentication | 401 | Obtain valid authentication |
| Insufficient authority/scope | 403 | Resolve access; do not retry unchanged |
| Expired, revoked or unsupported retired cursor | 410 | Discard incomplete run; restart |
| Oracle can no longer serve the historical snapshot | 410 | Discard incomplete run; restart |
| Transient DB outage/query timeout | 503 | Bounded retry of the same request |
| Server permission/configuration defect | 500 | Stop and alert; do not loop |

Map the verified Oracle cause codes, not every SQL exception, to these outcomes.
Preserve diagnostic information in sanitised server logs without returning
credentials, SQL internals or signed cursor data. A failed batch must not return
HTTP 200 with partial content.

## Consumer contract: retries, checkpoints and safe restarts

The consumer needs extraction-run staging, not merely blind upserts into the
live destination:

1. Create a fresh local run/staging area before the initial request. Permit only
   one sequential request stream for that run; do not parallelise guessed keys.
2. Fully receive/validate the response before applying the batch.
3. Within a durable consumer transaction, upsert staged rows using
   (run ID, bookingId, functionType) and save the next cursor/completion marker.
   If the destination cannot transact these together, replay-safe upserts and
   durable checkpoint ordering must provide equivalent crash safety.
4. Continue only after the batch/checkpoint is durable. A crash before commit
   replays the same request; a crash after commit resumes from its saved cursor.
5. Persist completion explicitly when nextCursor=null. An absent checkpoint is
   "not started", not "completed"; an empty successful extraction is valid.
6. Publish only a completed staging run, atomically switching the active dataset
   or using an equivalent reconciliation transaction. Failed runs never become
   visible as complete. Account for records absent from the new snapshot rather
   than leaving stale rows behind; do not delete unrelated destination data.
7. On HTTP 410, discard/isolate all staged rows from that run, clear its cursor,
   and restart under a new run ID. Never merge rows from different snapshots.
8. Use bounded exponential backoff with jitter for transient failures; refresh
   expired authentication without discarding a still-valid snapshot. Do not
   retry validation, authorisation or configuration errors indefinitely.

The initial request has no replayable snapshot cursor yet. If it is retried
before its batch/checkpoint is durable, abandon any uncheckpointed staging and
start a fresh run; do not assume the initial retry uses the same snapshot.

Exactly-once effects on an arbitrary consumer cannot be guaranteed by this GET
API. These durability/staging rules are acceptance requirements for a future
consumer, and must be demonstrated by a test harness in this spike.

## Local development and Oracle verification

HSQLDB remains useful for existing migrations, fixture builders and compatible
repository checks. It does NOT implement Oracle flashback. Do not add a silent
live-read adapter to pretend it meets this API's guarantee.

Local test layers:

- Unit tests: cursor codec, service/look-ahead logic, errors and Clock control.
- WebTestClient/ResourceTest: mock only the snapshot query boundary, retaining
  actual controller, validation, authorisation and error handling.
- Existing HSQLDB tests: preserve shared assignment entity/write behaviour.
- Authorised Oracle integration: execute the real query and snapshot capture,
  verify concurrent-change behaviour and run the complete extraction loop.

The real endpoint cannot be demonstrated against HSQLDB alone. Confirm the
Oracle setup before implementing rather than leaving this as a final surprise.
No Oracle tests may alter shared fixtures without approval. Use disposable
test data with exact cleanup and restore/delete only records created for tests.

## Implementation sequence

1. Complete Gate 0 and record evidence. Do not implement an unverified promise.
2. Add flat response/native projection and Oracle keyset query, retaining the
   existing entity/repository and dependencies.
3. Add cursor configuration/codec and snapshot export service.
4. Add the thin controller, explicit access checks, error mapping and OpenAPI.
5. Add tests and the consumer-loop/staging test harness.
6. Run focused tests and existing format/lint checks. Run affected assignment
   workflow tests if any shared code is changed.
7. Complete Oracle acceptance, operational documentation and owner review before
   enabling. No secrets, production runs or permission grants in this change.

Keep the endpoint disabled by default behind a narrowly scoped export-enabled
configuration property. When enabled, validate signing/source/lifetime/timeout
configuration without letting unrelated routes silently change behaviour.
Disabled endpoints must not require new secrets or Oracle privileges.

## Acceptance checklist

- Exhaustive small batches across multiple bookings/function types, with no gaps
  or duplicate keys in one completed traversal; compare to the same SCN dataset.
- Empty snapshot, size=1, partial and exactly-full final batches, look-ahead row
  not skipped, invalid sizes and unsupported request parameters.
- Real Oracle insert/delete/update, including key-changing updates if allowed,
  between batches: results retain the initial snapshot rows and values.
- Existing mixed-case/trailing-space function codes, if present, behave under
  the verified comparison rules. No hidden join/current-booking/active filter.
- Correct Oracle DATE/projection mapping and no nested JPA entity loads.
- Signature tampering, malformed/oversized tokens, wrong source/purpose/version,
  unknown/revoked key, expiry boundary and source-epoch changes.
- Cross-pod continuation, restart and rolling key rotation maintain valid runs.
- Transient retry returns the same snapshot batch; expiry/snapshot loss fails
  explicitly with no fallback. Permission failure is not mislabelled as expiry.
- Authentication/role/scope enforcement on both initial and continuation calls.
- No query before validation/auth; no cursor/body logging or cacheable data.
- Crash before/after batch/checkpoint commit, initial-response retry, last-batch
  retry and expired-run restart: harness proves no mixed/partial publication
  and no stale rows from abandoned runs.
- Oracle plan/timing at representative volume, bounded result/memory/timeout,
  primary-route verification and no connection retained across HTTP requests.
- Disabled/default configuration leaves existing endpoints/tests unchanged;
  existing assignment transfer/write behaviour remains intact.

## Handoff and non-goals

Document request/response examples, errors, permissions, primary routing,
approved timeout/lifetime/load, key rotation/source-epoch procedure and consumer
restart rules. Monitor batch latency/size, failures and snapshot expiries using
existing observability without logging rows or cursors. The consumer starts
with one request at a time; increased concurrency/load needs team approval.

Related-table expansion, other tables, destination business APIs, real consumer
deployment, ongoing change synchronisation and replica support are separate.
No plan can eliminate operational failure; this design makes failures explicit
and prevents declaring an incomplete extraction successful.
