package uk.gov.justice.hmpps.prison.service.transformers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.hmpps.prison.api.model.Alert
import uk.gov.justice.hmpps.prison.repository.jpa.model.AlertCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.AlertType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAlert
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.function.Function
import java.util.stream.Stream

internal class OffenderAlertTransformerTest {
  private fun assertCoreDataIsCopied(transformer: Function<OffenderAlert, Alert>) {
    val entity = OffenderAlert
      .builder()
      .alertDate(LocalDate.parse("2020-01-30"))
      .offenderBooking(
        OffenderBooking
          .builder()
          .offender(Offender.builder().nomsId("A1234JK").build())
          .build(),
      )
      .code(AlertCode("RSS", "Risk to Staff - Custody"))
      .alertCode("RSS")
      .comment("Do not trust this person")
      .createUser(
        StaffUserAccount
          .builder()
          .username("someuser")
          .staff(Staff.builder().firstName("JANE").lastName("BUBBLES").build())
          .build(),
      )
      .expiryDate(LocalDate.parse("2120-10-30"))
      .modifyUser(
        StaffUserAccount
          .builder()
          .username("someotheruser")
          .staff(Staff.builder().firstName("JACK").lastName("MATES").build())
          .build(),
      )
      .sequence(3)
      .type(AlertType("R", "Risk"))
      .alertType("R")
      .status("ACTIVE")
      .createDatetime(LocalDateTime.now().minusYears(10))
      .createUserId("someuser")
      .modifyDatetime(LocalDateTime.now().minusYears(1))
      .modifyUserId("someotheruser")
      .build()

    val alert = transformer.apply(entity)

    assertThat(alert.alertId).isEqualTo(3)
    assertThat(alert.alertCode).isEqualTo("RSS")
    assertThat(alert.alertType).isEqualTo("R")
    assertThat(alert.alertCodeDescription).isEqualTo("Risk to Staff - Custody")
    assertThat(alert.alertTypeDescription).isEqualTo("Risk")
    assertThat(alert.comment).isEqualTo("Do not trust this person")
    assertThat(alert.isExpired).isFalse()
    assertThat(alert.isActive).isTrue()
    assertThat(alert.dateCreated).isEqualTo("2020-01-30")
    assertThat(alert.dateExpires).isEqualTo("2120-10-30")
  }

  private fun assertBadCoreDataStillIsCopied(transformer: Function<OffenderAlert?, Alert>) {
    val entity: OffenderAlert? = OffenderAlert
      .builder()
      .alertDate(LocalDate.parse("2020-01-30"))
      .offenderBooking(
        OffenderBooking
          .builder()
          .offender(Offender.builder().nomsId("A1234JK").build())
          .build(),
      )
      .code(null)
      .alertCode("RSS")
      .comment(null)
      .createUser(null)
      .expiryDate(null)
      .modifyUser(null)
      .sequence(3)
      .type(null)
      .alertType("R")
      .status("BANANAS")
      .createDatetime(LocalDateTime.now().minusYears(10))
      .createUserId("someuser")
      .modifyDatetime(LocalDateTime.now().minusYears(1))
      .modifyUserId("someotheruser")
      .build()

    val alert = transformer.apply(entity)

    assertThat(alert.alertId).isEqualTo(3)
    assertThat(alert.alertCode).isEqualTo("RSS")
    assertThat(alert.alertType).isEqualTo("R")
    assertThat(alert.alertCodeDescription).isEqualTo("RSS")
    assertThat(alert.alertTypeDescription).isEqualTo("R")
    assertThat(alert.comment).isNull()
    assertThat(alert.isExpired).isFalse()
    assertThat(alert.isActive).isFalse()
    assertThat(alert.dateCreated).isEqualTo("2020-01-30")
    assertThat(alert.dateExpires).isNull()
  }

