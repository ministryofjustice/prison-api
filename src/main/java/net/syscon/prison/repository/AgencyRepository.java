package net.syscon.prison.repository;

import net.syscon.prison.api.model.Agency;
import net.syscon.prison.api.model.IepLevel;
import net.syscon.prison.api.model.Location;
import net.syscon.prison.api.model.PrisonContactDetail;
import net.syscon.prison.api.support.Order;
import net.syscon.prison.api.support.Page;
import net.syscon.prison.api.support.TimeSlot;
import net.syscon.prison.repository.support.StatusFilter;
import net.syscon.prison.service.OffenderIepReview;
import net.syscon.prison.service.OffenderIepReviewSearchCriteria;

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

    Optional<Agency> findAgency(String agencyId, StatusFilter filter, final String agencyType);

    List<PrisonContactDetail> getPrisonContactDetails(String agencyId);

    List<Location> getAgencyLocations(String agencyId, List<String> eventTypes, String sortFields, Order sortOrder);

    List<Location> getAgencyLocationsBooked(String agencyId, LocalDate bookedOnDay, TimeSlot bookedOnPeriod);

    List<IepLevel> getAgencyIepLevels(String agencyId);

    Page<OffenderIepReview> getPrisonIepReview(OffenderIepReviewSearchCriteria criteria);
}
