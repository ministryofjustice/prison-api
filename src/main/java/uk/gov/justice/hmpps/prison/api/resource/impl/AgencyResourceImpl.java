package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.IepLevel;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.LocationGroup;
import uk.gov.justice.hmpps.prison.api.model.OffenderCell;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.resource.AgencyResource;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.service.AgencyService;
import uk.gov.justice.hmpps.prison.service.LocationGroupService;
import uk.gov.justice.hmpps.prison.service.OffenderIepReview;
import uk.gov.justice.hmpps.prison.service.OffenderIepReviewSearchCriteria;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ACTIVE_ONLY;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ALL;

@RestController
@RequestMapping("${api.base.path}/agencies")
public class AgencyResourceImpl implements AgencyResource {
    private final AgencyService agencyService;
    private final LocationGroupService locationGroupService;

    public AgencyResourceImpl(
            final AgencyService agencyService,
            final LocationGroupService locationGroupService) {
        this.agencyService = agencyService;
        this.locationGroupService = locationGroupService;
    }

    @Override
    public ResponseEntity<List<Agency>> getAgencies(final Long pageOffset, final Long pageLimit) {
        return agencyService.getAgencies(pageOffset, pageLimit).getResponse();
    }

    @Override
    public List<Agency> getAgenciesByType(final String agencyType, boolean activeOnly) {
        return agencyService.getAgenciesByType(agencyType, activeOnly);
    }

    @Override
    public Agency getAgency(final String agencyId, final boolean activeOnly, final String agencyType) {
        return agencyService.getAgency(agencyId, activeOnly ? ACTIVE_ONLY : ALL, agencyType);
    }

    @Override
    public List<Location> getAgencyLocations(final String agencyId, final String eventType, final String sortFields, final Order sortOrder) {
        return agencyService.getAgencyLocations(agencyId, eventType, sortFields, sortOrder);
    }

    @Override
    public List<OffenderCell> getAgencyActiveCellsWithCapacity(final String agencyId, final String attribute) {
        return agencyService.getCellsWithCapacityInAgency(agencyId, attribute);
    }

    @Override
    public List<Location> getAgencyLocationsByType(final String agencyId, final String type) {
        return agencyService.getAgencyLocationsByType(agencyId, type);
    }

    @Override
    public List<IepLevel> getAgencyIepLevels(final String agencyId) {
        return agencyService.getAgencyIepLevels(agencyId);
    }

    @Override
    public List<LocationGroup> getAvailableLocationGroups(final String agencyId) {
        return locationGroupService.getLocationGroupsForAgency(agencyId);
    }

    @Override
    public List<Location> getAgencyEventLocations(final String agencyId, final String sortFields, final Order sortOrder) {
        return agencyService.getAgencyEventLocations(agencyId, sortFields, sortOrder);
    }

    @Override
    public List<Location> getAgencyEventLocationsBooked(final String agencyId, final LocalDate date, final TimeSlot timeSlot) {
        return agencyService.getAgencyEventLocationsBooked(agencyId, date, timeSlot);
    }

    @Override
    public List<Agency> getAgenciesByCaseload(final String caseload) {
        return agencyService.getAgenciesByCaseload(caseload);
    }

    @Override
    public List<PrisonContactDetail> getPrisonContactDetailList() {
        return agencyService.getPrisonContactDetail();
    }

    @Override
    public PrisonContactDetail getPrisonContactDetail(final String agencyId) {
        return agencyService.getPrisonContactDetail(agencyId);
    }

    @Override
    public ResponseEntity<List<OffenderIepReview>> getPrisonIepReview(final String agencyId,
                                                                      final String iepLevel,
                                                                      final String location,
                                                                      final Long pageOffset,
                                                                      final Long pageLimit) {
        final var criteria = OffenderIepReviewSearchCriteria.builder()
                .agencyId(agencyId)
                .iepLevel(iepLevel)
                .location(location)
                .pageRequest(new PageRequest(pageOffset, pageLimit))
                .build();

        final var prisonIepReview = agencyService.getPrisonIepReview(criteria);

        return ResponseEntity.ok()
                .headers(prisonIepReview.getPaginationHeaders())
                .body(prisonIepReview.getItems());
    }
}
