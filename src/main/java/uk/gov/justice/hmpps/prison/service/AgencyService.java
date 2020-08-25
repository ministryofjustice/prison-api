package uk.gov.justice.hmpps.prison.service;

import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentType;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentTypes;
import uk.gov.justice.hmpps.prison.api.model.IepLevel;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.OffenderCell;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter;

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

    List<OffenderCell> getCellsWithCapacityInAgency(String agencyId, String attribute);

    AgencyEstablishmentTypes getEstablishmentTypes(final String agencyId);
}
