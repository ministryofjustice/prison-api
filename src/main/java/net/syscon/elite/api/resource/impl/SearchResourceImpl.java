package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.SearchOffenderResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.SearchOffenderService;
import net.syscon.elite.service.support.SearchOffenderRequest;

import javax.ws.rs.Path;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("search-offenders")
public class SearchResourceImpl implements SearchOffenderResource {
    private final AuthenticationFacade authenticationFacade;
    private final SearchOffenderService searchOffenderService;

    public SearchResourceImpl(final AuthenticationFacade authenticationFacade, final SearchOffenderService searchOffenderService) {
        this.authenticationFacade = authenticationFacade;
        this.searchOffenderService = searchOffenderService;
    }

    @Override
    public SearchForOffendersLocationAndKeywordResponse searchForOffendersLocationAndKeyword(final String locationPrefix, final String keywords, final boolean returnIep, final boolean returnAlerts, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var request = SearchOffenderRequest.builder()
                .username(authenticationFacade.getCurrentUsername())
                .keywords(keywords)
                .locationPrefix(locationPrefix)
                .returnAlerts(returnAlerts)
                .returnIep(returnIep)
                .orderBy(sortFields)
                .order(sortOrder)
                .offset(nvl(pageOffset, 0L))
                .limit(nvl(pageLimit, 10L))
                .build();

        final var offenders = searchOffenderService.findOffenders(request);

        return SearchForOffendersLocationAndKeywordResponse.respond200WithApplicationJson(offenders);
    }
}
