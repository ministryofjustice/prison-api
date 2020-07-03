package net.syscon.prison.service.curfews;

import net.syscon.prison.api.model.ApprovalStatus;
import net.syscon.prison.api.model.HdcChecks;
import net.syscon.prison.api.model.HomeDetentionCurfew;

class ChecksPassedApprovedState extends CurfewState {
    ChecksPassedApprovedState(HomeDetentionCurfew curfew) {
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
        if (approvalStatus.isApproved()) {
            if (!approvalStatus.getDate().equals(curfew.getApprovalStatusDate())) {
                actions.setApprovalStatusDate(approvalStatus.getDate());
            }
        } else {
            actions.deleteTrackings(StatusTrackingCodes.GRANTED_CODE);
            actions.setApprovalStatus(approvalStatus);
            actions.setChecksPassedRefusedReason(approvalStatus.getRefusedReason());
        }
    }

    @Override
    void doDeleteHdcChecks() {
        actions.deleteHdcChecks();
        actions.deleteTrackings(StatusTrackingCodes.CHECKS_PASSED_AND_GRANTED_CODES);
    }

    @Override
    void doDeleteApprovalStatus() {
        actions.setApprovalStatus(ApprovalStatus.builder().build());
        actions.deleteTrackings(StatusTrackingCodes.GRANTED_CODE);
    }
}
