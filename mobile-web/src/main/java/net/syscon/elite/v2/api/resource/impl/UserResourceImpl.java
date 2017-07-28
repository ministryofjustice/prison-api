package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.v2.api.resource.UserResource;

import javax.ws.rs.Path;
import java.util.ArrayList;

@RestResource
@Path("/v2/users")
public class UserResourceImpl implements UserResource {
    @Override
    public GetUsersMeLocationsResponse getUsersMeLocations(Long offset, Long limit) {
        return GetUsersMeLocationsResponse.respond200WithApplicationJson(new ArrayList<>());
    }
}
