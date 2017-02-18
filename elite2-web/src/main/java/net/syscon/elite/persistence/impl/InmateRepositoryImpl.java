package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.InmateRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.*;
import net.syscon.util.DateFormatProvider;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class InmateRepositoryImpl extends RepositoryBase implements InmateRepository {

	private final Map<String, FieldMapper> assignedInmateMapping = new ImmutableMap.Builder<String, FieldMapper>()
		.put("OFFENDER_BOOK_ID", 	new FieldMapper("bookingId"))
		.put("BOOKING_NO", 			new FieldMapper("bookingNo"))
		.put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
		.put("AGY_LOC_ID", 			new FieldMapper("agencyId"))
		.put("FIRST_NAME", 			new FieldMapper("firstName"))
		.put("MIDDLE_NAME", 		new FieldMapper("middleName"))
		.put("LAST_NAME", 			new FieldMapper("lastName"))
		.put("ALERT_TYPES", 		new FieldMapper("alertsCodes", (value) -> Arrays.asList(value.toString().split(",")), null))
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
		.put("ALERT_TYPES", 		new FieldMapper("alertsCodes", (value) -> Arrays.asList(value.toString().split(",")), null))
		.put("LIVING_UNIT_ID", 		new FieldMapper("assignedLivingUnitId"))
		.put("FACE_IMAGE_ID",       new FieldMapper("facialImageId"))
		.put("BIRTH_DATE", 			new FieldMapper("alertsCodes", (value) -> DateFormatProvider.get("yyyy-MM-dd").format((Date)value), null))
	.build();

	@Override
	public List<AssignedInmate> findInmatesByLocation(Long locationId, int offset, int limit) {
		final String sql = getPagedQuery("FIND_INMATES_BY_LOCATION");
		final RowMapper<AssignedInmate> assignedInmateRowMapper = Row2BeanRowMapper.makeMapping(sql, AssignedInmate.class, assignedInmateMapping);
		final List<AssignedInmate> inmates = jdbcTemplate.query(sql, createParams("locationId", locationId, "offset", offset, "limit", limit), assignedInmateRowMapper);
		return inmates;
	}

	@Override
	public List<AssignedInmate> findAllInmates(int offset, int limit) {
		final String sql = getPagedQuery("FIND_ALL_INMATES");
		final RowMapper<AssignedInmate> assignedInmateRowMapper = Row2BeanRowMapper.makeMapping(sql, AssignedInmate.class, assignedInmateMapping);
		final List<AssignedInmate> inmates = jdbcTemplate.query(sql, createParams("offset", offset, "limit", limit), assignedInmateRowMapper);
		return inmates;
	}

	@Override
	public InmateDetail findInmate(Long bookingId) {
		String sql = getQuery("FIND_INMATE_DETAIL");
		RowMapper<InmateDetail> inmateDetailRowMapper = Row2BeanRowMapper.makeMapping(sql, InmateDetail.class, inmateDetailsMapping);
		InmateDetail inmate = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId), inmateDetailRowMapper);
		if (inmate != null) {
			inmate.setPhysicalAttributes(findPhysicalAttributes(inmate.getBookingId()));
			inmate.setPhysicalCharacteristics(this.findPhysicalCharacteristics(inmate.getBookingId()));
			inmate.setPhysicalMarks(this.findPhysicalMarks(inmate.getBookingId()));
		}
		return inmate;
	}

	private List<PhysicalMark> findPhysicalMarks(Long bookingId) {
		final String sql = getQuery("FIND_PHYSICAL_MARKS_BY_BOOKING");
		final Map<String, FieldMapper> physicalMarkMapping = new ImmutableMap.Builder<String, FieldMapper>()
				.put("COMMENT_TEXT",	new FieldMapper("comment"))
				.build();
		final RowMapper<PhysicalMark> physicalMarkRowMapper = Row2BeanRowMapper.makeMapping(sql, PhysicalMark.class, physicalMarkMapping);
		final List<PhysicalMark> physicalMarks = jdbcTemplate.query(sql, createParams("bookingId", bookingId), physicalMarkRowMapper);
		return physicalMarks;
	}

	private List<PhysicalCharacteristic> findPhysicalCharacteristics(Long bookingId) {
		final String sql = getQuery("FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING");
		final Map<String, FieldMapper> physicalCharacteristicsMapping = new ImmutableMap.Builder<String, FieldMapper>()
				.put("CHARACTERISTIC", 	new FieldMapper("characteristic"))
				.put("DETAIL",          new FieldMapper("detail"))
				.put("IMAGE_ID", new FieldMapper("imageId"))
				.build();
		final RowMapper<PhysicalCharacteristic> physicalCharacteristicsRowMapper = Row2BeanRowMapper.makeMapping(sql, PhysicalCharacteristic.class, physicalCharacteristicsMapping);
		final List<PhysicalCharacteristic> physicalCharacteristics = jdbcTemplate.query(sql, createParams("bookingId", bookingId), physicalCharacteristicsRowMapper);
		return physicalCharacteristics;
	}

	private PhysicalAttributes findPhysicalAttributes(Long bookingId) {
		return null;
	}

}


