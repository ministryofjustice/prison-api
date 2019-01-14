package net.syscon.elite.service;

import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.OffenderSentenceDetail;

import javax.validation.Valid;
import java.util.List;

public interface OffenderCurfewService {

    List<OffenderSentenceDetail> getHomeDetentionCurfewCandidates(String username, boolean newVersion);

    void setHdcChecks(long bookingId, @Valid HdcChecks hdcChecks);

    void setApprovalStatus(long bookingId, @Valid ApprovalStatus approvalStatus);
}
