package net.syscon.elite.repository.v1;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.OffenderPssDetailSP;
import net.syscon.elite.repository.v1.model.OffenderSP;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderDetails;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderImage;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderPssDetail;
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

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

@Repository
@Slf4j
public class OffenderV1Repository extends RepositoryBase {

    private static final String OFFENDER_DETAILS_REQUEST_TYPE = "offender_details_request";

    private final GetOffenderDetails getOffenderDetailsProc;
    private final GetOffenderImage getOffenderImageProc;
    private final GetOffenderPssDetail getOffenderPssDetailProc;

    public OffenderV1Repository(NomisV1SQLErrorCodeTranslator errorCodeTranslator,
                                GetOffenderDetails getOffenderDetailsProc,
                                GetOffenderImage getOffenderImageProc,
                                GetOffenderPssDetail getOffenderPssDetailProc) {

        this.getOffenderDetailsProc = getOffenderDetailsProc;
        this.getOffenderImageProc = getOffenderImageProc;
        this.getOffenderPssDetailProc = getOffenderPssDetailProc;

        //TODO: There will be a better way of doing this...
        this.getOffenderDetailsProc.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
        this.getOffenderImageProc.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
        this.getOffenderPssDetailProc.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
    }

    public Optional<OffenderSP> getOffender(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderDetailsProc.execute(param);
        var offender = (List<OffenderSP>) result.get(P_OFFENDER_CSR);

        return Optional.ofNullable(offender.isEmpty() ? null : offender.get(0));
    }

    public Optional<byte[]> getPhoto(final String nomsId) {

        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderImageProc.execute(param);
        var blobBytes = (Blob) result.get(P_IMAGE);
        try {
            return Optional.ofNullable(blobBytes != null ? IOUtils.toByteArray(blobBytes.getBinaryStream()) : null);
        } catch (IOException | SQLException e) {
            return Optional.empty();
        }
    }

    public Optional<OffenderPssDetailSP> getOffenderPssDetail(final String nomsId) {

        final var pNomsId = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var pRootOffenderId = new MapSqlParameterSource().addValue(P_ROOT_OFFENDER_ID, null);
        final var pSingleOffenderId = new MapSqlParameterSource().addValue(P_SINGLE_OFFENDER_ID, null);
        final var pAgyLocId = new MapSqlParameterSource().addValue(P_AGY_LOC_ID, null);

        try {

            final var result = getOffenderPssDetailProc.execute(pNomsId, pRootOffenderId, pSingleOffenderId, pAgyLocId);
            if (result.isEmpty()) {
                return Optional.empty();
            }

            final var pssDetail = OffenderPssDetailSP.builder()
                    .id(0L)
                    .eventTimestamp(timestampToCalendar((Timestamp) result.get(P_TIMESTAMP)))
                    .nomsId((String) result.get(P_NOMS_ID))
                    .rootOffenderId((Long) result.get(P_ROOT_OFFENDER_ID))
                    .singleOffenderId((String) result.get(P_SINGLE_OFFENDER_ID))
                    .prisonId((String) result.get(P_AGY_LOC_ID))
                    .eventType(OFFENDER_DETAILS_REQUEST_TYPE)
                    .eventData(clobToString((Clob)result.get(P_DETAILS_CLOB)))
                    .build();

            return Optional.ofNullable(pssDetail);

        } catch (Exception sql) {

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
