package uk.gov.justice.hmpps.prison.repository;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.Alias;
import uk.gov.justice.hmpps.prison.api.model.AssignedLivingUnit;
import uk.gov.justice.hmpps.prison.api.model.CategorisationDetail;
import uk.gov.justice.hmpps.prison.api.model.CategorisationUpdateDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryRejectionDetail;
import uk.gov.justice.hmpps.prison.api.model.ImprisonmentStatus;
import uk.gov.justice.hmpps.prison.api.model.InmateBasicDetails;
import uk.gov.justice.hmpps.prison.api.model.InmateBasicDetailsDto;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.api.model.OffenderCategorise;
import uk.gov.justice.hmpps.prison.api.model.OffenderCategoriseDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifierDto;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeed;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeedDto;
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes;
import uk.gov.justice.hmpps.prison.api.model.PhysicalCharacteristic;
import uk.gov.justice.hmpps.prison.api.model.PhysicalCharacteristicDto;
import uk.gov.justice.hmpps.prison.api.model.PhysicalMark;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailDto;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria;
import uk.gov.justice.hmpps.prison.api.model.ProfileInformation;
import uk.gov.justice.hmpps.prison.api.model.ProfileInformationDto;
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustment;
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustmentDto;
import uk.gov.justice.hmpps.prison.api.support.AssessmentStatusType;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.InmateRepositorySql;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.support.AssessmentDto;
import uk.gov.justice.hmpps.prison.service.support.InmateDto;
import uk.gov.justice.hmpps.prison.util.CalcDateRanges;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;
import uk.gov.justice.hmpps.prison.util.IQueryBuilder;

import java.sql.Types;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.justice.hmpps.prison.util.DateTimeConverter.getAge;

@Repository
@Slf4j
public class InmateRepository extends RepositoryBase {

    public final static String QUERY_OPERATOR_AND = "and:";
    public final static String QUERY_OPERATOR_OR = "or:";

    private final static Set<String> standardCategoryCodes = Set.of("B", "C", "D", "R", "S", "T");
    private final static Set<String> validCategoryCodes = Set.of("B", "C", "D", "U", "R", "S", "T");
    private final static Set<String> validAssessStatus = Set.of("A", "P");

    private static final Map<String, FieldMapper> OFFENDER_BOOKING_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
        .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
        .put("BOOKING_NO", new FieldMapper("bookingNo"))
        .put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
        .put("AGY_LOC_ID", new FieldMapper("agencyId"))
        .put("FIRST_NAME", new FieldMapper("firstName", null, null, StringUtils::upperCase))
        .put("MIDDLE_NAME", new FieldMapper("middleName", null, null, StringUtils::upperCase))
        .put("LAST_NAME", new FieldMapper("lastName", null, null, StringUtils::upperCase))
        .put("BIRTH_DATE", new FieldMapper("dateOfBirth", DateTimeConverter::toISO8601LocalDate))
        .put("ALERT_TYPES", new FieldMapper("alertsCodes", value -> Arrays.asList(value.toString().split(","))))
        .put("ALIASES", new FieldMapper("aliases", value -> Arrays.asList(value.toString().split(","))))
        .put("FACE_IMAGE_ID", new FieldMapper("facialImageId"))
        .put("LIVING_UNIT_ID", new FieldMapper("assignedLivingUnitId"))
        .put("LIVING_UNIT_DESC", new FieldMapper("assignedLivingUnitDesc", value -> RegExUtils.replaceFirst((String) value, "^[A-Z|a-z|0-9]+\\-", "")))
        .put("BAND_CODE", new FieldMapper("bandCode"))
        .put("IMPRISONMENT_STATUS", new FieldMapper("imprisonmentStatus"))
        .build();

    private final Map<String, FieldMapper> inmateDetailsMapping = new ImmutableMap.Builder<String, FieldMapper>()
        .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
        .put("BOOKING_NO", new FieldMapper("bookingNo"))
        .put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
        .put("FIRST_NAME", new FieldMapper("firstName"))
        .put("MIDDLE_NAME", new FieldMapper("middleName"))
        .put("LAST_NAME", new FieldMapper("lastName"))
        .put("AGY_LOC_ID", new FieldMapper("agencyId"))
        .put("LIVING_UNIT_ID", new FieldMapper("assignedLivingUnitId"))
        .put("RELIGION", new FieldMapper("religion")) // deprecated, please remove
        .put("FACE_IMAGE_ID", new FieldMapper("facialImageId"))
        .put("BIRTH_DATE", new FieldMapper("dateOfBirth", DateTimeConverter::toISO8601LocalDate))
        .put("ACTIVE_FLAG", new FieldMapper("activeFlag", value -> "Y".equalsIgnoreCase(value.toString())))
        .build();

    private final Map<String, FieldMapper> physicalAttributesMapping = new ImmutableMap.Builder<String, FieldMapper>()
        .put("GENDER", new FieldMapper("gender"))
        .put("ETHNICITY", new FieldMapper("ethnicity"))
        .put("RACE_CODE", new FieldMapper("raceCode"))
        .put("HEIGHT_FT", new FieldMapper("heightFeet"))
        .put("HEIGHT_IN", new FieldMapper("heightInches"))
        .put("HEIGHT_CM", new FieldMapper("heightCentimetres"))
        .put("WEIGHT_LBS", new FieldMapper("weightPounds"))
        .put("WEIGHT_KG", new FieldMapper("weightKilograms"))
        .build();


    private final Map<String, FieldMapper> assignedLivingUnitMapping = new ImmutableMap.Builder<String, FieldMapper>()
        .put("AGY_LOC_ID", new FieldMapper("agencyId"))
        .put("LIVING_UNIT_ID", new FieldMapper("locationId"))
        .put("LIVING_UNIT_DESCRIPTION", new FieldMapper("description", value -> RegExUtils.replaceFirst((String) value, "^[A-Z|a-z|0-9]+\\-", "")))
        .put("AGENCY_NAME", new FieldMapper("agencyName"))
        .build();

    private final Map<String, FieldMapper> physicalMarkMapping = new ImmutableMap.Builder<String, FieldMapper>()
        .put("COMMENT_TEXT", new FieldMapper("comment"))
        .build();

