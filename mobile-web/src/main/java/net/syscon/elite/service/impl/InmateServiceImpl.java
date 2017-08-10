package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.v2.api.model.OffenderBooking;
import net.syscon.elite.v2.api.model.OffenderBookingImpl;
import net.syscon.elite.v2.api.model.PrisonerDetail;
import net.syscon.elite.v2.api.model.PrisonerDetailImpl;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static net.syscon.elite.web.api.resource.BookingResource.Order.asc;

@Service
@Transactional(readOnly = true)
public class InmateServiceImpl implements InmateService {
    static final String DEFAULT_OFFENDER_SORT = "lastName,firstName,offenderNo";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
        return inmates.stream().map(this::convertToOffenderBooking).collect(Collectors.toList());
    }

    @Override
    public List<PrisonerDetail> findPrisoners(String firstName, String middleNames, String lastName, String pncNumber, String croNumber, Date dob, Date dobFrom, Date dobTo, String sortFields) {
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
        if (dob != null) {
            addAnd(query);
            LocalDate localDob = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            query.append(format("dateOfBirth:eq:'%s'", localDob.format(DATE_FORMAT)));
        }

        if (query.length() > 0) {
            final List<AssignedInmate> inmates = repository.findAllInmates(query.toString(), 0, Integer.MAX_VALUE, DEFAULT_OFFENDER_SORT, asc);
            return inmates.stream().map(this::convertToPrisonerDetail).collect(Collectors.toList());
        }
        return null;
    }

    private void addAnd(StringBuilder query) {
        if (query.length() > 0) {
            query.append(",and:");
        }
    }

    private OffenderBooking convertToOffenderBooking(AssignedInmate inmate) {
        final LocalDate dob = LocalDate.parse(inmate.getDateOfBirth(), DATE_FORMAT);
        final Date dobDateFmt = Date.from(dob.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return OffenderBookingImpl.builder()
                .bookingId(inmate.getBookingId() != null ? new BigDecimal(inmate.getBookingId()) : null)
                .bookingNo(inmate.getBookingNo())
                .offenderNo(inmate.getOffenderNo())
                .firstName(inmate.getFirstName())
                .middleName(inmate.getMiddleName())
                .lastName(inmate.getLastName())
                .age(inmate.getAge() != null ? inmate.getAge().intValue() : 0)
                .dateOfBirth(dobDateFmt)
                .agencyId(inmate.getAgencyId())
                .facialImageId(inmate.getFacialImageId() != null ? new BigDecimal(inmate.getFacialImageId()) : null)
                .alertsCodes(inmate.getAlertsCodes())
                .aliases(inmate.getAliases())
                .assignedLivingUnitId(new BigDecimal(inmate.getAssignedLivingUnitId()))
                .assignedLivingUnitDesc(inmate.getAssignedLivingUnitDesc())
                .assignedOfficerUserId(inmate.getAssignedOfficerUserId())
                .additionalProperties(inmate.getAdditionalProperties())
                .build();
    }

    private PrisonerDetail convertToPrisonerDetail(AssignedInmate inmate) {
        final LocalDate dob = LocalDate.parse(inmate.getDateOfBirth(), DATE_FORMAT);
        final Date dobDateFmt = Date.from(dob.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return PrisonerDetailImpl.builder()
                .nomsId(inmate.getOffenderNo())
                .firstName(inmate.getFirstName())
                .middleNames(inmate.getMiddleName())
                .lastName(inmate.getLastName())
                .dateOfBirth(dobDateFmt)
                .build();
    }
}
