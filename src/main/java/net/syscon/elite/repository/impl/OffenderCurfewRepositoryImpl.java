package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.HomeDetentionCurfew;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.support.OffenderCurfew;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Slf4j
public class OffenderCurfewRepositoryImpl extends RepositoryBase implements OffenderCurfewRepository {

    private static class AlmostStandardBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {
        AlmostStandardBeanPropertyRowMapper(Class<T> clazz) {
            super(clazz);
        }

        @Override
        protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
            if (Boolean.class == pd.getPropertyType()) {
                val value = rs.getString(index);
                if ("Y".equals(value)) {
                    return Boolean.TRUE;
                }
                if ("N".equals(value)) {
                    return Boolean.FALSE;
                }
                return null;
            }
            return super.getColumnValue(rs, index, pd);
        }
    }

    private static final RowMapper<OffenderCurfew> OFFENDER_CURFEW_ROW_MAPPER = new AlmostStandardBeanPropertyRowMapper<>(OffenderCurfew.class);
    private static final RowMapper<HomeDetentionCurfew> HOME_DETENTION_CURFEW_ROW_MAPPER = new AlmostStandardBeanPropertyRowMapper<>(HomeDetentionCurfew.class){
    };

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
    public void setHDCChecksPassed(final long curfewId, final HdcChecks hdcChecks) {
        final var rowsUpdated = jdbcTemplate.update(
                getQuery("UPDATE_CURFEW_CHECKS_PASSED"),
                createParams(
                        "curfewId", curfewId,
                        "date", hdcChecks.getDate(),
                        "checksPassed", hdcChecks.checksPassed()
                )
        );
        if (rowsUpdated < 1) {
            throw new EntityNotFoundException(String.format("OFFENDER_CURFEWS record for id %1$d not found.", curfewId));
        }
    }

    @Override
    public void setApprovalStatusForCurfew(final long curfewId, final ApprovalStatus approvalStatus) {
        val rowsUpdated = jdbcTemplate.update(
                getQuery("UPDATE_APPROVAL_STATUS"),
                createParams(
                        "curfewId", curfewId,
                        "date", approvalStatus.getDate(),
                        "approvalStatus", approvalStatus.getApprovalStatus()
                )
        );
        if (rowsUpdated < 1)
            throw new EntityNotFoundException(String.format("OFFENDER_CURFEWS having id %1$d not found.", curfewId));
    }

    @Override
    public OptionalLong findHdcStatusTracking(long curfewId, String statusTrackingCode) {
        val hdsStatusTrackingIds = jdbcTemplate.queryForList(
                getQuery("FIND_HDC_STATUS_TRACKING"),
                createParams(
                        "curfewId", curfewId,
                        "statusCode", statusTrackingCode),
                Long.class
        );
        return hdsStatusTrackingIds.isEmpty() ? OptionalLong.empty() : OptionalLong.of(hdsStatusTrackingIds.get(0));
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
                new String[]{"HDC_STATUS_TRACKING_ID"});

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
    public Optional<HomeDetentionCurfew> getLatestHomeDetentionCurfew(long bookingId, Set<String> statusTrackingCodesToMatch) {
        val results = jdbcTemplate.query(
                getQuery("LATEST_HOME_DETENTION_CURFEW"),
                Map.of(
                        "bookingId", bookingId,
                        "statusTrackingCodes", statusTrackingCodesToMatch),
                HOME_DETENTION_CURFEW_ROW_MAPPER);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
