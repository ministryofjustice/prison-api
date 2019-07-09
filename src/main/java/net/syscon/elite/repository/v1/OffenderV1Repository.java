package net.syscon.elite.repository.v1;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.OffenderPssDetailSP;
import net.syscon.elite.repository.v1.model.OffenderSP;
import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static net.syscon.elite.repository.v1.storedprocs.OffenderProcs.*;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

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

    public Optional<OffenderPssDetailSP> getOffenderPssDetail(final String nomsId) {

        // Last three parameters are hard-coded to null in current NomisAPI too
        final var params = new MapSqlParameterSource()
                .addValue(P_NOMS_ID, nomsId)
                .addValue(P_ROOT_OFFENDER_ID, null)
                .addValue(P_SINGLE_OFFENDER_ID, null)
                .addValue(P_AGY_LOC_ID, null);

        try {

            final var result = getOffenderPssDetailProc.execute(params);
            if (result.isEmpty()) {
                log.error("Result of procedure call was empty for {}", nomsId);
                return Optional.empty();
            }

            final var pssDetail = OffenderPssDetailSP.builder()
                    .id(Long.valueOf(0L))
                    .eventTimestamp(timestampToCalendar((Timestamp) result.get(P_TIMESTAMP)))
                    .nomsId((String) result.get(P_NOMS_ID))
                    .singleOffenderId((String) result.get(P_SINGLE_OFFENDER_ID))
                    .prisonId((String) result.get(P_AGY_LOC_ID))
                    .eventType(OFFENDER_DETAILS_REQUEST_TYPE)
                    .eventData(clobToString((Clob)result.get(P_DETAILS_CLOB)))
                    .build();

            return Optional.ofNullable(pssDetail);

        } catch (Exception sqle) {

            log.error("SQLException from pss_offender_details() = msg{}", sqle.getMessage());
            return Optional.empty();
        }
    }

    private Calendar timestampToCalendar(final Timestamp from) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(from.getTime());

        return calendar;
    }

    private String clobToString(final Clob clobField) throws SQLException, IOException {

        StringBuilder sb = new StringBuilder();
        Reader reader = clobField.getCharacterStream();
        BufferedReader br = new BufferedReader(reader);
        String line;

        while(null != (line = br.readLine())) {
           sb.append(line);
        }
        br.close();

        // Free resources associated with this Clob field - may cause write to temporary tablespace
        clobField.free();

        return sb.toString();
    }
}
