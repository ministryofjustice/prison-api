package net.syscon.elite.api.resource.impl;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.OffenderSentenceTerms;
import net.syscon.elite.api.resource.OffenderSentenceResource;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.OffenderCurfewService;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestResource
@Path("/offender-sentences")
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
    public GetOffenderSentencesResponse getOffenderSentences(final String agencyId, final List<String> offenderNos) {
        val sentences = bookingService.getOffenderSentencesSummary(
                agencyId,
                authenticationFacade.getCurrentUsername(),
                offenderNos);

        return GetOffenderSentencesResponse.respond200WithApplicationJson(sentences);
    }

    @Override
    public GetOffenderSentencesHomeDetentionCurfewCandidatesResponse getOffenderSentencesHomeDetentionCurfewCandidates( LocalDate minimumChecksPassedDateForAssessedCurfews) {
        val sentences =
                offenderCurfewService.getHomeDetentionCurfewCandidates(authenticationFacade.getCurrentUsername(), Optional.ofNullable(minimumChecksPassedDateForAssessedCurfews));

        return GetOffenderSentencesHomeDetentionCurfewCandidatesResponse.respond200WithApplicationJson(sentences);
    }

    @Override
    public Response getLatestHomeDetentionCurfew(Long bookingId) {
        return Response.ok(
                        offenderCurfewService.getLatestHomeDetentionCurfew(bookingId),
                        MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    @Override
    @ProxyUser
    public Response setCurfewChecks(final Long bookingId, final HdcChecks hdcChecks) {
            offenderCurfewService.setHdcChecks(bookingId, hdcChecks);
            return Response.noContent().build();
    }

    @Override
    @ProxyUser
    public Response clearCurfewChecks(Long bookingId) {
        offenderCurfewService.deleteHdcChecks(bookingId);
        return Response.noContent().build();
    }

    @Override
    @ProxyUser
    public Response setApprovalStatus(final Long bookingId, final ApprovalStatus approvalStatus) {

            offenderCurfewService.setApprovalStatus(bookingId, approvalStatus);
            return Response.noContent().build();
    }

    @Override
    @ProxyUser
    public Response clearApprovalStatus(Long bookingId) {
        offenderCurfewService.deleteApprovalStatus(bookingId);
        return Response.noContent().build();
    }

    @Override
    public PostOffenderSentencesResponse postOffenderSentences(final List<String> offenderNos) {
        validateOffenderList(offenderNos);

        //no agency id filter required here as offenderNos will always be provided
        val sentences = bookingService.getOffenderSentencesSummary(
                null, authenticationFacade.getCurrentUsername(), offenderNos);

        return PostOffenderSentencesResponse.respond200WithApplicationJson(sentences);
    }

    @Override
    public PostOffenderSentencesBookingsResponse postOffenderSentencesBookings(final List<Long> bookingIds) {
        validateOffenderList(bookingIds);

        val sentences = bookingService.getBookingSentencesSummary(
                authenticationFacade.getCurrentUsername(), bookingIds);

        return PostOffenderSentencesBookingsResponse.respond200WithApplicationJson(sentences);
    }

    @Override
    public List<OffenderSentenceTerms> getOffenderSentenceTerms(final Long bookingId) {
        return bookingService.getOffenderSentenceTerms(bookingId);
    }

    private void validateOffenderList(final List offenderList) {
        if (Collections.isEmpty(offenderList)) {
            throw new BadRequestException("List of Offender Ids must be provided");
        }
    }
}
