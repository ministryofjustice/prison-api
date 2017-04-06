package net.syscon.elite.persistence.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.model.PhysicalAttributes;
import net.syscon.elite.web.api.model.PhysicalCharacteristic;
import net.syscon.elite.web.api.model.PhysicalMark;
import net.syscon.util.DateFormatProvider;

@Repository
public class InmateRepositoryImpl extends RepositoryBase implements InmateRepository {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Map<String, FieldMapper> assignedInmateMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("OFFENDER_BOOK_ID", 	new FieldMapper("bookingId"))
		.put("BOOKING_NO", 			new FieldMapper("bookingNo"))
		.put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
		.put("AGY_LOC_ID", 			new FieldMapper("agencyId"))
		.put("FIRST_NAME", 			new FieldMapper("firstName"))
		.put("MIDDLE_NAME", 		new FieldMapper("middleName"))
		.put("LAST_NAME", 			new FieldMapper("lastName"))
		.put("ALERT_TYPES", 		new FieldMapper("alertsCodes", value -> Arrays.asList(value.toString().split(",")), null))
		.put("LIVING_UNIT_ID", 		new FieldMapper("assignedLivingUnitId"))
		.put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
	.build();

	private final Map<String, FieldMapper> inmateDetailsMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("OFFENDER_BOOK_ID", 	new FieldMapper("bookingId"))
		.put("BOOKING_NO", 			new FieldMapper("bookingNo"))
		.put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
		.put("AGY_LOC_ID", 			new FieldMapper("agencyId"))
		.put("FIRST_NAME", 			new FieldMapper("firstName"))
		.put("LAST_NAME", 			new FieldMapper("lastName"))
		.put("ALERT_TYPES", 		new FieldMapper("alertsCodes", value -> Arrays.asList(value.toString().split(",")), null))
		.put("LIVING_UNIT_ID", 		new FieldMapper("assignedLivingUnitId"))
		.put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
		.put("BIRTH_DATE", 			new FieldMapper("birthDate", value -> DateFormatProvider.get("yyyy-MM-dd").format((Date)value), null))
		.put("AGE",                 new FieldMapper("age"))
	.build();

	private final Map<String, FieldMapper> physicalAttributesMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("SEX_CODE",   new FieldMapper("gender"))
		.put("RACE_CODE",  new FieldMapper("ethnicity"))
		.put("HEIGHT_FT",  new FieldMapper("detail"))
		.put("HEIGHT_IN",  new FieldMapper("heightInches"))
		.put("HEIGHT_CM",  new FieldMapper("heightMeters", value -> ((Number) value).doubleValue() / 100.0, null))
		.put("WEIGHT_LBS", new FieldMapper("weightPounds"))
		.put("WEIGHT_KG",  new FieldMapper("weightKg"))
	.build();

	private final Map<String, FieldMapper> physicalCharacteristicsMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("CHARACTERISTIC", 	new FieldMapper("characteristic"))
		.put("DETAIL",          new FieldMapper("detail"))
		.put("IMAGE_ID", new FieldMapper("imageId"))
	.build();

	final Map<String, FieldMapper> physicalMarkMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("COMMENT_TEXT",	new FieldMapper("comment"))
	.build();




	@Override
	public List<AssignedInmate> findInmatesByLocation(final Long locationId, final int offset, final int limit) {
		final String sql = getPagedQuery("FIND_INMATES_BY_LOCATION");
		final RowMapper<AssignedInmate> assignedInmateRowMapper = Row2BeanRowMapper.makeMapping(sql, AssignedInmate.class, assignedInmateMapping);
		final List<AssignedInmate> inmates = jdbcTemplate.query(sql, createParams("locationId", locationId, "offset", offset, "limit", limit), assignedInmateRowMapper);
		return inmates;
	}

	@Override
	public List<AssignedInmate> findAllInmates(final int offset, final int limit) {
		final String sql = getPagedQuery("FIND_ALL_INMATES");
		final RowMapper<AssignedInmate> assignedInmateRowMapper = Row2BeanRowMapper.makeMapping(sql, AssignedInmate.class, assignedInmateMapping);
		final List<AssignedInmate> inmates = jdbcTemplate.query(sql, createParams("offset", offset, "limit", limit), assignedInmateRowMapper);
		return inmates;
	}

	private List<PhysicalMark> findPhysicalMarks(final Long bookingId) {
		final String sql = getQuery("FIND_PHYSICAL_MARKS_BY_BOOKING");
		final RowMapper<PhysicalMark> physicalMarkRowMapper = Row2BeanRowMapper.makeMapping(sql, PhysicalMark.class, physicalMarkMapping);
		final List<PhysicalMark> physicalMarks = jdbcTemplate.query(sql, createParams("bookingId", bookingId), physicalMarkRowMapper);
		return physicalMarks;
	}

	private List<PhysicalCharacteristic> findPhysicalCharacteristics(final Long bookingId) {
		final String sql = getQuery("FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING");
		final RowMapper<PhysicalCharacteristic> physicalCharacteristicsRowMapper = Row2BeanRowMapper.makeMapping(sql, PhysicalCharacteristic.class, physicalCharacteristicsMapping);
		final List<PhysicalCharacteristic> physicalCharacteristics = jdbcTemplate.query(sql, createParams("bookingId", bookingId), physicalCharacteristicsRowMapper);
		return physicalCharacteristics;
	}

	private PhysicalAttributes findPhysicalAttributes(final Long bookingId) {
		final String sql = getQuery("FIND_PHYSICAL_ATTRIBUTES_BY_BOOKING");
		final RowMapper<PhysicalAttributes> physicalAttributesRowMapper = Row2BeanRowMapper.makeMapping(sql, PhysicalAttributes.class, physicalAttributesMapping);
		final PhysicalAttributes physicalAttributes = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId), physicalAttributesRowMapper);
		return physicalAttributes;
	}


	@Override
	public InmateDetails findInmate(final Long bookingId) {
		final String sql = getQuery("FIND_INMATE_DETAIL");
		try {
			final RowMapper<InmateDetails> inmateDetailRowMapper = Row2BeanRowMapper.makeMapping(sql, InmateDetails.class, inmateDetailsMapping);
			final InmateDetails inmate = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId), inmateDetailRowMapper);
			if (inmate != null) {
				inmate.setPhysicalAttributes(findPhysicalAttributes(inmate.getBookingId()));
				inmate.setPhysicalCharacteristics(this.findPhysicalCharacteristics(inmate.getBookingId()));
				inmate.setPhysicalMarks(this.findPhysicalMarks(inmate.getBookingId()));
			}
			return inmate;
		} catch (final EmptyResultDataAccessException ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		}
	}

}


