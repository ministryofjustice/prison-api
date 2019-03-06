package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.LocationResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.LocationService;
import net.syscon.elite.service.SearchOffenderService;
import net.syscon.elite.service.support.SearchOffenderRequest;

import javax.ws.rs.Path;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/locations")
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
	public GetOffendersAtLocationDescriptionResponse getOffendersAtLocationDescription(
            final String locationPrefix, final String keywords, final List<String> alerts,
            final boolean returnIep, final boolean returnAlerts, final boolean returnCategory,
            final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var request = SearchOffenderRequest.builder()
				.username(authenticationFacade.getCurrentUsername())
				.keywords(keywords)
				.locationPrefix(locationPrefix)
				.returnAlerts(returnAlerts)
				.returnIep(returnIep)
				.returnCategory(returnCategory)
				.alerts(alerts)
				.orderBy(sortFields)
				.order(sortOrder)
				.offset(nvl(pageOffset, 0L))
				.limit(nvl(pageLimit, 10L))
				.build();

        final var offenders = searchOffenderService.findOffenders(request);

		return GetOffendersAtLocationDescriptionResponse.respond200WithApplicationJson(offenders);
	}

	@Override
    public GetLocationResponse getLocation(final Long locationId) {
        final var location = locationService.getLocation(locationId);

		return GetLocationResponse.respond200WithApplicationJson(location);
	}

	@Override
    public GetOffendersAtLocationResponse getOffendersAtLocation(final Long locationId, final String query, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var inmates = locationService.getInmatesFromLocation(
				locationId,
				authenticationFacade.getCurrentUsername(),
				query,
				sortFields,
				sortOrder,
				nvl(pageOffset, 0L),
				nvl(pageLimit, 10L));

		return GetOffendersAtLocationResponse.respond200WithApplicationJson(inmates);
	}

    @Override
    public GetLocationGroupResponse getLocationGroup(final String agencyId, final String name) {
        return GetLocationGroupResponse.respond200WithApplicationJson(locationService.getCellLocationsForGroup(agencyId, name));
    }
}
