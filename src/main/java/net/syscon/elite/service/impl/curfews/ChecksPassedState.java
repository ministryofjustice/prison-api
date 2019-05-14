package net.syscon.elite.service.impl.curfews;

import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.HomeDetentionCurfew;

import javax.ws.rs.NotFoundException;

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
