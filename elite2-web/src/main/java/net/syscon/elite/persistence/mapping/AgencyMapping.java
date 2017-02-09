package net.syscon.elite.persistence.mapping;

import net.syscon.elite.web.api.model.Agency;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AgencyMapping implements RowMapper<Agency> {

	@Override
	public Agency mapRow(ResultSet rs, int rowNum) throws SQLException {
		final Agency agency = new Agency();
		agency.setUid(rs.getLong("ID"));
		agency.setAgencyId(rs.getString("AGENCY_ID"));
		agency.setDescription(rs.getString("DESCRIPTION"));
		agency.setAgencyType(rs.getString("AGENCY_LOCATION_TYPE"));
		return agency;
	}
}

