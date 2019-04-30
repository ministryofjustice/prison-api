package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.HomeDetentionCurfew;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.support.OffenderCurfew;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Slf4j
public class OffenderCurfewRepositoryImpl extends RepositoryBase implements OffenderCurfewRepository {

    private static final RowMapper<OffenderCurfew> OFFENDER_CURFEW_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderCurfew.class);
    private static final RowMapper<HomeDetentionCurfew> HOME_DETENTION_CURFEW_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(HomeDetentionCurfew.class);

    @Override
    public Collection<OffenderCurfew> offenderCurfews(final Set<String> agencyIds) {

        final var sql = queryBuilderFactory
                .getQueryBuilder(
                        getQuery("OFFENDER_CURFEWS"),
                        Collections.emptyMap())
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("agencyLocationIds", agencyIds),
                OFFENDER_CURFEW_ROW_MAPPER);
    }

    @Override
    public void setHDCChecksPassed(final long bookingId, final HdcChecks hdcChecks) {
        final var rowsUpdated = jdbcTemplate.update(
                getQuery("UPDATE_CURFEW_CHECKS_PASSED"),
                createParams(
                        "bookingId", bookingId,
                        "date", hdcChecks.getDate(),
                        "checksPassed", hdcChecks.checksPassed()
                )
        );
        if (rowsUpdated < 1) {
            throw new EntityNotFoundException("There is no curfew resource for bookingId " + bookingId);
        }
    }

    @Override
    public void setApprovalStatusForCurfew(final long curfewId, final ApprovalStatus approvalStatus) {
        jdbcTemplate.update(
                getQuery("UPDATE_APPROVAL_STATUS"),
                createParams(
                        "curfewId", curfewId,
                        "date", approvalStatus.getDate(),
                        "approvalStatus", approvalStatus.getApprovalStatus()
                )
        );
    }

    @Override
    public long createHdcStatusTracking(long curfewId, String statusCode) {
        final var generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                getQuery("CREATE_HDC_STATUS_TRACKING"),
                createParams(
                        "offenderCurfewId", curfewId,
                        "statusCode", statusCode),
                generatedKeyHolder,
                new String[] {"HDC_STATUS_TRACKING_ID"});

        return generatedKeyHolder.getKey().longValue();

    }

    @Override
    public void createHdcStatusReason(long hdcStatusTrackingId, String statusReasonCode) {
        jdbcTemplate.update(
            getQuery("CREATE_HDC_STATUS_REASON"),
            createParams(
                    "hdcStatusTrackingId", hdcStatusTrackingId,
                    "statusReasonCode", statusReasonCode)
        );
    }

    @Override
    public boolean updateHdcStatusReason(Long curfewId, String hdcStatusTrackingCode, String hdcStatusReason){
        return jdbcTemplate.update(
                getQuery("UPDATE_HDC_STATUS_REASON"),
                Map.of(
                      "offenderCurfewId", curfewId,
                      "hdcStatusTrackingCode", hdcStatusTrackingCode,
                      "hdcStatusReason", hdcStatusReason
                )
        ) > 0;
    }


    @Override
    public Optional<HomeDetentionCurfew> getLatestHomeDetentionCurfew(Long bookingId, String statusTrackingCodeToMatch) {
        val results = jdbcTemplate.query(
                getQuery("LATEST_HOME_DETENTION_CURFEW"),
                Map.of(
                        "bookingId", bookingId,
                        "statusTrackingCode", statusTrackingCodeToMatch),
                HOME_DETENTION_CURFEW_ROW_MAPPER);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<Long> getLatestHomeDetentionCurfewId(long bookingId) {
        val results = jdbcTemplate.queryForList(
                getQuery("LATEST_HOME_DETENTION_CURFEW_ID"),
                Map.of("bookingId", bookingId),
                Long.class
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
