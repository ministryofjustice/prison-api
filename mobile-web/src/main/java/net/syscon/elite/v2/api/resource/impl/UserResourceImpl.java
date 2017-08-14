package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.v2.api.model.Location;
import net.syscon.elite.v2.api.resource.UserResource;
import net.syscon.elite.v2.service.LocationService;

import javax.ws.rs.Path;
import java.util.List;

@RestResource
@Path("v2/users")
public class UserResourceImpl implements UserResource {

    private final LocationService locationService;

    public UserResourceImpl(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public GetUsersMeLocationsResponse getUsersMeLocations() {
        List<Location> userLocations =
                locationService.getUserLocations(UserSecurityUtils.getCurrentUsername());

        return GetUsersMeLocationsResponse.respond200WithApplicationJson(userLocations);
    }
}
