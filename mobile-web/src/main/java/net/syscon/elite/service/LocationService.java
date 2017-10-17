package net.syscon.elite.service;

import net.syscon.elite.api.model.Location;

import java.util.List;

/**
 * Location API service interface.
 */
public interface LocationService {
    List<Location> getUserLocations(String username);
}
