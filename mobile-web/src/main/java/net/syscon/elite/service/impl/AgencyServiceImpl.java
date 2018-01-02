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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Agency API service implementation.
 */
@Service
@Transactional(readOnly = true)
public class AgencyServiceImpl implements AgencyService {
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

        List<Location> rawLocations = agencyRepository.getAgencyLocations(agencyId, eventType, orderBy, order);

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
