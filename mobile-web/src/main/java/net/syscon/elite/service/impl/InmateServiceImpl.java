package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.web.api.model.Alias;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateAssignmentSummary;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.resource.BookingResource;
import net.syscon.elite.web.api.resource.LocationsResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InmateServiceImpl implements InmateService {
    static final String DEFAULT_OFFENDER_SORT = "lastName,firstName,offenderNo";

    private final InmateRepository repository;

    @Inject
    public InmateServiceImpl(InmateRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AssignedInmate> findAllInmates(String query, int offset, int limit, String orderBy, BookingResource.Order order) {
        String colSort = StringUtils.isNotBlank(orderBy) ? orderBy : DEFAULT_OFFENDER_SORT;
        return repository.findAllInmates(query, offset, limit, colSort, order);
    }

    @Override
    public List<AssignedInmate> findInmatesByLocation(Long locationId, String query, String orderByField, LocationsResource.Order order, int offset, int limit) {
        String colSort = StringUtils.isNotBlank(orderByField) ? orderByField : DEFAULT_OFFENDER_SORT;
        return repository.findInmatesByLocation(locationId, query, colSort, order, offset, limit);
    }

    @Override
    public InmateDetails findInmate(Long inmateId) {
        return repository.findInmate(inmateId);
    }

    @Override
    public List<Alias> findInmateAliases(Long inmateId, String orderByField, BookingResource.Order order) {
        return repository.findInmateAliases(inmateId, orderByField, order);
    }

    @Override
    public List<InmateAssignmentSummary> findMyAssignments(long staffId, String currentCaseLoad, int offset, int limit) {
        return repository.findMyAssignments(staffId, currentCaseLoad, DEFAULT_OFFENDER_SORT, true, offset, limit);
    }
}
