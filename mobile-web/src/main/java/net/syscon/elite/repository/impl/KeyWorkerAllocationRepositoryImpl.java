package net.syscon.elite.repository.impl;

import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class KeyWorkerAllocationRepositoryImpl extends RepositoryBase implements KeyWorkerAllocationRepository {
    private static final StandardBeanPropertyRowMapper<KeyWorkerAllocation> KEY_WORKER_ALLOCATION_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(KeyWorkerAllocation.class);

    @Override
    public void createAllocation(KeyWorkerAllocation allocation, String username) {
        String sql = getQuery("INSERT_KEY_WORKER_ALLOCATION");
        final LocalDateTime localDateTime = LocalDateTime.now();

        Timestamp createdDateTime = DateTimeConverter.fromLocalDateTime(localDateTime);
        java.sql.Date createdDate = DateTimeConverter.fromTimestamp(createdDateTime);

        jdbcTemplate.update(
                sql,
                createParams("bookingId", allocation.getBookingId(),
                        "staffId", allocation.getStaffId(),
                        "assignedDate", createdDate,
                        "assignedTime", createdDateTime,
                        "agencyId", allocation.getAgencyId(),
                        "active", "Y",
                        "reason", allocation.getReason(),
                        "type", allocation.getType(),
                        "userId", username));

    }

    @Override
    public void deactivateAllocationForOffenderBooking(Long bookingId, String reason, String username) {
        String sql = getQuery("DEACTIVATE_KEY_WORKER_ALLOCATION_FOR_OFFENDER_BOOKING");
        final LocalDateTime localDateTime = LocalDateTime.now();

        Timestamp now = DateTimeConverter.fromLocalDateTime(localDateTime);

        jdbcTemplate.update(
                sql,
                createParams("bookingId", bookingId,
                        "deallocationReason", reason,
                        "expiryDate", now));
    }

    @Override
    public void deactivateAllocationsForKeyWorker(Long staffId, String reason, String username) {
        String sql = getQuery("DEACTIVATE_KEY_WORKER_ALLOCATIONS_FOR_KEY_WORKER");
        final LocalDateTime localDateTime = LocalDateTime.now();

        Timestamp now = DateTimeConverter.fromLocalDateTime(localDateTime);

        jdbcTemplate.update(
                sql,
                createParams("staffId", staffId,
                        "deallocationReason", reason,
                        "expiryDate", now));
    }

    @Override
    public Optional<KeyWorkerAllocation> getCurrentAllocationForOffenderBooking(Long bookingId) {
        String sql = getQuery("GET_ACTIVE_ALLOCATION_FOR_OFFENDER_BOOKING");

        return getKeyWorkerAllocationByOffenderBooking(bookingId, sql);
    }

    @Override
    public Optional<KeyWorkerAllocation> getLatestAllocationForOffenderBooking(Long bookingId) {
        String sql = getQuery("GET_LATEST_ALLOCATION_FOR_OFFENDER_BOOKING");

        return getKeyWorkerAllocationByOffenderBooking(bookingId, sql);
    }

    @Override
    public List<KeyWorkerAllocation> getAllocationHistoryForPrisoner(Long offenderId, String orderByFields, Order order) {
        String initialSql = getQuery("GET_ALLOCATION_HISTORY_FOR_OFFENDER");

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, KEY_WORKER_ALLOCATION_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        final List<KeyWorkerAllocation> allocation = jdbcTemplate.query(
                sql,
                createParams("offenderId", offenderId),
                KEY_WORKER_ALLOCATION_ROW_MAPPER);

        return allocation;
    }

    private Optional<KeyWorkerAllocation> getKeyWorkerAllocationByOffenderBooking(Long bookingId, String sql) {
        KeyWorkerAllocation allocation;
        try {
            allocation = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId),
                    KEY_WORKER_ALLOCATION_ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            allocation = null;
        }

        return Optional.ofNullable(allocation);
    }

}