    private static final RowMapper<PersonalCareNeedDto> PERSONAL_CARE_NEEDS_MAPPER = new DataClassByColumnRowMapper<>(PersonalCareNeedDto.class);
    private static final RowMapper<ReasonableAdjustmentDto> REASONABLE_ADJUSTMENTS_MAPPER = new DataClassByColumnRowMapper<>(ReasonableAdjustmentDto.class);

    private static final StandardBeanPropertyRowMapper<AssessmentDto> ASSESSMENT_MAPPER = new StandardBeanPropertyRowMapper<>(AssessmentDto.class);
    private static final RowMapper<PhysicalCharacteristicDto> PHYSICAL_CHARACTERISTIC_MAPPER = new DataClassByColumnRowMapper<>(PhysicalCharacteristicDto.class);
    private static final RowMapper<InmateDto> INMATE_MAPPER = new StandardBeanPropertyRowMapper<>(InmateDto.class);
    private static final RowMapper<ProfileInformationDto> PROFILE_INFORMATION_MAPPER = new DataClassByColumnRowMapper<>(ProfileInformationDto.class);
    private static final RowMapper<OffenderIdentifierDto> OFFENDER_IDENTIFIER_MAPPER = new DataClassByColumnRowMapper<>(OffenderIdentifierDto.class);
    private static final RowMapper<OffenderCategoriseDto> OFFENDER_CATEGORY_MAPPER = new DataClassByColumnRowMapper<>(OffenderCategoriseDto.class);

    private static final DataClassByColumnRowMapper<PrisonerDetailDto> PRISONER_DETAIL_MAPPER = new DataClassByColumnRowMapper<>(PrisonerDetailDto.class);

    private static final RowMapper<InmateBasicDetailsDto> OFFENDER_BASIC_DETAILS_MAPPER = new DataClassByColumnRowMapper<>(InmateBasicDetailsDto.class);

    private static final RowMapper<ImprisonmentStatus> IMPRISONMENT_STATUS_MAPPER = new StandardBeanPropertyRowMapper<>(ImprisonmentStatus.class);

    private static final Map<String, FieldMapper> ALIAS_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
        .put("LAST_NAME", new FieldMapper("lastName"))
        .put("FIRST_NAME", new FieldMapper("firstName"))
        .put("MIDDLE_NAME", new FieldMapper("middleName"))
        .put("BIRTH_DATE", new FieldMapper("dob", DateTimeConverter::toISO8601LocalDate))
        .put("SEX", new FieldMapper("gender"))
        .put("ETHNICITY", new FieldMapper("ethnicity"))
        .put("ALIAS_TYPE", new FieldMapper("nameType"))
        .put("CREATE_DATE", new FieldMapper("createDate", DateTimeConverter::toISO8601LocalDate))
        .build();

    private static final Set<String> UNSENTENCED_OR_UNCLASSIFIED_CATEGORY_CODES = Set.of("U", "X", "Z");

    private final Map<String, FieldMapper> PRISONER_DETAIL_WITH_OFFENDER_ID_FIELD_MAP;
    private final Clock clock;

    InmateRepository(final Clock clock) {
        final Map<String, FieldMapper> map = new HashMap<>(PRISONER_DETAIL_MAPPER.getFieldMap());
        map.put("OFFENDER_ID", new FieldMapper("OFFENDER_ID"));
        PRISONER_DETAIL_WITH_OFFENDER_ID_FIELD_MAP = map;
        this.clock = clock;
    }


