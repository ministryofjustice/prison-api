package net.syscon.elite.repository;

import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.service.support.OffenderCurfew;

import java.util.Collection;
import java.util.Set;


public interface OffenderCurfewRepository {
    /**
     * Retrieve the OffenderCurfews for every offender within the given agencies (prisons)
     * @param agencyIds The Ids of those agencies for which OffenderCurfews should be returned
     * @return A Collection of OffenderCurfew
     */
    Collection<OffenderCurfew> offenderCurfews(Set<String> agencyIds);

    void setHDCChecksPassed(long bookingId, HdcChecks hdcChecks);

    void setApprovalStatusForLatestCurfew(long bookingId, ApprovalStatus approvalStatus);
}
