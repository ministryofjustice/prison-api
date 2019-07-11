package net.syscon.elite.repository.v1;

import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.ChargeSP;
import net.syscon.elite.repository.v1.model.LegalCaseSP;
import net.syscon.elite.repository.v1.storedprocs.LegalProcs;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

@Repository
public class LegalV1Repository extends RepositoryBase {

    private final LegalProcs.GetBookingCases getBookingCases;
    private final LegalProcs.GetCaseCharges getCaseCharges;

    public LegalV1Repository(final LegalProcs.GetBookingCases getBookingCases,
                             final LegalProcs.GetCaseCharges getCaseCharges) {
        this.getBookingCases = getBookingCases;
        this.getCaseCharges = getCaseCharges;
    }

    public List<LegalCaseSP> getBookingCases(final Long bookingId) {
        final var param = new MapSqlParameterSource().addValue(P_OFFENDER_BOOK_ID, bookingId);
        final var result = getBookingCases.execute(param);
        //noinspection unchecked
        return (List<LegalCaseSP>) result.get(P_CASES_CSR);
    }

    public List<ChargeSP> getCaseCharges(final Long caseId) {
        final var param = new MapSqlParameterSource().addValue(P_CASE_ID, caseId);
        final var result = getCaseCharges.execute(param);
        //noinspection unchecked
        return (List<ChargeSP>) result.get(P_CHARGES_CSR);
    }
}
