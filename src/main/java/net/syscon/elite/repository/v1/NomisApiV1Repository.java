package net.syscon.elite.repository.v1;

import net.syscon.elite.api.model.v1.CodeDescription;
import net.syscon.elite.api.model.v1.InternalLocation;
import net.syscon.elite.api.model.v1.Location;
import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.v1.model.LocationInformation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class NomisApiV1Repository extends RepositoryBase {

    private static final String P_BOOKING_CSR = "p_booking_csr";

    private SimpleJdbcCall latestBookingProc;

    @PostConstruct
    private void init() {
        latestBookingProc = getLatestBookingProc();
    }

    public Optional<Location> getLatestBookingLocation(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue("p_noms_id", nomsId);
        final var result = latestBookingProc.execute(param);
        var locations = (List<LocationInformation>) result.get(P_BOOKING_CSR);

        var location = Optional.ofNullable(locations.isEmpty() ? null : locations.get(0));

        return location.map(locationInformation -> Location.builder()
                        .establishment(new CodeDescription(locationInformation.getAgyLocId(), locationInformation.getAgyLocDesc()))
                        .housingLocation(StringUtils.isNotBlank(locationInformation.getHousingLocation()) ? new InternalLocation(locationInformation.getHousingLocation(), locationInformation.getHousingLevels()) : null)
                        .build()
            );
    }

    private SimpleJdbcCall getLatestBookingProc() {
        // Prepare Stored Procedure call
        final var simpleJdbcCall = new SimpleJdbcCall(getJdbcTemplateBase())
                .withSchemaName("api_owner")
                .withCatalogName("api_booking_procs")
                .withProcedureName("get_latest_booking")
                .declareParameters(
                        new SqlParameter("p_noms_id", Types.VARCHAR),
                        new SqlOutParameter(P_BOOKING_CSR, Types.REF_CURSOR))
                .returningResultSet(P_BOOKING_CSR,
                        StandardBeanPropertyRowMapper.newInstance(LocationInformation.class));

        simpleJdbcCall.compile();
        return simpleJdbcCall;
    }

}
