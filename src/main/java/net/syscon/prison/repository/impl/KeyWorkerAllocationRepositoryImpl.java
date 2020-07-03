package net.syscon.prison.repository.impl;

import net.syscon.prison.api.model.KeyWorkerAllocationDetail;
import net.syscon.prison.api.model.Keyworker;
import net.syscon.prison.api.model.OffenderKeyWorker;
import net.syscon.prison.api.support.Page;
import net.syscon.prison.api.support.PageRequest;
import net.syscon.prison.repository.KeyWorkerAllocationRepository;
import net.syscon.prison.repository.mapping.PageAwareRowMapper;
import net.syscon.prison.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.DateTimeConverter;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class KeyWorkerAllocationRepositoryImpl extends RepositoryBase implements KeyWorkerAllocationRepository {

    private static final StandardBeanPropertyRowMapper<KeyWorkerAllocationDetail> KEY_WORKER_ALLOCATION_DETAIL_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(KeyWorkerAllocationDetail.class);

    private static final StandardBeanPropertyRowMapper<Keyworker> KEY_WORKER_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(Keyworker.class);

    private static final StandardBeanPropertyRowMapper<OffenderKeyWorker> OFFENDER_KEY_WORKER_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderKeyWorker.class);


    @Override
    public List<Keyworker> getAvailableKeyworkers(final String agencyId) {
        final var sql = getQuery("GET_AVAILABLE_KEY_WORKERS");

        return jdbcTemplate.query(
                sql,
                createParams("agencyId", agencyId, "role", "KW"),
                KEY_WORKER_ROW_MAPPER);
    }

    @Override
    public Optional<Keyworker> getKeyworkerDetailsByBooking(final Long bookingId) {
        Keyworker keyworker;

        try {
            keyworker = jdbcTemplate.queryForObject(
                    getQuery("GET_KEY_WORKER_DETAILS_FOR_OFFENDER"),
                    createParams("bookingId", bookingId, "currentDate", DateTimeConverter.toDate(LocalDate.now())),
                    KEY_WORKER_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            keyworker = null;
        }

        return Optional.ofNullable(keyworker);
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworker(final Long staffId, final List<String> agencyIds) {
        return getAllocationDetailsForKeyworkers(Collections.singletonList(staffId), agencyIds);
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworkers(final List<Long> staffIds, final List<String> agencyIds) {
        final var sql = getQuery("GET_ALLOCATION_DETAIL_FOR_KEY_WORKERS");

        return jdbcTemplate.query(
                sql,
                createParams("staffIds", staffIds, "agencyIds", agencyIds),
                KEY_WORKER_ALLOCATION_DETAIL_ROW_MAPPER);
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForOffenders(final List<String> offenderNos, final List<String> agencyIds) {
        final var sql = getQuery("GET_ALLOCATION_DETAIL_FOR_OFFENDERS");

        return jdbcTemplate.query(
                sql,
                createParams("offenderNos", offenderNos, "agencyIds", agencyIds),
                KEY_WORKER_ALLOCATION_DETAIL_ROW_MAPPER);
    }

    @Override
    public boolean checkKeyworkerExists(final Long staffId) {
        try {
            jdbcTemplate.queryForObject(getQuery("CHECK_KEY_WORKER_EXISTS"), createParams("staffId", staffId),
                    KEY_WORKER_ROW_MAPPER);
            return true;
        } catch (final EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public Page<OffenderKeyWorker> getAllocationHistoryByAgency(final String agencyId, final PageRequest pageRequest) {
        Validate.notBlank(agencyId, "Agency id is required.");
        Validate.notNull(pageRequest, "Page request details are requreid.");

        final var initialSql = getQuery("GET_ALLOCATION_HISTORY_BY_AGENCY");

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_KEY_WORKER_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addRowCount()
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<OffenderKeyWorker>(OFFENDER_KEY_WORKER_ROW_MAPPER);

        final var results = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "agencyId", agencyId),
                paRowMapper);

        return new Page<>(results, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    public List<OffenderKeyWorker> getAllocationHistoryByOffenderNos(final List<String> offenderNos) {
        Validate.notEmpty(offenderNos, "At least 1 offender No is required.");

        final var sql = getQuery("GET_ALLOCATION_HISTORY_BY_OFFENDER");

        return jdbcTemplate.query(
                sql,
                createParams("offenderNos", offenderNos),
                OFFENDER_KEY_WORKER_ROW_MAPPER);
    }

    @Override
    public List<OffenderKeyWorker> getAllocationHistoryByStaffIds(final List<Long> staffIds) {
        Validate.notEmpty(staffIds, "At least 1 staff Id is required.");

        final var sql = getQuery("GET_ALLOCATION_HISTORY_BY_STAFF");

        return jdbcTemplate.query(
                sql,
                createParams("staffIds", staffIds),
                OFFENDER_KEY_WORKER_ROW_MAPPER);
    }

}
