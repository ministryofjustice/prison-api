package net.syscon.prison.api.resource.impl;

import net.syscon.prison.api.model.OffenderBooking;
import net.syscon.prison.api.resource.SearchOffenderResource;
import net.syscon.prison.api.support.Order;
import net.syscon.prison.security.AuthenticationFacade;
import net.syscon.prison.service.SearchOffenderService;
import net.syscon.prison.service.support.SearchOffenderRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestController
@RequestMapping("${api.base.path}/search-offenders")
public class SearchResourceImpl implements SearchOffenderResource {
    private final AuthenticationFacade authenticationFacade;
    private final SearchOffenderService searchOffenderService;

    public SearchResourceImpl(final AuthenticationFacade authenticationFacade, final SearchOffenderService searchOffenderService) {
        this.authenticationFacade = authenticationFacade;
        this.searchOffenderService = searchOffenderService;
    }

    @Override
    public ResponseEntity<List<OffenderBooking>> searchForOffendersLocationAndKeyword(final String locationPrefix, final String keywords, final boolean returnIep, final boolean returnAlerts, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
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

        final var offendersPaged = searchOffenderService.findOffenders(request);

        final var responseHeaders = new HttpHeaders();
        responseHeaders.set("Total-Records", String.valueOf(offendersPaged.getTotalRecords()));
        responseHeaders.set("Page-Offset",   String.valueOf(offendersPaged.getPageOffset()));
        responseHeaders.set("Page-Limit",    String.valueOf(offendersPaged.getPageLimit()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(offendersPaged.getItems());
    }
}
