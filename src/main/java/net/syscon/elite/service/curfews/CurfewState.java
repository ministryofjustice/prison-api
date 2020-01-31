package net.syscon.elite.service.curfews;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.HomeDetentionCurfew;

@Slf4j
abstract class CurfewState {
    final HomeDetentionCurfew curfew;
    CurfewActions actions;

    CurfewState(HomeDetentionCurfew curfew) {
        this.curfew = curfew;
    }

    static CurfewState getState(HomeDetentionCurfew curfew) {
        if (curfew.getPassed() == null) {
            return new InitialState(curfew);
        }

        if (curfew.getPassed()) {
            if (curfew.getApprovalStatus() == null) {
                return new ChecksPassedState(curfew);
            } else if (ApprovalStatus.APPROVED_STATUS.equals(curfew.getApprovalStatus())) {
                return new ChecksPassedApprovedState(curfew);
            } else {
                return new ChecksPassedRefusedState(curfew);
            }
        } else {
            if (curfew.getApprovalStatus() == null) {
                return new ChecksFailedState(curfew);
            } else {
                return new ChecksFailedRefusedState(curfew);
            }
        }
    }

    final CurfewState with(CurfewActions actions) {
        this.actions = actions;
        return this;
    }

    final void setHdcChecks(final HdcChecks hdcChecks) {
        log.debug("({}) Set {}", className(), hdcChecks);
        assertCurfewActions();
        doSetHdcChecks(hdcChecks);
    }

    final void setApprovalStatus(final ApprovalStatus approvalStatus) {
        log.debug("({}) Set {}", className(), approvalStatus);
        assertCurfewActions();
        doSetApprovalStatus(approvalStatus);
    }

    final void deleteHdcChecks() {
        log.debug("({}) Delete HDC Check", className());
        assertCurfewActions();
        doDeleteHdcChecks();
    }

    final void deleteApprovalStatus() {
        log.debug("({}) Delete Approval Status", className());
        assertCurfewActions();
        doDeleteApprovalStatus();
    }

    abstract void doSetHdcChecks(HdcChecks hdcChecks);

    abstract void doSetApprovalStatus(ApprovalStatus approvalStatus);

    abstract void doDeleteHdcChecks();

    abstract void doDeleteApprovalStatus();

    private void assertCurfewActions() {
        if (actions == null) throw new IllegalStateException("Missing CurfewActions");
    }

    private String className() {
        val fullName = getClass().getName();
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }
}