  @Nested
  @DisplayName("transformForBooking")
  internal inner class TransformForBooking {
    @Test
    @DisplayName("will transform core alert data")
    fun willTransformCoreAlertData() {
      assertCoreDataIsCopied { OffenderAlertTransformer.transformForBooking(it) }
    }

    @Test
    @DisplayName("even bad alert data is copied as best we can")
    fun evenBadAlertDataIsCopiedAsBestWeCan() {
      assertBadCoreDataStillIsCopied { OffenderAlertTransformer.transformForBooking(it) }
    }

    @Test
    @DisplayName("date created is actually the alert date NOT the created date")
    fun dateCreatedIsActuallyTheAlertDateNOTTheCreatedDate() {
      val alert = anAlert()
        .toBuilder()
        .alertDate(LocalDate.parse("2020-01-30"))
        .createDatetime(LocalDateTime.parse("2021-04-30T12:00"))
        .build()

      assertThat(OffenderAlertTransformer.transformForBooking(alert).dateCreated)
        .isEqualTo("2020-01-30")
    }

    @Test
    @DisplayName("expired is true when expiry date is present and is today or in the past")
    fun expiredIsTrueWhenExpiryDateIsPresentAndIsTodayOrInThePast() {
      assertThat(
        OffenderAlertTransformer.transformForBooking(
          anAlert().toBuilder().expiryDate(null).build(),
        ),
      )
        .extracting { it.isExpired }
        .isEqualTo(false)

      assertThat(
        OffenderAlertTransformer.transformForBooking(
          anAlert().toBuilder().expiryDate(
            LocalDate.now().plusDays(1),
          ).build(),
        ),
      )
        .extracting { it.isExpired }
        .isEqualTo(false)

      assertThat(
        OffenderAlertTransformer.transformForBooking(
          anAlert().toBuilder().expiryDate(
            LocalDate.now().plusYears(999),
          ).build(),
        ),
      )
        .extracting { it.isExpired }
        .isEqualTo(false)

      assertThat(
        OffenderAlertTransformer.transformForBooking(
          anAlert().toBuilder().expiryDate(
            LocalDate.now(),
          ).build(),
        ),
      )
        .extracting { it.isExpired }
        .isEqualTo(true)

      assertThat(
        OffenderAlertTransformer.transformForBooking(
          anAlert().toBuilder().expiryDate(
            LocalDate.now().minusDays(1),
          ).build(),
        ),
      )
        .extracting { it.isExpired }
        .isEqualTo(true)

      assertThat(
        OffenderAlertTransformer.transformForBooking(
          anAlert().toBuilder().expiryDate(
            LocalDate.now().minusYears(999),
          ).build(),
        ),
      )
        .extracting { it.isExpired }
        .isEqualTo(true)
    }

    @Test
    @DisplayName("bookingId is not copied")
    fun bookingIdIsNotCopied() {
      val offenderBooking = OffenderBooking
        .builder()
        .bookingId(99L)
        .offender(Offender.builder().nomsId("A1234JK").build())
        .build()
      val offenderAlert: OffenderAlert? = anAlert().toBuilder().offenderBooking(offenderBooking).build()

      assertThat(OffenderAlertTransformer.transformForBooking(offenderAlert))
        .extracting { it.bookingId }
        .isNull()
    }

    @Test
    @DisplayName("offender number is not copied")
    fun offenderNumberIsNotCopied() {
      val offenderBooking = OffenderBooking
        .builder()
        .bookingId(99L)
        .offender(Offender.builder().nomsId("A1234JK").build())
        .build()
      val offenderAlert: OffenderAlert? = anAlert().toBuilder().offenderBooking(offenderBooking).build()

      assertThat(OffenderAlertTransformer.transformForBooking(offenderAlert))
        .extracting { it.offenderNo }
        .isNull()
    }

    @Test
    @DisplayName("detailed information about who last updated or created the event is copied")
    fun detailedInformationAboutWhoLastUpdatedOrCreatedTheEventIsCopied() {
      val offenderAlert: OffenderAlert? = anAlert()
        .toBuilder()
        .createUser(
          StaffUserAccount
            .builder()
            .username("someuser")
            .staff(Staff.builder().firstName("JANE").lastName("BUBBLES").build())
            .build(),
        )
        .modifyUser(
          StaffUserAccount
            .builder()
            .username("someotheruser")
            .staff(Staff.builder().firstName("JACK").lastName("MATES").build())
            .build(),
        )
        .build()

      assertThat(OffenderAlertTransformer.transformForBooking(offenderAlert))
        .extracting { it.addedByFirstName }
        .isEqualTo("JANE")
      assertThat(OffenderAlertTransformer.transformForBooking(offenderAlert))
        .extracting { it.addedByLastName }
        .isEqualTo("BUBBLES")
      assertThat(OffenderAlertTransformer.transformForBooking(offenderAlert))
        .extracting { it.expiredByFirstName }
        .isEqualTo("JACK")
      assertThat(OffenderAlertTransformer.transformForBooking(offenderAlert))
        .extracting { it.expiredByLastName }
        .isEqualTo("MATES")
    }

    @Test
    @DisplayName("expired user is actually the last modified user and may not indicate who expired the alert")
    fun expiredUserIsActuallyTheLastModifiedUserAndMayNotIndicateWhoExpiredTheAlert() {
      val offenderAlert: OffenderAlert? = anAlert()
        .toBuilder()
        .expiryDate(null)
        .modifyUser(
          StaffUserAccount
            .builder()
            .username("someotheruser")
            .staff(Staff.builder().firstName("JACK").lastName("MATES").build())
            .build(),
        )
        .build()

      assertThat(OffenderAlertTransformer.transformForBooking(offenderAlert)).extracting { it.isExpired }.isEqualTo(false)
      assertThat(OffenderAlertTransformer.transformForBooking(offenderAlert))
        .extracting { it.expiredByFirstName }
        .isEqualTo("JACK")
      assertThat(OffenderAlertTransformer.transformForBooking(offenderAlert))
        .extracting { it.expiredByLastName }
        .isEqualTo("MATES")
    }
  }

