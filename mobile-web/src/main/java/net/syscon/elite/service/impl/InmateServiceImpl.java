package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.v2.api.model.OffenderBooking;
import net.syscon.elite.v2.api.model.OffenderBookingImpl;
import net.syscon.elite.web.api.model.Alias;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateAssignmentSummary;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.resource.BookingResource;
import net.syscon.elite.web.api.resource.LocationsResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static net.syscon.elite.web.api.resource.BookingResource.Order.asc;

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
        return repository.findInmate(inmateId).orElseThrow(new EntityNotFoundException(String.valueOf(inmateId)));
    }

    @Override
    public List<Alias> findInmateAliases(Long inmateId, String orderByField, BookingResource.Order order) {
        return repository.findInmateAliases(inmateId, orderByField, order);
    }

    @Override
    public List<InmateAssignmentSummary> findMyAssignments(long staffId, String currentCaseLoad, int offset, int limit) {
        return repository.findMyAssignments(staffId, currentCaseLoad, DEFAULT_OFFENDER_SORT, true, offset, limit);
    }

    @Override
    public List<OffenderBooking> findOffenders(String keywords, String locationId, String sortFields, String sortOrder, Long offset, Long limit) {

        final String query = StringUtils.isNotBlank(keywords) ? format("lastName:like:'%s%%'", keywords) : null;
        final List<AssignedInmate> inmates = repository.findAllInmates(query, offset != null ? offset.intValue() : 0, limit != null ? limit.intValue() : Integer.MAX_VALUE, DEFAULT_OFFENDER_SORT, StringUtils.equalsIgnoreCase(sortOrder, "desc") ? BookingResource.Order.desc : asc);
        return inmates.stream().map(this::convert).collect(Collectors.toList());
    }

    private OffenderBookingImpl convert(AssignedInmate inmate) {
        final OffenderBookingImpl target = new OffenderBookingImpl();
        BeanUtils.copyProperties(inmate, target);
        return target;
    }
}
