# OFFENDER_TEAM_ASSIGNMENTS paginated-read spike

## Purpose and scope

- Repository: ministryofjustice/prison-api.
- Branch: spike/offender-team-assignments-endpoint.
- Add one read-only endpoint to extract assignment rows across all bookings.
- The data is suspected to be unused; that is an assumption, not yet proven.
- Follow existing Prison API implementation and pagination patterns.
- Return flat objects first; related-table expansion is a separate change.
- No writes to NOMIS, source deletion, new NOMIS tables or production access.
- This is a plan; no endpoint implementation has been made.

## Existing implementation to reuse

- OffenderTeamAssignment.java already maps OFFENDER_TEAM_ASSIGNMENTS.
- Its composite key is (OFFENDER_BOOK_ID, FUNCTION_TYPE).
- OffenderTeamAssignmentRepository.java already supports Spring Data paging.
- V1_267__OFFENDER_TEAM_ASSIGNMENTS.sql already creates the local table.
- OffenderTeamAssignmentBuilder.kt and existing booking/team builders can
  supply test fixtures.
- Follow README: thin Spring REST controllers, service-layer logic, Spring
  JPA, ControllerAdvice and WebTestClient integration tests using ResourceTest.
- Preserve existing entity mappings and assignment write/transfer workflows.

## Proposed endpoint

`GET /api/offender-team-assignments?page=0&size=500`

Use the existing Spring Data Page/Pageable convention:

- Zero-based page; default size 500; use the existing maximum page size of 5000.
- Always order by OFFENDER_BOOK_ID ASC, FUNCTION_TYPE ASC, the full unique key.
  Keep the ordering fixed rather than allowing callers to replace it.
- Return every assignment row: no current-booking, active-team, prison or
  caseload filter.
- Apply database-level pagination; never fetch the entire table into memory.
- Empty results or pages beyond the end return HTTP 200 with empty content.
- Validate negative page numbers and non-positive sizes; document and test
  Spring's existing size-cap behaviour instead of inventing a second limit.
- Require authentication and explicit global prisoner-read permission, using
  existing VIEW_PRISONER_DATA and read-scope conventions. Confirm API-owner
  approval before exposing this bulk-read route.
- Use the existing ErrorResponse/ControllerAdvice and OpenAPI conventions.

Example response fields (other standard Page metadata may also be present):

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
  "number": 0,
  "size": 500,
  "numberOfElements": 1,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

"All data" means every assignment row with these four flat fields, not every
audit column or nested entity. The date follows the existing entity's LocalDate
mapping. If later migration requirements need audit fields or time-of-day
preservation, explicitly extend the response before relying on it as a backup.

## Implementation

1. Add a flat response DTO with OpenAPI field descriptions.
2. Reuse the existing repository's paged read, supplying the full-key sort in
   the service. Check the generated query and avoid relationship loading/N+1
   issues; use an existing-style scalar projection if needed.
3. Add a read-only transactional service to fetch and map the Page, without
   serialising JPA entities or including nested team/booking objects.
4. Add a thin Spring REST controller with documented page parameters and
   explicit authorisation. Use existing default database routing initially;
   readOnly=true does not itself select the replica. Do not introduce
   SlowReportQuery replica routing without confirming that route's suitability.
5. Add focused service/repository and WebTestClient integration tests.

No Oracle flashback/SCN queries, signed cursors, signing-key configuration,
custom pagination envelope or snapshot infrastructure are needed for this spike.

## Local development

Use the documented nomis-hsqldb profile. Existing Flyway migrations create
the table; reuse fixture builders rather than installing a new mock database.

```sh
./gradlew bootRun --args='--spring.profiles.active=nomis-hsqldb'
```

Use the repository's existing local authentication setup and test JWT helpers.
Do not disable endpoint security.

Ask Tim/the team whether they normally use HSQLDB or Oracle/T3 for local work
before an Oracle smoke test. Nobody has been contacted as part of this plan.
An authorised Oracle check should verify actual API-account table access,
Oracle pagination SQL and representative query/count performance. HSQLDB tests
do not establish Oracle permissions or deployed connectivity.

## Consumer loop

1. Request page 0 using a fixed size, such as 500.
2. Process/save content, then advance only after that batch succeeds.
3. Increment page until last=true; an empty table is a valid completed extract.
4. Retry transient failures on the same page, with bounded backoff.
5. Make destination writes replay-safe using (bookingId, functionType), and
   checkpoint only after processing succeeds. This API does not provide
   exactly-once downstream delivery.

Production consumer implementation and destination persistence are separate.
For a real move, stage/check the complete extract before publishing it. On a
restart from scratch, clear/isolate an incomplete staging run rather than
silently mixing it with a new run. Do not delete the NOMIS source in this spike.

## Completeness assumption and migration checks

Full-key sorting plus offset pagination visits every row of an UNCHANGED table.
It is not a point-in-time guarantee if rows change during extraction.

- Establish with the data owner whether writes still occur and choose a quiet
  window or agreed write-freeze for the actual move if appropriate.
- Creation/modification timestamps and equal row counts are useful signals,
  but do not prove inactivity or detect every deletion/replacement.
- Against a stable source, compare source/destination row counts, distinct
  composite keys and field values before declaring the move complete.
- If changes are discovered, pause the move and agree rerun/reconciliation or
  a consistent export mechanism; do not claim ordinary paging is lossless on
  a changing source.
- Do not add a cursor/snapshot framework unless that requirement is confirmed.

## Acceptance criteria

- Multiple bookings and multiple function types on one booking are returned.
- Concatenating small pages from stable fixtures yields all expected records
  exactly once in full-key order, with no gaps or duplicate composite keys.
- Cover empty results, partial and exactly-full final pages, beyond-end pages,
  invalid pagination, page-size capping and attempted sort override.
- Verify standard Page metadata and flat DTO fields; no related entities leak.
- Unauthenticated requests and callers without global-read permission fail;
  authorised callers can retrieve rows across bookings.
- Run focused tests and existing format/lint checks. Run affected existing
  assignment workflow tests if any shared code is modified.
- Verify bounded database pagination and assess query/count cost before a
  large extraction; no unbounded findAll() or unrelated dependency changes.
- Confirm destination validation/source-deletion decisions remain outside
  this read-only spike.
