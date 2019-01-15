package net.syscon.elite.service.impl;

import com.google.common.annotations.VisibleForTesting;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.service.support.AlphaNumericComparator;
import net.syscon.elite.service.support.LocationProcessor;
import net.syscon.elite.service.support.ReferenceDomain;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agency API service implementation.
 */
@Service
@Transactional(readOnly = true)
public class AgencyServiceImpl implements AgencyService {

    private static final Comparator<Location> LOCATION_DESCRIPTION_COMPARATOR = Comparator.comparing(
            Location::getDescription,
            new AlphaNumericComparator());


    private final AuthenticationFacade authenticationFacade;
    private final AgencyRepository agencyRepository;
    private final ReferenceDomainService referenceDomainService;

    public AgencyServiceImpl(AuthenticationFacade authenticationFacade, AgencyRepository agencyRepository,
            ReferenceDomainService referenceDomainService) {
        this.authenticationFacade = authenticationFacade;
        this.agencyRepository = agencyRepository;
        this.referenceDomainService = referenceDomainService;
    }

    @Override
    public Agency getAgency(String agencyId) {
        Agency agency = agencyRepository.getAgency(agencyId).orElseThrow(EntityNotFoundException.withId(agencyId));
        agency.setDescription(LocationProcessor.formatLocation(agency.getDescription()));
        return agency;
    }

    @Override
    public void checkAgencyExists(String agencyId) {
        Objects.requireNonNull(agencyId, "agencyId is a required parameter");

        if(! agencyRepository.getAgency(agencyId).isPresent()) {
            throw EntityNotFoundException.withId(agencyId);
        }
    }

    @Override
    public Page<Agency> getAgencies(long offset, long limit) {
        return agencyRepository.getAgencies("agencyId", Order.ASC, offset, limit);
    }

    @Override
    public List<Agency> findAgenciesByUsername(String username) {
        List<Agency> agenciesByUsername = agencyRepository.findAgenciesByUsername(username);
        agenciesByUsername.forEach(a -> a.setDescription(LocationProcessor.formatLocation(a.getDescription())));
        return agenciesByUsername;
    }

    /**
     * Gets set of agency location ids accessible to current authenticated user. This governs access to bookings - a user
     * cannot have access to an offender unless they are in a location that the authenticated user is also associated with.
     *
     * @return set of agency location ids accessible to current authenticated user.
     */
    @Override
    public Set<String> getAgencyIds() {
        return findAgenciesByUsername(authenticationFacade.getCurrentUsername())
              .stream()
              .map(Agency::getAgencyId)
              .collect(Collectors.toSet());
    }

    /**
     * Verifies that current user is authorised to access specified agency. If this
     * agency location is not part of any caseload accessible to the current user, a 'Resource Not Found'
     * exception is thrown.
     *
     * @param agencyId the agency.
     * @throws EntityNotFoundException if current user does not have access to this agency.
     */
    @Override
    public void verifyAgencyAccess(String agencyId) {
        Objects.requireNonNull(agencyId, "agencyId is a required parameter");

        var agencyIds = getAgencyIds();
        if (UserSecurityUtils.hasRoles("INACTIVE_BOOKINGS")) {
            agencyIds.addAll(Set.of("OUT", "TRN"));
        }
        if (!agencyIds.contains(agencyId)) {
            throw EntityNotFoundException.withId(agencyId);
        }
    }

    @Override
    public List<Location> getAgencyLocations(String agencyId, String eventType, String sortFields, Order sortOrder) {
        // If no sort fields defined, sort in ascending order of user description then description (by default)
        String orderBy = StringUtils.defaultIfBlank(sortFields, "userDescription,description");
        Order order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);

        List<String> eventTypes = StringUtils.isBlank(eventType) ? Collections.emptyList() : Collections.singletonList(eventType);
        List<Location> rawLocations = agencyRepository.getAgencyLocations(agencyId, eventTypes, orderBy, order);

        return LocationProcessor.processLocations(rawLocations);
    }

    @Override
    public List<Location> getAgencyEventLocations(String agencyId, String sortFields, Order sortOrder) {
        String orderBy = StringUtils.defaultIfBlank(sortFields, "userDescription,description");
        Order order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);

        // Get all location usages for locations that an event could possibly be held in. (reference domain ILOC_USG )
        // Note this should be cached. Also assuming small number of values
        final List<String> allEventLocationUsages = referenceDomainService
                .getReferenceCodesByDomain(ReferenceDomain.INTERNAL_LOCATION_USAGE.getDomain(), false, null, null, 0, 1000)
                .getItems().stream().map(ReferenceCode::getCode).collect(Collectors.toList());

        List<Location> rawLocations = agencyRepository.getAgencyLocations(agencyId, allEventLocationUsages, orderBy, order);

        return LocationProcessor.processLocations(rawLocations);
    }

    @Override
    public List<Location> getAgencyEventLocationsBooked(String agencyId, LocalDate bookedOnDay, TimeSlot bookedOnPeriod) {
        Objects.requireNonNull(bookedOnDay, "bookedOnDay must be specified.");

        List<Location> locations = agencyRepository.getAgencyLocationsBooked(agencyId, bookedOnDay, bookedOnPeriod);

        List<Location> processedLocations =  LocationProcessor.processLocations(locations, true);

        processedLocations.sort(LOCATION_DESCRIPTION_COMPARATOR);

        return processedLocations;
    }

    @Override
    public List<PrisonContactDetail> getPrisonContactDetail() {
        return removeBlankAddresses(agencyRepository.getPrisonContactDetails(null));
    }

    @Override
    public PrisonContactDetail getPrisonContactDetail(String agencyId) {

        final List<PrisonContactDetail> prisonContactDetailList = removeBlankAddresses(agencyRepository.getPrisonContactDetails(agencyId));
        if(prisonContactDetailList.isEmpty()) {
            throw EntityNotFoundException.withMessage(String.format("Contact details not found for Prison %s", agencyId));
        }
        return prisonContactDetailList.get(0);
    }

    @Override
    public List<Agency> getAgenciesByCaseload(String caseload) {
        List<Agency> agenciesByCaseload = agencyRepository.findAgenciesByCaseload(caseload);
        agenciesByCaseload.forEach(a -> a.setDescription(LocationProcessor.formatLocation(a.getDescription())));
        return agenciesByCaseload;
    }

    //It is possible for invalid/empty address records to be persisted
    @VisibleForTesting
    List<PrisonContactDetail> removeBlankAddresses(List<PrisonContactDetail> list) {
        return list.stream().filter(pcd -> !isBlankAddress(pcd)).collect(Collectors.toList());
    }

    private boolean isBlankAddress(PrisonContactDetail pcd) {
        return pcd.getPremise() == null && pcd.getCity() == null && pcd.getLocality() == null && pcd.getPostCode() == null;
    }
}
