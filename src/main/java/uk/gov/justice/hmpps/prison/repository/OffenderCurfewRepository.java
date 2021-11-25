package uk.gov.justice.hmpps.prison.repository;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus;
import uk.gov.justice.hmpps.prison.api.model.HdcChecks;
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew;
import uk.gov.justice.hmpps.prison.repository.sql.OffenderCurfewRepositorySql;
import uk.gov.justice.hmpps.prison.service.support.OffenderCurfew;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

@Repository
@Slf4j
public class OffenderCurfewRepository extends RepositoryBase {

    private static class AlmostStandardBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {
        AlmostStandardBeanPropertyRowMapper(Class<T> clazz) {
            super(clazz);
        }


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
    private static final RowMapper<HomeDetentionCurfew> HOME_DETENTION_CURFEW_ROW_MAPPER = new AlmostStandardBeanPropertyRowMapper<>(HomeDetentionCurfew.class) {
    };


    public Collection<OffenderCurfew> offenderCurfews(final Set<String> agencyIds) {

        final var sql = queryBuilderFactory
                .getQueryBuilder(
                        OffenderCurfewRepositorySql.OFFENDER_CURFEWS.getSql(),
                        Collections.emptyMap())
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("agencyLocationIds", agencyIds),
                OFFENDER_CURFEW_ROW_MAPPER);
    }


    public void setHDCChecksPassed(final long curfewId, final HdcChecks hdcChecks) {
        jdbcTemplate.update(
                OffenderCurfewRepositorySql.UPDATE_CURFEW_CHECKS_PASSED.getSql(),
                createParams(
                        "curfewId", curfewId,
                        "date", hdcChecks.getDate(),
                        "checksPassed", hdcChecks.checksPassed()
                )
        );
    }


    public void setHdcChecksPassedDate(long curfewId, LocalDate date) {
        jdbcTemplate.update(
                OffenderCurfewRepositorySql.UPDATE_CURFEW_CHECKS_PASSED_DATE.getSql(),
                createParams(
                        "curfewId", curfewId,
                        "date", date
                )
        );
    }


    public void setApprovalStatus(final long curfewId, final ApprovalStatus approvalStatus) {
        jdbcTemplate.update(
                OffenderCurfewRepositorySql.UPDATE_APPROVAL_STATUS.getSql(),
                createParams(
                        "curfewId", curfewId,
                        "date", approvalStatus.getDate(),
                        "approvalStatus", approvalStatus.getApprovalStatus()
                )
        );
    }


    public void setApprovalStatusDate(long curfewId, LocalDate date) {
        jdbcTemplate.update(
                OffenderCurfewRepositorySql.UPDATE_APPROVAL_STATUS_DATE.getSql(),
                createParams(
                        "curfewId", curfewId,
                        "date", date
                )
        );
    }


    public OptionalLong findHdcStatusTracking(long curfewId, String statusTrackingCode) {
        val hdsStatusTrackingIds = jdbcTemplate.queryForList(
                OffenderCurfewRepositorySql.FIND_HDC_STATUS_TRACKING.getSql(),
                createParams(
                        "curfewId", curfewId,
                        "statusCode", statusTrackingCode),
                Long.class
        );
        return hdsStatusTrackingIds.isEmpty() ? OptionalLong.empty() : OptionalLong.of(hdsStatusTrackingIds.get(0));
    }


    public void deleteStatusReasons(long curfewId, Set<String> statusTrackingCodesToMatch) {
        jdbcTemplate.update(
                OffenderCurfewRepositorySql.DELETE_HDC_STATUS_REASONS.getSql(),
                createParams(
                        "curfewId", curfewId,
                        "codes", statusTrackingCodesToMatch
                )
        );
    }


    public void deleteStatusTrackings(long curfewId, Set<String> statusTrackingCodesToMatch) {
        jdbcTemplate.update(
                OffenderCurfewRepositorySql.DELETE_HDC_STATUS_TRACKINGS.getSql(),
                createParams(
                        "curfewId", curfewId,
                        "codes", statusTrackingCodesToMatch
                )
        );
    }


    public long createHdcStatusTracking(long curfewId, String statusCode) {
        final var generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                OffenderCurfewRepositorySql.CREATE_HDC_STATUS_TRACKING.getSql(),
                createParams(
                        "offenderCurfewId", curfewId,
                        "statusCode", statusCode),
                generatedKeyHolder,
                new String[]{"HDC_STATUS_TRACKING_ID"});

        return generatedKeyHolder.getKey().longValue();

    }


    public void createHdcStatusReason(long hdcStatusTrackingId, String statusReasonCode) {
        jdbcTemplate.update(
                OffenderCurfewRepositorySql.CREATE_HDC_STATUS_REASON.getSql(),
                createParams(
                        "hdcStatusTrackingId", hdcStatusTrackingId,
                        "statusReasonCode", statusReasonCode)
        );
    }


    public Optional<HomeDetentionCurfew> getLatestHomeDetentionCurfew(long bookingId, Set<String> statusTrackingCodesToMatch) {
        val results = jdbcTemplate.query(
                OffenderCurfewRepositorySql.LATEST_HOME_DETENTION_CURFEW.getSql(),
                createParams(
                        "bookingId", bookingId,
                        "statusTrackingCodes", statusTrackingCodesToMatch),
                HOME_DETENTION_CURFEW_ROW_MAPPER);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }


    public List<HomeDetentionCurfew> getBatchLatestHomeDetentionCurfew(List<Long> bookingIds, Set<String> statusTrackingCodesToMatch) {
        val results = jdbcTemplate.query(
                OffenderCurfewRepositorySql.LATEST_BATCH_HOME_DETENTION_CURFEW.getSql(),
                createParams(
                  "bookingIds", bookingIds,
                  "statusTrackingCodes", statusTrackingCodesToMatch),
                  HOME_DETENTION_CURFEW_ROW_MAPPER
                );
        return results;
    }


    public void resetCurfew(long curfewId) {
        jdbcTemplate.update(
                OffenderCurfewRepositorySql.RESET_OFFENDER_CURFEW.getSql(),
                createParams("curfewId", curfewId));
    }
}
