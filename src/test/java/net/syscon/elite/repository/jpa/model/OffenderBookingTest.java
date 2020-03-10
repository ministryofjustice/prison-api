package net.syscon.elite.repository.jpa.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderBookingTest {

    @Test
    void booking_is_not_active() {
        assertThat(OffenderBooking.builder().build().isActive()).isFalse();
    }

    @Test
    void booking_is_active() {
        assertThat(OffenderBooking.builder().bookingSequence(1).build().isActive()).isTrue();
    }
}
