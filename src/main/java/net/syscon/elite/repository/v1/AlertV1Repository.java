package net.syscon.elite.repository.v1;

import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.AlertSP;
import net.syscon.elite.repository.v1.storedprocs.AlertsProcs;
import net.syscon.elite.repository.v1.storedprocs.AlertsProcs.GetAlerts;
import net.syscon.util.DateTimeConverter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static net.syscon.elite.repository.v1.storedprocs.AlertsProcs.P_INCLUDE_INACTIVE;
import static net.syscon.elite.repository.v1.storedprocs.AlertsProcs.P_MODIFIED_SINCE;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_ID;

@Repository
public class AlertV1Repository extends RepositoryBase {

    private final GetAlerts getAlerts;

    public AlertV1Repository(NomisV1SQLErrorCodeTranslator errorCodeTranslator,
                             GetAlerts getAlerts) {
        this.getAlerts = getAlerts;
        this.getAlerts.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
    }

    public List<AlertSP> getAlerts(final String nomsId, final boolean includeInactive, final LocalDateTime modifiedSince) {
        final var param = new MapSqlParameterSource()
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_INCLUDE_INACTIVE, includeInactive ? "Y" : "N")
                .addValue(P_MODIFIED_SINCE, DateTimeConverter.fromLocalDateTime(modifiedSince));

        final var result = getAlerts.execute(param);
        return (List<AlertSP>) result.get(AlertsProcs.P_ALERTS_CSR);
    }
}
