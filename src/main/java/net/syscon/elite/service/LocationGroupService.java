package net.syscon.elite.service;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.LocationGroup;

import java.util.List;
import java.util.function.Predicate;

public interface LocationGroupService {

    List<LocationGroup> getLocationGroupsForAgency(String agencyId);
    List<LocationGroup> getLocationGroups(String agencyId);

    /**
     * Supply a sequence of filter predicates for LocationGroups.  The sequence is used to
     * assist with ordering matching locations.
     * @param agencyId
     * @param groupName
     * @return
     */
//    List<Predicate<Location>> locationGroupFilters(String agencyId, String groupName);

    Predicate<Location> locationGroupFilter(String agencyId, String groupName);
}
