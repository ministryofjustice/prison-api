package net.syscon.elite.service;

import net.syscon.elite.api.model.Agency;

import java.util.List;

/**
 * Agency API service interface.
 */
public interface AgencyService {
    Agency getAgency(String agencyId);
    List<Agency> getAgencies(long offset, long limit);
    List<Agency> findAgenciesByUsername(String username);
}
