package net.syscon.elite.api.resource.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.OffenderSentenceResource;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.curfews.OffenderCurfewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("${api.base.path}/offender-sentences")
public class OffenderReleaseResourceImpl implements OffenderSentenceResource {
    private final AuthenticationFacade authenticationFacade;
    private final BookingService bookingService;
    private final OffenderCurfewService offenderCurfewService;

    public OffenderReleaseResourceImpl(
            final AuthenticationFacade authenticationFacade,
            final BookingService bookingService,
            final OffenderCurfewService offenderCurfewService) {
        this.authenticationFacade = authenticationFacade;
        this.bookingService = bookingService;
        this.offenderCurfewService = offenderCurfewService;
    }

    @Override
    public List<OffenderSentenceDetail> getOffenderSentences(final String agencyId, final List<String> offenderNos) {
        return bookingService.getOffenderSentencesSummary(
                agencyId,
                offenderNos);
    }

    @Override
    public List<OffenderSentenceCalc> getOffenderSentencesHomeDetentionCurfewCandidates() {
        return offenderCurfewService.getHomeDetentionCurfewCandidates(authenticationFacade.getCurrentUsername());
    }

    @Override
    public HomeDetentionCurfew getLatestHomeDetentionCurfew(Long bookingId) {
        return offenderCurfewService.getLatestHomeDetentionCurfew(bookingId);
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> setCurfewChecks(final Long bookingId, final HdcChecks hdcChecks) {
        offenderCurfewService.setHdcChecks(bookingId, hdcChecks);
        return ResponseEntity.noContent().build();
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> clearCurfewChecks(Long bookingId) {
        offenderCurfewService.deleteHdcChecks(bookingId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> setApprovalStatus(final Long bookingId, final ApprovalStatus approvalStatus) {

        offenderCurfewService.setApprovalStatus(bookingId, approvalStatus);
        return ResponseEntity.noContent().build();
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> clearApprovalStatus(Long bookingId) {
        offenderCurfewService.deleteApprovalStatus(bookingId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public List<OffenderSentenceDetail> postOffenderSentences(final List<String> offenderNos) {
        validateOffenderList(offenderNos);

        //no agency id filter required here as offenderNos will always be provided
        return bookingService.getOffenderSentencesSummary(null, offenderNos);

    }

    @Override
    public List<OffenderSentenceDetail> postOffenderSentencesBookings(final List<Long> bookingIds) {
        validateOffenderList(bookingIds);
        return bookingService.getBookingSentencesSummary(bookingIds);
    }

    @Override
    public List<OffenderSentenceTerms> getOffenderSentenceTerms(final Long bookingId) {
        return bookingService.getOffenderSentenceTerms(bookingId);
    }

    private void validateOffenderList(final List<?> offenderList) {
        if (CollectionUtils.isEmpty(offenderList)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "List of Offender Ids must be provided");
        }
    }
}
