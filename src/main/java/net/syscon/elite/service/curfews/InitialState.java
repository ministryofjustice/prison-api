package net.syscon.elite.service.curfews;

import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.HomeDetentionCurfew;

class InitialState extends CurfewState {
    InitialState(HomeDetentionCurfew curfew) {
        super(curfew);
    }

    @Override
    void doSetHdcChecks(HdcChecks hdcChecks) {
        actions.setHdcChecks(hdcChecks);
    }

    @Override
    void doSetApprovalStatus(ApprovalStatus approvalStatus) {
        throw new IllegalStateException("Approval status cannot be set when HDC checks passed/failed is not set.");
    }

    @Override
    void doDeleteHdcChecks() {
    }

    @Override
    void doDeleteApprovalStatus() {
    }
}
