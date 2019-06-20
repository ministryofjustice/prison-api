package net.syscon.elite.repository.v1;

import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.BookingSP;
import net.syscon.elite.repository.v1.storedprocs.BookingProcs;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.P_BOOKING_CSR;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_ID;

@Repository
public class BookingV1Repository extends RepositoryBase {

    private final BookingProcs.GetLatestBooking getLatestBookingProc;
    private final BookingProcs.GetOffenderBookings getOffenderBookings;

    public BookingV1Repository(NomisV1SQLErrorCodeTranslator errorCodeTranslator,
                               BookingProcs.GetLatestBooking getLatestBookingProc,
                               BookingProcs.GetOffenderBookings getOffenderBookings) {
        this.getLatestBookingProc = getLatestBookingProc;
        this.getOffenderBookings = getOffenderBookings;

        //TODO: There will be a better way of doing this...
        this.getLatestBookingProc.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
        this.getOffenderBookings.getJdbcTemplate().setExceptionTranslator(errorCodeTranslator);
    }

    public Optional<BookingSP> getLatestBooking(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getLatestBookingProc.execute(param);
        var latestBooking = (List<BookingSP>) result.get(P_BOOKING_CSR);
        return Optional.ofNullable(latestBooking.isEmpty() ? null : latestBooking.get(0));
    }

    public List<BookingSP> getOffenderBookings(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderBookings.execute(param);
        return (List<BookingSP>) result.get(P_BOOKING_CSR);
    }
}
