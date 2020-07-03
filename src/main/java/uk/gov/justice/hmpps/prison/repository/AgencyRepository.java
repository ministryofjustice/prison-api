package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.IepLevel;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter;
import uk.gov.justice.hmpps.prison.service.OffenderIepReview;
import uk.gov.justice.hmpps.prison.service.OffenderIepReviewSearchCriteria;

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
