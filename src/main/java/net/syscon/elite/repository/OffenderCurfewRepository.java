package net.syscon.elite.repository;

import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.HomeDetentionCurfew;
import net.syscon.elite.service.support.OffenderCurfew;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;


public interface OffenderCurfewRepository {
    /**
     * Retrieve the OffenderCurfews for every offender within the given agencies (prisons)
     * @param agencyIds The Ids of those agencies for which OffenderCurfews should be returned
     * @return A Collection of OffenderCurfew
     */
    Collection<OffenderCurfew> offenderCurfews(Set<String> agencyIds);

    void setHDCChecksPassed(long bookingId, HdcChecks hdcChecks);

    void setApprovalStatusForCurfew(long curfewId, ApprovalStatus approvalStatus);

    long createHdcStatusTracking(long curfewId, String statusCode);

    void createHdcStatusReason(long hdcStatusTrackingId, String statusReasonCode);

    Optional<HomeDetentionCurfew> getLatestHomeDetentionCurfew(Long bookingId, String statusTrackingCodeToMatch);

    Optional<Long> getLatestHomeDetentionCurfewId(long bookingId);

    /**
     * Set th hdcStatusReason for the HDC curfew identified by curfewId to hdcStatusReason
     * @param curfewId
     * @param hdcStatusTrackingCode The status tracking code to match when searching for a record to update
     * @param hdcStatusReason
     * @return true if the HDC curfew already has an entry and it was updated otherwise false
     */
    boolean updateHdcStatusReason(Long curfewId, String hdcStatusTrackingCode, String hdcStatusReason);
}
