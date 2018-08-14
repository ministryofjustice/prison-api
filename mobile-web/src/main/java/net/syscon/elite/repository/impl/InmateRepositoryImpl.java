package net.syscon.elite.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.*;

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
            .put("LIVING_UNIT_DESC",    new FieldMapper("assignedLivingUnitDesc", value -> StringUtils.replaceFirst((String)value, "^[A-Z|a-z|0-9]+\\-", "")))
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
            .put("RELIGION", 			new FieldMapper("religion"))
			.put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
			.put("BIRTH_DATE", 			new FieldMapper("dateOfBirth", DateTimeConverter::toISO8601LocalDate))
			.put("ASSIGNED_OFFICER_ID", new FieldMapper("assignedOfficerId"))
			.put("ACTIVE_FLAG",         new FieldMapper("activeFlag", value -> "Y".equalsIgnoreCase(value.toString())))
			.build();

	private final Map<String, FieldMapper> physicalAttributesMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("GENDER",   new FieldMapper("gender"))
			.put("ETHNICITY",  new FieldMapper("ethnicity"))
			.put("HEIGHT_FT",  new FieldMapper("heightFeet"))
			.put("HEIGHT_IN",  new FieldMapper("heightInches"))
			.put("HEIGHT_CM",  new FieldMapper("heightCentimetres"))
			.put("WEIGHT_LBS", new FieldMapper("weightPounds"))
			.put("WEIGHT_KG",  new FieldMapper("weightKilograms"))
			.build();


	private final Map<String, FieldMapper> assignedLivingUnitMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("AGY_LOC_ID", 	new FieldMapper("agencyId"))
			.put("LIVING_UNIT_ID",          new FieldMapper("locationId"))
			.put("LIVING_UNIT_DESCRIPTION", new FieldMapper("description", value -> StringUtils.replaceFirst((String)value, "^[A-Z|a-z|0-9]+\\-", "")))
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


    private final StandardBeanPropertyRowMapper<PrisonerDetail> PRISONER_DETAIL_MAPPER =
            new StandardBeanPropertyRowMapper<>(PrisonerDetail.class);

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

	@Override
	public Page<OffenderBooking> findInmatesByLocation(Long locationId, String locationTypeRoot, String caseLoadId, String query, String orderByField, Order order, long offset, long limit) {
		String initialSql = getQuery("FIND_INMATES_BY_LOCATION");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

		String sql = builder
				.addRowCount()
				.addQuery(query)
				.addOrderBy(order, orderByField)
				.addPagination()
				.build();

		RowMapper<OffenderBooking> assignedInmateRowMapper =
				Row2BeanRowMapper.makeMapping(sql, OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

		PageAwareRowMapper<OffenderBooking> paRowMapper = new PageAwareRowMapper<>(assignedInmateRowMapper);

		List<OffenderBooking> results = jdbcTemplate.query(
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
    public List<InmateDto> findInmatesByLocation(String agencyId, List<Long> locations, Set<String> caseLoadIds) {
        List<InmateDto> results = jdbcTemplate.query(getQuery("FIND_INMATES_OF_LOCATION_LIST"),
                createParams("agencyId", agencyId, "locations", locations, "caseLoadIds", caseLoadIds), INMATE_MAPPER);
        return results;
    }

	@Override
	public Page<OffenderBooking> findAllInmates(Set<String> caseloads, String locationTypeRoot, String query, PageRequest pageRequest) {
		String initialSql = getQuery("FIND_ALL_INMATES");
		if (!caseloads.isEmpty()) {
			initialSql += " AND " + getQuery("CASELOAD_FILTER");
		}
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

		String sql = builder
				.addRowCount()
				.addQuery(query)
				.addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
				.addPagination()
				.build();

		RowMapper<OffenderBooking> assignedInmateRowMapper =
				Row2BeanRowMapper.makeMapping(sql, OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

		PageAwareRowMapper<OffenderBooking> paRowMapper = new PageAwareRowMapper<>(assignedInmateRowMapper);

		List<OffenderBooking> inmates = jdbcTemplate.query(
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
	public Page<OffenderBooking> searchForOffenderBookings(Set<String> caseloads, String offenderNo, String searchTerm1, String searchTerm2, String locationPrefix, String locationTypeRoot, PageRequest pageRequest) {
		String initialSql = getQuery("FIND_ALL_INMATES");
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

		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

		String sql = builder
				.addRowCount()
				.addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
				.addPagination()
				.build();

		RowMapper<OffenderBooking> offenderBookingRowMapper =
				Row2BeanRowMapper.makeMapping(sql, OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

		PageAwareRowMapper<OffenderBooking> paRowMapper = new PageAwareRowMapper<>(offenderBookingRowMapper);

		List<OffenderBooking> offenderBookings = jdbcTemplate.query(
		        sql,
                createParams("offenderNo", offenderNo,
                        "searchTerm1", StringUtils.trimToEmpty(searchTerm1) + "%",
                        "searchTerm2", StringUtils.trimToEmpty(searchTerm2) + "%",
                        "locationPrefix", StringUtils.trimToEmpty(locationPrefix) + "-%",
                        "caseLoadId", caseloads,
                        "locationTypeRoot", locationTypeRoot,
                        "offset", pageRequest.getOffset(), "limit", pageRequest.getLimit()),
                paRowMapper);
		offenderBookings.forEach(b -> b.setAge(DateTimeConverter.getAge(b.getDateOfBirth())));
		return new Page<>(offenderBookings, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
	}

	@Override
	public List<Long> getPersonalOfficerBookings(long staffId) {
		return jdbcTemplate.queryForList(
				getQuery("FIND_PERSONAL_OFFICER_BOOKINGS"),
				createParams("staffId", staffId),
				Long.class);
	}

    @Override
    public Page<PrisonerDetail> findOffenders(String query, PageRequest pageRequest) {
        String initialSql = getQuery("FIND_OFFENDERS");

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, PRISONER_DETAIL_MAPPER.getFieldMap());

        String sql = builder
                .addQuery(query)
                .addRowCount()
                .addPagination()
                .addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
                .build();

		PageAwareRowMapper<PrisonerDetail> paRowMapper = new PageAwareRowMapper<>(PRISONER_DETAIL_MAPPER);

		MapSqlParameterSource params =
				createParams( "offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        List<PrisonerDetail> prisonerDetails = jdbcTemplate.query( sql, params, paRowMapper);

        return new Page<>(prisonerDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

	@Override
    @Cacheable("bookingPhysicalMarks")
    public List<PhysicalMark> findPhysicalMarks(long bookingId) {
		String sql = getQuery("FIND_PHYSICAL_MARKS_BY_BOOKING");

		RowMapper<PhysicalMark> physicalMarkRowMapper =
				Row2BeanRowMapper.makeMapping(sql, PhysicalMark.class, physicalMarkMapping);

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
				physicalMarkRowMapper);
	}

	@Override
    @Cacheable("bookingPhysicalCharacteristics")
    public List<PhysicalCharacteristic> findPhysicalCharacteristics(long bookingId) {
		String sql = getQuery("FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING");

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
				PHYSICAL_CHARACTERISTIC_MAPPER);
	}

	@Override
    @Cacheable("bookingProfileInformation")
    public List<ProfileInformation> getProfileInformation(long bookingId) {
		String sql = getQuery("FIND_PROFILE_INFORMATION_BY_BOOKING");

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
                PROFILE_INFORMATION_MAPPER);
	}

    @Override
    public Optional<ImageDetail> getMainBookingImage(long bookingId) {
        final String sql = getQuery("GET_IMAGE_DATA_FOR_BOOKING");
        ImageDetail imageDetail;
        try {
            imageDetail = jdbcTemplate.queryForObject(sql,
					createParams("bookingId", bookingId),
					IMAGE_DETAIL_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            imageDetail = null;
        }
        return Optional.ofNullable(imageDetail);
    }

	@Override
    @Cacheable("offenderIdentifiers")
	public List<OffenderIdentifier> getOffenderIdentifiers(long bookingId) {
		String sql = getQuery("GET_OFFENDER_IDENTIFIERS_BY_BOOKING");

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
				OFFENDER_IDENTIFIER_MAPPER);
	}

	@Override
    @Cacheable("bookingPhysicalAttributes")
    public Optional<PhysicalAttributes> findPhysicalAttributes(long bookingId) {
		String sql = getQuery("FIND_PHYSICAL_ATTRIBUTES_BY_BOOKING");

		RowMapper<PhysicalAttributes> physicalAttributesRowMapper =
				Row2BeanRowMapper.makeMapping(sql, PhysicalAttributes.class, physicalAttributesMapping);

		PhysicalAttributes physicalAttributes;
		try {
			physicalAttributes = jdbcTemplate.queryForObject(
					sql,
					createParams("bookingId", bookingId),
					physicalAttributesRowMapper);
		} catch (EmptyResultDataAccessException e) {
			physicalAttributes = null;
		}
		return Optional.ofNullable(physicalAttributes);
	}

    @Override
    @Cacheable("bookingAssessments")
    public List<AssessmentDto> findAssessments(List<Long> bookingIds, String assessmentCode, Set<String> caseLoadId) {
        return doFindAssessments(bookingIds, assessmentCode, caseLoadId, "FIND_ACTIVE_APPROVED_ASSESSMENT", "bookingIds");
    }

    @Override
    @Cacheable("offenderAssessments")
    public List<AssessmentDto> findAssessmentsByOffenderNo(List<String> offenderNos, String assessmentCode, Set<String> caseLoadId) {
        return doFindAssessments(offenderNos, assessmentCode, caseLoadId, "FIND_ACTIVE_APPROVED_ASSESSMENT_BY_OFFENDER_NO", "offenderNos");
    }

    private List<AssessmentDto> doFindAssessments(List<?> ids, String assessmentCode,
            Set<String> caseLoadId, final String queryName, final String idParam) {
        String initialSql = getQuery(queryName);
        if (!caseLoadId.isEmpty()) {
            initialSql += " AND " + getQuery("ASSESSMENT_CASELOAD_FILTER");
        }
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, ASSESSMENT_MAPPER.getFieldMap());

		String sql = builder
				.addOrderBy(Order.DESC, "cellSharingAlertFlag")
				.addOrderBy(Order.DESC, "assessmentDate")
				.addOrderBy(Order.ASC, "assessmentCode")
				.build();

        final MapSqlParameterSource params = createParams(
                idParam, ids,
                "assessmentCode", assessmentCode,
                "caseLoadId", caseLoadId);

        return jdbcTemplate.query(sql, params, ASSESSMENT_MAPPER);
    }

	@Override
    public Optional<AssignedLivingUnit> findAssignedLivingUnit(long bookingId, String locationTypeRoot) {
		String sql = getQuery("FIND_ASSIGNED_LIVING_UNIT");

		RowMapper<AssignedLivingUnit> assignedLivingUnitRowMapper =
				Row2BeanRowMapper.makeMapping(sql, AssignedLivingUnit.class, assignedLivingUnitMapping);

		AssignedLivingUnit assignedLivingUnit;
		try {
			assignedLivingUnit = jdbcTemplate.queryForObject(
					sql,
					createParams("bookingId", bookingId, "locationTypeRoot", locationTypeRoot),
					assignedLivingUnitRowMapper);
		} catch (EmptyResultDataAccessException ex) {
			assignedLivingUnit = null;
		}

		return Optional.ofNullable(assignedLivingUnit);
	}

	@Override
    @Cacheable("findInmate")
	public Optional<InmateDetail> findInmate(Long bookingId) {
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(getQuery("FIND_INMATE_DETAIL"), inmateDetailsMapping);
		String sql = builder.build();

		RowMapper<InmateDetail> inmateRowMapper =Row2BeanRowMapper.makeMapping(sql, InmateDetail.class, inmateDetailsMapping);
		InmateDetail inmate;
		try {
			inmate = jdbcTemplate.queryForObject(
					sql,
					createParams("bookingId", bookingId),
					inmateRowMapper);
			inmate.setAge(DateTimeConverter.getAge(inmate.getDateOfBirth()));
		} catch (EmptyResultDataAccessException ex) {
			inmate = null;
		}

		return Optional.ofNullable(inmate);
	}

	@Override
    @Cacheable("basicInmateDetail")
	public Optional<InmateDetail> getBasicInmateDetail(Long bookingId) {
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(getQuery("FIND_BASIC_INMATE_DETAIL"), inmateDetailsMapping);
		String sql = builder.build();

		RowMapper<InmateDetail> inmateRowMapper = Row2BeanRowMapper.makeMapping(sql, InmateDetail.class, inmateDetailsMapping);
		InmateDetail inmate;
		try {
			inmate = jdbcTemplate.queryForObject(
					sql,
					createParams("bookingId", bookingId),
					inmateRowMapper);
		} catch (EmptyResultDataAccessException ex) {
			inmate = null;
		}

		return Optional.ofNullable(inmate);
	}

	@Override
	public Page<Alias> findInmateAliases(Long bookingId, String orderByFields, Order order, long offset, long limit) {
		String initialSql = getQuery("FIND_INMATE_ALIASES");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, aliasMapping);

		String sql = builder
				.addRowCount()
				.addPagination()
				.addOrderBy(order, orderByFields)
				.build();

		RowMapper<Alias> aliasAttributesRowMapper = Row2BeanRowMapper.makeMapping(sql, Alias.class, aliasMapping);
		PageAwareRowMapper<Alias> paRowMapper = new PageAwareRowMapper<>(aliasAttributesRowMapper);

		List<Alias> results = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "offset", offset, "limit", limit),
                paRowMapper);
		results.forEach(alias -> alias.setAge(DateTimeConverter.getAge(alias.getDob())));
		return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
	}
}
