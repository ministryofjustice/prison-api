package uk.gov.justice.hmpps.prison.api.model.bulkappointments

import jakarta.validation.Validation
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AppointmentsToCreateTest {
  private val validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun shouldFlattenNoAppointments() {
    val flattened = AppointmentsToCreate
      .builder()
      .appointments(listOf())
      .appointmentDefaults(AppointmentDefaults.builder().build())
      .build()
      .withDefaults()

    assertThat(flattened).isNotNull()
  }

  @Test
  fun shouldUseDefaults() {
    val flattened = AppointmentsToCreate
      .builder()
      .appointmentDefaults(
        AppointmentDefaults
          .builder()
          .appointmentType("AT")
          .locationId(1000L)
          .startTime(DEFAULT_START_TIME)
          .endTime(DEFAULT_END_TIME)
          .comment("DC")
          .build(),
      )
      .appointments(
        listOf(
          AppointmentDetails.builder()
            .bookingId(1L)
            .build(),
        ),
      )
      .build()
      .withDefaults()

    assertThat(flattened).containsExactly(
      AppointmentDetails.builder()
        .startTime(DEFAULT_START_TIME)
        .endTime(DEFAULT_END_TIME)
        .comment("DC")
        .bookingId(1L)
        .build(),
    )
  }

  @Test
  fun shouldOverrideDefaults() {
    val flattened = AppointmentsToCreate
      .builder()
      .appointmentDefaults(
        AppointmentDefaults
          .builder()
          .appointmentType("AT")
          .locationId(1000L)
          .startTime(DEFAULT_START_TIME)
          .endTime(DEFAULT_END_TIME)
          .comment("DC")
          .build(),
      )
      .appointments(
        listOf(
          AppointmentDetails.builder()
            .bookingId(1L)
            .startTime(START_TIME)
            .endTime(END_TIME)
            .comment("C")
            .build(),
        ),
      )
      .build()
      .withDefaults()

    assertThat(flattened).containsExactly(
      AppointmentDetails.builder()
        .startTime(START_TIME)
        .endTime(END_TIME)
        .comment("C")
        .bookingId(1L)
        .build(),
    )
  }

  @Test
  fun shouldRejectEmptyAppointmentsToCreate() {
    val validations = validator.validate(AppointmentsToCreate.builder().build())
    assertThat(validations).hasSize(2)
  }

  @Test
  fun shouldRejectMissingDefaults() {
    val validations = validator.validate(
      AppointmentsToCreate
        .builder()
        .appointmentDefaults(AppointmentDefaults.builder().build())
        .appointments(listOf())
        .build(),
    )
    assertThat(validations).hasSize(3)
  }

  @Test
  fun shouldAcceptValidDefaults() {
    val validations = validator.validate(
      AppointmentsToCreate
        .builder()
        .appointmentDefaults(VALID_DEFAULTS)
        .appointments(listOf())
        .build(),
    )
    assertThat(validations).hasSize(0)
  }

  @Test
  fun shouldRejectMissingAppointmentDetails() {
    val validations = validator.validate(
      AppointmentsToCreate
        .builder()
        .appointmentDefaults(VALID_DEFAULTS)
        .appointments(
          listOf(
            AppointmentDetails.builder().build(),
          ),
        )
        .build(),
    )
    assertThat(validations).hasSize(1)
  }

  @Test
  fun shouldRejectInvalidAppointmentDetails() {
    val validations = validator.validate(
      AppointmentsToCreate
        .builder()
        .appointmentDefaults(VALID_DEFAULTS)
        .appointments(
          listOf(
            AppointmentDetails.builder()
              .bookingId(0L)
              .startTime(LocalDateTime.now().minusHours(1))
              .comment(StringUtils.repeat('A', 4001))
              .build(),
          ),
        )
        .build(),
    )
    assertThat(validations).hasSize(2)
  }

  @Test
  fun shouldAcceptValidAppointmentDetails() {
    val validations = validator.validate(
      AppointmentsToCreate
        .builder()
        .appointmentDefaults(VALID_DEFAULTS)
        .appointments(
          listOf(
            AppointmentDetails.builder()
              .bookingId(0L)
              .startTime(LocalDateTime.now().plusHours(1))
              .comment(StringUtils.repeat('A', 4000))
              .build(),
          ),
        )
        .build(),
    )
    assertThat(validations).hasSize(0)
  }

  @Test
  fun shouldRejectInvalidRepeatPeriod() {
    val validations = validator.validate(
      AppointmentsToCreate
        .builder()
        .appointmentDefaults(VALID_DEFAULTS)
        .appointments(listOf())
        .repeat(
          Repeat
            .builder()
            .count(1)
            .build(),
        )
        .build(),
    )
    assertThat(validations).hasSize(1)
  }

  @Test
  fun shouldRejectInvalidRepeatCount() {
    val validations = validator.validate(
      AppointmentsToCreate
        .builder()
        .appointmentDefaults(VALID_DEFAULTS)
        .appointments(listOf())
        .repeat(
          Repeat
            .builder()
            .repeatPeriod(RepeatPeriod.DAILY)
            .count(0)
            .build(),
        )
        .build(),
    )
    assertThat(validations).hasSize(1)
  }

  @Test
  fun shouldAcceptValidRepeat() {
    val validations = validator.validate(
      AppointmentsToCreate
        .builder()
        .appointmentDefaults(VALID_DEFAULTS)
        .appointments(listOf())
        .repeat(
          Repeat
            .builder()
            .repeatPeriod(RepeatPeriod.DAILY)
            .count(1)
            .build(),
        )
        .build(),
    )
    assertThat(validations).hasSize(0)
  }

  companion object {
    private val DEFAULT_START_TIME: LocalDateTime = LocalDateTime.of(2019, 1, 1, 0, 0)
    private val DEFAULT_END_TIME: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0)
    private val START_TIME: LocalDateTime = LocalDateTime.of(2021, 1, 1, 0, 0)
    private val END_TIME: LocalDateTime = LocalDateTime.of(2022, 1, 1, 0, 0)

    private val VALID_DEFAULTS: AppointmentDefaults? = AppointmentDefaults
      .builder()
      .locationId(1L)
      .appointmentType("ABC")
      .startTime(LocalDateTime.now().plusHours(1))
      .build()
  }
}
