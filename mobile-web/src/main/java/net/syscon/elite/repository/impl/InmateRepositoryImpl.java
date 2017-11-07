package net.syscon.elite.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.PageRequest;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

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
			.put("IEP_LEVEL", new FieldMapper("iepLevel"))
            .build();

    private static final Map<String, FieldMapper> OFFENDER_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
			.put("TITLE", 				new FieldMapper("title", null, null, StringUtils::upperCase))
			.put("SUFFIX", 				new FieldMapper("suffix", null, null, StringUtils::upperCase))
            .put("FIRST_NAME", 			new FieldMapper("firstName", null, null, StringUtils::upperCase))
            .put("MIDDLE_NAMES", 		new FieldMapper("middleNames", null, null, StringUtils::upperCase))
            .put("LAST_NAME", 			new FieldMapper("lastName", null, null, StringUtils::upperCase))
            .put("BIRTH_DATE", 			new FieldMapper("dateOfBirth", DateTimeConverter::toISO8601LocalDate))
            .put("ETHNICITY", 			new FieldMapper("ethnicity"))
            .put("SEX", 			    new FieldMapper("gender"))
            .put("BIRTH_COUNTRY", 		new FieldMapper("birthCountry"))
			.put("CONVICTED_STATUS", 	new FieldMapper("convictedStatus"))
			.put("NATIONALITIES", 		new FieldMapper("nationalities"))
			.put("RELIGION", 	        new FieldMapper("religion"))
			.put("MARITAL_STATUS", 	    new FieldMapper("maritalStatus"))
			.put("IMPRISONMENT_STATUS", new FieldMapper("imprisonmentStatus"))
			.put("PNC_NUMBER", 			new FieldMapper("pncNumber"))
			.put("CRO_NUMBER", 			new FieldMapper("croNumber"))
			.put("ACTIVE_FLAG", 		new FieldMapper("currentlyInPrison"))
			.put("BOOKING_BEGIN_DATE", 	new FieldMapper("receptionDate", DateTimeConverter::toISO8601LocalDate))
			.put("RELEASE_DATE",    	new FieldMapper("releaseDate", DateTimeConverter::toISO8601LocalDate))
			.put("AGY_LOC_ID", 			new FieldMapper("latestLocationId"))
			.put("AGY_LOC_DESC", 		new FieldMapper("latestLocation"))
            .build();

    private final Map<String, FieldMapper> inmateDetailsMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_BOOK_ID", 	new FieldMapper("bookingId"))
			.put("BOOKING_NO", 			new FieldMapper("bookingNo"))
			.put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
			.put("FIRST_NAME", 			new FieldMapper("firstName"))
			.put("MIDDLE_NAME", 		new FieldMapper("middleName"))
			.put("LAST_NAME", 			new FieldMapper("lastName"))
            .put("RELIGION", 			new FieldMapper("religion"))
			.put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
			.put("BIRTH_DATE", 			new FieldMapper("dateOfBirth", DateTimeConverter::toISO8601LocalDate))
			.put("ASSIGNED_OFFICER_ID", new FieldMapper("assignedOfficerId"))
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
	private final StandardBeanPropertyRowMapper<ProfileInformation> PROFILE_INFORMATION_MAPPER = new StandardBeanPropertyRowMapper<>(ProfileInformation.class);

	private final Map<String, FieldMapper> aliasMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("LAST_NAME",		new FieldMapper("lastName"))
			.put("FIRST_NAME",		new FieldMapper("firstName"))
			.put("MIDDLE_NAME",		new FieldMapper("middleName"))
			.put("BIRTH_DATE",		new FieldMapper("dob", DateTimeConverter::toISO8601LocalDate))
			.put("SEX",				new FieldMapper("gender"))
			.put("ETHNICITY",		new FieldMapper("ethinicity"))
			.put("ALIAS_TYPE",		new FieldMapper("nameType"))
			.put("CREATE_DATE", new FieldMapper("createDate", DateTimeConverter::toISO8601LocalDate))
			.build();

	@Override
	public Page<OffenderBooking> findInmatesByLocation(Long locationId, String locationTypeRoot, String query, String orderByField, Order order, long offset, long limit) {
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
                        "caseLoadId", getCurrentCaseLoad(),
                        "offset", offset,
                        "limit", limit),
                paRowMapper);

		results.forEach(b -> b.setAge(DateTimeConverter.getAge(b.getDateOfBirth())));
		return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
	}

	@Override
	public Page<OffenderBooking> findAllInmates(Set<String> caseloads, String locationTypeRoot, String query, PageRequest pageRequest) {
		String initialSql = getQuery("FIND_ALL_INMATES");
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
	public Page<OffenderBooking> searchForOffenderBookings(Set<String> caseloads, String offenderNo, String lastName, String firstName, String locationPrefix, String locationTypeRoot, PageRequest pageRequest) {
		String initialSql = getQuery("FIND_ALL_INMATES");

		if (StringUtils.isNotBlank(locationPrefix)) {
			initialSql += " AND " + getQuery("LOCATION_FILTER_SQL");
		}

		if (StringUtils.isNotBlank(offenderNo)) {
			initialSql += " AND O.OFFENDER_ID_DISPLAY = :offenderNo ";
		}

		if (StringUtils.isNotBlank(lastName)) {
			initialSql += " AND O.LAST_NAME like :lastName ";
		}

		if (StringUtils.isNotBlank(firstName)) {
			initialSql += " AND O.FIRST_NAME like :firstName ";
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
                        "lastName", StringUtils.trimToEmpty(lastName) + "%",
                        "firstName", StringUtils.trimToEmpty(firstName) + "%",
                        "locationPrefix", StringUtils.trimToEmpty(locationPrefix) + "%",
                        "caseLoadId", caseloads,
                        "locationTypeRoot", locationTypeRoot,
                        "offset", pageRequest.getOffset(), "limit", pageRequest.getLimit()),
                paRowMapper);
		offenderBookings.forEach(b -> b.setAge(DateTimeConverter.getAge(b.getDateOfBirth())));
		return new Page<>(offenderBookings, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
	}

	@Override
	public Page<OffenderBooking> findMyAssignments(long staffId, String currentCaseLoad, String locationTypeRoot, String orderBy, boolean sortAscending, long offset, long limit) {
		String initialSql = getQuery("FIND_MY_ASSIGNMENTS");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

		String sql = builder
				.addRowCount()
				.addOrderBy(sortAscending, orderBy)
				.addPagination()
				.build();

		RowMapper<OffenderBooking> assignedInmateRowMapper =
				Row2BeanRowMapper.makeMapping(sql, OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

		PageAwareRowMapper<OffenderBooking> paRowMapper = new PageAwareRowMapper<>(assignedInmateRowMapper);

		List<OffenderBooking> results = jdbcTemplate.query(
                sql,
                createParams("staffId", staffId,
                        "caseLoadId", currentCaseLoad,
                        "locationTypeRoot", locationTypeRoot,
                        "currentDate", DateTimeConverter.toDate(LocalDate.now()),
                        "offset", offset,
                        "limit", limit),
                paRowMapper);
		results.forEach(b -> b.setAge(DateTimeConverter.getAge(b.getDateOfBirth())));
		return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
	}

    @Override
    public Page<PrisonerDetail> searchForOffenders(String query, LocalDate fromDobDate, LocalDate toDobDate, String sortFields, boolean sortAscending, long offset, long limit) {
        String initialSql = getQuery("FIND_PRISONERS");

        boolean hasDateRange = fromDobDate != null && toDobDate != null;

        if (hasDateRange) {
            initialSql += " WHERE O.BIRTH_DATE BETWEEN :fromDob AND :toDob ";

            log.debug("Running between {} and {}", fromDobDate, toDobDate);
        }

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_MAPPING);

        String sql = builder
                .addQuery(query)
                .addRowCount()
                .addPagination()
                .addOrderBy(sortAscending, sortFields)
                .build();

        RowMapper<PrisonerDetail> prisonerDetailRowMapper =
                Row2BeanRowMapper.makeMapping(sql, PrisonerDetail.class, OFFENDER_MAPPING);

		PageAwareRowMapper<PrisonerDetail> paRowMapper = new PageAwareRowMapper<>(prisonerDetailRowMapper);

        List<PrisonerDetail> prisonerDetails = jdbcTemplate.query(
                sql,
                hasDateRange ? createParams("limit", limit,
                        "offset", offset,
                        "fromDob", DateTimeConverter.toDate(fromDobDate),
                        "toDob", DateTimeConverter.toDate(toDobDate))
                        : createParams("limit", limit, "offset", offset),
                paRowMapper);

        return new Page<>(prisonerDetails, paRowMapper.getTotalRecords(), offset, limit);
    }

	public List<PhysicalMark> findPhysicalMarks(long bookingId) {
		String sql = getQuery("FIND_PHYSICAL_MARKS_BY_BOOKING");

		RowMapper<PhysicalMark> physicalMarkRowMapper =
				Row2BeanRowMapper.makeMapping(sql, PhysicalMark.class, physicalMarkMapping);

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
				physicalMarkRowMapper);
	}

	public List<PhysicalCharacteristic> findPhysicalCharacteristics(long bookingId) {
		String sql = getQuery("FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING");

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
				PHYSICAL_CHARACTERISTIC_MAPPER);
	}

	public List<ProfileInformation> getProfileInformation(long bookingId) {
		String sql = getQuery("FIND_PROFILE_INFORMATION_BY_BOOKING");

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
                PROFILE_INFORMATION_MAPPER);
	}

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

	public List<AssessmentDto> findAssessments(long bookingId) {
		String sql = getQuery("FIND_ACTIVE_APPROVED_ASSESSMENT");

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
				ASSESSMENT_MAPPER);
	}

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
	public Optional<InmateDetail> findInmate(Long bookingId, Set<String> caseloads, String locationTypeRoot) {
		String sql = getQuery("FIND_INMATE_DETAIL");

		RowMapper<InmateDetail> inmateRowMapper =Row2BeanRowMapper.makeMapping(sql, InmateDetail.class, inmateDetailsMapping);
		InmateDetail inmate;
		try {
			inmate = jdbcTemplate.queryForObject(
					sql,
					createParams("bookingId", bookingId, "caseLoadId", caseloads, "currentDate", DateTimeConverter.toDate(LocalDate.now())),
					inmateRowMapper);
			inmate.setAge(DateTimeConverter.getAge(inmate.getDateOfBirth()));
		} catch (EmptyResultDataAccessException ex) {
			inmate = null;
		}

		return Optional.ofNullable(inmate);
	}

	public List<String> findActiveAlertCodes(long bookingId) {
		String sql = getQuery("FIND_ALERT_TYPES_FOR_OFFENDER");

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
				(rs, rowNum) -> rs.getString("ALERT_TYPE"));
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
