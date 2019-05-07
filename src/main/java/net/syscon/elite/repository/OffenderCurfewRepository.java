package net.syscon.elite.repository;

import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.HomeDetentionCurfew;
import net.syscon.elite.service.support.OffenderCurfew;

import java.util.Collection;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;


public interface OffenderCurfewRepository {
    /**
     * Retrieve the OffenderCurfews for every offender within the given agencies (prisons)
     * @param agencyIds The Ids of those agencies for which OffenderCurfews should be returned
     * @return A Collection of OffenderCurfew
     */
    Collection<OffenderCurfew> offenderCurfews(Set<String> agencyIds);

    void setHDCChecksPassed(long curfewId, HdcChecks hdcChecks);

    void setApprovalStatusForCurfew(long curfewId, ApprovalStatus approvalStatus);

    long createHdcStatusTracking(long curfewId, String statusCode);

    void createHdcStatusReason(long hdcStatusTrackingId, String statusReasonCode);

    Optional<HomeDetentionCurfew> getLatestHomeDetentionCurfew(long bookingId, Set<String> statusTrackingCodesToMatch);

    OptionalLong findHdcStatusTracking(long curfewId, String statusTrackingCode);
}
