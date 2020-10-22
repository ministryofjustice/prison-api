package uk.gov.justice.hmpps.prison.repository.v1;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.RepositoryBase;
import uk.gov.justice.hmpps.prison.repository.v1.model.ChargeSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.LegalCaseSP;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.LegalProcs;

import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CASES_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CASE_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CHARGES_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_OFFENDER_BOOK_ID;

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
