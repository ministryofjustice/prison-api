package uk.gov.justice.hmpps.prison.service.transformers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AlertCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AlertType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAlert;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.hmpps.prison.service.transformers.OffenderAlertTransformer.mapSortProperties;
import static uk.gov.justice.hmpps.prison.service.transformers.OffenderAlertTransformer.transformForBooking;
import static uk.gov.justice.hmpps.prison.service.transformers.OffenderAlertTransformer.transformForOffender;

class OffenderAlertTransformerTest {
    private static OffenderAlert anAlert() {
        return OffenderAlert
            .builder()
            .alertDate(LocalDate.parse("2020-01-30"))
            .offenderBooking(OffenderBooking.builder().offender(Offender.builder().nomsId("A1234JK").build()).build())
            .code(new AlertCode("RSS", "Risk to Staff - Custody"))
            .alertCode("RSS")
            .comment("Do not trust this person")
            .createUser(StaffUserAccount
                .builder()
                .username("someuser")
                .staff(Staff.builder().firstName("JANE").lastName("BUBBLES").build())
                .build())
            .expiryDate(LocalDate.parse("2120-10-30"))
            .modifyUser(StaffUserAccount
                .builder()
                .username("someotheruser")
                .staff(Staff.builder().firstName("JACK").lastName("MATES").build())
                .build())
            .sequence(3)
            .type(new AlertType("R", "Risk"))
            .alertType("R")
            .status("ACTIVE")
            .createDatetime(LocalDateTime.now().minusYears(10))
            .createUserId("someuser")
            .modifyDatetime(LocalDateTime.now().minusYears(1))
            .modifyUserId("someotheruser")
            .build();
    }

    private void assertCoreDataIsCopied(final Function<OffenderAlert, Alert> transformer) {
        final var entity = OffenderAlert
            .builder()
            .alertDate(LocalDate.parse("2020-01-30"))
            .offenderBooking(OffenderBooking
                .builder()
                .offender(Offender.builder().nomsId("A1234JK").build())
                .build())
            .code(new AlertCode("RSS", "Risk to Staff - Custody"))
            .alertCode("RSS")
            .comment("Do not trust this person")
            .createUser(StaffUserAccount
                .builder()
                .username("someuser")
                .staff(Staff.builder().firstName("JANE").lastName("BUBBLES").build())
                .build())
            .expiryDate(LocalDate.parse("2120-10-30"))
            .modifyUser(StaffUserAccount
                .builder()
                .username("someotheruser")
                .staff(Staff.builder().firstName("JACK").lastName("MATES").build())
                .build())
            .sequence(3)
            .type(new AlertType("R", "Risk"))
            .alertType("R")
            .status("ACTIVE")
            .createDatetime(LocalDateTime.now().minusYears(10))
            .createUserId("someuser")
            .modifyDatetime(LocalDateTime.now().minusYears(1))
            .modifyUserId("someotheruser")
            .build();

        final var alert = transformer.apply(entity);

        assertThat(alert.getAlertId()).isEqualTo(3);
        assertThat(alert.getAlertCode()).isEqualTo("RSS");
        assertThat(alert.getAlertType()).isEqualTo("R");
        assertThat(alert.getAlertCodeDescription()).isEqualTo("Risk to Staff - Custody");
        assertThat(alert.getAlertTypeDescription()).isEqualTo("Risk");
        assertThat(alert.getComment()).isEqualTo("Do not trust this person");
        assertThat(alert.isExpired()).isFalse();
        assertThat(alert.isActive()).isTrue();
        assertThat(alert.getDateCreated()).isEqualTo("2020-01-30");
        assertThat(alert.getDateExpires()).isEqualTo("2120-10-30");

    }
    private void assertBadCoreDataStillIsCopied(final Function<OffenderAlert, Alert> transformer) {
        final var entity = OffenderAlert
            .builder()
            .alertDate(LocalDate.parse("2020-01-30"))
            .offenderBooking(OffenderBooking
                .builder()
                .offender(Offender.builder().nomsId("A1234JK").build())
                .build())
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
            .build();

        final var alert = transformer.apply(entity);

        assertThat(alert.getAlertId()).isEqualTo(3);
        assertThat(alert.getAlertCode()).isEqualTo("RSS");
        assertThat(alert.getAlertType()).isEqualTo("R");
        assertThat(alert.getAlertCodeDescription()).isEqualTo("RSS");
        assertThat(alert.getAlertTypeDescription()).isEqualTo("R");
        assertThat(alert.getComment()).isNull();
        assertThat(alert.isExpired()).isFalse();
        assertThat(alert.isActive()).isFalse();
        assertThat(alert.getDateCreated()).isEqualTo("2020-01-30");
        assertThat(alert.getDateExpires()).isNull();

    }

