package net.syscon.elite.service.impl;

import com.google.common.annotations.VisibleForTesting;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonContactDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.support.LocationProcessor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agency API service implementation.
 */
@Service
@Transactional(readOnly = true)
public class AgencyServiceImpl implements AgencyService {

    // All location usages an event could possibly be held in (reference domain ILOC_USG )
    private static final List<String> EVENT_LOCATION_TYPES = Arrays.asList(
            "APP", // appointments
            "MOVEMENT", "OCCUR",
            "OIC", //Adjudication Hearing Location
            "OTHER",
            "PROG", //Programmes & Activities
            "PROP", 
            "VISIT" // Visits
    //TODO currently this is all of them but some may be N/A
    );

    private final AgencyRepository agencyRepository;

    public AgencyServiceImpl(AgencyRepository agencyRepository) {
        this.agencyRepository = agencyRepository;
    }

    @Override
    public Agency getAgency(String agencyId) {
        return agencyRepository.getAgency(agencyId).orElseThrow(EntityNotFoundException.withId(agencyId));
    }

    @Override
    public Page<Agency> getAgencies(long offset, long limit) {
        return agencyRepository.getAgencies("agencyId", Order.ASC, offset, limit);
    }

    @Override
    public List<Agency> findAgenciesByUsername(String username) {
        return agencyRepository.findAgenciesByUsername(username);
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

        List<Location> rawLocations = agencyRepository.getAgencyLocations(agencyId, EVENT_LOCATION_TYPES, orderBy, order);

        return LocationProcessor.processLocations(rawLocations);
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

    //It is possible for invalid/empty address records to be persisted
    @VisibleForTesting
    List<PrisonContactDetail> removeBlankAddresses(List<PrisonContactDetail> list) {
        return list.stream().filter(pcd -> !isBlankAddress(pcd)).collect(Collectors.toList());
    }

    private boolean isBlankAddress(PrisonContactDetail pcd) {
        return pcd.getPremise() == null && pcd.getCity() == null && pcd.getLocality() == null && pcd.getPostCode() == null;
    }
}
