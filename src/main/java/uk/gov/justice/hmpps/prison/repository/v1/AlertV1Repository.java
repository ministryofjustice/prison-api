package uk.gov.justice.hmpps.prison.repository.v1;

import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.RepositoryBase;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.v1.model.AlertSP;
import uk.gov.justice.hmpps.prison.repository.v1.sql.AlertV1RepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_ID;

@Repository
public class AlertV1Repository extends RepositoryBase {
    private static final String P_MODIFIED_SINCE = "p_modified_since";

    private static final StandardBeanPropertyRowMapper<AlertSP> ALERT_V1_MAPPER = new StandardBeanPropertyRowMapper<>(AlertSP.class);

    public List<AlertSP> getAlerts(final String nomsId, final boolean includeInactive, final LocalDateTime modifiedSince) {
        final var sql = includeInactive ? AlertV1RepositorySql.ALERTS_BY_OFFENDER_WTIH_INACTIVE.getSql() :
                                                 AlertV1RepositorySql.ALERTS_BY_OFFENDER.getSql();
        return jdbcTemplate.query(
                sql,
                createParams(
                        P_NOMS_ID, new SqlParameterValue(Types.VARCHAR, nomsId),
                        P_MODIFIED_SINCE, new SqlParameterValue(Types.TIMESTAMP, DateTimeConverter.toDate(modifiedSince))),
                ALERT_V1_MAPPER);
    }
}
