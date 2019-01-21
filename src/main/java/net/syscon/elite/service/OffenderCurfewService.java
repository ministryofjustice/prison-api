package net.syscon.elite.service;

import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.OffenderSentenceCalc;

import javax.validation.Valid;
import java.util.List;

public interface OffenderCurfewService {

    List<OffenderSentenceCalc> getHomeDetentionCurfewCandidates(String username);

    void setHdcChecks(long bookingId, @Valid HdcChecks hdcChecks);

    void setApprovalStatus(long bookingId, @Valid ApprovalStatus approvalStatus);
}
