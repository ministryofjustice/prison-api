package net.syscon.elite.repository.v1;

import net.syscon.elite.api.model.v1.Image;
import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.OffenderSP;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderDetails;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderImage;
import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderImage.P_IMAGE;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_ID;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.P_OFFENDER_CSR;

@Repository
public class OffenderV1Repository extends RepositoryBase {

    private final GetOffenderDetails getOffenderDetailsProc;
    private final GetOffenderImage getOffenderImageProc;

    public OffenderV1Repository(NomisV1SQLErrorCodeTranslator errorCodeTranslator,
                                GetOffenderDetails getOffenderDetailsProc,
                                GetOffenderImage getOffenderImageProc) {
        this.getOffenderDetailsProc = getOffenderDetailsProc;
        this.getOffenderImageProc = getOffenderImageProc;

        //TODO: There will be a better way of doing this...
        this.getOffenderDetailsProc.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
        this.getOffenderImageProc.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
    }

    public Optional<OffenderSP> getOffender(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderDetailsProc.execute(param);
        var offender = (List<OffenderSP>) result.get(P_OFFENDER_CSR);

        return Optional.ofNullable(offender.isEmpty() ? null : offender.get(0));
    }

    public Optional<Image> getPhoto(final String nomsId) {

        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderImageProc.execute(param);
        var blobBytes = (Blob) result.get(P_IMAGE);
        try {
            var image = blobBytes != null ? Image.builder().image(IOUtils.toByteArray(blobBytes.getBinaryStream())).build() : null;
            return Optional.ofNullable(image);
        } catch (IOException | SQLException e) {
            return Optional.empty();
        }
    }

}
