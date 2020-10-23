package uk.gov.justice.hmpps.prison.repository.v1;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.RepositoryBase;
import uk.gov.justice.hmpps.prison.repository.v1.model.BookingSP;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.BookingProcs;

import java.util.List;
import java.util.Optional;

import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_BOOKING_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_ID;

@Repository
public class BookingV1Repository extends RepositoryBase {

    private final BookingProcs.GetLatestBooking getLatestBookingProc;
    private final BookingProcs.GetOffenderBookings getOffenderBookings;

    public BookingV1Repository(final BookingProcs.GetLatestBooking getLatestBookingProc,
                               final BookingProcs.GetOffenderBookings getOffenderBookings) {
        this.getLatestBookingProc = getLatestBookingProc;
        this.getOffenderBookings = getOffenderBookings;
    }

    public Optional<BookingSP> getLatestBooking(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getLatestBookingProc.execute(param);
        //noinspection unchecked
        final var latestBooking = (List<BookingSP>) result.get(P_BOOKING_CSR);
        return Optional.ofNullable(latestBooking.isEmpty() ? null : latestBooking.get(0));
    }

    public List<BookingSP> getOffenderBookings(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderBookings.execute(param);
        //noinspection unchecked
        return (List<BookingSP>) result.get(P_BOOKING_CSR);
    }
}
