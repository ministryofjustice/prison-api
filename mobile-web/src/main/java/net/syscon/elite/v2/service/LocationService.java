package net.syscon.elite.v2.service;

import net.syscon.elite.v2.api.model.Location;

import java.util.List;

/**
 * Location API (v2) service interface.
 */
public interface LocationService {
    List<Location> getUserLocations(String username);
}
