package net.syscon.elite.api.model.bulkappointments;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AppointmentsToCreateTest {
    private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.of(2019,1,1,0,0);
    private static final LocalDateTime DEFAULT_END_TIME = LocalDateTime.of(2020,1,1,0,0);
    private static final LocalDateTime START_TIME = LocalDateTime.of(2021,1,1,0,0);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2022,1,1,0,0);
    @Test
    public void shouldFlattenNoAppointments() {
        List<AppointmentDetails> flattened = AppointmentsToCreate
                .builder()
                .appointments(Collections.emptyList())
                .appointmentDefaults(AppointmentDefaults.builder().build())
                .build()
                .withDefaults();

        assertThat(flattened).isNotNull();
    }

    @Test
    public void shouldUseDefaults() {
        List<AppointmentDetails> flattened = AppointmentsToCreate
                .builder()
                .appointmentDefaults(AppointmentDefaults
                        .builder()
                        .appointmentType("AT")
                        .locationId(1000L)
                        .startTime(DEFAULT_START_TIME)
                        .endTime(DEFAULT_END_TIME)
                        .comment("DC")
                        .build())
                .appointments(Collections.singletonList(AppointmentDetails
                        .builder()
                        .bookingId(1L)
                        .build()))
                .build()
                .withDefaults();

        assertThat(flattened).containsExactly(AppointmentDetails
                .builder()
                .startTime(DEFAULT_START_TIME)
                .endTime(DEFAULT_END_TIME)
                .comment("DC")
                .bookingId(1L)
                .build());
    }

    @Test
    public void shouldOverrideDefaults() {
        List<AppointmentDetails> flattened = AppointmentsToCreate
                .builder()
                .appointmentDefaults(AppointmentDefaults
                        .builder()
                        .appointmentType("AT")
                        .locationId(1000L)
                        .startTime(DEFAULT_START_TIME)
                        .endTime(DEFAULT_END_TIME)
                        .comment("DC")
                        .build())
                .appointments(Collections.singletonList(AppointmentDetails
                        .builder()
                        .bookingId(1L)
                        .startTime(START_TIME)
                        .endTime(END_TIME)
                        .comment("C")
                        .build()))
                .build()
                .withDefaults();

        assertThat(flattened).containsExactly(AppointmentDetails
                .builder()
                .startTime(START_TIME)
                .endTime(END_TIME)
                .comment("C")
                .bookingId(1L)
                .build());
    }
}
