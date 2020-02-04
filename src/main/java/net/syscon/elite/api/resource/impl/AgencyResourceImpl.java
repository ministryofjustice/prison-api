package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.AgencyResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.service.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static net.syscon.elite.repository.support.StatusFilter.ACTIVE_ONLY;
import static net.syscon.elite.repository.support.StatusFilter.ALL;
import static net.syscon.util.ResourceUtils.nvl;

/**
 * TODO DT-526 Replace locationGroupService with newLocationGroupService once the new endpoint has been tested from whereabouts
 *      API and prisonstaffhub UI has been changed to no longer point to this API (replaced with call to whereabouts API).
 *      At the same time LocationGroupServiceSelector and LocationGroupFromDbService can be deleted.
 */

@RestController
@RequestMapping("${api.base.path}/agencies")
public class AgencyResourceImpl implements AgencyResource {
    private final AgencyService agencyService;
    private final LocationGroupService locationGroupService;
    private final LocationGroupService newLocationGroupService;
    private final WhereaboutsEnabledService whereaboutsEnabledService;

    public AgencyResourceImpl(
            final AgencyService agencyService,
            @Qualifier("locationGroupServiceSelector") final LocationGroupService locationGroupService,
            @Qualifier("defaultLocationGroupService") final LocationGroupService newLocationGroupService,
            WhereaboutsEnabledService whereaboutsEnabledService) {
        this.agencyService = agencyService;
        this.locationGroupService = locationGroupService;
        this.newLocationGroupService = newLocationGroupService;
        this.whereaboutsEnabledService = whereaboutsEnabledService;
    }

    @Override
    public ResponseEntity<List<Agency>> getAgencies(final Long pageOffset, final Long pageLimit) {
        final var agencies = agencyService.getAgencies(nvl(pageOffset, 0L), nvl(pageLimit, 10L));

        return ResponseEntity.ok()
                .headers(agencies.getPaginationHeaders())
                .body(agencies.getItems());
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
    public List<LocationGroup> getAvailableLocationGroupsNew(final String agencyId) {
        return newLocationGroupService.getLocationGroupsForAgency(agencyId);
    }

    @Override
    public WhereaboutsConfig getWhereabouts(final String agencyId) {
        return WhereaboutsConfig.builder().enabled(whereaboutsEnabledService.isEnabled(agencyId)).build();
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
