package net.syscon.elite.api.resource.impl;

import io.jsonwebtoken.lang.Collections;
import net.syscon.elite.api.model.OffenderSentenceDetail;
import net.syscon.elite.api.resource.OffenderSentenceResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.OffenderCurfewService;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import java.util.List;

@RestResource
@Path("/offender-sentences")
public class OffenderReleaseResourceImpl implements OffenderSentenceResource {
    private final AuthenticationFacade authenticationFacade;
    private final BookingService bookingService;
    private final OffenderCurfewService offenderCurfewService;

    public OffenderReleaseResourceImpl(
            AuthenticationFacade authenticationFacade,
            BookingService bookingService,
            OffenderCurfewService offenderCurfewService) {
        this.authenticationFacade = authenticationFacade;
        this.bookingService = bookingService;
        this.offenderCurfewService = offenderCurfewService;
    }

    @Override
    public GetOffenderSentencesResponse getOffenderSentences(String agencyId, List<String> offenderNos) {
        List<OffenderSentenceDetail> sentences = bookingService.getOffenderSentencesSummary(
                agencyId,
                authenticationFacade.getCurrentUsername(),
                offenderNos);

        return GetOffenderSentencesResponse.respond200WithApplicationJson(sentences);
    }

    @Override
    public GetOffenderSentencesHomeDetentionCurfewCandidatesResponse getOffenderSentencesHomeDetentionCurfewCandidates(String agencyId) {
        List<OffenderSentenceDetail> sentences = offenderCurfewService.getHomeDetentionCurfewCandidates(
                agencyId,
                authenticationFacade.getCurrentUsername());

        return GetOffenderSentencesHomeDetentionCurfewCandidatesResponse.respond200WithApplicationJson(sentences);
    }

    @Override
    public PostOffenderSentencesResponse postOffenderSentences(List<String> offenderNos) {
        validateOffenderList(offenderNos);

        //no agency id filter required here as offenderNos will always be provided
        List<OffenderSentenceDetail> sentences = bookingService.getOffenderSentencesSummary(
                null, authenticationFacade.getCurrentUsername(), offenderNos);

        return PostOffenderSentencesResponse.respond200WithApplicationJson(sentences);
    }

    private void validateOffenderList(List offenderList) {
        if (Collections.isEmpty(offenderList)) {
            throw new BadRequestException("List of Offender Ids must be provided");
        }
    }
}
