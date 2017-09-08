package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.v2.api.model.OffenderBooking;
import net.syscon.elite.v2.api.model.PrisonerDetail;
import net.syscon.elite.web.api.model.*;
import net.syscon.elite.web.api.resource.BookingResource;
import net.syscon.elite.web.api.resource.LocationsResource;
import net.syscon.util.DateFormatProvider;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
@Slf4j
public class InmateRepositoryImpl extends RepositoryBase implements InmateRepository {

	private final Map<String, FieldMapper> INMATE_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_BOOK_ID", 	new FieldMapper("bookingId"))
			.put("BOOKING_NO", 			new FieldMapper("bookingNo"))
			.put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
			.put("AGY_LOC_ID", 			new FieldMapper("agencyId"))
			.put("FIRST_NAME", 			new FieldMapper("firstName", null, null, StringUtils::upperCase))
			.put("MIDDLE_NAME", 		new FieldMapper("middleName", null, null, StringUtils::upperCase))
			.put("LAST_NAME", 			new FieldMapper("lastName", null, null, StringUtils::upperCase))
			.put("BIRTH_DATE", 			new FieldMapper("dateOfBirth", DateFormatProvider::toISO8601Date))
			.put("AGE",                 new FieldMapper("age"))
			.put("ALERT_TYPES", 		new FieldMapper("alertsCodes", value -> Arrays.asList(value.toString().split(","))))
			.put("ALIASES", 		    new FieldMapper("aliases", value -> Arrays.asList(value.toString().split(","))))
			.put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
			.put("LIVING_UNIT_ID",      new FieldMapper("assignedLivingUnitId"))
			.put("LIVING_UNIT_DESC",    new FieldMapper("assignedLivingUnitDesc"))
			.put("ASSIGNED_OFFICER_ID", new FieldMapper("assignedOfficerId"))
			.build();

    private static final Map<String, FieldMapper> OFFENDER_BOOKING_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
            .put("OFFENDER_BOOK_ID", 	new FieldMapper("bookingId"))
            .put("BOOKING_NO", 			new FieldMapper("bookingNo"))
            .put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
            .put("AGY_LOC_ID", 			new FieldMapper("agencyId"))
            .put("FIRST_NAME", 			new FieldMapper("firstName", null, null, StringUtils::upperCase))
            .put("MIDDLE_NAME", 		new FieldMapper("middleName", null, null, StringUtils::upperCase))
            .put("LAST_NAME", 			new FieldMapper("lastName", null, null, StringUtils::upperCase))
            .put("BIRTH_DATE", 			new FieldMapper("dateOfBirth"))
            .put("AGE",                 new FieldMapper("age"))
            .put("ALERT_TYPES", 		new FieldMapper("alertsCodes", value -> Arrays.asList(value.toString().split(","))))
            .put("ALIASES", 		    new FieldMapper("aliases", value -> Arrays.asList(value.toString().split(","))))
            .put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
            .put("LIVING_UNIT_ID",      new FieldMapper("assignedLivingUnitId"))
            .put("LIVING_UNIT_DESC",    new FieldMapper("assignedLivingUnitDesc"))
            .put("ASSIGNED_OFFICER_ID", new FieldMapper("assignedOfficerId"))
            .build();

    private static final Map<String, FieldMapper> OFFENDER_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
			.put("TITLE", 				new FieldMapper("title", null, null, StringUtils::upperCase))
			.put("SUFFIX", 				new FieldMapper("suffix", null, null, StringUtils::upperCase))
            .put("FIRST_NAME", 			new FieldMapper("firstName", null, null, StringUtils::upperCase))
            .put("MIDDLE_NAMES", 		new FieldMapper("middleNames", null, null, StringUtils::upperCase))
            .put("LAST_NAME", 			new FieldMapper("lastName", null, null, StringUtils::upperCase))
            .put("BIRTH_DATE", 			new FieldMapper("dateOfBirth"))
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
			.put("BOOKING_BEGIN_DATE", 	new FieldMapper("receptionDate"))
			.put("RELEASE_DATE",    	new FieldMapper("releaseDate"))
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
			.put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
			.put("BIRTH_DATE", 			new FieldMapper("dateOfBirth", DateFormatProvider::toISO8601Date))
			.put("AGE",                 new FieldMapper("age"))
			.put("ASSIGNED_OFFICER_ID", new FieldMapper("assignedOfficerId"))
			.build();

	private final Map<String, FieldMapper> physicalAttributesMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("GENDER",   new FieldMapper("gender"))
			.put("ETHNICITY",  new FieldMapper("ethnicity"))
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

