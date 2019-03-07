package net.syscon.elite.repository.impl;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.InmateDto;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static net.syscon.elite.repository.ImageRepository.IMAGE_DETAIL_MAPPER;

@Repository
@Slf4j
public class InmateRepositoryImpl extends RepositoryBase implements InmateRepository {

	private static final Map<String, FieldMapper> OFFENDER_BOOKING_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
            .put("OFFENDER_BOOK_ID", 	new FieldMapper("bookingId"))
            .put("BOOKING_NO", 			new FieldMapper("bookingNo"))
            .put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
            .put("AGY_LOC_ID", 			new FieldMapper("agencyId"))
            .put("FIRST_NAME", 			new FieldMapper("firstName", null, null, StringUtils::upperCase))
            .put("MIDDLE_NAME", 		new FieldMapper("middleName", null, null, StringUtils::upperCase))
            .put("LAST_NAME", 			new FieldMapper("lastName", null, null, StringUtils::upperCase))
            .put("BIRTH_DATE", 			new FieldMapper("dateOfBirth", DateTimeConverter::toISO8601LocalDate))
            .put("ALERT_TYPES", 		new FieldMapper("alertsCodes", value -> Arrays.asList(value.toString().split(","))))
            .put("ALIASES", 		    new FieldMapper("aliases", value -> Arrays.asList(value.toString().split(","))))
            .put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
            .put("LIVING_UNIT_ID",      new FieldMapper("assignedLivingUnitId"))
            .put("LIVING_UNIT_DESC",    new FieldMapper("assignedLivingUnitDesc", value -> RegExUtils.replaceFirst((String)value, "^[A-Z|a-z|0-9]+\\-", "")))
            .put("ASSIGNED_OFFICER_ID", new FieldMapper("assignedOfficerId"))
            .build();

    private final Map<String, FieldMapper> inmateDetailsMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_BOOK_ID", 	new FieldMapper("bookingId"))
			.put("BOOKING_NO", 			new FieldMapper("bookingNo"))
			.put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
			.put("FIRST_NAME", 			new FieldMapper("firstName"))
			.put("MIDDLE_NAME", 		new FieldMapper("middleName"))
			.put("LAST_NAME", 			new FieldMapper("lastName"))
			.put("AGY_LOC_ID", 			new FieldMapper("agencyId"))
			.put("LIVING_UNIT_ID",      new FieldMapper("assignedLivingUnitId"))
            .put("RELIGION", 			new FieldMapper("religion")) // deprecated, please remove
			.put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
			.put("BIRTH_DATE", 			new FieldMapper("dateOfBirth", DateTimeConverter::toISO8601LocalDate))
			.put("ASSIGNED_OFFICER_ID", new FieldMapper("assignedOfficerId"))
			.put("ACTIVE_FLAG",         new FieldMapper("activeFlag", value -> "Y".equalsIgnoreCase(value.toString())))
			.build();

	private final Map<String, FieldMapper> physicalAttributesMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("GENDER",   new FieldMapper("gender"))
			.put("ETHNICITY",  new FieldMapper("ethnicity"))
			.put("RACE_CODE",  new FieldMapper("raceCode"))
			.put("HEIGHT_FT",  new FieldMapper("heightFeet"))
			.put("HEIGHT_IN",  new FieldMapper("heightInches"))
			.put("HEIGHT_CM",  new FieldMapper("heightCentimetres"))
			.put("WEIGHT_LBS", new FieldMapper("weightPounds"))
			.put("WEIGHT_KG",  new FieldMapper("weightKilograms"))
			.build();


	private final Map<String, FieldMapper> assignedLivingUnitMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("AGY_LOC_ID", 	new FieldMapper("agencyId"))
			.put("LIVING_UNIT_ID",          new FieldMapper("locationId"))
			.put("LIVING_UNIT_DESCRIPTION", new FieldMapper("description", value -> RegExUtils.replaceFirst((String)value, "^[A-Z|a-z|0-9]+\\-", "")))
			.put("AGENCY_NAME", new FieldMapper("agencyName"))
			.build();

	private final Map<String, FieldMapper> physicalMarkMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("COMMENT_TEXT",	new FieldMapper("comment"))
			.build();

