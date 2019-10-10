package net.syscon.elite.service.impl.whereabouts;

import lombok.val;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.LocationGroup;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.LocationGroupService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

@Service("locationGroupServiceSelector")
public class LocationGroupServiceSelector implements LocationGroupService {
    private final LocationGroupService defaultService;
    private final LocationGroupService overrideService;

    public LocationGroupServiceSelector(
            @Qualifier("defaultLocationGroupService") LocationGroupService defaultService,
            @Qualifier("overrideLocationGroupService") LocationGroupService overrideService) {
        this.defaultService = defaultService;
        this.overrideService = overrideService;
    }

    @Override
    @VerifyAgencyAccess
    public List<LocationGroup> getLocationGroupsForAgency(final String agencyId) {
        return getLocationGroups(agencyId);
    }

    @Override
    public List<LocationGroup> getLocationGroups(String agencyId) {
        val groups = overrideService.getLocationGroups(agencyId);
        if (!groups.isEmpty()) {
            return groups;
        }
        return defaultService.getLocationGroups(agencyId);
    }

    @Override
    public Predicate<Location> locationGroupFilter(String agencyId, String groupName) {
        if (!overrideService.getLocationGroups(agencyId).isEmpty()) {
            return overrideService.locationGroupFilter(agencyId, groupName);
        }
        return defaultService.locationGroupFilter(agencyId, groupName);
    }
}
