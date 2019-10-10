package net.syscon.elite.service;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.LocationGroup;

import java.util.List;
import java.util.function.Predicate;

public interface LocationGroupService {

    List<LocationGroup> getLocationGroupsForAgency(String agencyId);

    List<LocationGroup> getLocationGroups(String agencyId);

    /**
     * Supply a filter predicate for LocationGroups.
     *
     * @param agencyId
     * @param groupName
     * @return a suitable predicate.
     */
    Predicate<Location> locationGroupFilter(String agencyId, String groupName);
}
