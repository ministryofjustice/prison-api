package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.resource.LocationResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.LocationService;
import net.syscon.elite.service.SearchOffenderService;
import net.syscon.elite.service.support.SearchOffenderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestController
@RequestMapping("/locations")
public class LocationsResourceImpl implements LocationResource {
    private final AuthenticationFacade authenticationFacade;
    private final LocationService locationService;
    private final SearchOffenderService searchOffenderService;

    public LocationsResourceImpl(final AuthenticationFacade authenticationFacade, final LocationService locationService, final SearchOffenderService searchOffenderService) {
        this.authenticationFacade = authenticationFacade;
        this.locationService = locationService;
        this.searchOffenderService = searchOffenderService;
    }

    @Override
    public ResponseEntity<List<OffenderBooking>> getOffendersAtLocationDescription(
            final String locationPrefix, final String keywords, final LocalDate fromDob, final LocalDate toDob, final List<String> alerts,
            final boolean returnIep, final boolean returnAlerts, final boolean returnCategory, final String convictedStatus,
            final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var request = SearchOffenderRequest.builder()
                .username(authenticationFacade.getCurrentUsername())
                .keywords(keywords)
                .locationPrefix(locationPrefix)
                .returnAlerts(returnAlerts)
                .returnIep(returnIep)
                .returnCategory(returnCategory)
                .convictedStatus(convictedStatus)
                .alerts(alerts)
                .fromDob(fromDob)
                .toDob(toDob)
                .orderBy(sortFields)
                .order(sortOrder)
                .offset(nvl(pageOffset, 0L))
                .limit(nvl(pageLimit, 10L))
                .build();

        final var offenders = searchOffenderService.findOffenders(request);

        return ResponseEntity.ok()
                .headers(offenders.getPaginationHeaders())
                .body(offenders.getItems());
    }

    @Override
    public Location getLocation(final Long locationId) {
        return locationService.getLocation(locationId);
    }

    @Override
    public ResponseEntity<List<OffenderBooking>> getOffendersAtLocation(final Long locationId, final String query, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var inmates = locationService.getInmatesFromLocation(
                locationId,
                authenticationFacade.getCurrentUsername(),
                query,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return ResponseEntity.ok()
                .headers(inmates.getPaginationHeaders())
                .body(inmates.getItems());
    }

    @Override
    public List<Location> getLocationGroup(final String agencyId, final String name) {
        return locationService.getCellLocationsForGroup(agencyId, name);
    }
}
