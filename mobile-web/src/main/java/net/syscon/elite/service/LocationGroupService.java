package net.syscon.elite.service;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.LocationGroup;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface LocationGroupService {

    List<LocationGroup> getLocationGroupsForAgency(String agencyId);

    /**
     * Supply a sequence of filter predicates for LocationGroups.  The sequence is used to
     * assist with ordering matching locations.
     * @param agencyId
     * @param groupName
     * @return
     */
    List<Predicate<Location>> locationGroupFilters(String agencyId, String groupName);

}
