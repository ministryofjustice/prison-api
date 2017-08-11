package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.v2.api.model.OffenderBookingImpl;
import net.syscon.elite.v2.api.model.PrisonerDetailImpl;
import net.syscon.elite.web.api.model.Alias;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateAssignmentSummary;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.resource.BookingResource.Order;
import net.syscon.elite.web.api.resource.LocationsResource;
import net.syscon.util.CalcDateRanges;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

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
    public List<AssignedInmate> findAllInmates(String query, int offset, int limit, String orderBy, Order order) {
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
    public List<Alias> findInmateAliases(Long inmateId, String orderByField, Order order) {
        return repository.findInmateAliases(inmateId, orderByField, order);
    }

    @Override
    public List<InmateAssignmentSummary> findMyAssignments(long staffId, String currentCaseLoad, int offset, int limit) {
        return repository.findMyAssignments(staffId, currentCaseLoad, DEFAULT_OFFENDER_SORT, true, offset, limit);
    }

    @Override
    public List<OffenderBookingImpl> findOffenders(String keywords, String locationId, String sortFields, String sortOrder, Long offset, Long limit) {
        final boolean descendingOrder = StringUtils.equalsIgnoreCase(sortOrder, "desc");
        return repository.searchForOffenderBookings(keywords, locationId, offset != null ? offset.intValue() : 0, limit != null ? limit.intValue() : Integer.MAX_VALUE, DEFAULT_OFFENDER_SORT, !descendingOrder);
    }

    @Override
    public List<PrisonerDetailImpl> findPrisoners(String firstName, String middleNames, String lastName, String pncNumber, String croNumber, Date dob, Date dobFrom, Date dobTo, String sortFields) {
        final String query = generateQuery(firstName, middleNames, lastName, pncNumber, croNumber);
        CalcDateRanges calcDates = new CalcDateRanges(dob, dobFrom, dobTo);
        if (query != null || calcDates.hasDobRange()) {
            return repository.searchForOffenders(query, calcDates.getDobDateFrom(), calcDates.getDobDateTo(), StringUtils.isNotBlank(sortFields) ? sortFields : DEFAULT_OFFENDER_SORT, true);
        }
        return null;
    }

    private String generateQuery(String firstName, String middleNames, String lastName, String pncNumber, String croNumber) {
        final StringBuilder query = new StringBuilder();

        if (StringUtils.isNotBlank(firstName)) {
            query.append(format("firstName:like:'%s%%'", firstName));
        }
        if (StringUtils.isNotBlank(middleNames)) {
            addAnd(query);
            query.append(format("middleName:like:'%s%%'", middleNames));
        }
        if (StringUtils.isNotBlank(lastName)) {
            addAnd(query);
            query.append(format("lastName:like:'%s%%'", lastName));
        }
        if (StringUtils.isNotBlank(pncNumber)) {
            addAnd(query);
            query.append(format("pncNumber:eq:'%s'", pncNumber));
        }
        if (StringUtils.isNotBlank(croNumber)) {
            addAnd(query);
            query.append(format("croNumber:eq:'%s'", croNumber));
        }
        return StringUtils.trimToNull(query.toString());
    }

    private void addAnd(StringBuilder query) {
        if (query.length() > 0) {
            query.append(",and:");
        }
    }


}
