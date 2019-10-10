package net.syscon.elite.repository;

import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.HomeDetentionCurfew;
import net.syscon.elite.service.support.OffenderCurfew;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;


public interface OffenderCurfewRepository {
    /**
     * Retrieve the OffenderCurfews for every offender within the given agencies (prisons)
     *
     * @param agencyIds The Ids of those agencies for which OffenderCurfews should be returned
     * @return A Collection of OffenderCurfew
     */
    Collection<OffenderCurfew> offenderCurfews(Set<String> agencyIds);

    Optional<HomeDetentionCurfew> getLatestHomeDetentionCurfew(long bookingId, Set<String> statusTrackingCodesToMatch);

    void setHDCChecksPassed(long curfewId, HdcChecks hdcChecks);

    void setHdcChecksPassedDate(long curfewId, LocalDate date);

    void setApprovalStatus(long curfewId, ApprovalStatus approvalStatus);

    void setApprovalStatusDate(long curfewId, LocalDate date);

    long createHdcStatusTracking(long curfewId, String statusCode);

    OptionalLong findHdcStatusTracking(long curfewId, String statusTrackingCode);

    void deleteStatusTrackings(long curfewId, Set<String> statusTrackingCodesToMatch);

    void createHdcStatusReason(long hdcStatusTrackingId, String statusReasonCode);

    void deleteStatusReasons(long curfewId, Set<String> statusTrackingCodesToMatch);

    void resetCurfew(long curfewId);
}
