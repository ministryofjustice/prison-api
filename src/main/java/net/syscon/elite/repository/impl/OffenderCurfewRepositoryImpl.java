package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.repository.OffenderCurfewRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.support.OffenderCurfew;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Repository
@Slf4j
public class OffenderCurfewRepositoryImpl extends RepositoryBase implements OffenderCurfewRepository {

    private static final RowMapper<OffenderCurfew> OFFENDER_CURFEW_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderCurfew.class);

    @Override
    public Collection<OffenderCurfew> offenderCurfews(Set<String> agencyIds) {

        String sql = queryBuilderFactory
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
    public void setHDCChecksPassed(long bookingId, HdcChecks hdcChecks) {
        final int rowsUpdated = jdbcTemplate.update(
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
    public void setApprovalStatusForLatestCurfew(long bookingId, ApprovalStatus approvalStatus) {
        int rowsUpdated = jdbcTemplate.update(
                getQuery("UPDATE_APPROVAL_STATUS"),
                createParams(
                        "bookingId", bookingId,
                        "date", approvalStatus.getDate(),
                        "approvalStatus", approvalStatus.getApprovalStatus()
                )
        );
        if (rowsUpdated < 1) {
            throw new EntityNotFoundException("There is no curfew resource for bookingId "  + bookingId);
        }
    }
}
