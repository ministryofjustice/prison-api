package uk.gov.justice.hmpps.prison.repository.v1;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.RepositoryBase;
import uk.gov.justice.hmpps.prison.repository.v1.model.EventSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.OffenderSP;

import java.io.IOException;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.OffenderProcs.GetOffenderDetails;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.OffenderProcs.GetOffenderImage;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.OffenderProcs.GetOffenderPssDetail;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_AGY_LOC_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_DETAILS_CLOB;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_IMAGE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_OFFENDER_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_ROOT_OFFENDER_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_SINGLE_OFFENDER_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TIMESTAMP;

@Repository
@Slf4j
public class OffenderV1Repository extends RepositoryBase {

    private static final String OFFENDER_DETAILS_REQUEST_TYPE = "OFFENDER_DETAILS_REQUEST";

    private final GetOffenderDetails getOffenderDetailsProc;
    private final GetOffenderImage getOffenderImageProc;
    private final GetOffenderPssDetail getOffenderPssDetailProc;

    public OffenderV1Repository(final GetOffenderDetails getOffenderDetailsProc,
                                final GetOffenderImage getOffenderImageProc,
                                final GetOffenderPssDetail getOffenderPssDetailProc) {

        this.getOffenderDetailsProc = getOffenderDetailsProc;
        this.getOffenderImageProc = getOffenderImageProc;
        this.getOffenderPssDetailProc = getOffenderPssDetailProc;
    }

    public Optional<OffenderSP> getOffender(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderDetailsProc.execute(param);
        //noinspection unchecked
        final var offender = (List<OffenderSP>) result.get(P_OFFENDER_CSR);

        return Optional.ofNullable(offender.isEmpty() ? null : offender.get(0));
    }

    public Optional<byte[]> getPhoto(final String nomsId) {

        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderImageProc.execute(param);
        final var blobBytes = (Blob) result.get(P_IMAGE);
        try {
            return Optional.ofNullable(blobBytes != null ? IOUtils.toByteArray(blobBytes.getBinaryStream()) : null);
        } catch (final IOException | SQLException e) {
            log.error("Caught {} trying to get photo for {}", e.getClass().getName(), nomsId, e);
            return Optional.empty();
        }
    }

    public Optional<EventSP> getOffenderPssDetail(final String nomsId) {

        // Last three parameters are hard-coded to null in current NomisAPI too
        final var params = new MapSqlParameterSource()
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_ROOT_OFFENDER_ID, null)
                .addValue(P_SINGLE_OFFENDER_ID, null)
                .addValue(P_AGY_LOC_ID, null);

        final var result = getOffenderPssDetailProc.execute(params);
        if (result.isEmpty()) {
            log.info("Result of procedure call was empty for {}", nomsId);
            return Optional.empty();
        }

        var pssDetail = EventSP.builder()
                .apiEventId(0L)
                .eventTimestamp(((Timestamp) result.get(P_TIMESTAMP)).toLocalDateTime())
                .nomsId((String) result.get(P_NOMS_ID))
                .agyLocId((String) result.get(P_AGY_LOC_ID))
                .eventType(OFFENDER_DETAILS_REQUEST_TYPE)
                .eventData_1(clobToString((Clob) result.get(P_DETAILS_CLOB)))
                .build();

        return Optional.ofNullable(pssDetail);
    }

    private String clobToString(final Clob clobField) {
        try {
            Reader reader = clobField.getCharacterStream();
            final var response = IOUtils.toString(reader);
            // Free resources associated with this Clob field - may cause write to temporary tablespace.
            clobField.free();
            return response;
        } catch (final SQLException | IOException e) {
            log.error("Exception in PSS detail response clobToString {}", e.getClass().getName(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