    public Page<OffenderBooking> findInmatesByLocation(final Long locationId,
                                                       final String locationTypeRoot,
                                                       final String caseLoadId,
                                                       final String orderByField,
                                                       final Order order,
                                                       final long offset,
                                                       final long limit) {

        final var initialSql = InmateRepositorySql.FIND_INMATES_BY_LOCATION.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

        final var sql = builder
            .addRowCount()
            .addOrderBy(order, orderByField)
            .addPagination()
            .build();

        final var assignedInmateRowMapper =
            Row2BeanRowMapper.makeMapping(OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

        final var paRowMapper = new PageAwareRowMapper<>(assignedInmateRowMapper);

        final var results = jdbcTemplate.query(
            sql,
            createParams("locationId", locationId,
                "locationTypeRoot", locationTypeRoot,
                "caseLoadId", caseLoadId,
                "offset", offset,
                "limit", limit),
            paRowMapper);

        results.forEach(this::calcAdditionalInformation);

        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
    }

    private void calcAdditionalInformation(final OffenderBooking booking) {
        booking.setAge(getAge(booking.getDateOfBirth(), LocalDate.now(clock)));
        booking.deriveLegalDetails();
    }


    public List<InmateDto> findInmatesByLocation(final String agencyId, final List<Long> locations, final Set<String> caseLoadIds) {
        return jdbcTemplate.query(InmateRepositorySql.FIND_INMATES_OF_LOCATION_LIST.getSql(),
            createParams("agencyId", agencyId, "locations", locations, "caseLoadIds", caseLoadIds), INMATE_MAPPER);
    }


    public Page<OffenderBooking> searchForOffenderBookings(final OffenderBookingSearchRequest request) {
        var initialSql = InmateRepositorySql.FIND_ALL_INMATES.getSql();
        initialSql += " AND " + InmateRepositorySql.LOCATION_FILTER_SQL.getSql();

        if (!request.getCaseloads().isEmpty()) {
            initialSql += " AND " + InmateRepositorySql.CASELOAD_FILTER.getSql();
        }

        if (StringUtils.isNotBlank(request.getOffenderNo())) {
            initialSql += " AND O.OFFENDER_ID_DISPLAY = :offenderNo ";
        }

        if (StringUtils.isNotBlank(request.getSearchTerm1()) && StringUtils.isNotBlank(request.getSearchTerm2())) {
            initialSql += " AND ((O.LAST_NAME like :searchTerm1 and O.FIRST_NAME like :searchTerm2) " +
                "OR (O.FIRST_NAME like :searchTerm1 and O.LAST_NAME like :searchTerm2) " +
                "OR (O.FIRST_NAME like :searchTermCombined) " +
                "OR (O.LAST_NAME like :searchTermCombined)) ";
        } else if (StringUtils.isNotBlank(request.getSearchTerm1())) {
            initialSql += " AND (O.FIRST_NAME like :searchTerm1 OR O.LAST_NAME like :searchTerm1) ";
        } else if (StringUtils.isNotBlank(request.getSearchTerm2())) {
            initialSql += " AND (O.FIRST_NAME like :searchTerm2 OR O.LAST_NAME like :searchTerm2) ";
        }

        if (request.getAlerts() != null && !request.getAlerts().isEmpty()) {
            initialSql += " AND " + InmateRepositorySql.ALERT_FILTER.getSql();
        }

        // Search by specific convictedStatus (Convicted is any sentence with a bandCode <=8, Remand is any with a bandCode > 8)

        if (request.getConvictedStatus() != null && !StringUtils.equalsIgnoreCase(request.getConvictedStatus(), "all")) {
            if (StringUtils.equalsIgnoreCase(request.getConvictedStatus(), "convicted")) {
                initialSql += " AND (CAST(IST.BAND_CODE AS int) <= 8 OR CAST(IST.BAND_CODE AS int) = 11) ";
            } else if (StringUtils.equalsIgnoreCase(request.getConvictedStatus(), "remand")) {
                initialSql += " AND ((CAST(IST.BAND_CODE AS int) > 8 AND CAST(IST.BAND_CODE AS int) < 11) OR CAST(IST.BAND_CODE AS int) > 11)";
            } else {
                log.info("Ignoring unrecognised value requested for convictionStatus [" + request.getConvictedStatus() + "]");
            }
        }

        if (request.getFromDob() != null || request.getToDob() != null) {
            if (request.getFromDob() != null) {
                initialSql += " AND O.BIRTH_DATE >= :fromDob ";
            }
            if (request.getToDob() != null) {
                initialSql += " AND O.BIRTH_DATE <= :toDob ";
            }
        }


        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

        final var sql = builder
            .addRowCount()
            .addOrderBy(request.getPageRequest().getOrder(), request.getPageRequest().getOrderBy())
            .addPagination()
            .build();

        final var offenderBookingRowMapper =
            Row2BeanRowMapper.makeMapping(OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

        final var paRowMapper = new PageAwareRowMapper<>(offenderBookingRowMapper);

        final var trimmedSearch1 = StringUtils.trimToEmpty(request.getSearchTerm1());
        final var trimmedSearch2 = StringUtils.trimToEmpty(request.getSearchTerm2());
        final var offenderBookings = jdbcTemplate.query(
            sql,
            createParams(
                "offenderNo", request.getOffenderNo(),
                "searchTerm1", trimmedSearch1 + "%",
                "searchTerm2", trimmedSearch2 + "%",
                "searchTermCombined", trimmedSearch1 + "%" + trimmedSearch2 + "%",
                "locationPrefix", StringUtils.trimToEmpty(request.getLocationPrefix()) + "-%",
                "caseLoadId", request.getCaseloads(),
                "fromDob", request.getFromDob(),
                "toDob", request.getToDob(),
                "alerts", request.getAlerts(),
                "offset", request.getPageRequest().getOffset(),
                "limit", request.getPageRequest().getLimit()),
            paRowMapper);

        offenderBookings.forEach(this::calcAdditionalInformation);

        return new Page<>(offenderBookings, paRowMapper.getTotalRecords(), request.getPageRequest().getOffset(), request.getPageRequest().getLimit());
    }


    public Page<PrisonerDetail> findOffenders(final String query, final PageRequest pageRequest) {
        final var initialSql = InmateRepositorySql.FIND_OFFENDERS.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, PRISONER_DETAIL_MAPPER.getFieldMap());
        return getPrisonerDetailPage(query, pageRequest, builder);
    }


    public Page<PrisonerDetail> findOffendersWithAliases(final String query, final PageRequest pageRequest) {
        final var initialSql = InmateRepositorySql.FIND_OFFENDERS_WITH_ALIASES.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, PRISONER_DETAIL_WITH_OFFENDER_ID_FIELD_MAP);

        return getPrisonerDetailPage(
            query,
            new PageRequest(
                pageRequest.getOrderBy() + ",OFFENDER_ID",
                pageRequest.getOrder(),
                pageRequest.getOffset(),
                pageRequest.getLimit()
            ),
            builder);
    }

