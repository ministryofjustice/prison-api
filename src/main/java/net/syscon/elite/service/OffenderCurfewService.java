package net.syscon.elite.service;

import net.syscon.elite.api.model.*;

import javax.validation.Valid;
import java.util.List;

public interface OffenderCurfewService {

    List<OffenderSentenceCalc> getHomeDetentionCurfewCandidates(String username);

    void setHdcChecks(long bookingId, @Valid HdcChecks hdcChecks);

    void setApprovalStatus(long bookingId, @Valid ApprovalStatus approvalStatus);

    HomeDetentionCurfew getLatestHomeDetentionCurfew(long bookingId);

    void deleteHdcChecks(Long bookingId);

    void deleteApprovalStatus(Long bookingId);
}
