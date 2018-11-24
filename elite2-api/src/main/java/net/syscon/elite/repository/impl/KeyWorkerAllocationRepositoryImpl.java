package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderKeyWorker;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
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
    public List<Keyworker> getAvailableKeyworkers(String agencyId) {
        final String sql = getQuery("GET_AVAILABLE_KEY_WORKERS");

        return jdbcTemplate.query(
                sql,
                createParams("agencyId", agencyId, "role", "KW"),
                KEY_WORKER_ROW_MAPPER);
    }

    @Override
    public Optional<Keyworker> getKeyworkerDetailsByBooking(Long bookingId) {
        Keyworker keyworker;

        try {
            keyworker = jdbcTemplate.queryForObject(
                    getQuery("GET_KEY_WORKER_DETAILS_FOR_OFFENDER"),
                    createParams("bookingId", bookingId, "currentDate", DateTimeConverter.toDate(LocalDate.now())),
                    KEY_WORKER_ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            keyworker = null;
        }

        return Optional.ofNullable(keyworker);
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworker(Long staffId, List<String> agencyIds) {
        return getAllocationDetailsForKeyworkers(Collections.singletonList(staffId), agencyIds);
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworkers(List<Long> staffIds, List<String> agencyIds) {
        String sql = getQuery("GET_ALLOCATION_DETAIL_FOR_KEY_WORKERS");

        return jdbcTemplate.query(
                sql,
                createParams("staffIds", staffIds, "agencyIds", agencyIds),
                KEY_WORKER_ALLOCATION_DETAIL_ROW_MAPPER);
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForOffenders(List<String> offenderNos, List<String> agencyIds) {
        String sql = getQuery("GET_ALLOCATION_DETAIL_FOR_OFFENDERS");

        return jdbcTemplate.query(
                sql,
                createParams("offenderNos", offenderNos, "agencyIds", agencyIds),
                KEY_WORKER_ALLOCATION_DETAIL_ROW_MAPPER);
    }

    @Override
    public boolean checkKeyworkerExists(Long staffId) {
        try {
            jdbcTemplate.queryForObject(getQuery("CHECK_KEY_WORKER_EXISTS"), createParams("staffId", staffId),
                    KEY_WORKER_ROW_MAPPER);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public Page<OffenderKeyWorker> getAllocationHistoryByAgency(String agencyId, PageRequest pageRequest) {
        Validate.notBlank(agencyId, "Agency id is required.");
        Validate.notNull(pageRequest, "Page request details are requreid.");

        String initialSql = getQuery("GET_ALLOCATION_HISTORY_BY_AGENCY");

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_KEY_WORKER_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addRowCount()
                .addPagination()
                .build();

        PageAwareRowMapper<OffenderKeyWorker> paRowMapper = new PageAwareRowMapper<>(OFFENDER_KEY_WORKER_ROW_MAPPER);

        final List<OffenderKeyWorker> results = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "agencyId", agencyId),
                paRowMapper);

        return new Page<>(results, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    public List<OffenderKeyWorker> getAllocationHistoryByOffenderNos(List<String> offenderNos) {
        Validate.notEmpty(offenderNos, "At least 1 offender No is required.");

        String sql = getQuery("GET_ALLOCATION_HISTORY_BY_OFFENDER");

        return jdbcTemplate.query(
                sql,
                createParams("offenderNos", offenderNos),
                OFFENDER_KEY_WORKER_ROW_MAPPER);
    }

    @Override
    public List<OffenderKeyWorker> getAllocationHistoryByStaffIds(List<Long> staffIds) {
        Validate.notEmpty(staffIds, "At least 1 staff Id is required.");

        String sql = getQuery("GET_ALLOCATION_HISTORY_BY_STAFF");

        return jdbcTemplate.query(
                sql,
                createParams("staffIds", staffIds),
                OFFENDER_KEY_WORKER_ROW_MAPPER);
    }

}
