package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.RequestForTemporaryAbsenceArrival;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;
import uk.gov.justice.hmpps.prison.service.BadRequestException;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.PrisonerReleaseAndTransferService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;

@ContextConfiguration
public class OffendersResourceTemporaryAbsenceArrivalTest extends ResourceTest {

    @MockBean
    PrisonerReleaseAndTransferService prisonerReleaseAndTransferService;

    @Test
    public void updateTemporaryAbsenceArrivalTest() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);
        final var prisonerNo = "A1180HI";
        final var now = LocalDateTime.now();
        final var requestForTemporaryAbsenceArrival = Map.of("agencyId", "MDI",
            "movementReasonCode", "CA",
            "commentText", "admitted",
            "dateTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        InmateDetail inmateDetail = new InmateDetail();

        Mockito.when(prisonerReleaseAndTransferService.temporaryAbsenceArrival(Mockito.eq(prisonerNo), Mockito.any(RequestForTemporaryAbsenceArrival.class)))
            .thenReturn(inmateDetail);

        final var temporaryAbsenceEntity = createHttpEntity(token, requestForTemporaryAbsenceArrival);

        final var transferInResponse = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/temporary-absence-arrival",
            PUT,
            temporaryAbsenceEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );
        assertThat(transferInResponse.getStatusCodeValue()).isEqualTo(200);

    }

    @Test
    public void updateTemporaryAbsenceArrivalWhenInputDataCorruptedTest() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);
        final var prisonerNo = "A1180HI";
        final var now = LocalDateTime.now();
        final var requestForTemporaryAbsenceArrival = Map.of("agencyId", "MDI",
            "movementReasonCode", "CA",
            "commentText", "admitted",
            "dateTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        Mockito.when(prisonerReleaseAndTransferService.temporaryAbsenceArrival(Mockito.eq(prisonerNo), Mockito.any(RequestForTemporaryAbsenceArrival.class)))
            .thenThrow(new BadRequestException("Latest movement not a temporary absence movement"));

        final var temporaryAbsenceEntity = createHttpEntity(token, requestForTemporaryAbsenceArrival);

        final var transferInResponse = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/temporary-absence-arrival",
            PUT,
            temporaryAbsenceEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThat(transferInResponse.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void updateTemporaryAbsenceArrivalWhenPrisonerNotFound() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);
        final var prisonerNo = "A1180HI";
        final var now = LocalDateTime.now();
        final var requestForTemporaryAbsenceArrival = Map.of("agencyId", "MDI",
            "movementReasonCode", "CA",
            "commentText", "admitted",
            "dateTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        Mockito.when(prisonerReleaseAndTransferService.temporaryAbsenceArrival(Mockito.eq(prisonerNo), Mockito.any(RequestForTemporaryAbsenceArrival.class)))
            .thenThrow(EntityNotFoundException.withMessage(format("No bookings found for prisoner number %s", prisonerNo)));

        final var temporaryAbsenceEntity = createHttpEntity(token, requestForTemporaryAbsenceArrival);

        final var transferInResponse = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/temporary-absence-arrival",
            PUT,
            temporaryAbsenceEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );

        assertThat(transferInResponse.getStatusCodeValue()).isEqualTo(404);
    }
}
