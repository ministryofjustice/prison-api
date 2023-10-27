package uk.gov.justice.hmpps.prison.repository;

import org.apache.commons.lang3.Validate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.KeyWorkerAllocationDetail;
import uk.gov.justice.hmpps.prison.api.model.KeyWorkerAllocationDetailDto;
import uk.gov.justice.hmpps.prison.api.model.Keyworker;
import uk.gov.justice.hmpps.prison.api.model.KeyworkerDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyWorker;
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyWorkerDto;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.KeyWorkerAllocationRepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class KeyWorkerAllocationRepository extends RepositoryBase {

    private static final RowMapper<KeyWorkerAllocationDetailDto> KEY_WORKER_ALLOCATION_DETAIL_ROW_MAPPER =
            new DataClassByColumnRowMapper<>(KeyWorkerAllocationDetailDto.class);

    private static final RowMapper<KeyworkerDto> KEY_WORKER_ROW_MAPPER =
            new DataClassByColumnRowMapper<>(KeyworkerDto.class);

    private static final DataClassByColumnRowMapper<OffenderKeyWorkerDto> OFFENDER_KEY_WORKER_ROW_MAPPER =
            new DataClassByColumnRowMapper<>(OffenderKeyWorkerDto.class);



    public List<Keyworker> getAvailableKeyworkers(final String agencyId) {
        final var sql = KeyWorkerAllocationRepositorySql.GET_AVAILABLE_KEY_WORKERS.getSql();

        final var keyworkers = jdbcTemplate.query(
                sql,
                createParams("agencyId", agencyId, "role", "KW"),
                KEY_WORKER_ROW_MAPPER);
        return keyworkers.stream().map(KeyworkerDto::toKeyworker).toList();
    }


    public Optional<Keyworker> getKeyworkerDetailsByBooking(final Long bookingId) {
        KeyworkerDto keyworker;

        try {
            keyworker = jdbcTemplate.queryForObject(
                    KeyWorkerAllocationRepositorySql.GET_KEY_WORKER_DETAILS_FOR_OFFENDER.getSql(),
                    createParams("bookingId", bookingId, "currentDate", DateTimeConverter.toDate(LocalDate.now())),
                    KEY_WORKER_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            keyworker = null;
        }

        return Optional.ofNullable(keyworker).map(KeyworkerDto::toKeyworker);
    }


    public List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworkers(final List<Long> staffIds, final List<String> agencyIds) {
        final var sql = KeyWorkerAllocationRepositorySql.GET_ALLOCATION_DETAIL_FOR_KEY_WORKERS.getSql();

        final var details = jdbcTemplate.query(
                sql,
                createParams("staffIds", staffIds, "agencyIds", agencyIds),
                KEY_WORKER_ALLOCATION_DETAIL_ROW_MAPPER);
        return details.stream().map(KeyWorkerAllocationDetailDto::toKeyWorkerAllocationDetail).toList();
    }


    public List<KeyWorkerAllocationDetail> getAllocationDetailsForOffenders(final List<String> offenderNos, final List<String> agencyIds) {
        final var sql = KeyWorkerAllocationRepositorySql.GET_ALLOCATION_DETAIL_FOR_OFFENDERS.getSql();

        final var details = jdbcTemplate.query(
                sql,
                createParams("offenderNos", offenderNos, "agencyIds", agencyIds),
                KEY_WORKER_ALLOCATION_DETAIL_ROW_MAPPER);
        return details.stream().map(KeyWorkerAllocationDetailDto::toKeyWorkerAllocationDetail).toList();
    }


    public boolean checkKeyworkerExists(final Long staffId) {
        try {
            jdbcTemplate.queryForObject(KeyWorkerAllocationRepositorySql.CHECK_KEY_WORKER_EXISTS.getSql(), createParams("staffId", staffId),
                    KEY_WORKER_ROW_MAPPER);
            return true;
        } catch (final EmptyResultDataAccessException e) {
            return false;
        }
    }


    public List<OffenderKeyWorker> getAllocationHistoryByOffenderNos(final List<String> offenderNos) {
        Validate.notEmpty(offenderNos, "At least 1 offender No is required.");

        final var sql = KeyWorkerAllocationRepositorySql.GET_ALLOCATION_HISTORY_BY_OFFENDER.getSql();

        final var results = jdbcTemplate.query(
                sql,
                createParams("offenderNos", offenderNos),
                OFFENDER_KEY_WORKER_ROW_MAPPER);
        return results.stream().map(OffenderKeyWorkerDto::toOffenderKeyWorker).toList();
    }
}
