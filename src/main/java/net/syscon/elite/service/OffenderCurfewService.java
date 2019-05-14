package net.syscon.elite.service;

import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.HomeDetentionCurfew;
import net.syscon.elite.api.model.OffenderSentenceCalc;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OffenderCurfewService {

    List<OffenderSentenceCalc> getHomeDetentionCurfewCandidates(String username, Optional<LocalDate> minimumChecksPassedDateForAssessedCurfews);

    void setHdcChecks(long bookingId, @Valid HdcChecks hdcChecks);

    void setApprovalStatus(long bookingId, @Valid ApprovalStatus approvalStatus);

    HomeDetentionCurfew getLatestHomeDetentionCurfew(long bookingId);

    void deleteHdcChecks(Long bookingId);

    void deleteApprovalStatus(Long bookingId);
}