    private Page<PrisonerDetail> getPrisonerDetailPage(final String query, final PageRequest pageRequest, final IQueryBuilder builder) {
        final var sql = builder
            .addQuery(query)
            .addRowCount()
            .addPagination()
            .addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
            .build();

        final var paRowMapper = new PageAwareRowMapper<>(PRISONER_DETAIL_MAPPER);

        final var params = createParams("offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        final var prisonerDetailDtos = jdbcTemplate.query(sql, params, paRowMapper);
        final var prisonerDetails = prisonerDetailDtos.stream()
            .map(PrisonerDetailDto::toPrisonerDetail)
            .map(PrisonerDetail::deriveLegalDetails).collect(Collectors.toList());
        return new Page<>(prisonerDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }


    public List<PhysicalMark> findPhysicalMarks(final long bookingId) {
        final var sql = InmateRepositorySql.FIND_PHYSICAL_MARKS_BY_BOOKING.getSql();

        final var physicalMarkRowMapper =
            Row2BeanRowMapper.makeMapping(PhysicalMark.class, physicalMarkMapping);

        return jdbcTemplate.query(
            sql,
            createParams("bookingId", bookingId),
            physicalMarkRowMapper);
    }


    public List<PersonalCareNeed> findPersonalCareNeeds(final long bookingId, final Set<String> problemCodes) {
        final var sql = InmateRepositorySql.FIND_PERSONAL_CARE_NEEDS_BY_BOOKING.getSql();

        final var needs = jdbcTemplate.query(
            sql,
            createParams("bookingId", bookingId, "problemCodes", problemCodes),
            PERSONAL_CARE_NEEDS_MAPPER);
        return needs.stream().map(PersonalCareNeedDto::toPersonalCareNeed).collect(Collectors.toList());
    }


    public List<PersonalCareNeed> findPersonalCareNeeds(final List<String> offenderNos, final Set<String> problemCodes) {
        final var sql = InmateRepositorySql.FIND_PERSONAL_CARE_NEEDS_BY_OFFENDER.getSql();

        final var needs = jdbcTemplate.query(
            sql,
            createParams("offenderNos", offenderNos, "problemCodes", problemCodes),
            PERSONAL_CARE_NEEDS_MAPPER);
        return needs.stream().map(PersonalCareNeedDto::toPersonalCareNeed).collect(Collectors.toList());
    }

    public List<ReasonableAdjustment> findReasonableAdjustments(final long bookingId, final List<String> treatmentCodes) {
        final var sql = InmateRepositorySql.FIND_REASONABLE_ADJUSTMENTS_BY_BOOKING.getSql();

        final var adjustments = jdbcTemplate.query(
            sql,
            createParams("bookingId", bookingId, "treatmentCodes", treatmentCodes),
            REASONABLE_ADJUSTMENTS_MAPPER);
        return adjustments.stream().map(ReasonableAdjustmentDto::toReasonableAdjustment).collect(Collectors.toList());
    }


    public List<PhysicalCharacteristic> findPhysicalCharacteristics(final long bookingId) {
        final var sql = InmateRepositorySql.FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING.getSql();

        final var characteristics = jdbcTemplate.query(
            sql,
            createParams("bookingId", bookingId),
            PHYSICAL_CHARACTERISTIC_MAPPER);
        return characteristics.stream().map(PhysicalCharacteristicDto::toPhysicalCharacteristic).collect(Collectors.toList());
    }


    public List<ProfileInformation> getProfileInformation(final long bookingId) {
        final var sql = InmateRepositorySql.FIND_PROFILE_INFORMATION_BY_BOOKING.getSql();

        final var information = jdbcTemplate.query(
            sql,
            createParams("bookingId", bookingId),
            PROFILE_INFORMATION_MAPPER);
        return information.stream().map(ProfileInformationDto::toProfileInformation).collect(Collectors.toList());
    }


    public List<OffenderIdentifier> getOffenderIdentifiers(final long bookingId) {
        final var sql = InmateRepositorySql.GET_OFFENDER_IDENTIFIERS_BY_BOOKING.getSql();

        final var identifiers = jdbcTemplate.query(
            sql,
            createParams("bookingId", bookingId),
            OFFENDER_IDENTIFIER_MAPPER);
        return identifiers.stream().map(OffenderIdentifierDto::toOffenderIdentifier).collect(Collectors.toList());
    }

    public List<OffenderIdentifier> getOffenderIdentifiersByOffenderId(final long offenderId) {
        final var sql = InmateRepositorySql.GET_OFFENDER_IDENTIFIERS_BY_OFFENDER_ID.getSql();

        final var identifiers = jdbcTemplate.query(
            sql,
            createParams("offenderId", offenderId),
            OFFENDER_IDENTIFIER_MAPPER);
        return identifiers.stream().map(OffenderIdentifierDto::toOffenderIdentifier).collect(Collectors.toList());
    }

    public List<OffenderIdentifier> getOffenderIdentifiersByTypeAndValue(final String identifierType, final String identifierValue) {
        final var sql = InmateRepositorySql.FIND_IDENTIFIER_RECORDS_BY_TYPE_AND_VALUE.getSql();

        final var identifiers = jdbcTemplate.query(
            sql,
            createParams("identifierType", identifierType, "identifierValue", identifierValue),
            OFFENDER_IDENTIFIER_MAPPER);
        return identifiers.stream().map(OffenderIdentifierDto::toOffenderIdentifier).collect(Collectors.toList());
    }


    public Optional<PhysicalAttributes> findPhysicalAttributes(final long bookingId) {
        final var sql = InmateRepositorySql.FIND_PHYSICAL_ATTRIBUTES_BY_BOOKING.getSql();

        final var physicalAttributesRowMapper =
            Row2BeanRowMapper.makeMapping(PhysicalAttributes.class, physicalAttributesMapping);

        PhysicalAttributes physicalAttributes;
        try {
            physicalAttributes = jdbcTemplate.queryForObject(
                sql,
                createParams("bookingId", bookingId),
                physicalAttributesRowMapper);
        } catch (final EmptyResultDataAccessException e) {
            physicalAttributes = null;
        }
        return Optional.ofNullable(physicalAttributes);
    }


    public List<AssessmentDto> findAssessments(final List<Long> bookingIds, final String assessmentCode, final Set<String> caseLoadId) {
        var initialSql = InmateRepositorySql.FIND_ACTIVE_APPROVED_ASSESSMENT.getSql();
        if (!caseLoadId.isEmpty()) {
            initialSql += " AND " + InmateRepositorySql.ASSESSMENT_CASELOAD_FILTER.getSql();
        }
        return doFindAssessments(bookingIds, assessmentCode, caseLoadId, initialSql, "bookingIds");
    }


    public List<AssessmentDto> findAssessmentsByOffenderNo(final List<String> offenderNos, final String assessmentCode, final Set<String> caseLoadId, final boolean latestOnly, final boolean activeOnly) {
        var initialSql = InmateRepositorySql.FIND_APPROVED_ASSESSMENT_BY_OFFENDER_NO.getSql();
        if (!caseLoadId.isEmpty()) {
            initialSql += " AND " + InmateRepositorySql.ASSESSMENT_CASELOAD_FILTER.getSql();
        }
        if (latestOnly) {
            initialSql += " AND OB.BOOKING_SEQ = 1";
        }
        if (activeOnly) {
            initialSql += " AND OFF_ASS.ASSESS_STATUS = 'A'";
        }
        return doFindAssessments(offenderNos, assessmentCode, caseLoadId, initialSql, "offenderNos");
    }

    private List<AssessmentDto> doFindAssessments(final List<?> ids, final String assessmentCode,
                                                  final Set<String> caseLoadId, final String initialSql, final String idParam) {
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, ASSESSMENT_MAPPER.getFieldMap());

        final var sql = builder
            .addOrderBy(Order.ASC, "bookingId")
            // ensure CSRA is the first:
            .addOrderBy(Order.DESC, "cellSharingAlertFlag,assessmentDate,assessmentSeq")
            .build();

        final var params = createParams(
            idParam, ids,
            "assessmentCode", assessmentCode,
            "caseLoadId", caseLoadId);

        return jdbcTemplate.query(sql, params, ASSESSMENT_MAPPER);
    }


    public List<OffenderCategorise> getUncategorised(final String agencyId) {
        final var dtos = jdbcTemplate.query(
            InmateRepositorySql.GET_UNCATEGORISED.getSql(),
            createParams("agencyId", agencyId, "assessmentId", getCategoryAssessmentTypeId()),
            OFFENDER_CATEGORY_MAPPER);

        final var rawData = dtos.stream().map(OffenderCategoriseDto::toOffenderCategorise).toList();
        return applyCategorisationRestrictions(rawData);
    }


    public List<OffenderCategorise> getApprovedCategorised(final String agencyId, final LocalDate cutoffDate) {
        final var dtos = jdbcTemplate.query(
            InmateRepositorySql.GET_APPROVED_CATEGORISED.getSql(),
            createParams("agencyId", agencyId,
                "cutOffDate", DateTimeConverter.toDate(cutoffDate),
                "assessStatus", "A",
                "assessmentId", getCategoryAssessmentTypeId()),
            OFFENDER_CATEGORY_MAPPER);

        final var rawData = dtos.stream().map(OffenderCategoriseDto::toOffenderCategorise).toList();
        return removeEarlierCategorisations(rawData);
    }


    public List<OffenderCategorise> getRecategorise(final String agencyId, final LocalDate cutoffDate) {
        final var rawData = jdbcTemplate.query(
            InmateRepositorySql.GET_RECATEGORISE.getSql(),
            createParams("agencyId", agencyId,
                "assessmentId", getCategoryAssessmentTypeId()),
            OFFENDER_CATEGORY_MAPPER);

        final var offenderNoMap = rawData.stream()
            .map(OffenderCategoriseDto::toOffenderCategorise)
            .collect(Collectors.groupingBy(OffenderCategorise::getOffenderNo));
        final var offendersLatestCategorisations = offenderNoMap
            .values().stream()
            .filter(this::oneStandardCategorisationExists)
            .map(this::getCategorisationsWithValidAssessStatus)
            .map(this::getLatestOffenderCategorisations)
            .flatMap(List::stream);
        return offendersLatestCategorisations
            .filter(this::validCategoryCode)
            .filter(categorisation -> nextReviewDateIsBeforeCutOffDateOrPendingCategorisation(categorisation, cutoffDate))
            .toList();
    }

    private boolean oneStandardCategorisationExists(final List<OffenderCategorise> offenderCategorisations) {
        return offenderCategorisations.stream().anyMatch(cat -> cat.getCategory() != null && standardCategoryCodes.contains(cat.getCategory()));
    }

    private boolean nextReviewDateIsBeforeCutOffDateOrPendingCategorisation(OffenderCategorise categorisation, final LocalDate cutoffDate) {
        return "P".equals(categorisation.getAssessStatus()) || (categorisation.getNextReviewDate() != null && !cutoffDate.isBefore(categorisation.getNextReviewDate()));
    }

    private boolean validCategoryCode(OffenderCategorise categorisation) {
        return categorisation.getCategory() != null && validCategoryCodes.contains(categorisation.getCategory());
    }

    private List<OffenderCategorise> getCategorisationsWithValidAssessStatus(final List<OffenderCategorise> offenderCategorisations) {
        return offenderCategorisations.stream().filter(cat -> cat.getCategory() != null && validAssessStatus.contains(cat.getAssessStatus())).toList();
    }

    public List<OffenderCategorise> getOffenderCategorisations(final List<Long> bookingIds, final String agencyId, final boolean latestOnly) {
        final var dtos = jdbcTemplate.query(
            InmateRepositorySql.GET_OFFENDER_CATEGORISATIONS.getSql(),
            createParams("bookingIds", bookingIds,
                "agencyId", agencyId,
                "assessmentId", getCategoryAssessmentTypeId()),
            OFFENDER_CATEGORY_MAPPER);

        final var rawData = dtos.stream().map(OffenderCategoriseDto::toOffenderCategorise).toList();
        return latestOnly ? removeEarlierCategorisations(rawData) : rawData;
    }

    private Long getCategoryAssessmentTypeId() {
        return jdbcTemplate.queryForObject(InmateRepositorySql.GET_CATEGORY_ASSESSMENT_ID.getSql(), Map.of(), Long.class);
    }

    private List<OffenderCategorise> applyCategorisationRestrictions(final List<OffenderCategorise> catListRaw) {
        // for every group check that assessment is null OR it is the latest categorisation record
        final var catList = removeEarlierCategorisations(catListRaw);

        // remove the active assessment status offenders - we only want null assessment, pending assessments, or
        // 'unclassified' (Z,X) or 'unsentenced' (U) categories
        return catList.stream()
            .filter(o -> o.getAssessStatus() == null || o.getAssessStatus().equals("P")
                || UNSENTENCED_OR_UNCLASSIFIED_CATEGORY_CODES.contains(o.getCategory()))

            .map(OffenderCategorise::deriveStatus)
            .toList();
    }

    private List<OffenderCategorise> getLatestOffenderCategorisations(final List<OffenderCategorise> individualCatList) {
        final var maxSeqOpt = individualCatList.stream().max(Comparator.comparing(OffenderCategorise::getAssessmentSeq));
        final var maxDateOpt = individualCatList.stream().max(Comparator.comparing(OffenderCategorise::getAssessmentDate));
        if (maxDateOpt.isEmpty()) return individualCatList;

        return individualCatList.stream()
            .filter(oc -> oc.getAssessmentSeq() == null || (oc.getAssessmentSeq().equals(maxSeqOpt.get().getAssessmentSeq()) && oc.getAssessmentDate().equals(maxDateOpt.get().getAssessmentDate())))
            .toList();
    }

    private List<OffenderCategorise> removeEarlierCategorisations(final List<OffenderCategorise> catList) {
        final var bookingIdMap = catList.stream().collect(Collectors.groupingBy(OffenderCategorise::getBookingId));
        bookingIdMap.replaceAll((k, v) -> getLatestOffenderCategorisations(v));

        return bookingIdMap.values().stream()
            .flatMap(List::stream)
            .toList();
    }

    public Optional<AssignedLivingUnit> findAssignedLivingUnit(final long bookingId, final String locationTypeRoot) {
        final var sql = InmateRepositorySql.FIND_ASSIGNED_LIVING_UNIT.getSql();

        final var assignedLivingUnitRowMapper =
            Row2BeanRowMapper.makeMapping(AssignedLivingUnit.class, assignedLivingUnitMapping);

        AssignedLivingUnit assignedLivingUnit;
        try {
            assignedLivingUnit = jdbcTemplate.queryForObject(
                sql,
                createParams("bookingId", bookingId, "locationTypeRoot", locationTypeRoot),
                assignedLivingUnitRowMapper);
        } catch (final EmptyResultDataAccessException ex) {
            assignedLivingUnit = null;
        }

        return Optional.ofNullable(assignedLivingUnit);
    }

    public Optional<InmateDetail> findInmate(final Long bookingId) {
        try {
            final var inmate = Optional.ofNullable(jdbcTemplate.queryForObject(
                InmateRepositorySql.FIND_INMATE_DETAIL.getSql(),
                createParams("bookingId", bookingId),
                new StandardBeanPropertyRowMapper<>(InmateDetail.class)));
            inmate.ifPresent(o -> o.setAge(getAge(o.getDateOfBirth(), LocalDate.now(clock))));
            return inmate;
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<InmateDetail> findOffender(final String offenderNo) {
        final var offender = jdbcTemplate.query(
                InmateRepositorySql.FIND_OFFENDER.getSql(),
                createParams("offenderNo", offenderNo),
                new StandardBeanPropertyRowMapper<>(InmateDetail.class))
            .stream()
            .findFirst();
        offender.ifPresent(o -> o.setAge(getAge(o.getDateOfBirth(), LocalDate.now(clock))));
        return offender;
    }


    public Optional<InmateDetail> getBasicInmateDetail(final Long bookingId) {
        final var builder = queryBuilderFactory.getQueryBuilder(InmateRepositorySql.FIND_BASIC_INMATE_DETAIL.getSql(), inmateDetailsMapping);
        final var sql = builder.build();

        final var inmateRowMapper = Row2BeanRowMapper.makeMapping(InmateDetail.class, inmateDetailsMapping);
        InmateDetail inmate;
        try {
            inmate = jdbcTemplate.queryForObject(
                sql,
                createParams("bookingId", bookingId),
                inmateRowMapper);
        } catch (final EmptyResultDataAccessException ex) {
            inmate = null;
        }

        return Optional.ofNullable(inmate);
    }


    public Page<Alias> findInmateAliases(final Long bookingId, final String orderByFields, final Order order, final long offset, final long limit) {
        final var initialSql = InmateRepositorySql.FIND_INMATE_ALIASES.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, ALIAS_MAPPING);

        final var sql = builder
            .addRowCount()
            .addPagination()
            .addOrderBy(order, orderByFields)
            .build();

        final var aliasAttributesRowMapper = Row2BeanRowMapper.makeMapping(Alias.class, ALIAS_MAPPING);
        final var paRowMapper = new PageAwareRowMapper<>(aliasAttributesRowMapper);

        final var results = jdbcTemplate.query(
            sql,
            createParams("bookingId", bookingId, "offset", offset, "limit", limit),
            paRowMapper);
        results.forEach(alias -> alias.setAge(getAge(alias.getDob(), LocalDate.now(clock))));
        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
    }


    public Map<String, Long> insertCategory(final CategorisationDetail detail, final String agencyId, final Long assessStaffId, final String userId) {

        final var newSeq = getOffenderAssessmentSeq(detail.getBookingId()) + 1;
        jdbcTemplate.update(
            InmateRepositorySql.INSERT_CATEGORY.getSql(),
            createParams("bookingId", detail.getBookingId(),
                "assessmentTypeId", getCategoryAssessmentTypeId(),
                "seq", newSeq,
                "assessmentDate", LocalDate.now(),
                "assessStatus", "P",
                "category", detail.getCategory(),
                "assessStaffId", assessStaffId,
                "assessComment", detail.getComment(),
                "reviewDate", detail.getNextReviewDate(),
                "userId", userId,
                "assessCommitteeCode", detail.getCommittee(),
                "dateTime", LocalDateTime.now(),
                "agencyId", agencyId,
                "placementAgencyId", detail.getPlacementAgencyId()));

        return Map.of("sequenceNumber", (long) newSeq, "bookingId", detail.getBookingId());
    }


    public void updateCategory(final CategorisationUpdateDetail detail) {

        final int result = jdbcTemplate.update(
            InmateRepositorySql.UPDATE_CATEGORY.getSql(),
            createParams("bookingId", detail.getBookingId(),
                "seq", detail.getAssessmentSeq(),
                "assessmentTypeId", getCategoryAssessmentTypeId(),
                "assessmentDate", LocalDate.now(),
                "category", detail.getCategory(),
                "assessComment", detail.getComment(),
                "reviewDate", detail.getNextReviewDate(),
                "assessCommitteeCode", detail.getCommittee()));
        if (result != 1) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("No pending category assessment found, category %.10s, booking %d, seq %d",
                detail.getCategory(),
                detail.getBookingId(),
                detail.getAssessmentSeq()));
        }
    }


