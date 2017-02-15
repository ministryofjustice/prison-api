package net.syscon.elite.persistence.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.exception.RowMappingException;
import net.syscon.elite.persistence.repository.InmateRepository;
import net.syscon.elite.web.api.model.AssignedInmate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class InmateRepositoryImpl extends RepositoryBase implements InmateRepository {

	private final Map<String, String> assignedInmateMapping = new ImmutableMap.Builder<String, String>()
		.put("OFFENDER_ID", "inmateId")
		.put("OFFENDER_BOOK_ID", "bookingId")
		.put("OFFENDER_ID_DISPLAY", "offenderId")
		.put("AGY_LOC_ID", "agencyId")
		.put("FIRST_NAME", "firstName")
		.put("LAST_NAME", "lastName")
		.put("LIVING_UNIT_ID", "assignedLivingUnitId").build();

	@Override
	public List<AssignedInmate> findInmatesByLocation(Long locationId, int offset, int limit) {
		final String sql = getPagedQuery("FIND_INMATES_BY_LOCATION");
		final RowMapper<AssignedInmate> assignedInmateRowMapper = Row2BeanRowMapper.makeMapping(sql, AssignedInmate.class, assignedInmateMapping, (rs, inmate) -> {
			try {
				if (rs.getString("ALERT_TYPES") != null) {
					inmate.setAlertsCodes(Arrays.asList(rs.getString("ALERT_TYPES").split(",")));
				}
			} catch (final SQLException ex) {
				throw new RowMappingException(ex.getMessage(), ex);
			}
			return null;
		});
		final List<AssignedInmate> inmates = jdbcTemplate.query(sql, createParams("locationId", locationId, "offset", offset, "limit", limit), assignedInmateRowMapper);
		return inmates;
	}




}


