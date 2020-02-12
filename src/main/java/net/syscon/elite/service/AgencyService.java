package net.syscon.elite.service;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.IepLevel;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.repository.support.StatusFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Agency API service interface.
 */
public interface AgencyService {
    Agency getAgency(String agencyId, StatusFilter filter, final String agencyType);

    void checkAgencyExists(String agencyId);

    List<Agency> getAgenciesByType(String agencyType, boolean activeOnly);

    Page<Agency> getAgencies(long offset, long limit);

    List<Agency> findAgenciesByUsername(String username);

    Set<String> getAgencyIds();

    void verifyAgencyAccess(String agencyId);

    List<Location> getAgencyLocations(String agencyId, String eventType, String sortFields, Order sortOrder);

    List<Location> getAgencyLocationsByType(String agencyId, String type);

    List<Location> getAgencyEventLocations(String agencyId, String sortFields, Order sortOrder);

    List<Location> getAgencyEventLocationsBooked(String agencyId, LocalDate bookedOnDay, TimeSlot bookedOnPeriod);

    List<IepLevel> getAgencyIepLevels(String agencyId);

    List<PrisonContactDetail> getPrisonContactDetail();

    PrisonContactDetail getPrisonContactDetail(String agencyId);

    List<Agency> getAgenciesByCaseload(String caseload);

    Page<OffenderIepReview> getPrisonIepReview(OffenderIepReviewSearchCriteria criteria);
}
