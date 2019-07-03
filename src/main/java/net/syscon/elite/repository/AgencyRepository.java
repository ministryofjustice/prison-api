package net.syscon.elite.repository;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.IepLevel;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.repository.support.StatusFilter;
import net.syscon.elite.service.OffenderIepReview;
import net.syscon.elite.service.OffenderIepReviewSearchCriteria;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Agency API repository interface.
 */
public interface AgencyRepository {

    Page<Agency> getAgencies(String orderByField, Order order, long offset, long limit);
    List<Agency> getAgenciesByType(String agencyType);
    List<Agency> findAgenciesByUsername(String username);
    List<Agency> findAgenciesForCurrentCaseloadByUsername(String username);
    List<Agency> findAgenciesByCaseload(String caseload);
    Optional<Agency> findAgency(String agencyId, StatusFilter filter);
    List<PrisonContactDetail> getPrisonContactDetails(String agencyId);
    List<Location> getAgencyLocations(String agencyId, List<String> eventTypes, String sortFields, Order sortOrder);
    List<Location> getAgencyLocationsBooked(String agencyId, LocalDate bookedOnDay, TimeSlot bookedOnPeriod);
    List<IepLevel> getAgencyIepLevels(String agencyId);
    Page<OffenderIepReview> getPrisonIepReview(OffenderIepReviewSearchCriteria criteria);
}