  @Nested
  @TestInstance(PER_CLASS)
  internal inner class MapSortProperties {
    @ParameterizedTest
    @MethodSource("apiModelToEntityNames")
    @DisplayName("will map known values to entity field names")
    fun willMapKnownValuesToEntityFieldNames(apiName: String, expectedEntityNames: Array<String>) {
      assertThat(OffenderAlertTransformer.mapSortProperties(apiName))
        .containsExactly(*expectedEntityNames)
    }

    private fun apiModelToEntityNames(): Stream<Arguments> = Stream.of(
      Arguments.of("alertId", arrayOf("sequence")),
      Arguments.of("bookingId", arrayOf("offenderBooking.bookingId")),
      Arguments.of("alertType", arrayOf("alertType")),
      Arguments.of("alertCode", arrayOf("alertCode")),
      Arguments.of("dateCreated", arrayOf("alertDate")),
      Arguments.of("dateExpires", arrayOf("expiryDate")),
      Arguments.of("active", arrayOf("status")),
      Arguments.of("banana", arrayOf<String>()),
      Arguments.of(
        "active,bookingId,dateCreated",
        arrayOf("status", "offenderBooking.bookingId", "alertDate"),
      ),
      Arguments.of("active,banana,dateCreated", arrayOf("status", "alertDate")),
    )
  }

  companion object {
    private fun anAlert(): OffenderAlert = OffenderAlert
      .builder()
      .alertDate(LocalDate.parse("2020-01-30"))
      .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234JK").build()).build())
      .code(AlertCode("RSS", "Risk to Staff - Custody"))
      .alertCode("RSS")
      .comment("Do not trust this person")
      .createUser(
        StaffUserAccount
          .builder()
          .username("someuser")
          .staff(Staff.builder().firstName("JANE").lastName("BUBBLES").build())
          .build(),
      )
      .expiryDate(LocalDate.parse("2120-10-30"))
      .modifyUser(
        StaffUserAccount
          .builder()
          .username("someotheruser")
          .staff(Staff.builder().firstName("JACK").lastName("MATES").build())
          .build(),
      )
      .sequence(3)
      .type(AlertType("R", "Risk"))
      .alertType("R")
      .status("ACTIVE")
      .createDatetime(LocalDateTime.now().minusYears(10))
      .createUserId("someuser")
      .modifyDatetime(LocalDateTime.now().minusYears(1))
      .modifyUserId("someotheruser")
      .build()
  }
}