	private final Map<String, FieldMapper> physicalMarkMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("COMMENT_TEXT",	new FieldMapper("comment"))
			.build();

	private final Map<String, FieldMapper> assessmentMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("ASSESSMENT_CODE",	new FieldMapper("assessmentCode"))
			.put("ASSESSMENT_DESCRIPTION",	new FieldMapper("assessmentDesc"))
			.put("CLASSIFICATION", 	new FieldMapper("classification"))
			.build();

	private final Map<String, FieldMapper> aliasMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("LAST_NAME",	new FieldMapper("lastName"))
			.put("FIRST_NAME",	new FieldMapper("firstName"))
			.put("MIDDLE_NAME",	new FieldMapper("middleName"))
			.put("BIRTH_DATE",	new FieldMapper("dob", DateFormatProvider::toISO8601Date))
			.put("AGE",			new FieldMapper("age"))
			.put("SEX",			new FieldMapper("gender"))
			.put("ETHNICITY",	new FieldMapper("ethinicity"))
			.put("ALIAS_TYPE",	new FieldMapper("nameType"))
			.build();

	@Override
	public List<InmatesSummary> findInmatesByLocation(final Long locationId, String query, String orderByField, LocationsResource.Order order, final int offset, final int limit) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_INMATES_BY_LOCATION"), INMATE_MAPPING)
				.addRowCount()
				.addQuery(query)
				.addOrderBy(order == LocationsResource.Order.asc, orderByField)
				.addPagination()
				.build();
		final RowMapper<InmatesSummary> assignedInmateRowMapper = Row2BeanRowMapper.makeMapping(sql, InmatesSummary.class, INMATE_MAPPING);
		try {
			return jdbcTemplate.query(sql, createParams("locationId", locationId, "caseLoadId", getCurrentCaseLoad(), "offset", offset, "limit", limit), assignedInmateRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public List<InmatesSummary> findAllInmates(Set<String> caseloads, String query, int offset, int limit, String orderBy, BookingResource.Order order) {
		String initialSql = getQuery("FIND_ALL_INMATES");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, INMATE_MAPPING);
		boolean isAscendingOrder = (order == BookingResource.Order.asc);

		String sql = builder
				.addRowCount()
				.addQuery(query)
				.addOrderBy(isAscendingOrder, orderBy)
				.addPagination()
				.build();

		RowMapper<InmatesSummary> assignedInmateRowMapper =
				Row2BeanRowMapper.makeMapping(sql, InmatesSummary.class, INMATE_MAPPING);

		List<InmatesSummary> inmates;
		try {
			inmates = jdbcTemplate.query(
					sql,
					createParams("caseLoadId", caseloads,
							"offset", offset,
							"limit", limit),
					assignedInmateRowMapper);
		} catch (EmptyResultDataAccessException e) {
			inmates = Collections.emptyList();
		}

		return inmates;
	}

	@Override
	public List<OffenderBooking> searchForOffenderBookings(Set<String> caseloads, String keywords, String locationPrefix, int offset, int limit, String orderBy, boolean ascendingOrder) {
		String initialSql = getQuery("FIND_ALL_INMATES");

        if (StringUtils.isNotBlank(locationPrefix)) {
            initialSql += " AND " + getQuery("LOCATION_FILTER_SQL");
        }

		final String keywordSearch = StringUtils.upperCase(StringUtils.trimToEmpty(keywords));
        if (StringUtils.isNotBlank(keywordSearch)) {
            initialSql += " AND " + getQuery("NAME_AND_ID_FILTER_SQL");
        }
		String sql = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING)
				.addRowCount()
				.addOrderBy(ascendingOrder, orderBy)
				.addPagination()
				.build();

		RowMapper<OffenderBooking> offenderBookingRowMapper = Row2BeanRowMapper.makeMapping(sql, OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

		List<OffenderBooking> offenderBookings;
		try {
			offenderBookings = jdbcTemplate.query(sql,
					createParams("keywords", keywordSearch + "%",
                            "locationPrefix", StringUtils.trimToEmpty(locationPrefix) + "%",
                            "caseLoadId", caseloads,
                            "offset", offset, "limit", limit),
					offenderBookingRowMapper);
		} catch (EmptyResultDataAccessException e) {
			offenderBookings = Collections.emptyList();
		}

		return offenderBookings;
	}

	@Override
	public List<InmatesSummary> findMyAssignments(long staffId, String currentCaseLoad, String orderBy, boolean ascendingSort, int offset, int limit) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_MY_ASSIGNMENTS"), INMATE_MAPPING).
				addRowCount().
				addOrderBy(ascendingSort, orderBy).
				addPagination()
				.build();

		final RowMapper<InmatesSummary> assignedInmateRowMapper = Row2BeanRowMapper.makeMapping(sql, InmatesSummary.class, INMATE_MAPPING);
		try {
			return jdbcTemplate.query(sql, createParams("staffId", staffId, "caseLoadId", currentCaseLoad, "offset", offset, "limit", limit), assignedInmateRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return Collections.emptyList();
		}
	}

    @Override
    public List<PrisonerDetail> searchForOffenders(String query, Date fromDobDate, Date toDobDate, String sortFields, boolean ascendingOrder, long offset, long limit) {
        String initialSql = getQuery("FIND_PRISONERS");

        final boolean hasDateRange = fromDobDate != null && toDobDate != null;
        if (hasDateRange) {
            initialSql += " WHERE O.BIRTH_DATE BETWEEN :fromDob AND :toDob ";
            log.debug("Running between Dates {} and {}", fromDobDate, toDobDate);

        }

        String sql = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_MAPPING)
                .addQuery(query)
                .addRowCount()
                .addPagination()
                .addOrderBy(ascendingOrder, sortFields)
                .build();

        RowMapper<PrisonerDetail> prisonerDetailRowMapper =
                Row2BeanRowMapper.makeMapping(sql, PrisonerDetail.class, OFFENDER_MAPPING);

        List<PrisonerDetail> prisonerDetails;
        try {
            prisonerDetails = jdbcTemplate.query(sql,
                    hasDateRange ? createParams("limit", limit, "offset", offset, "fromDob", fromDobDate, "toDob", toDobDate) : createParams("limit", limit, "offset", offset),
                    prisonerDetailRowMapper);
        } catch (EmptyResultDataAccessException e) {
            prisonerDetails = Collections.emptyList();
        }

        return prisonerDetails;
    }