    public void approveCategory(final CategoryApprovalDetail detail) {
        final var assessmentId = getCategoryAssessmentTypeId();

        // get all active or pending categorisation sequences ordered desc
        final var sequences = jdbcTemplate.query(
            InmateRepositorySql.GET_OFFENDER_CATEGORY_SEQUENCES.getSql(),
            createParams("bookingId", detail.getBookingId(),
                "assessmentTypeId", assessmentId,
                "statuses", Arrays.asList("A", "P")),
            SingleColumnRowMapper.newInstance(Integer.class));
        if (CollectionUtils.isEmpty(sequences)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("No category assessment found, category %.10s, booking %d",
                detail.getCategory(),
                detail.getBookingId()));
        }
        final int maxSequence = sequences.get(0);

        if (detail.getAssessmentSeq() != null && detail.getAssessmentSeq() != maxSequence) {
            log.warn(String.format("approveCategory: sequences do not match for booking id %d: maxSequence = %d, PG Nomis seq = %d",
                detail.getBookingId(),
                maxSequence,
                detail.getAssessmentSeq()));
        }

        final var approvalResult = jdbcTemplate.update(
            InmateRepositorySql.APPROVE_CATEGORY.getSql(),
            createParams("bookingId", detail.getBookingId(),
                "seq", maxSequence,
                "assessmentTypeId", assessmentId,
                "assessStatus", "A",
                "category", detail.getCategory(),
                "evaluationDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(detail.getEvaluationDate())),
                "evaluationResultCode", "APP",
                "reviewCommitteeCode", detail.getReviewCommitteeCode(),
                "committeeCommentText", detail.getCommitteeCommentText(),
                "nextReviewDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(detail.getNextReviewDate())),
                "approvedCategoryComment", detail.getApprovedCategoryComment(),
                "approvedPlacementAgencyId", detail.getApprovedPlacementAgencyId(),
                "approvedPlacementText", detail.getApprovedPlacementText()
            )
        );
        if (approvalResult != 1) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("No pending category assessment found, category %.10s, booking %d, seq %d",
                detail.getCategory(),
                detail.getBookingId(),
                maxSequence));
        }
        if (sequences.size() > 1) {
            final var previousSequences = sequences.stream().skip(1)
                .toList();
            final var updatePreviousResult = jdbcTemplate.update(
                InmateRepositorySql.CATEGORY_SET_STATUS.getSql(),
                createParams("bookingId", detail.getBookingId(),
                    "seq", previousSequences,
                    "assessStatus", "I"
                )
            );
            if (updatePreviousResult < 1) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("Previous category assessment not found, booking %d, seq %s",
                    detail.getBookingId(),
                    previousSequences));
            }
        }
    }


    public void rejectCategory(final CategoryRejectionDetail detail) {
        final var assessmentId = getCategoryAssessmentTypeId();
        final var result = jdbcTemplate.update(
            InmateRepositorySql.REJECT_CATEGORY.getSql(),
            createParams("bookingId", detail.getBookingId(),
                "seq", detail.getAssessmentSeq(),
                "assessmentTypeId", assessmentId,
                "evaluationDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(detail.getEvaluationDate())),
                "evaluationResultCode", "REJ",
                "reviewCommitteeCode", detail.getReviewCommitteeCode(),
                "committeeCommentText", detail.getCommitteeCommentText()
            )
        );
        if (result != 1) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("Category assessment not found, booking %d, seq %d",
                detail.getBookingId(),
                detail.getAssessmentSeq()));
        }
    }


    public int setCategorisationInactive(final long bookingId, final AssessmentStatusType status) {
        final var assessmentId = getCategoryAssessmentTypeId();
        final var mapper = SingleColumnRowMapper.newInstance(Integer.class);
        // get all active categorisation sequences
        final var sequences = jdbcTemplate.query(
            InmateRepositorySql.GET_OFFENDER_CATEGORY_SEQUENCES.getSql(),
            createParams("bookingId", bookingId,
                "assessmentTypeId", assessmentId,
                "statuses", List.of(status == AssessmentStatusType.PENDING ? "P" : "A")),
            mapper);
        if (CollectionUtils.isEmpty(sequences)) {
            log.warn(String.format("No active category assessments found for booking id %d", bookingId));
            return 0;
        }
        final var updateResult = jdbcTemplate.update(
            InmateRepositorySql.CATEGORY_SET_STATUS.getSql(),
            createParams("bookingId", bookingId,
                "seq", sequences,
                "assessStatus", "I"
            )
        );
        if (updateResult != 1) {
            log.warn(String.format("Expected one row to be updated, got %d for booking id %d", updateResult, bookingId));
        }
        return updateResult;
    }


    public void updateActiveCategoryNextReviewDate(final long bookingId, final LocalDate date) {
        log.debug("Updating categorisation next Review date for booking id {} with value {}", bookingId, date);
        final var assessmentId = getCategoryAssessmentTypeId();

        final var result = jdbcTemplate.update(
            InmateRepositorySql.UPDATE_CATEORY_NEXT_REVIEW_DATE.getSql(),
            createParams("bookingId", bookingId,
                "assessmentTypeId", assessmentId,
                "nextReviewDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))
            )
        );

        if (result != 1) {
            var message = String.format("Unable to update next review date, could not find latest, active categorisation for booking id %d, result count = %d", bookingId, result);
            log.error(message);
            throw new EntityNotFoundException(message);
        }
    }


    public List<InmateBasicDetails> getBasicInmateDetailsForOffenders(final Set<String> offenders, final boolean accessToAllData, final Set<String> caseloads, boolean active) {
        final var baseSql = InmateRepositorySql.FIND_BASIC_INMATE_DETAIL_BY_OFFENDER_NO.getSql();
        final var withCaseloadSql = accessToAllData ? baseSql : String.format("%s AND %s", baseSql, InmateRepositorySql.CASELOAD_FILTER.getSql());
        final var sql = active ? String.format("%s AND %s", withCaseloadSql, InmateRepositorySql.ACTIVE_BOOKING_FILTER.getSql()) : withCaseloadSql;

        final var details = jdbcTemplate.query(
            sql,
            createParams("offenders", offenders, "caseLoadId", caseloads, "bookingSeq", 1),
            OFFENDER_BASIC_DETAILS_MAPPER);
        return details.stream().map(InmateBasicDetailsDto::toInmateBasicDetails).toList();
    }


    public Optional<ImprisonmentStatus> getImprisonmentStatus(final long bookingId) {
        Optional<ImprisonmentStatus> imprisonmentStatus;
        try {
            imprisonmentStatus = jdbcTemplate.query(
                    InmateRepositorySql.GET_IMPRISONMENT_STATUS.getSql(),
                    createParams("bookingId", bookingId),
                    IMPRISONMENT_STATUS_MAPPER)
                .stream().max(Comparator.comparingInt(ImprisonmentStatus::getImprisonStatusSeq));
            imprisonmentStatus.ifPresent(s -> s.setLegalStatus(uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus.calcLegalStatus(s.getBandCode(), s.getImprisonmentStatus())));
        } catch (final EmptyResultDataAccessException e) {
            imprisonmentStatus = Optional.empty();
        }
        return imprisonmentStatus;
    }


    public List<InmateBasicDetails> getBasicInmateDetailsByBookingIds(final String caseload, final List<Long> bookingIds) {
        final var sql = InmateRepositorySql.FIND_BASIC_INMATE_DETAIL_BY_BOOKING_IDS.getSql();
        final var details = jdbcTemplate.query(
            sql,
            createParams("bookingIds", bookingIds, "caseloadId", caseload),
            OFFENDER_BASIC_DETAILS_MAPPER);
        return details.stream().map(InmateBasicDetailsDto::toInmateBasicDetails).toList();
    }

    private Integer getOffenderAssessmentSeq(final Long bookingId) {

        Integer maxSeq = null;

        try {
            maxSeq = jdbcTemplate.queryForObject(
                InmateRepositorySql.OFFENDER_ASSESSMENTS_SEQ_MAX.getSql(),
                createParams("bookingId", bookingId), Integer.class);
        } catch (final EmptyResultDataAccessException ex) {
            // no row - null response
        }

        return maxSeq == null ? 1 : maxSeq;
    }

    public String generateFindOffendersQuery(final PrisonerDetailSearchCriteria criteria) {
        final var likeTemplate = "%s:like:'%s%%'";
        final var eqTemplate = "%s:eq:'%s'";
        final var inTemplate = "%s:in:%s";

        final var nameMatchingTemplate = criteria.isPartialNameMatch() ? likeTemplate : eqTemplate;
        final var logicOperator = criteria.isAnyMatch() ? QUERY_OPERATOR_OR : QUERY_OPERATOR_AND;

        final var query = new StringBuilder();

        final var sexCode = "ALL".equals(criteria.getGender()) ? null : criteria.getGender();

        if (criteria.getOffenderNos() != null && !criteria.getOffenderNos().isEmpty()) {
            if (criteria.getOffenderNos().size() == 1) {
                appendNonBlankCriteria(query, "offenderNo", criteria.getOffenderNos().get(0), eqTemplate, logicOperator);
            } else {
                appendNonBlankCriteria(query, "offenderNo", criteria.getOffenderNos().stream().collect(Collectors.joining("'|'", "'", "'")), inTemplate, logicOperator);
            }
        }

        appendNonBlankNameCriteria(query, "firstName", criteria.getFirstName(), nameMatchingTemplate, logicOperator);
        appendNonBlankNameCriteria(query, "middleNames", criteria.getMiddleNames(), nameMatchingTemplate, logicOperator);
        appendNonBlankNameCriteria(query, "lastName", criteria.getLastName(), nameMatchingTemplate, logicOperator);
        appendNonBlankNameCriteria(query, "sexCode", sexCode, nameMatchingTemplate, logicOperator);
        appendLocationCriteria(query, criteria.getLocation(), nameMatchingTemplate, logicOperator);
        appendPNCNumberCriteria(query, criteria.getPncNumber(), logicOperator);
        appendNonBlankCriteria(query, "croNumber", criteria.getCroNumber(), eqTemplate, logicOperator);

        appendDateRangeCriteria(query, criteria, logicOperator);

        return StringUtils.trimToNull(query.toString());
    }

    static void appendLocationCriteria(final StringBuilder query, final String criteriaValue,
                                       final String operatorTemplate, final String logicOperator) {
        final var neqTemplate = "%s:neq:'%s'";

        if (StringUtils.isNotBlank(criteriaValue)) {
            switch (criteriaValue) {
                case "OUT" -> appendNonBlankNameCriteria(query, "latestLocationId", criteriaValue, operatorTemplate, logicOperator);
                case "IN" -> appendNonBlankNameCriteria(query, "latestLocationId", "OUT", neqTemplate, logicOperator);
            }
        }
    }

    static void appendNonBlankNameCriteria(final StringBuilder query, final String criteriaName, final String criteriaValue,
                                           final String operatorTemplate, final String logicOperator) {
        if (StringUtils.isNotBlank(criteriaValue)) {
            final String escapedCriteriaValue;

            if (StringUtils.contains(criteriaValue, "''")) {
                escapedCriteriaValue = criteriaValue;
            } else {
                escapedCriteriaValue = RegExUtils.replaceAll(criteriaValue, "'", "''");
            }

            appendNonBlankCriteria(query, criteriaName, escapedCriteriaValue, operatorTemplate, logicOperator);
        }
    }

    static void appendNonBlankCriteria(final StringBuilder query, final String criteriaName, final String criteriaValue,
                                       final String operatorTemplate, final String logicOperator) {
        if (StringUtils.isNotBlank(criteriaValue)) {
            if (query.length() > 0) {
                query.append(",").append(logicOperator);
            }

            query.append(format(operatorTemplate, criteriaName, criteriaValue.toUpperCase()));
        }
    }

    static void appendDateRangeCriteria(final StringBuilder query, final PrisonerDetailSearchCriteria criteria,
                                        final String logicOperator) {
        final var calcDates = new CalcDateRanges(
            criteria.getDob(), criteria.getDobFrom(), criteria.getDobTo(), criteria.getMaxYearsRange());

        if (calcDates.hasDateRange()) {
            final var dateRange = calcDates.getDateRange();

            query.append(format("(%s%s:gteq:'%s':'YYYY-MM-DD',and:%s:lteq:'%s':'YYYY-MM-DD')", logicOperator, "dateOfBirth",
                DateTimeFormatter.ISO_LOCAL_DATE.format(dateRange.getMinimum()), "dateOfBirth",
                DateTimeFormatter.ISO_LOCAL_DATE.format(dateRange.getMaximum())));
        }
    }

    static void appendPNCNumberCriteria(final StringBuilder query, final String criteriaValue, final String logicOperator) {
        if (StringUtils.isNotBlank(criteriaValue)) {
            final var slashIdx = criteriaValue.indexOf('/');

            if ((slashIdx != 2) && (slashIdx != 4)) {
                throw new IllegalArgumentException("Incorrectly formatted PNC number.");
            }

            if (query.length() > 0) {
                query.append(",").append(logicOperator);
            }

            final var criteriaName = "pncNumber";

            if (slashIdx == 2) {
                query.append(format("%s:like:'%%%s'", criteriaName, criteriaValue.toUpperCase()));
            } else {
                final var altValue = StringUtils.substring(criteriaValue, 2);

                query.append(format("(%s:eq:'%s',or:%s:eq:'%s')", criteriaName, criteriaValue.toUpperCase(), criteriaName, altValue.toUpperCase()));
            }
        }
    }

}
