package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;
import uk.gov.justice.hmpps.prison.service.BadRequestException;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.receiveandtransfer.PrisonTransferService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;

@ContextConfiguration
public class OffendersResourceCourtTransfersTest extends ResourceTest {

    @MockBean
    PrisonTransferService prisonTransferService;

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
        InmateDetail inmateDetail = new InmateDetail();

        Mockito.when(prisonTransferService.transferViaCourt(Mockito.eq(prisonerNo), Mockito.any(RequestForCourtTransferIn.class)))
            .thenReturn(inmateDetail);

        final var courtReturnEntity = createHttpEntity(token, courtReturnRequest);

        final var transferInResponse = testRestTemplate.exchange(
            "/api/offenders/{nomsId}/court-transfer-in",
            PUT,
            courtReturnEntity,
            new ParameterizedTypeReference<String>() {
            },
            prisonerNo
        );
        assertThat(transferInResponse.getStatusCodeValue()).isEqualTo(200);

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

        Mockito.when(prisonTransferService.transferViaCourt(Mockito.eq(prisonerNo), Mockito.any(RequestForCourtTransferIn.class)))
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

        assertThat(transferInResponse.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void updateCourtTransferInWhenPrisonerNotFound() {
        final var token = authTokenHelper.getToken(AuthToken.CREATE_BOOKING_USER);
        final var prisonerNo = "A1180HI";
        final var now = LocalDateTime.now();
        final var courtReturnRequest = Map.of("agencyId", "MDI",
            "movementReasonCode", "CA",
            "commentText", "admitted",
            "dateTime", now.minusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        Mockito.when(prisonTransferService.transferViaCourt(Mockito.eq(prisonerNo), Mockito.any(RequestForCourtTransferIn.class)))
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

        assertThat(transferInResponse.getStatusCodeValue()).isEqualTo(404);
    }
}
