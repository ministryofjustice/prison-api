package uk.gov.justice.hmpps.prison.repository;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Getter
@ToString
@EqualsAndHashCode
public class OffenderBookingIdSeq {
    private final String offenderNo;
    private final Optional<BookingAndSeq> bookingAndSeq;

    public OffenderBookingIdSeq(final String offenderNo, final Long bookingId, final Integer seq) {
        this.offenderNo = offenderNo;
        if (bookingId != null && seq != null) {
            this.bookingAndSeq = Optional.of(new BookingAndSeq(bookingId, seq));
        } else {
            this.bookingAndSeq = Optional.empty();
        }
    }
    @Getter
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class BookingAndSeq {
        private final long bookingId;
        private final int bookingSeq;
    }
}
