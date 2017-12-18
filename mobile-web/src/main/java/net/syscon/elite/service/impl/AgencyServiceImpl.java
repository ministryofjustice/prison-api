package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.PrisonContactDetails;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.EntityNotFoundException;
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
    public List<Location> getAvailableLocations(String agencyId, String eventType) {
        return agencyRepository.getAvailableLocations(agencyId, eventType);
    }

    @Override
    public List<PrisonContactDetails> getPrisonContactDetails() {
        return removeBlankAddresses(agencyRepository.getPrisonContactDetails(null));
    }

    @Override
    public PrisonContactDetails getPrisonContactDetails(String agencyId) {

        final List<PrisonContactDetails> prisonContactDetailsList = removeBlankAddresses(agencyRepository.getPrisonContactDetails(agencyId));
        if(prisonContactDetailsList.isEmpty()) {
            throw new EntityNotFoundException(
                    String.format("Contact details not found for Prison %s", agencyId));
        }
        return prisonContactDetailsList.get(0);
    }

    //It is possible for invalid/empty address records to be persisted
    List<PrisonContactDetails> removeBlankAddresses(List<PrisonContactDetails> list) {
        return list.stream().filter(pcd -> {
            return !isBlankAddress(pcd);
        }).collect(Collectors.toList());
    }

    private boolean isBlankAddress(PrisonContactDetails pcd) {
        return pcd.getPremise() == null && pcd.getCity() == null && pcd.getLocality() == null && pcd.getPostCode() == null;
    }
}
