package net.syscon.elite.repository;

import java.util.Collection;


public interface OffenderCurfewRepository {
    /**
     * Find the OFFENDER_BOOKING_ID of those offenders having no NOMIS approval status.
     * Limit results to those matching the supplied agencyFilter clause.
     */
    Collection<Long> offendersWithoutCurfewApprovalStatus(String agencyFilterClause);
}
