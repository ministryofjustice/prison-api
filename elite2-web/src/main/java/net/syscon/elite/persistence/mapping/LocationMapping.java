package net.syscon.elite.persistence.mapping;

import net.syscon.elite.web.api.model.Location;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class LocationMapping implements RowMapper<Location> {

	@Override
	public Location mapRow(ResultSet rs, int rowNum) throws SQLException {
		final Location location = new Location();
		location.setLocationId(rs.getLong("INTERNAL_LOCATION_ID"));
		location.setAgencyId(rs.getString("AGY_LOC_ID"));
		location.setLocationType(rs.getString("INTERNAL_LOCATION_TYPE"));
		location.setParentLocationId(rs.getLong("PARENT_INTERNAL_LOCATION_ID"));
		location.setCurrentOccupancy(rs.getLong("NO_OF_OCCUPANT"));
		return location;
	}
}
