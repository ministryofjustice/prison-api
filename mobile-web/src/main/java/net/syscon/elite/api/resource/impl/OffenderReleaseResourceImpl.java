package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.OffenderSentenceDetail;
import net.syscon.elite.api.resource.OffenderSentenceResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.BookingService;

import javax.ws.rs.Path;
import java.util.List;

@RestResource
@Path("/offender-sentences")
public class OffenderReleaseResourceImpl implements OffenderSentenceResource {
    private final AuthenticationFacade authenticationFacade;
    private final BookingService bookingService;

    public OffenderReleaseResourceImpl(AuthenticationFacade authenticationFacade, BookingService bookingService) {
        this.authenticationFacade = authenticationFacade;
        this.bookingService = bookingService;
    }

    @Override
    public GetOffenderSentencesResponse getOffenderSentences(String agencyId, List<String> offenderNos) {
        List<OffenderSentenceDetail> sentences = bookingService.getOffenderSentencesSummary(
                agencyId, authenticationFacade.getCurrentUsername(), offenderNos);

        return GetOffenderSentencesResponse.respond200WithApplicationJson(sentences);
    }
}
