package net.syscon.elite.persistence.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.repository.InmateRepository;
import net.syscon.elite.persistence.repository.mapping.FieldMapper;
import net.syscon.elite.persistence.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateDetail;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
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
		.put("ALERT_TYPES", 		new FieldMapper("alertsCodes", (value) -> Arrays.asList(value.toString().split(",")), null))
		.put("LAST_NAME", 			new FieldMapper("lastName"))
		.put("LIVING_UNIT_ID", 		new FieldMapper("assignedLivingUnitId")).build();

	@Override
	public List<AssignedInmate> findInmatesByLocation(Long locationId, int offset, int limit) {
		final String sql = getPagedQuery("FIND_INMATES_BY_LOCATION");
		final RowMapper<AssignedInmate> assignedInmateRowMapper = Row2BeanRowMapper.makeMapping(sql, AssignedInmate.class, assignedInmateMapping);
		final List<AssignedInmate> inmates = jdbcTemplate.query(sql, createParams("locationId", locationId, "offset", offset, "limit", limit), assignedInmateRowMapper);
		return inmates;
	}

	@Override
	public InmateDetail findInmate(Long inmateId) {
		return null;
	}

}


