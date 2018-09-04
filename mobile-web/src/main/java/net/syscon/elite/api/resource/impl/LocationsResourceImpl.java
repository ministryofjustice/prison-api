package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.resource.LocationResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
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

	public LocationsResourceImpl(AuthenticationFacade authenticationFacade, LocationService locationService, SearchOffenderService searchOffenderService) {
		this.authenticationFacade = authenticationFacade;
		this.locationService = locationService;
		this.searchOffenderService = searchOffenderService;
	}

	@Override
	public GetOffendersAtLocationDescriptionResponse getOffendersAtLocationDescription(String locationPrefix,
			String keywords, List<String> alerts, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		SearchOffenderRequest request = SearchOffenderRequest.builder()
				.username(authenticationFacade.getCurrentUsername())
				.keywords(keywords)
				.locationPrefix(locationPrefix)
                .alerts(alerts)
				.orderBy(sortFields)
				.order(sortOrder)
				.offset(nvl(pageOffset, 0L))
				.limit(nvl(pageLimit, 10L))
				.build();

		Page<OffenderBooking> offenders = searchOffenderService.findOffenders(request);

		return GetOffendersAtLocationDescriptionResponse.respond200WithApplicationJson(offenders);
	}

	@Override
	public GetLocationResponse getLocation(Long locationId) {
		Location location = locationService.getLocation(locationId);

		return GetLocationResponse.respond200WithApplicationJson(location);
	}

	@Override
	public GetOffendersAtLocationResponse getOffendersAtLocation(Long locationId, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
		Page<OffenderBooking> inmates = locationService.getInmatesFromLocation(
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
    public GetLocationGroupResponse getLocationGroup(String agencyId, String name) {
        return GetLocationGroupResponse.respond200WithApplicationJson(locationService.getCellLocationsForGroup(agencyId, name));
    }
}
