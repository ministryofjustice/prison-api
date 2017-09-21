package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.CaseLoadRepository;
import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import net.syscon.elite.v2.api.model.OffenderBooking;
import net.syscon.elite.v2.api.model.PrisonerDetail;
import net.syscon.elite.web.api.model.Alias;
import net.syscon.elite.web.api.model.CaseLoad;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.model.InmatesSummary;
import net.syscon.elite.web.api.resource.BookingResource.Order;
import net.syscon.util.CalcDateRanges;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Transactional(readOnly = true)
public class InmateServiceImpl implements InmateService {
    static final String DEFAULT_OFFENDER_SORT = "lastName,firstName,offenderNo";

    private final InmateRepository repository;
    private final CaseLoadRepository caseLoadRepository;
    private final int maxYears;
    private final String locationTypeGranularity;

    @Autowired
    public InmateServiceImpl(InmateRepository repository, CaseLoadRepository caseLoadRepository, @Value("${offender.dob.max.range.years:10}") int maxYears,
                             @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity ) {
        this.repository = repository;
        this.caseLoadRepository = caseLoadRepository;
        this.maxYears = maxYears;
        this.locationTypeGranularity = locationTypeGranularity;
    }

    @Override
    public List<InmatesSummary> findAllInmates(String query, int offset, int limit, String orderBy, Order order) {
        String colSort = StringUtils.isNotBlank(orderBy) ? orderBy : DEFAULT_OFFENDER_SORT;
        return repository.findAllInmates(getUserCaseloadIds(), locationTypeGranularity, query, offset, limit, colSort, order);
    }

    @Override
    public InmateDetails findInmate(Long inmateId) {
        return repository.findInmate(inmateId, getUserCaseloadIds()).orElseThrow(new EntityNotFoundException(String.valueOf(inmateId)));
    }

    @Override
    public List<Alias> findInmateAliases(Long inmateId, String orderByField, Order order) {
        return repository.findInmateAliases(inmateId, orderByField, order);
    }

    @Override
    public List<OffenderBooking> findOffenders(String keywords, String locationPrefix, String sortFields, net.syscon.elite.v2.api.support.Order sortOrder, Long offset, Long limit) {

        final boolean descendingOrder = net.syscon.elite.v2.api.support.Order.DESC == sortOrder;
        return repository.searchForOffenderBookings(getUserCaseloadIds(), keywords, locationPrefix,
                locationTypeGranularity, offset != null ? offset.intValue() : 0,
                limit != null ? limit.intValue() : Integer.MAX_VALUE, StringUtils.isNotBlank(sortFields) ? sortFields : DEFAULT_OFFENDER_SORT, !descendingOrder);
    }

    @Override
    public List<PrisonerDetail> findPrisoners(PrisonerDetailSearchCriteria criteria, String sortFields, net.syscon.elite.v2.api.support.Order sortOrder, Long offset, Long limit) {
        final String query = generateQuery(criteria);
        CalcDateRanges calcDates = new CalcDateRanges(criteria.getDob(), criteria.getDobFrom(), criteria.getDobTo(), maxYears);
        if (query != null || calcDates.hasDobRange()) {
            long rowLimit = (limit != null ? limit : 50L);
            long startOffset = (offset != null ? offset : 0L);
            return repository.searchForOffenders(query, calcDates.getDobDateFrom(), calcDates.getDobDateTo(),
                    StringUtils.isNotBlank(sortFields) ? sortFields : DEFAULT_OFFENDER_SORT, net.syscon.elite.v2.api.support.Order.ASC == sortOrder, startOffset, rowLimit);
        }
        return null;
    }

    private String generateQuery(PrisonerDetailSearchCriteria criteria) {
        final StringBuilder query = new StringBuilder();

        if (StringUtils.isNotBlank(criteria.getFirstName())) {
            query.append(format("firstName:like:'%s%%'", criteria.getFirstName()));
        }
        if (StringUtils.isNotBlank(criteria.getMiddleNames())) {
            addAnd(query);
            query.append(format("middleNames:like:'%s%%'", criteria.getMiddleNames()));
        }
        if (StringUtils.isNotBlank(criteria.getLastName())) {
            addAnd(query);
            query.append(format("lastName:like:'%s%%'", criteria.getLastName()));
        }
        if (StringUtils.isNotBlank(criteria.getPncNumber())) {
            addAnd(query);
            query.append(format("pncNumber:eq:'%s'", criteria.getPncNumber()));
        }
        if (StringUtils.isNotBlank(criteria.getCroNumber())) {
            addAnd(query);
            query.append(format("croNumber:eq:'%s'", criteria.getCroNumber()));
        }
        return StringUtils.trimToNull(query.toString());
    }

    private void addAnd(StringBuilder query) {
        if (query.length() > 0) {
            query.append(",and:");
        }
    }

    private Set<String> getUserCaseloadIds() {
        return caseLoadRepository.findCaseLoadsByUsername(UserSecurityUtils.getCurrentUsername()).stream().map(CaseLoad::getCaseLoadId).collect(Collectors.toSet());
    }

}
