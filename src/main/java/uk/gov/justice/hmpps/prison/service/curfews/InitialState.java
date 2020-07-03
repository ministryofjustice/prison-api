package uk.gov.justice.hmpps.prison.service.curfews;

import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus;
import uk.gov.justice.hmpps.prison.api.model.HdcChecks;
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew;

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
