package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;
import uk.gov.justice.hmpps.prison.service.BadRequestException;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.PrisonerReleaseAndTransferService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.http.HttpMethod.PUT;

@ContextConfiguration(classes = OffendersResourceCourtTransfersTest.TestClock.class)
public class OffendersResourceCourtTransfersTest extends ResourceTest {

    @TestConfiguration
    static class TestClock {
        private final LocalDateTime timeIs_2020_10_01T000000 = LocalDate.parse("2020-10-01", DateTimeFormatter.ISO_DATE).atStartOfDay();

        @Bean
        public Clock clock() {
            return Clock.fixed(timeIs_2020_10_01T000000.toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
        }
    }

    @MockBean
    PrisonerReleaseAndTransferService prisonerReleaseAndTransferService;

    private final String OFFENDER_NUMBER = "A1234AB";

    @Test
    public void updateCourtTransferInTest() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);
        final var prisonerNo = "A1180HI";
        final var now = LocalDateTime.now();
        final var courtReturnRequest = Map.of("agencyId", "MDI",
            "movementReasonCode", "CA",
            "commentText", "admitted",
            "dateTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        InmateDetail inmateDetail = Mockito.mock(InmateDetail.class);

        Mockito.when(prisonerReleaseAndTransferService.courtTransferIn(Mockito.eq(prisonerNo), Mockito.any(RequestForCourtTransferIn.class))).thenReturn(inmateDetail);


        final var courtReturnEntity = createHttpEntity(token, courtReturnRequest);

        final var transferInResponse = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/court-transfer-in",
            PUT,
            courtReturnEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThatStatus(transferInResponse, 200);
    }
    @Test
    public void updateCourtTransferInWhenInputDataCorruptedTest() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);
        final var prisonerNo = "A1180HI";
        final var now = LocalDateTime.now();
        final var courtReturnRequest = Map.of("agencyId", "MDI",
            "movementReasonCode", "CA",
            "commentText", "admitted",
            "dateTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        InmateDetail inmateDetail = Mockito.mock(InmateDetail.class);

        Mockito.when(prisonerReleaseAndTransferService.courtTransferIn(Mockito.eq(prisonerNo), Mockito.any(RequestForCourtTransferIn.class)))
            .thenThrow(new BadRequestException("Latest movement not a court movement"));

        final var courtReturnEntity = createHttpEntity(token, courtReturnRequest);

        final var transferInResponse = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/court-transfer-in",
            PUT,
            courtReturnEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThatStatus(transferInResponse, 400);
    }
    @Test
    public void testCourtTransferInWhenPrisonerNotFound() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);
        final var prisonerNo = "A1180HI";
        final var now = LocalDateTime.now();
        final var courtReturnRequest = Map.of("agencyId", "MDI",
            "movementReasonCode", "CA",
            "commentText", "admitted",
            "dateTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        InmateDetail inmateDetail = Mockito.mock(InmateDetail.class);

        Mockito.when(prisonerReleaseAndTransferService.courtTransferIn(Mockito.eq(prisonerNo), Mockito.any(RequestForCourtTransferIn.class)))
            .thenThrow(EntityNotFoundException.withMessage(format("No bookings found for prisoner number %s", prisonerNo)));

        final var courtReturnEntity = createHttpEntity(token, courtReturnRequest);

        final var transferInResponse = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/court-transfer-in",
            PUT,
            courtReturnEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThatStatus(transferInResponse, 404);
    }
}
