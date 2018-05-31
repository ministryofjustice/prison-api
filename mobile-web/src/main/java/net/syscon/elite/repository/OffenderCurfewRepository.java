package net.syscon.elite.repository;

import net.syscon.elite.service.support.OffenderCurfew;

import java.util.Collection;
import java.util.Set;


public interface OffenderCurfewRepository {
    /**
     * Find the OFFENDER_BOOKING_ID of those offenders having no NOMIS approval status.
     * Limit results to those matching the supplied agencyFilter clause.
     */
    Collection<Long> offendersWithoutCurfewApprovalStatus(String agencyFilterClause);

    /**
     * Retrieve the OffenderCurfews for every offender within the given agencies (prisons)
     * @param agencyIds
     * @return A Collection of OffenderCurfew
     */
    Collection<OffenderCurfew> offenderCurfews(Set<String> agencyIds);
}
