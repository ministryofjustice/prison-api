package net.syscon.elite.v2.service.impl;

import net.syscon.elite.persistence.LocationRepository;
import net.syscon.elite.v2.api.model.Location;
import net.syscon.elite.v2.api.model.LocationImpl;
import net.syscon.elite.v2.service.LocationService;
import net.syscon.elite.web.api.resource.LocationsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Location API (v2) service implementation.
 */
@Service
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final LocationRepository locationRepository;

    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public List<Location> getUserLocations(String username, Long offset, Long limit) {
        final List<net.syscon.elite.web.api.model.Location> locations =
                locationRepository.findLocations(null, "locationId", LocationsResource.Order.asc, offset != null ? offset.intValue() : 0, limit != null ? limit.intValue() : Integer.MAX_VALUE);

        return locations.stream().map(this::convert).collect(Collectors.toList());
    }

    private net.syscon.elite.v2.api.model.Location convert(net.syscon.elite.web.api.model.Location location) {
        final net.syscon.elite.v2.api.model.Location target = new LocationImpl();
        BeanUtils.copyProperties(location, target);
        return target;
    }
}
