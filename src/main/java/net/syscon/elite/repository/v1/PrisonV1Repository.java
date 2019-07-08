package net.syscon.elite.repository.v1;

import lombok.AllArgsConstructor;
import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.LiveRollSP;
import net.syscon.elite.repository.v1.storedprocs.PrisonProcs.GetLiveRoll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.P_AGY_LOC_ID;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.P_ROLL_CSR;

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
