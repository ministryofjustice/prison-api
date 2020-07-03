package net.syscon.prison.service.curfews;

import net.syscon.prison.api.model.ApprovalStatus;
import net.syscon.prison.api.model.HdcChecks;
import net.syscon.prison.api.model.HomeDetentionCurfew;

class ChecksPassedRefusedState extends CurfewState {

    ChecksPassedRefusedState(HomeDetentionCurfew curfew) {
        super(curfew);
    }

    @Override
    void doSetHdcChecks(HdcChecks hdcChecks) {
        if (hdcChecks.getPassed()) {
            if (!hdcChecks.getDate().equals(curfew.getChecksPassedDate())) {
                actions.setHdcChecksPassedDate(hdcChecks.getDate());
            }
        } else {
            doDeleteHdcChecks();
            actions.setHdcChecks(hdcChecks);
        }
    }

    @Override
    void doSetApprovalStatus(ApprovalStatus approvalStatus) {
        actions.setApprovalStatus(approvalStatus);
        actions.deleteTrackings(StatusTrackingCodes.REFUSED_CODE);

        if (!approvalStatus.isApproved()) {
            actions.setChecksPassedRefusedReason(approvalStatus.getRefusedReason());
        }
    }

    @Override
    void doDeleteHdcChecks() {
        actions.deleteHdcChecks();
        actions.deleteTrackings(StatusTrackingCodes.CHECKS_PASSED_AND_REFUSED_CODES);
    }

    @Override
    void doDeleteApprovalStatus() {
        actions.setApprovalStatus(ApprovalStatus.builder().build());
        actions.deleteTrackings(StatusTrackingCodes.REFUSED_CODE);
    }
}
