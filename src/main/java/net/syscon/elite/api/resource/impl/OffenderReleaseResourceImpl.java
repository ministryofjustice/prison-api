package net.syscon.elite.api.resource.impl;

import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.resource.OffenderSentenceResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.OffenderCurfewService;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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
    public GetOffenderSentencesHomeDetentionCurfewCandidatesResponse getOffenderSentencesHomeDetentionCurfewCandidates() {
        val sentences =
                offenderCurfewService.getHomeDetentionCurfewCandidates(authenticationFacade.getCurrentUsername());

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
    public Response setCurfewChecks(final Long bookingId, final HdcChecks hdcChecks) {
        try {
            offenderCurfewService.setHdcChecks(bookingId, hdcChecks);
            return Response.ok().build();
        } catch (final Throwable t) {
            log.debug("PUT HDC check status failed");
            throw t;
        }
    }

    @Override
    public Response setApprovalStatus(final Long bookingId, final ApprovalStatus approvalStatus) {
        try {
            offenderCurfewService.setApprovalStatus(bookingId, approvalStatus);
            return Response.ok().build();
        } catch (final Throwable t) {
            log.debug("PUT ApprovalStatus failed");
            throw t;
        }
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
    public GetOffenderSentenceTermsResponse getOffenderSentenceTerms(final Long bookingId) {
        val sentences = bookingService.getOffenderSentenceTerms(bookingId);

        return GetOffenderSentenceTermsResponse.respond200WithApplicationJson(sentences);
    }

    private void validateOffenderList(final List offenderList) {
        if (Collections.isEmpty(offenderList)) {
            throw new BadRequestException("List of Offender Ids must be provided");
        }
    }
}