	private final StandardBeanPropertyRowMapper<AssessmentDto> ASSESSMENT_MAPPER = new StandardBeanPropertyRowMapper<>(AssessmentDto.class);
    private final StandardBeanPropertyRowMapper<PhysicalCharacteristic> PHYSICAL_CHARACTERISTIC_MAPPER = new StandardBeanPropertyRowMapper<>(PhysicalCharacteristic.class);
    private final StandardBeanPropertyRowMapper<InmateDto> INMATE_MAPPER = new StandardBeanPropertyRowMapper<>(InmateDto.class);
	private final StandardBeanPropertyRowMapper<ProfileInformation> PROFILE_INFORMATION_MAPPER = new StandardBeanPropertyRowMapper<>(ProfileInformation.class);
	private final StandardBeanPropertyRowMapper<OffenderIdentifier> OFFENDER_IDENTIFIER_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderIdentifier.class);
	private final StandardBeanPropertyRowMapper<OffenderCategorise> UNCATEGORISED_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderCategorise.class);

    private final StandardBeanPropertyRowMapper<PrisonerDetail> PRISONER_DETAIL_MAPPER =
            new StandardBeanPropertyRowMapper<>(PrisonerDetail.class);

    private final StandardBeanPropertyRowMapper<InmateBasicDetails> OFFENDER_BASIC_DETAILS_MAPPER = new StandardBeanPropertyRowMapper<>(InmateBasicDetails.class);

    private final Map<String, FieldMapper> PRISONER_DETAIL_WITH_OFFENDER_ID_FIELD_MAP;

    private final Map<String, FieldMapper> aliasMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("LAST_NAME",		new FieldMapper("lastName"))
			.put("FIRST_NAME",		new FieldMapper("firstName"))
			.put("MIDDLE_NAME",		new FieldMapper("middleName"))
			.put("BIRTH_DATE",		new FieldMapper("dob", DateTimeConverter::toISO8601LocalDate))
			.put("SEX",				new FieldMapper("gender"))
			.put("ETHNICITY",		new FieldMapper("ethnicity"))
			.put("ALIAS_TYPE",		new FieldMapper("nameType"))
			.put("CREATE_DATE",     new FieldMapper("createDate", DateTimeConverter::toISO8601LocalDate))
			.build();

    InmateRepositoryImpl() {
        final Map<String, FieldMapper> map = new HashMap<>(PRISONER_DETAIL_MAPPER.getFieldMap());
        map.put("OFFENDER_ID", new FieldMapper("OFFENDER_ID"));
        PRISONER_DETAIL_WITH_OFFENDER_ID_FIELD_MAP = map;
    }

	@Override
    public Page<OffenderBooking> findInmatesByLocation(final Long locationId, final String locationTypeRoot, final String caseLoadId, final String query, final String orderByField, final Order order, final long offset, final long limit) {
        final var initialSql = getQuery("FIND_INMATES_BY_LOCATION");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

        final var sql = builder
				.addRowCount()
				.addQuery(query)
				.addOrderBy(order, orderByField)
				.addPagination()
				.build();

        final var assignedInmateRowMapper =
				Row2BeanRowMapper.makeMapping(sql, OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

        final var paRowMapper = new PageAwareRowMapper<OffenderBooking>(assignedInmateRowMapper);

        final var results = jdbcTemplate.query(
                sql,
                createParams("locationId", locationId,
                        "locationTypeRoot", locationTypeRoot,
                        "caseLoadId", caseLoadId,
                        "offset", offset,
                        "limit", limit),
                paRowMapper);

		results.forEach(b -> b.setAge(DateTimeConverter.getAge(b.getDateOfBirth())));

		return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
	}

    @Override
    public List<InmateDto> findInmatesByLocation(final String agencyId, final List<Long> locations, final Set<String> caseLoadIds) {
        return jdbcTemplate.query(getQuery("FIND_INMATES_OF_LOCATION_LIST"),
                createParams("agencyId", agencyId, "locations", locations, "caseLoadIds", caseLoadIds), INMATE_MAPPER);
    }

	@Override
    public Page<OffenderBooking> findAllInmates(final Set<String> caseloads, final String locationTypeRoot, final String query, final PageRequest pageRequest) {
        var initialSql = getQuery("FIND_ALL_INMATES");
		if (!caseloads.isEmpty()) {
			initialSql += " AND " + getQuery("CASELOAD_FILTER");
		}
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

        final var sql = builder
				.addRowCount()
				.addQuery(query)
				.addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
				.addPagination()
				.build();

        final var assignedInmateRowMapper =
				Row2BeanRowMapper.makeMapping(sql, OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

        final var paRowMapper = new PageAwareRowMapper<OffenderBooking>(assignedInmateRowMapper);

        final var inmates = jdbcTemplate.query(
                sql,
                createParams("caseLoadId", caseloads,
                        "locationTypeRoot", locationTypeRoot,
                        "offset", pageRequest.getOffset(),
                        "limit", pageRequest.getLimit()),
                paRowMapper);
		inmates.forEach(b -> b.setAge(DateTimeConverter.getAge(b.getDateOfBirth())));
		return new Page<>(inmates, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
	}

	@Override
	@Cacheable("searchForOffenderBookings")
    public Page<OffenderBooking> searchForOffenderBookings(final Set<String> caseloads, final String offenderNo, final String searchTerm1, final String searchTerm2,
                                                           final String locationPrefix, final List<String> alerts, final String locationTypeRoot, final PageRequest pageRequest) {
        var initialSql = getQuery("FIND_ALL_INMATES");
		initialSql += " AND " + getQuery("LOCATION_FILTER_SQL");

		if (!caseloads.isEmpty()) {
			initialSql += " AND " + getQuery("CASELOAD_FILTER");
		}

		if (StringUtils.isNotBlank(offenderNo)) {
			initialSql += " AND O.OFFENDER_ID_DISPLAY = :offenderNo ";
		}

		if (StringUtils.isNotBlank(searchTerm1) && StringUtils.isNotBlank(searchTerm2)) {
			initialSql += " AND ((O.LAST_NAME like :searchTerm1 and O.FIRST_NAME like :searchTerm2) " +
					"OR (O.FIRST_NAME like :searchTerm1 and O.LAST_NAME like :searchTerm2) ) ";
		} else if (StringUtils.isNotBlank(searchTerm1)) {
			initialSql += " AND (O.FIRST_NAME like :searchTerm1 OR O.LAST_NAME like :searchTerm1) ";
		} else if (StringUtils.isNotBlank(searchTerm2)) {
			initialSql += " AND (O.FIRST_NAME like :searchTerm2 OR O.LAST_NAME like :searchTerm2) ";
		}

		if (alerts != null && !alerts.isEmpty()) {
			initialSql += " AND " + getQuery("ALERT_FILTER");
		}

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

        final var sql = builder
				.addRowCount()
				.addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
				.addPagination()
				.build();

        final var offenderBookingRowMapper =
				Row2BeanRowMapper.makeMapping(sql, OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

        final var paRowMapper = new PageAwareRowMapper<OffenderBooking>(offenderBookingRowMapper);

        final var offenderBookings = jdbcTemplate.query(
		        sql,
                createParams("offenderNo", offenderNo,
                        "searchTerm1", StringUtils.trimToEmpty(searchTerm1) + "%",
                        "searchTerm2", StringUtils.trimToEmpty(searchTerm2) + "%",
                        "locationPrefix", StringUtils.trimToEmpty(locationPrefix) + "-%",
                        "caseLoadId", caseloads,
                        "locationTypeRoot", locationTypeRoot,
                        "alerts", alerts,
                        "offset", pageRequest.getOffset(), "limit", pageRequest.getLimit()),
                paRowMapper);
		offenderBookings.forEach(b -> b.setAge(DateTimeConverter.getAge(b.getDateOfBirth())));
		return new Page<>(offenderBookings, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
	}


	@Override
    public List<Long> getPersonalOfficerBookings(final long staffId) {
		return jdbcTemplate.queryForList(
				getQuery("FIND_PERSONAL_OFFICER_BOOKINGS"),
				createParams("staffId", staffId),
				Long.class);
	}

    @Override
    public Page<PrisonerDetail> findOffenders(final String query, final PageRequest pageRequest) {
        final var initialSql = getQuery("FIND_OFFENDERS");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, PRISONER_DETAIL_MAPPER.getFieldMap());
        return getPrisonerDetailPage(query, pageRequest, builder);
    }

    @Override
    public Page<PrisonerDetail> findOffendersWithAliases(final String query, final PageRequest pageRequest) {
        final var initialSql = getQuery("FIND_OFFENDERS_WITH_ALIASES");
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

        final var paRowMapper = new PageAwareRowMapper<PrisonerDetail>(PRISONER_DETAIL_MAPPER);

        final var params = createParams("offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        final var prisonerDetails = jdbcTemplate.query(sql, params, paRowMapper);

        return new Page<>(prisonerDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    @Cacheable("bookingPhysicalMarks")
    public List<PhysicalMark> findPhysicalMarks(final long bookingId) {
        final var sql = getQuery("FIND_PHYSICAL_MARKS_BY_BOOKING");

        final var physicalMarkRowMapper =
				Row2BeanRowMapper.makeMapping(sql, PhysicalMark.class, physicalMarkMapping);

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
				physicalMarkRowMapper);
	}

	@Override
    @Cacheable("bookingPhysicalCharacteristics")
    public List<PhysicalCharacteristic> findPhysicalCharacteristics(final long bookingId) {
        final var sql = getQuery("FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING");

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
				PHYSICAL_CHARACTERISTIC_MAPPER);
	}

	@Override
    @Cacheable("bookingProfileInformation")
    public List<ProfileInformation> getProfileInformation(final long bookingId) {
        final var sql = getQuery("FIND_PROFILE_INFORMATION_BY_BOOKING");

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
                PROFILE_INFORMATION_MAPPER);
	}

    @Override
    public Optional<ImageDetail> getMainBookingImage(final long bookingId) {
        final var sql = getQuery("GET_IMAGE_DATA_FOR_BOOKING");
        ImageDetail imageDetail;
        try {
            imageDetail = jdbcTemplate.queryForObject(sql,
					createParams("bookingId", bookingId),
					IMAGE_DETAIL_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            imageDetail = null;
        }
        return Optional.ofNullable(imageDetail);
    }

	@Override
    @Cacheable("offenderIdentifiers")
    public List<OffenderIdentifier> getOffenderIdentifiers(final long bookingId) {
        final var sql = getQuery("GET_OFFENDER_IDENTIFIERS_BY_BOOKING");

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
				OFFENDER_IDENTIFIER_MAPPER);
	}

	@Override
    @Cacheable("bookingPhysicalAttributes")
    public Optional<PhysicalAttributes> findPhysicalAttributes(final long bookingId) {
        final var sql = getQuery("FIND_PHYSICAL_ATTRIBUTES_BY_BOOKING");

        final var physicalAttributesRowMapper =
				Row2BeanRowMapper.makeMapping(sql, PhysicalAttributes.class, physicalAttributesMapping);

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

    @Override
    @Cacheable("bookingAssessments")
    public List<AssessmentDto> findAssessments(final List<Long> bookingIds, final String assessmentCode, final Set<String> caseLoadId) {
        var initialSql = getQuery("FIND_ACTIVE_APPROVED_ASSESSMENT");
        if (!caseLoadId.isEmpty()) {
            initialSql += " AND " + getQuery("ASSESSMENT_CASELOAD_FILTER");
        }
        return doFindAssessments(bookingIds, assessmentCode, caseLoadId, initialSql, "bookingIds");
    }

    @Override
    @Cacheable("offenderAssessments")
    public List<AssessmentDto> findAssessmentsByOffenderNo(final List<String> offenderNos, final String assessmentCode, final Set<String> caseLoadId, final boolean latestOnly) {
        var initialSql = getQuery("FIND_APPROVED_ASSESSMENT_BY_OFFENDER_NO");
        if (!caseLoadId.isEmpty()) {
            initialSql += " AND " + getQuery("ASSESSMENT_CASELOAD_FILTER");
        }
        if (latestOnly) {
            initialSql += " AND OB.BOOKING_SEQ = 1";
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

	@Override
    public List<OffenderCategorise> getUncategorised(final String agencyId) {
        final var rawData = jdbcTemplate.query(
				getQuery("GET_UNCATEGORISED"),
				createParams("agencyId", agencyId),
				UNCATEGORISED_MAPPER);

        return applyCategorisationRestrictions(rawData);
	}

	@Override
    public List<OffenderCategorise> getApprovedCategorised(final String agencyId, final LocalDate cutoffDate) {
        final var rawData = jdbcTemplate.query(
				getQuery("GET_APPROVED_CATEGORISED"),
				createParams("agencyId", agencyId, "cutOffDate", DateTimeConverter.toDate(cutoffDate), "assessStatus", "A"),
				UNCATEGORISED_MAPPER);


		return removeEarlierCategorisations(rawData);
	}


    private List<OffenderCategorise> applyCategorisationRestrictions(final List<OffenderCategorise> catListRaw) {
		// for every group check that assessment is null OR it is the latest categorisation record
		final var catList = removeEarlierCategorisations(catListRaw);

		// remove the active assessment status offenders - we only want null assessment or pending assessments
		return catList.stream()
				.filter(o -> ((o.getAssessStatus() == null || !o.getAssessStatus().equals("A"))))
				.map(o -> OffenderCategorise.deriveStatus(o))
				.collect(Collectors.toList());
	}

    private List<OffenderCategorise> removeEarlierCategorisations(final List<OffenderCategorise> catList) {
		final var bookingIdMap = catList.stream().collect(Collectors.groupingBy(OffenderCategorise::getBookingId));
		bookingIdMap.replaceAll((k, v) -> cleanDuplicateRecordsUsingAssessmentSeq(v));

		return bookingIdMap.values().stream()
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

    private List<OffenderCategorise> cleanDuplicateRecordsUsingAssessmentSeq(final List<OffenderCategorise> individualCatList) {
        final var maxSeqOpt = individualCatList.stream().max(Comparator.comparing(OffenderCategorise::getAssessmentSeq));
        final var maxDateOpt = individualCatList.stream().max(Comparator.comparing(OffenderCategorise::getAssessmentDate));
		if (maxDateOpt.isEmpty() || maxSeqOpt.isEmpty()) return individualCatList;

		final var toReplace = individualCatList.stream()
				.filter(oc -> oc.getAssessmentSeq() == null || (oc.getAssessmentSeq().equals(maxSeqOpt.get().getAssessmentSeq()) && oc.getAssessmentDate().equals(maxDateOpt.get().getAssessmentDate())))
				.collect(Collectors.toList());
		return toReplace;
	}

	@Override
    public Optional<AssignedLivingUnit> findAssignedLivingUnit(final long bookingId, final String locationTypeRoot) {
        final var sql = getQuery("FIND_ASSIGNED_LIVING_UNIT");

        final var assignedLivingUnitRowMapper =
				Row2BeanRowMapper.makeMapping(sql, AssignedLivingUnit.class, assignedLivingUnitMapping);

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

	@Override
    @Cacheable("findInmate")
    public Optional<InmateDetail> findInmate(final Long bookingId) {
        final var builder = queryBuilderFactory.getQueryBuilder(getQuery("FIND_INMATE_DETAIL"), inmateDetailsMapping);
        final var sql = builder.build();

        final var inmateRowMapper = Row2BeanRowMapper.makeMapping(sql, InmateDetail.class, inmateDetailsMapping);
		InmateDetail inmate;
		try {
			inmate = jdbcTemplate.queryForObject(
					sql,
					createParams("bookingId", bookingId),
					inmateRowMapper);
			inmate.setAge(DateTimeConverter.getAge(inmate.getDateOfBirth()));
        } catch (final EmptyResultDataAccessException ex) {
			inmate = null;
		}

		return Optional.ofNullable(inmate);
	}

	@Override
    @Cacheable("basicInmateDetail")
    public Optional<InmateDetail> getBasicInmateDetail(final Long bookingId) {
        final var builder = queryBuilderFactory.getQueryBuilder(getQuery("FIND_BASIC_INMATE_DETAIL"), inmateDetailsMapping);
        final var sql = builder.build();

        final var inmateRowMapper = Row2BeanRowMapper.makeMapping(sql, InmateDetail.class, inmateDetailsMapping);
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

	@Override
    public Page<Alias> findInmateAliases(final Long bookingId, final String orderByFields, final Order order, final long offset, final long limit) {
        final var initialSql = getQuery("FIND_INMATE_ALIASES");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, aliasMapping);

        final var sql = builder
				.addRowCount()
				.addPagination()
				.addOrderBy(order, orderByFields)
				.build();

        final var aliasAttributesRowMapper = Row2BeanRowMapper.makeMapping(sql, Alias.class, aliasMapping);
        final var paRowMapper = new PageAwareRowMapper<Alias>(aliasAttributesRowMapper);

        final var results = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "offset", offset, "limit", limit),
                paRowMapper);
		results.forEach(alias -> alias.setAge(DateTimeConverter.getAge(alias.getDob())));
		return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
	}

	@Override
    public void insertCategory(final CategorisationDetail detail, final String agencyId, final Long assessStaffId, final String userId) {

        final var assessmentId = jdbcTemplate.queryForObject(getQuery("GET_CATEGORY_ASSESSMENT_ID"), Map.of(), Long.class);

		jdbcTemplate.update(
				getQuery("INSERT_CATEGORY"),
                createParams("bookingId", detail.getBookingId(),
                        "assessmentId", assessmentId,
                        "seq", getOffenderAssessmentSeq(detail.getBookingId()) + 1,
                        "assessmentDate", LocalDate.now(),
                        "assessStatus", "P",
                        "category", detail.getCategory(),
                        "assessStaffId", assessStaffId,
                        "assessComment", detail.getComment(),
                        "reviewDate", LocalDate.now().plusMonths(6),
                        "userId", userId,
                        "assessCommitteeCode", detail.getCommittee(),
                        "dateTime", LocalDateTime.now(),
                        "agencyId", agencyId));
	}

	@Override
    public void approveCategory(final CategoryApprovalDetail detail, final UserDetail currentUser) {
		final var assessmentId = jdbcTemplate.queryForObject(getQuery("GET_CATEGORY_ASSESSMENT_ID"), Map.of(), Long.class);
final var mapper = SingleColumnRowMapper.newInstance(Integer.class);
		final var sequences = jdbcTemplate.query(
				getQuery("GET_ACTIVE_OFFENDER_CATEGORY_SEQUENCES"),
				createParams("bookingId", detail.getBookingId(),
						"assessmentTypeId", assessmentId),
				mapper);
		if (CollectionUtils.isEmpty(sequences)) {
			throw new BadRequestException(String.format("No category assessment found, category %.10s, booking %d",
					detail.getCategory(),
					detail.getBookingId()));
		}

		final var maxSequence = sequences.get(0);
		final var result = jdbcTemplate.update(
				getQuery("APPROVE_CATEGORY"),
				createParams("bookingId", detail.getBookingId(),
						"seq", maxSequence,
						"assessmentTypeId", assessmentId,
						"assessStatus", "A",
						"category", detail.getCategory(),
						"evaluationDate", detail.getEvaluationDate(),
						"evaluationResultCode", "APP", // or 'REJ'
						"reviewCommitteeCode", detail.getReviewCommitteeCode(),
						"committeeCommentText", detail.getCommitteeCommentText(),
						"reviewPlacementAgencyId", detail.getReviewPlacementAgencyId(),
						"reviewPlacementText", detail.getReviewPlacementText(),
						"nextReviewDate", detail.getNextReviewDate(),
						"approvedCategoryComment", detail.getApprovedCategoryComment(),
						"userId", currentUser.getUsername(),
						"dateTime", LocalDateTime.now())
		);
		if (result != 1) {
			throw new BadRequestException(String.format("No pending category assessment found, category %.10s, booking %d, seq %d",
					detail.getCategory(),
					detail.getBookingId(),
					maxSequence));
		}
		if (sequences.size() > 1) {
			final var previousSequence = sequences.get(1);
			final var result2 = jdbcTemplate.update(
					getQuery("APPROVE_CATEGORY_SET_STATUS"),
					createParams("bookingId", detail.getBookingId(),
							"seq", previousSequence,
							"assessStatus", "I",
							"userId", currentUser.getUsername(),
							"dateTime", LocalDateTime.now())
			);
			if (result2 != 1) {
				throw new BadRequestException(String.format("Previous category assessment not found, booking %d, seq %d",
						detail.getBookingId(),
						previousSequence));
			}
		}
	}

	@Override
    public List<InmateBasicDetails> getBasicInmateDetailsForOffenders(final Set<String> offenders, final boolean accessToAllData, final Set<String> caseloads) {
        final var baseSql = getQuery("FIND_BASIC_INMATE_DETAIL_BY_OFFENDER_NO");
        final var sql = accessToAllData ? baseSql : String.format("%s AND %s", baseSql ,getQuery("CASELOAD_FILTER"));

        return jdbcTemplate.query(
                sql,
                createParams("offenders", offenders, "caseLoadId", caseloads, "bookingSeq", 1, "activeFlag",  "Y"),
                OFFENDER_BASIC_DETAILS_MAPPER);
    }

    private Integer getOffenderAssessmentSeq(final Long bookingId) {

		Integer maxSeq = null;

        try {
            maxSeq = jdbcTemplate.queryForObject(
					getQuery("OFFENDER_ASSESSMENTS_SEQ_MAX"),
                    createParams("bookingId", bookingId), Integer.class);
        } catch (final EmptyResultDataAccessException ex) {
            // no row - null response
        }

        return maxSeq == null ? 1 : maxSeq;
    }
}
