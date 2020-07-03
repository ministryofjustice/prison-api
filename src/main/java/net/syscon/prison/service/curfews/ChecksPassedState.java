package net.syscon.prison.service.curfews;

import net.syscon.prison.api.model.ApprovalStatus;
import net.syscon.prison.api.model.HdcChecks;
import net.syscon.prison.api.model.HomeDetentionCurfew;

class ChecksPassedState extends CurfewState {

    ChecksPassedState(HomeDetentionCurfew curfew) {
        super(curfew);
    }

    @Override
    void doSetHdcChecks(HdcChecks hdcChecks) {
        if (!hdcChecks.getPassed()) {
            actions.deleteTrackings(StatusTrackingCodes.CHECKS_PASSED_CODES);
            actions.setHdcChecks(hdcChecks);
        } else if (!hdcChecks.getDate().equals(curfew.getChecksPassedDate())) {
            actions.setHdcChecksPassedDate(hdcChecks.getDate());
        }
    }

    @Override
    void doSetApprovalStatus(ApprovalStatus approvalStatus) {
        actions.setApprovalStatus(approvalStatus);

        if (!approvalStatus.isApproved()) {
            actions.setChecksPassedRefusedReason(approvalStatus.getRefusedReason());
        }
    }

    @Override
    void doDeleteHdcChecks() {
        actions.deleteHdcChecks();
        actions.deleteTrackings(StatusTrackingCodes.CHECKS_PASSED_CODES);
    }

    @Override
    void doDeleteApprovalStatus() {
    }
}
