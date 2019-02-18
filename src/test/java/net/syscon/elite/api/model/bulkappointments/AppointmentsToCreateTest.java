package net.syscon.elite.api.model.bulkappointments;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AppointmentsToCreateTest {
    private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.of(2019, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_END_TIME = LocalDateTime.of(2020, 1, 1, 0, 0);
    private static final LocalDateTime START_TIME = LocalDateTime.of(2021, 1, 1, 0, 0);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2022, 1, 1, 0, 0);

    private static final AppointmentDefaults VALID_DEFAULTS = AppointmentDefaults
            .builder()
            .locationId(1L)
            .appointmentType("ABC")
            .startTime(LocalDateTime.now().plusHours(1))
            .build();

    private static Validator validator;

    @BeforeClass
    public static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

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

    @Test
    public void shouldRejectEmptyAppointmentsToCreate() {
        var validations = validator.validate(AppointmentsToCreate.builder().build());
        assertThat(validations).hasSize(2);
    }

    @Test
    public void shouldRejectMissingDefaults() {
        var validations = validator.validate(
                AppointmentsToCreate
                        .builder()
                        .appointmentDefaults(AppointmentDefaults.builder().build())
                        .appointments(Collections.emptyList())
                        .build());
        assertThat(validations).hasSize(3);
    }

    @Test
    public void shouldAcceptValidDefaults() {
        var validations = validator.validate(
                AppointmentsToCreate
                        .builder()
                        .appointmentDefaults(VALID_DEFAULTS)
                        .appointments(Collections.emptyList())
                        .build());
        assertThat(validations).hasSize(0);
    }

    @Test
    public void shouldRejectMissingAppointmentDetails() {
        var validations = validator.validate(
                AppointmentsToCreate
                        .builder()
                        .appointmentDefaults(VALID_DEFAULTS)
                        .appointments(Collections.singletonList(
                                AppointmentDetails.builder().build()
                        ))
                        .build());
        assertThat(validations).hasSize(1);
    }

    @Test
    public void shouldRejectInvalidAppointmentDetails() {
        var validations = validator.validate(
                AppointmentsToCreate
                        .builder()
                        .appointmentDefaults(VALID_DEFAULTS)
                        .appointments(Collections.singletonList(
                                AppointmentDetails
                                        .builder()
                                        .bookingId(0L)
                                        .startTime(LocalDateTime.now().minusHours(1))
                                        .comment(StringUtils.repeat('A', 4001))
                                        .build()
                        ))
                        .build());
        assertThat(validations).hasSize(2);
    }

    @Test
    public void shouldAcceptValidAppointmentDetails() {
        var validations = validator.validate(
                AppointmentsToCreate
                        .builder()
                        .appointmentDefaults(VALID_DEFAULTS)
                        .appointments(Collections.singletonList(
                                AppointmentDetails
                                        .builder()
                                        .bookingId(0L)
                                        .startTime(LocalDateTime.now().plusHours(1))
                                        .comment(StringUtils.repeat('A', 4000))
                                        .build()
                        ))
                        .build());
        assertThat(validations).hasSize(0);
    }

    @Test
    public void shouldRejectInvalidRepeatPeriod() {
        var validations = validator.validate(
                AppointmentsToCreate
                        .builder()
                        .appointmentDefaults(VALID_DEFAULTS)
                        .appointments(Collections.emptyList())
                        .repeat(Repeat
                                .builder()
                                .count(1)
                                .build())
                        .build());
        assertThat(validations).hasSize(1);
    }

    @Test
    public void shouldRejectInvalidRepeatCount() {
        var validations = validator.validate(
                AppointmentsToCreate
                        .builder()
                        .appointmentDefaults(VALID_DEFAULTS)
                        .appointments(Collections.emptyList())

                        .repeat(Repeat
                                .builder()
                                .repeatPeriod(RepeatPeriod.DAILY)
                                .count(0)
                                .build())
                        .build());
        assertThat(validations).hasSize(1);
    }

    @Test
    public void shouldAcceptValidRepeat() {
        var validations = validator.validate(
                AppointmentsToCreate
                        .builder()
                        .appointmentDefaults(VALID_DEFAULTS)
                        .appointments(Collections.emptyList())

                        .repeat(Repeat
                                .builder()
                                .repeatPeriod(RepeatPeriod.DAILY)
                                .count(1)
                                .build())
                        .build());
        assertThat(validations).hasSize(0);
    }
}
