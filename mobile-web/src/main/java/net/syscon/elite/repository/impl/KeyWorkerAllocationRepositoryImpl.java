package net.syscon.elite.repository.impl;

import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.DateTimeConverter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class KeyWorkerAllocationRepositoryImpl extends RepositoryBase implements KeyWorkerAllocationRepository {
    private static final RowMapper<KeyWorkerAllocation> KEY_WORKER_ALLOCATION_ROW_MAPPER =
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
    public void deactivateCurrentAllocationForOffenderBooking(Long bookingId, String username) {
        String sql = getQuery("DEACTIVATE_KEY_WORKER_ALLOCATION_FOR_OFFENDER_BOOKING");
        final LocalDateTime localDateTime = LocalDateTime.now();

        Timestamp now = DateTimeConverter.fromLocalDateTime(localDateTime);

        jdbcTemplate.update(
                sql,
                createParams("bookingId", bookingId,
                        "expiryDate", now,
                        "modifyTimestamp", now,
                        "userId", username));
    }

    @Override
    /* TODO determine usage:
       will throw EmptyResultDataAccessException if no current active allocation exists
       Optional may be be appropriate */
    public KeyWorkerAllocation getCurrentAllocationForOffenderBooking(Long bookingId) {
        String sql = getQuery("GET_ACTIVE_ALLOCATION_FOR_OFFENDER_BOOKING");

        final KeyWorkerAllocation allocation = jdbcTemplate.queryForObject(
                sql,
                createParams("bookingId", bookingId),
                KEY_WORKER_ALLOCATION_ROW_MAPPER);

        return allocation;
    }

    @Override
    public List<KeyWorkerAllocation> getAllocationHistoryForPrisoner(Long offenderId) {
        String sql = getQuery("GET_ALLOCATION_HISTORY_FOR_OFFENDER");

        final List<KeyWorkerAllocation> allocation = jdbcTemplate.query(
                sql,
                createParams("offenderId", offenderId),
                KEY_WORKER_ALLOCATION_ROW_MAPPER);

        return allocation;
    }


}
