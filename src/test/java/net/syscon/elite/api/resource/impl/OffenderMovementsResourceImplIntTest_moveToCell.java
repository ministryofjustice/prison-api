package net.syscon.elite.api.resource.impl;

import net.syscon.elite.repository.jpa.repository.BedAssignmentHistoriesRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.service.BedAssignmentHistoryService;
import net.syscon.elite.util.JwtParameters;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class OffenderMovementsResourceImplIntTest_moveToCell extends ResourceTest {

    @Autowired
    private OffenderBookingRepository offenderBookingRepository;
    @Autowired
    private BedAssignmentHistoriesRepository bedAssignmentHistoriesRepository;
    @Autowired
    private Clock clock;
    @SpyBean
    private BedAssignmentHistoryService bedAssignmentHistoryService;

    @Configuration
    class TestConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.now(), ZoneId.systemDefault());
        }
    }

    private static final Long BOOKING_ID = -33L;
    private static final String BOOKING_ID_S = "-33";

    private static final Long INITIAL_CELL = -14L;
    private static final String INITIAL_CELL_S = "-14";
    private static final String INITIAL_REASON = "ADM";
    private static final LocalDateTime INITIAL_DATE_TIME = LocalDateTime.now().minusDays(1);

    private static final Long NEW_CELL = -4L;
    private static final Long CELL_DIFF_PRISON = -41L;
    private static final String NEW_CELL_S = "-4";
    private static final String CELL_DIFF_PRISON_S = "-41";

    @After
    public void tearDown() {
        // Return the offender back to his original cell as configured in the test data in R__3_6_1__OFFENDER_BOOKINGS.sql
        requestMoveToCell(validToken(), BOOKING_ID_S, INITIAL_CELL_S, INITIAL_REASON, INITIAL_DATE_TIME.format(ISO_LOCAL_DATE_TIME));
    }

    @Test
    public void validRequest() {
        final var dateTime = LocalDateTime.now().minusHours(1);

        final var response = requestMoveToCell(validToken(), BOOKING_ID_S, NEW_CELL_S, "BEH", dateTime.format(ISO_LOCAL_DATE_TIME));

        verifySuccessResponse(response, BOOKING_ID, NEW_CELL);
        verifyOffenderBookingLivingUnit(BOOKING_ID, NEW_CELL);
        verifyLastBedAssignmentHistory(BOOKING_ID, NEW_CELL, "BEH", dateTime);
    }

    @Test
    public void missingDate_defaultsToNow() {
        final var expectedDateTime = clock.instant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        final var response = requestMoveToCell(validToken(), BOOKING_ID_S, NEW_CELL_S, "BEH", "");

        verifySuccessResponse(response, BOOKING_ID, NEW_CELL);
        verifyOffenderBookingLivingUnit(BOOKING_ID, NEW_CELL);
        verifyLastBedAssignmentHistory(BOOKING_ID, NEW_CELL, "BEH", expectedDateTime);
    }

    @Test
    public void backToOriginalCell() {
        final var dateTime = LocalDateTime.now().minusHours(1);
        final var moveBackDateTime = LocalDateTime.now().minusMinutes(1);

        requestMoveToCell(validToken(), BOOKING_ID_S, NEW_CELL_S, "BEH", dateTime.format(ISO_LOCAL_DATE_TIME));
        verifyOffenderBookingLivingUnit(BOOKING_ID, NEW_CELL);
        verifyLastBedAssignmentHistory(BOOKING_ID, NEW_CELL, "BEH", dateTime);

        final var response = requestMoveToCell(validToken(), BOOKING_ID_S, INITIAL_CELL_S, "CON", moveBackDateTime.format(ISO_LOCAL_DATE_TIME));
        verifySuccessResponse(response, BOOKING_ID, INITIAL_CELL);
        verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL);
        verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL, "CON", moveBackDateTime);
    }

    @Test
    public void noChange_notUpdated() {
        final var dateTime = LocalDateTime.now().minusHours(1);

        final var response = requestMoveToCell(validToken(), BOOKING_ID_S, INITIAL_CELL_S, "BEH", dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME));

        verifySuccessResponse(response, BOOKING_ID, INITIAL_CELL);
        verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL);
        verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL, INITIAL_REASON, INITIAL_DATE_TIME);
    }

    @Test
    public void notFound() {
        final var dateTime = LocalDateTime.now().minusHours(1);
        final var unknownCell = "-69854";

        final var response = requestMoveToCell(validToken(), unknownCell, NEW_CELL_S, "BEH", dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME));

        verifyErrorResponse(response, NOT_FOUND, unknownCell);
        verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL);
        verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL, INITIAL_REASON, INITIAL_DATE_TIME);
    }

    @Test
    public void noBookingAccess_notFound() {
        final var dateTime = LocalDateTime.now().minusHours(1);

        final var response = requestMoveToCell(differentAgencyToken(), BOOKING_ID_S, NEW_CELL_S, "BEH", dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME));

        verifyErrorResponse(response, NOT_FOUND, BOOKING_ID_S);
        verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL);
        verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL, INITIAL_REASON, INITIAL_DATE_TIME);
    }

    @Test
    public void userRedOnly_forbidden() {
        final var dateTime = LocalDateTime.now().minusHours(1);

        final var response = requestMoveToCell(readOnlyToken(), BOOKING_ID_S, NEW_CELL_S, "BEH", dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME));

        verifyErrorResponse(response, FORBIDDEN, null);
        verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL);
        verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL, INITIAL_REASON, INITIAL_DATE_TIME);
    }

    @Test
    public void offenderInDifferentPrison_badRequest() {
        final var dateTime = LocalDateTime.now().minusHours(1);

        final var response = requestMoveToCell(validToken(), BOOKING_ID_S, CELL_DIFF_PRISON_S, "BEH", dateTime.plusMinutes(1).format(ISO_LOCAL_DATE_TIME));

        verifyErrorResponse(response, BAD_REQUEST, "MDI", "LEI");
        verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL);
        verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL, INITIAL_REASON, INITIAL_DATE_TIME);
    }

    @Test
    @WithMockUser(username = "ITAG_USER", authorities = "SCOPE_write") // Required because stubbing the BedAssignmentHistoryService means we don't pick up the usual Authentication from Spring AOP.
    public void transactionRolledBack() {
        final var dateTime = LocalDateTime.now().minusHours(1);

        doThrow(RuntimeException.class).when(bedAssignmentHistoryService).add(BOOKING_ID, NEW_CELL, "BEH", dateTime);
        final var response = requestMoveToCell(validToken(), BOOKING_ID_S, NEW_CELL_S, "BEH", dateTime.format(ISO_LOCAL_DATE_TIME));

        verifyErrorResponse(response, INTERNAL_SERVER_ERROR, null);
        verifyOffenderBookingLivingUnit(BOOKING_ID, INITIAL_CELL);
        verifyLastBedAssignmentHistory(BOOKING_ID, INITIAL_CELL, INITIAL_REASON, INITIAL_DATE_TIME);
    }

    private String validToken() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of())
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String readOnlyToken() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("ITAG_USER")
                        .scope(List.of("read"))
                        .roles(List.of())
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private String differentAgencyToken() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("WAI_USER")
                        .scope(List.of("read", "write"))
                        .roles(List.of())
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

    private ResponseEntity<String> requestMoveToCell(final String bearerToken, final String bookingId, final String livingUnitId, final String reasonCode, final String dateTime) {
        final var entity = createHttpEntity(bearerToken, null);
        return testRestTemplate.exchange(
                format("/api/bookings/%s/living-unit/%s?reasonCode=%s&dateTime=%s", bookingId, livingUnitId, reasonCode, dateTime),
                PUT,
                entity,
                new ParameterizedTypeReference<>() {
                });

    }

    private void verifySuccessResponse(final ResponseEntity<String> response, final Long bookingId, final Long internalLocationId) {
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.bookingId").isEqualTo(bookingId.intValue());
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.assignedLivingUnitId").isEqualTo(internalLocationId.intValue());
    }

    private void verifyErrorResponse(final ResponseEntity<String> response, final HttpStatus status, final String... partialMessages) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.status").isEqualTo(status.value());
        if (partialMessages != null) {
            Arrays.stream(partialMessages).forEach(partialMessage ->
                    assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.userMessage").contains(partialMessage)
            );
        }
    }

    private void verifyOffenderBookingLivingUnit(final Long bookingId, final Long livingUnitId) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow();
        assertThat(offenderBooking.getAssignedLivingUnitId()).isEqualTo(livingUnitId);
    }

    private void verifyLastBedAssignmentHistory(final Long bookingId, final Long livingUnitId, final String reason, final LocalDateTime dateTime) {
        final var bedAssignmentHistories = bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(bookingId);
        assertThat(bedAssignmentHistories.get(bedAssignmentHistories.size() - 1))
                .extracting("offenderBooking.bookingId", "livingUnitId", "assignmentReason", "assignmentDate", "assignmentDateTime")
                .containsExactlyInAnyOrder(bookingId, livingUnitId, reason, dateTime.toLocalDate(), dateTime.withNano(0));
    }

}
