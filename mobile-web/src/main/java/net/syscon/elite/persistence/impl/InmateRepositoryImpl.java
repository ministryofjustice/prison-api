package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.*;
import net.syscon.elite.web.api.resource.BookingResource;
import net.syscon.elite.web.api.resource.LocationsResource;
import net.syscon.util.DateFormatProvider;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
public class InmateRepositoryImpl extends RepositoryBase implements InmateRepository {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Map<String, FieldMapper> assignedInmateMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_BOOK_ID", 	new FieldMapper("bookingId"))
			.put("BOOKING_NO", 			new FieldMapper("bookingNo"))
			.put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
			.put("AGY_LOC_ID", 			new FieldMapper("agencyId"))
			.put("FIRST_NAME", 			new FieldMapper("firstName", null, null, StringUtils::upperCase))
			.put("MIDDLE_NAME", 		new FieldMapper("middleName", null, null, StringUtils::upperCase))
			.put("LAST_NAME", 			new FieldMapper("lastName", null, null, StringUtils::upperCase))
			.put("ALERT_TYPES", 		new FieldMapper("alertsCodes", value -> Arrays.asList(value.toString().split(","))))
			.put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
			.put("LIVING_UNIT_ID",      new FieldMapper("assignedLivingUnitId"))
			.put("LIVING_UNIT_DESC",    new FieldMapper("assignedLivingUnitDesc"))
			.put("ASSIGNED_OFFICER_ID", new FieldMapper("assignedOfficerId"))
			.build();

	private final Map<String, FieldMapper> inmateDetailsMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_BOOK_ID", 	new FieldMapper("bookingId"))
			.put("BOOKING_NO", 			new FieldMapper("bookingNo"))
			.put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
			.put("FIRST_NAME", 			new FieldMapper("firstName"))
			.put("MIDDLE_NAME", 		new FieldMapper("middleName"))
			.put("LAST_NAME", 			new FieldMapper("lastName"))
			.put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
			.put("BIRTH_DATE", 			new FieldMapper("dateOfBirth", value -> DateFormatProvider.get("yyyy-MM-dd").format((Date)value)))
			.put("AGE",                 new FieldMapper("age"))
			.put("ASSIGNED_OFFICER_ID", new FieldMapper("assignedOfficerId"))
			.build();

	private final Map<String, FieldMapper> physicalAttributesMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("SEX_CODE",   new FieldMapper("gender"))
			.put("RACE_CODE",  new FieldMapper("ethnicity"))
			.put("HEIGHT_IN",  new FieldMapper("heightInches"))
			.put("HEIGHT_CM",  new FieldMapper("heightMeters", value -> ((Number) value).doubleValue() / 100.0))
			.put("WEIGHT_LBS", new FieldMapper("weightPounds"))
			.put("WEIGHT_KG",  new FieldMapper("weightKg"))
			.build();

	private final Map<String, FieldMapper> physicalCharacteristicsMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("CHARACTERISTIC", 	new FieldMapper("characteristic"))
			.put("DETAIL",          new FieldMapper("detail"))
			.put("IMAGE_ID", new FieldMapper("imageId"))
			.build();

	private final Map<String, FieldMapper> assignedLivingUnitMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("AGY_LOC_ID", 	new FieldMapper("agencyId"))
			.put("LIVING_UNIT_ID",          new FieldMapper("locationId"))
			.put("LIVING_UNIT_DESCRITION", new FieldMapper("description"))
			.build();

	final Map<String, FieldMapper> physicalMarkMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("COMMENT_TEXT",	new FieldMapper("comment"))
			.build();

	final Map<String, FieldMapper> assessmentMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("ASSESSMENT_CODE",	new FieldMapper("assessmentCode"))
			.put("ASSESSMENT_DESCRIPTION",	new FieldMapper("assessmentDesc"))
			.put("CLASSIFICATION", 	new FieldMapper("classification"))
			.build();

