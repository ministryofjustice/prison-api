package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.api.resource.SearchOffenderResource;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.SearchOffenderService;
import uk.gov.justice.hmpps.prison.service.support.SearchOffenderRequest;

import java.util.List;

import static uk.gov.justice.hmpps.prison.util.ResourceUtils.nvl;

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
