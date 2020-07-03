package net.syscon.prison.repository.v1;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.prison.repository.impl.RepositoryBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static net.syscon.prison.repository.v1.storedprocs.CoreProcs.GetActiveOffender;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_BIRTH_DATE;
import static net.syscon.prison.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_NUMBER;

@Slf4j
@Repository
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CoreV1Repository extends RepositoryBase {

    private final GetActiveOffender getActiveOffender;

    public BigDecimal getActiveOffender(String nomsId, LocalDate birthDate) {

        final var params = new MapSqlParameterSource()
                .addValue(P_NOMS_NUMBER, nomsId)
                .addValue(P_BIRTH_DATE, birthDate);
        return getActiveOffender.executeFunction(BigDecimal.class, params);
    }

}
