package net.syscon.elite.repository.v1;

import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.LatestBookingSP;
import net.syscon.elite.repository.v1.model.OffenderSP;
import net.syscon.elite.repository.v1.storedprocs.GetLatestBookingProc;
import net.syscon.elite.repository.v1.storedprocs.GetOffenderDetailsProc;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

@Repository
public class NomisApiV1Repository extends RepositoryBase {

    private final GetLatestBookingProc getLatestBookingProc;
    private final GetOffenderDetailsProc getOffenderDetailsProc;

    public NomisApiV1Repository(GetLatestBookingProc getLatestBookingProc,
                                GetOffenderDetailsProc getOffenderDetailsProc,
                                NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
        this.getLatestBookingProc = getLatestBookingProc;
        this.getOffenderDetailsProc = getOffenderDetailsProc;

        //TODO: There will be a better way of doing this...
        this.getLatestBookingProc.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
        this.getOffenderDetailsProc.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
    }

    public Optional<LatestBookingSP> getLatestBooking(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getLatestBookingProc.execute(param);
        var locations = (List<LatestBookingSP>) result.get(P_BOOKING_CSR);

        return Optional.ofNullable(locations.isEmpty() ? null : locations.get(0));
    }

    public Optional<OffenderSP> getOffender(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderDetailsProc.execute(param);
        var offender = (List<OffenderSP>) result.get(P_OFFENDER_CSR);

        return Optional.ofNullable(offender.isEmpty() ? null : offender.get(0));
    }

}