	final Map<String, FieldMapper> aliasMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("LAST_NAME",	new FieldMapper("lastName"))
			.put("FIRST_NAME",	new FieldMapper("firstName"))
			.put("MIDDLE_NAME",	new FieldMapper("middleName"))
			.put("BIRTH_DATE",	new FieldMapper("dob", value -> DateFormatProvider.get("yyyy-MM-dd").format((Date)value)))
			.put("AGE",			new FieldMapper("age"))
			.put("SEX",			new FieldMapper("gender"))
			.put("ETHNICITY",	new FieldMapper("ethinicity"))
			.put("ALIAS_TYPE",	new FieldMapper("nameType"))
			.build();

	@Override
	public List<AssignedInmate> findInmatesByLocation(final Long locationId, String query, String orderByField, LocationsResource.Order order, final int offset, final int limit) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_INMATES_BY_LOCATION"), assignedInmateMapping)
				.addRowCount()
				.addQuery(query)
				.addOrderBy(order == LocationsResource.Order.asc, orderByField)
				.addPagination()
				.build();
		final RowMapper<AssignedInmate> assignedInmateRowMapper = Row2BeanRowMapper.makeMapping(sql, AssignedInmate.class, assignedInmateMapping);
		try {
			return jdbcTemplate.query(sql, createParams("locationId", locationId, "caseLoadId", getCurrentCaseLoad(), "offset", offset, "limit", limit), assignedInmateRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public List<AssignedInmate> findAllInmates(String query, int offset, int limit, String orderBy, BookingResource.Order order) {
		String initialSql = getQuery("FIND_ALL_INMATES");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, assignedInmateMapping);
		boolean isAscendingOrder = (order == BookingResource.Order.asc);

		String sql = builder
				.addRowCount()
				.addQuery(query)
				.addOrderBy(isAscendingOrder, orderBy)
				.addPagination()
				.build();

		RowMapper<AssignedInmate> assignedInmateRowMapper =
				Row2BeanRowMapper.makeMapping(sql, AssignedInmate.class, assignedInmateMapping);

		List<AssignedInmate> inmates;
		try {
			inmates = jdbcTemplate.query(
					sql,
					createParams("caseLoadId", getCurrentCaseLoad(),
							"offset", offset,
							"limit", limit),
					assignedInmateRowMapper);
		} catch (EmptyResultDataAccessException e) {
			inmates = Collections.emptyList();
		}

		return inmates;
	}

	@Override
	public List<InmateAssignmentSummary> findMyAssignments(long staffId, String currentCaseLoad, String orderBy, boolean ascendingSort, int offset, int limit) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_MY_ASSIGNMENTS"), assignedInmateMapping).
				addRowCount().
				addOrderBy(ascendingSort, orderBy).
				addPagination()
				.build();

		final RowMapper<InmateAssignmentSummary> assignedInmateRowMapper = Row2BeanRowMapper.makeMapping(sql, InmateAssignmentSummary.class, assignedInmateMapping);
		try {
			return jdbcTemplate.query(sql, createParams("staffId", staffId, "caseLoadId", currentCaseLoad, "offset", offset, "limit", limit), assignedInmateRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return Collections.emptyList();
		}
	}

	private List<PhysicalMark> findPhysicalMarks(final Long bookingId) {
		final String sql = getQuery("FIND_PHYSICAL_MARKS_BY_BOOKING");
		final RowMapper<PhysicalMark> physicalMarkRowMapper = Row2BeanRowMapper.makeMapping(sql, PhysicalMark.class, physicalMarkMapping);
		final List<PhysicalMark> physicalMarks = jdbcTemplate.query(sql, createParams("bookingId", bookingId, "caseLoadId", getCurrentCaseLoad()), physicalMarkRowMapper);
		return physicalMarks;
	}

	private List<PhysicalCharacteristic> findPhysicalCharacteristics(final Long bookingId) {
		final String sql = getQuery("FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING");
		final RowMapper<PhysicalCharacteristic> physicalCharacteristicsRowMapper = Row2BeanRowMapper.makeMapping(sql, PhysicalCharacteristic.class, physicalCharacteristicsMapping);
		final List<PhysicalCharacteristic> physicalCharacteristics = jdbcTemplate.query(sql, createParams("bookingId", bookingId, "caseLoadId", getCurrentCaseLoad()), physicalCharacteristicsRowMapper);
		return physicalCharacteristics;
	}

	private PhysicalAttributes findPhysicalAttributes(final Long bookingId) {
		final String sql = getQuery("FIND_PHYSICAL_ATTRIBUTES_BY_BOOKING");
		final RowMapper<PhysicalAttributes> physicalAttributesRowMapper = Row2BeanRowMapper.makeMapping(sql, PhysicalAttributes.class, physicalAttributesMapping);
		final PhysicalAttributes physicalAttributes = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "caseLoadId", getCurrentCaseLoad()), physicalAttributesRowMapper);
		return physicalAttributes;
	}

	private List<Assessment> findAssessments(final long bookingId) {
		final String sql = getQuery("FIND_ACTIVE_APPROVED_ASSESSMENT");
		final RowMapper<Assessment> assessmentAttributesRowMapper = Row2BeanRowMapper.makeMapping(sql, Assessment.class, assessmentMapping);
		final List<Assessment> assessments = jdbcTemplate.query(sql, createParams("bookingId", bookingId, "caseLoadId", getCurrentCaseLoad()), assessmentAttributesRowMapper);
		return assessments;
	}

	private AssignedLivingUnit findAssignedLivingUnit(final long bookingId) {
		final String sql = getQuery("FIND_ASSIGNED_LIVING_UNIT");
		final RowMapper<AssignedLivingUnit> assignedLivingUnitRowMapper = Row2BeanRowMapper.makeMapping(sql, AssignedLivingUnit.class, assignedLivingUnitMapping);
		final AssignedLivingUnit assignedLivingUnit = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId), assignedLivingUnitRowMapper);
		return assignedLivingUnit;
	}

	@Override
	public Optional<InmateDetails> findInmate(final Long bookingId) {
		String sql = getQuery("FIND_INMATE_DETAIL");
		RowMapper<InmateDetails> inmateRowMapper = Row2BeanRowMapper.makeMapping(sql, InmateDetails.class, inmateDetailsMapping);

		InmateDetails inmate;

		try {
			inmate = jdbcTemplate.queryForObject(
					sql,
					createParams("bookingId", bookingId, "caseLoadId", getCurrentCaseLoad()),
					inmateRowMapper);

			if (inmate != null) {
				inmate.setPhysicalAttributes(findPhysicalAttributes(inmate.getBookingId()));
				inmate.setPhysicalCharacteristics(findPhysicalCharacteristics(inmate.getBookingId()));
				inmate.setPhysicalMarks(findPhysicalMarks(inmate.getBookingId()));
				inmate.setAssessments(findAssessments(inmate.getBookingId()));
				inmate.setAssignedLivingUnit(findAssignedLivingUnit(bookingId));
				inmate.setAlertsCodes(findActiveAlertCodes(bookingId));
			}
		} catch (EmptyResultDataAccessException ex) {
			inmate = null;
		}

		return Optional.ofNullable(inmate);
	}

	private List<String> findActiveAlertCodes(Long bookingId) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_ALERT_TYPES_FOR_OFFENDER"), null).build();
		return jdbcTemplate.query(sql, createParams("bookingId", bookingId), (rs, rowNum) -> rs.getString("ALERT_TYPE"));
	}

	@Override
	public List<Alias> findInmateAliases(Long bookingId, String orderByField, BookingResource.Order order) {
		String initialSql = getQuery("FIND_INMATE_ALIASES");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, aliasMapping);
		boolean isAscendingOrder = (order == BookingResource.Order.asc);

		String sql = builder
				.addOrderBy(isAscendingOrder,
						    StringUtils.defaultString(StringUtils.trimToNull(orderByField), "firstName"))
				.build();

		RowMapper<Alias> aliasAttributesRowMapper = Row2BeanRowMapper.makeMapping(sql, Alias.class, aliasMapping);

		return jdbcTemplate.query(
				sql,
				createParams("bookingId", bookingId),
				aliasAttributesRowMapper);
	}
}
