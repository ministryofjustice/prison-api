package uk.gov.justice.hmpps.prison.repository.v1;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.RepositoryBase;
import uk.gov.justice.hmpps.prison.repository.v1.model.LiveRollSP;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.PrisonProcs.GetLiveRoll;

import java.util.List;

import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_AGY_LOC_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_ROLL_CSR;

@Repository
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PrisonV1Repository extends RepositoryBase {

    private final GetLiveRoll getLiveRollProc;

    public List<LiveRollSP> getLiveRoll(final String prisonId) {
        final var param = new MapSqlParameterSource().addValue(P_AGY_LOC_ID, prisonId);
        final var result = getLiveRollProc.execute(param);
        //noinspection unchecked
        return (List<LiveRollSP>) result.get(P_ROLL_CSR);
    }
}
