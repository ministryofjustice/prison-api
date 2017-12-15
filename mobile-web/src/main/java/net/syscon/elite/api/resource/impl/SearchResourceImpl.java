package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.resource.SearchOffenderResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
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

    public SearchResourceImpl(AuthenticationFacade authenticationFacade, SearchOffenderService searchOffenderService) {
        this.authenticationFacade = authenticationFacade;
        this.searchOffenderService = searchOffenderService;
    }

    @Override
    public SearchForOffendersLocationOnlyResponse searchForOffendersLocationOnly(String locationPrefix, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        SearchOffenderRequest request = SearchOffenderRequest.builder()
                .username(authenticationFacade.getCurrentUsername())
                .locationPrefix(locationPrefix)
                .orderBy(sortFields)
                .order(sortOrder)
                .offset(nvl(pageOffset, 0L))
                .limit(nvl(pageLimit, 10L))
                .build();

        Page<OffenderBooking> offenders = searchOffenderService.findOffenders(request);

        return SearchForOffendersLocationOnlyResponse.respond200WithApplicationJson(offenders);
    }

    @Override
    public SearchForOffendersLocationAndKeywordResponse searchForOffendersLocationAndKeyword(String locationPrefix, String keywords, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        SearchOffenderRequest request = SearchOffenderRequest.builder()
                .username(authenticationFacade.getCurrentUsername())
                .keywords(keywords)
                .locationPrefix(locationPrefix)
                .orderBy(sortFields)
                .order(sortOrder)
                .offset(nvl(pageOffset, 0L))
                .limit(nvl(pageLimit, 10L))
                .build();

        Page<OffenderBooking> offenders = searchOffenderService.findOffenders(request);

        return SearchForOffendersLocationAndKeywordResponse.respond200WithApplicationJson(offenders);
    }
}