    @Nested
    @DisplayName("transformForOffender")
    class TransformForOffender {
        @Test
        @DisplayName("will transform core alert data")
        void willTransformCoreAlertData() {
            assertCoreDataIsCopied(OffenderAlertTransformer::transformForOffender);
        }

        @Test
        @DisplayName("even bad alert data is copied as best we can")
        void evenBadAlertDataIsCopiedAsBestWeCan() {
            assertBadCoreDataStillIsCopied(OffenderAlertTransformer::transformForOffender);
        }

        @Test
        @DisplayName("date created is actually the alert date NOT the created date")
        void dateCreatedIsActuallyTheAlertDateNOTTheCreatedDate() {
            final var alert = anAlert()
                .toBuilder()
                .alertDate(LocalDate.parse("2020-01-30"))
                .createDatetime(LocalDateTime.parse("2021-04-30T12:00"))
                .build();

            assertThat(transformForOffender(alert).getDateCreated()).isEqualTo("2020-01-30");
        }

        @Test
        @DisplayName("expired is true when expiry date is present and is today or in the past")
        void expiredIsTrueWhenExpiryDateIsPresentAndIsTodayOrInThePast() {

            assertThat(transformForOffender(anAlert().toBuilder().expiryDate(null).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(false);

            assertThat(transformForOffender(anAlert().toBuilder().expiryDate(LocalDate.now().plusDays(1)).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(false);

            assertThat(transformForOffender(anAlert().toBuilder().expiryDate(LocalDate.now().plusYears(999)).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(false);

            assertThat(transformForOffender(anAlert().toBuilder().expiryDate(LocalDate.now()).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(true);

            assertThat(transformForOffender(anAlert().toBuilder().expiryDate(LocalDate.now().minusDays(1)).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(true);

            assertThat(transformForOffender(anAlert().toBuilder().expiryDate(LocalDate.now().minusYears(999)).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(true);

        }

        @Test
        @DisplayName("bookingId is copied")
        void bookingIdIsCopied() {
            final var offenderBooking = OffenderBooking
                .builder()
                .bookingId(99L)
                .offender(Offender.builder().nomsId("A1234JK").build())
                .build();
            final var offenderAlert = anAlert().toBuilder().offenderBooking(offenderBooking).build();

            assertThat(transformForOffender(offenderAlert))
                .extracting(Alert::getBookingId)
                .isEqualTo(99L);
        }

        @Test
        @DisplayName("offender number is copied")
        void offenderNumberIsCopied() {
            final var offenderBooking = OffenderBooking
                .builder()
                .bookingId(99L)
                .offender(Offender.builder().nomsId("A1234JK").build())
                .build();
            final var offenderAlert = anAlert().toBuilder().offenderBooking(offenderBooking).build();

            assertThat(transformForOffender(offenderAlert))
                .extracting(Alert::getOffenderNo)
                .isEqualTo("A1234JK");
        }

        @Test
        @DisplayName("detailed information about who last updated or created the event is not copied")
        void detailedInformationAboutWhoLastUpdatedOrCreatedTheEventIsNotCopied() {
            assertThat(transformForOffender(anAlert()))
                .extracting(Alert::getAddedByFirstName)
                .isNull();
            assertThat(transformForOffender(anAlert()))
                .extracting(Alert::getAddedByLastName)
                .isNull();
            assertThat(transformForOffender(anAlert()))
                .extracting(Alert::getExpiredByFirstName)
                .isNull();
            assertThat(transformForOffender(anAlert()))
                .extracting(Alert::getExpiredByLastName)
                .isNull();
        }


    }

    @Nested
    @DisplayName("transformForBooking")
    class TransformForBooking {
        @Test
        @DisplayName("will transform core alert data")
        void willTransformCoreAlertData() {
            assertCoreDataIsCopied(OffenderAlertTransformer::transformForBooking);
        }

        @Test
        @DisplayName("even bad alert data is copied as best we can")
        void evenBadAlertDataIsCopiedAsBestWeCan() {
            assertBadCoreDataStillIsCopied(OffenderAlertTransformer::transformForBooking);
        }

        @Test
        @DisplayName("date created is actually the alert date NOT the created date")
        void dateCreatedIsActuallyTheAlertDateNOTTheCreatedDate() {
            final var alert = anAlert()
                .toBuilder()
                .alertDate(LocalDate.parse("2020-01-30"))
                .createDatetime(LocalDateTime.parse("2021-04-30T12:00"))
                .build();

            assertThat(transformForBooking(alert).getDateCreated()).isEqualTo("2020-01-30");
        }

        @Test
        @DisplayName("expired is true when expiry date is present and is today or in the past")
        void expiredIsTrueWhenExpiryDateIsPresentAndIsTodayOrInThePast() {

            assertThat(transformForBooking(anAlert().toBuilder().expiryDate(null).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(false);

            assertThat(transformForBooking(anAlert().toBuilder().expiryDate(LocalDate.now().plusDays(1)).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(false);

            assertThat(transformForBooking(anAlert().toBuilder().expiryDate(LocalDate.now().plusYears(999)).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(false);

            assertThat(transformForBooking(anAlert().toBuilder().expiryDate(LocalDate.now()).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(true);

            assertThat(transformForBooking(anAlert().toBuilder().expiryDate(LocalDate.now().minusDays(1)).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(true);

            assertThat(transformForBooking(anAlert().toBuilder().expiryDate(LocalDate.now().minusYears(999)).build()))
                .extracting(Alert::isExpired)
                .isEqualTo(true);

        }

        @Test
        @DisplayName("bookingId is not copied")
        void bookingIdIsNotCopied() {
            final var offenderBooking = OffenderBooking
                .builder()
                .bookingId(99L)
                .offender(Offender.builder().nomsId("A1234JK").build())
                .build();
            final var offenderAlert = anAlert().toBuilder().offenderBooking(offenderBooking).build();

            assertThat(transformForBooking(offenderAlert))
                .extracting(Alert::getBookingId)
                .isNull();
        }

        @Test
        @DisplayName("offender number is not copied")
        void offenderNumberIsNotCopied() {
            final var offenderBooking = OffenderBooking
                .builder()
                .bookingId(99L)
                .offender(Offender.builder().nomsId("A1234JK").build())
                .build();
            final var offenderAlert = anAlert().toBuilder().offenderBooking(offenderBooking).build();

            assertThat(transformForBooking(offenderAlert))
                .extracting(Alert::getOffenderNo)
                .isNull();
        }

        @Test
        @DisplayName("detailed information about who last updated or created the event is copied")
        void detailedInformationAboutWhoLastUpdatedOrCreatedTheEventIsCopied() {
            final var offenderAlert = anAlert()
                .toBuilder()
                .createUser(StaffUserAccount
                    .builder()
                    .username("someuser")
                    .staff(Staff.builder().firstName("JANE").lastName("BUBBLES").build())
                    .build())
                .modifyUser(StaffUserAccount
                    .builder()
                    .username("someotheruser")
                    .staff(Staff.builder().firstName("JACK").lastName("MATES").build())
                    .build())
                .build();

            assertThat(transformForBooking(offenderAlert))
                .extracting(Alert::getAddedByFirstName)
                .isEqualTo("JANE");
            assertThat(transformForBooking(offenderAlert))
                .extracting(Alert::getAddedByLastName)
                .isEqualTo("BUBBLES");
            assertThat(transformForBooking(offenderAlert))
                .extracting(Alert::getExpiredByFirstName)
                .isEqualTo("JACK");
            assertThat(transformForBooking(offenderAlert))
                .extracting(Alert::getExpiredByLastName)
                .isEqualTo("MATES");
        }

        @Test
        @DisplayName("expired user is actually the last modified user and may not indicate who expired the alert")
        void expiredUserIsActuallyTheLastModifiedUserAndMayNotIndicateWhoExpiredTheAlert() {
            final var offenderAlert = anAlert()
                .toBuilder()
                .expiryDate(null)
                .modifyUser(StaffUserAccount
                    .builder()
                    .username("someotheruser")
                    .staff(Staff.builder().firstName("JACK").lastName("MATES").build())
                    .build())
                .build();

            assertThat(transformForBooking(offenderAlert)).extracting(Alert::isExpired).isEqualTo(false);
            assertThat(transformForBooking(offenderAlert))
                .extracting(Alert::getExpiredByFirstName)
                .isEqualTo("JACK");
            assertThat(transformForBooking(offenderAlert))
                .extracting(Alert::getExpiredByLastName)
                .isEqualTo("MATES");

        }
    }

    @Nested
    class MapSortProperties {
        @ParameterizedTest
        @MethodSource("apiModelToEntityNames")
        @DisplayName("will map known values to entity field names")
        void willMapKnownValuesToEntityFieldNames(String apiName, String[] expectedEntityNames) {
            assertThat(mapSortProperties(apiName)).containsExactly(expectedEntityNames);
        }

        private static Stream<Arguments> apiModelToEntityNames() {
            return Stream.of(
                Arguments.of("alertId", new String[]{"sequence"}),
                Arguments.of("bookingId", new String[]{"offenderBooking.bookingId"}),
                Arguments.of("alertType", new String[]{"alertType"}),
                Arguments.of("alertCode", new String[]{"alertCode"}),
                Arguments.of("dateCreated", new String[]{"alertDate"}),
                Arguments.of("dateExpires", new String[]{"expiryDate"}),
                Arguments.of("active", new String[]{"status"}),
                Arguments.of("banana", new String[]{}),
                Arguments.of("active,bookingId,dateCreated", new String[]{"status", "offenderBooking.bookingId", "alertDate"}),
                Arguments.of("active,banana,dateCreated", new String[]{"status", "alertDate"})
            );
        }
    }
}