    private List<PhysicalMark> findPhysicalMarks(final Long bookingId) {
		final String sql = getQuery("FIND_PHYSICAL_MARKS_BY_BOOKING");
		final RowMapper<PhysicalMark> physicalMarkRowMapper = Row2BeanRowMapper.makeMapping(sql, PhysicalMark.class, physicalMarkMapping);
		return jdbcTemplate.query(sql, createParams("bookingId", bookingId), physicalMarkRowMapper);
	}

	private List<PhysicalCharacteristic> findPhysicalCharacteristics(final Long bookingId) {
		final String sql = getQuery("FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING");
		final RowMapper<PhysicalCharacteristic> physicalCharacteristicsRowMapper = Row2BeanRowMapper.makeMapping(sql, PhysicalCharacteristic.class, physicalCharacteristicsMapping);
		return jdbcTemplate.query(sql, createParams("bookingId", bookingId), physicalCharacteristicsRowMapper);
	}

	private PhysicalAttributes findPhysicalAttributes(final Long bookingId) {
		final String sql = getQuery("FIND_PHYSICAL_ATTRIBUTES_BY_BOOKING");
		final RowMapper<PhysicalAttributes> physicalAttributesRowMapper = Row2BeanRowMapper.makeMapping(sql, PhysicalAttributes.class, physicalAttributesMapping);
		return jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId), physicalAttributesRowMapper);
	}

	private List<Assessment> findAssessments(final long bookingId) {
		final String sql = getQuery("FIND_ACTIVE_APPROVED_ASSESSMENT");
		final RowMapper<Assessment> assessmentAttributesRowMapper = Row2BeanRowMapper.makeMapping(sql, Assessment.class, assessmentMapping);
		return jdbcTemplate.query(sql, createParams("bookingId", bookingId), assessmentAttributesRowMapper);
	}

	private AssignedLivingUnit findAssignedLivingUnit(final long bookingId) {
		final String sql = getQuery("FIND_ASSIGNED_LIVING_UNIT");
		final RowMapper<AssignedLivingUnit> assignedLivingUnitRowMapper = Row2BeanRowMapper.makeMapping(sql, AssignedLivingUnit.class, assignedLivingUnitMapping);
		return jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId), assignedLivingUnitRowMapper);
	}

	@Override
	public Optional<InmateDetails> findInmate(final Long bookingId, Set<String> caseloads) {
		String sql = getQuery("FIND_INMATE_DETAIL");
		RowMapper<InmateDetails> inmateRowMapper = Row2BeanRowMapper.makeMapping(sql, InmateDetails.class, inmateDetailsMapping);

		InmateDetails inmate;

		try {
			inmate = jdbcTemplate.queryForObject(
					sql,
					createParams("bookingId", bookingId, "caseLoadId", caseloads),
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
