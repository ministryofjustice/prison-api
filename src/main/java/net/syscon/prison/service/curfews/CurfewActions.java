package net.syscon.prison.service.curfews;

import lombok.extern.slf4j.Slf4j;
import net.syscon.prison.api.model.ApprovalStatus;
import net.syscon.prison.api.model.HdcChecks;
import net.syscon.prison.repository.OffenderCurfewRepository;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
class CurfewActions {
    private final long curfewId;
    private final OffenderCurfewRepository repository;

    CurfewActions(long curfewId, OffenderCurfewRepository repository) {
        this.curfewId = curfewId;
        this.repository = repository;
    }

    void setHdcChecks(HdcChecks hdcChecks) {
        log.debug("Set {}", hdcChecks);
        repository.setHDCChecksPassed(curfewId, hdcChecks);
    }

    void setHdcChecksPassedDate(LocalDate date) {
        log.debug("Set HDC checks passed date to {}.", date);
        repository.setHdcChecksPassedDate(curfewId, date);
    }

    void deleteTrackings(Set<String> hdcStatusTrackingCodes) {
        log.debug("Delete HDC Status Trackings having codes matching {}.", hdcStatusTrackingCodes);
        repository.deleteStatusReasons(curfewId, hdcStatusTrackingCodes);
        repository.deleteStatusTrackings(curfewId, hdcStatusTrackingCodes);
    }

    void setApprovalStatus(ApprovalStatus approvalStatus) {
        log.debug("Set {}", approvalStatus);
        repository.setApprovalStatus(curfewId, approvalStatus);
    }

    void setApprovalStatusDate(LocalDate date) {
        log.debug("Set Approval Status date to {}.", date);
        repository.setApprovalStatusDate(curfewId, date);
    }

    void setChecksPassedRefusedReason(String refusedReason) {
        log.debug("HDC Checks Passed. Set Approval status refused reason to {}.", refusedReason);
        long hdcStatusTrackingId = repository.createHdcStatusTracking(curfewId, StatusTrackingCodes.REFUSED);
        repository.createHdcStatusReason(hdcStatusTrackingId, refusedReason);
    }

    void setChecksFailedRefusedReason(String refusedReason) {
        log.debug("HDC Checks Failed. Set Approval Status refused reason to  {}.", refusedReason);
        repository
                .findHdcStatusTracking(curfewId, StatusTrackingCodes.MAN_CK_FAIL)
                .ifPresent(hdcStatusTrackingId -> repository.createHdcStatusReason(hdcStatusTrackingId, refusedReason));
    }

    void deleteChecksFailedRefusedReason() {
        log.debug("HDC Checks Failed. Delete Approval Status refused reason.");
        repository.deleteStatusReasons(curfewId, StatusTrackingCodes.MAN_CK_FAIL_CODE);
    }

    public void deleteHdcChecks() {
        log.debug("Delete HDC Checks (resets current curfew data to initial state)");
        repository.resetCurfew(curfewId);
    }
}
