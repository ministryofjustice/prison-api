package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import uk.gov.justice.hmpps.prison.api.model.CreateExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.MovementsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class MovementResourceIntTest_createExternalMovement extends ResourceTest {

    @MockBean
    private MovementsService movementsService;

    @Nested
    class CreateExternalMovements {
        @Test
        public void createExternalMovement() throws Exception {
            makeRequest().expectStatus().isCreated();

            verify(movementsService).createExternalMovement(1134751L, CreateExternalMovement.builder()
                .bookingId(1134751L)
                .fromAgencyId("HAZLWD")
                .toAgencyId("OUT")
                .movementTime(LocalDateTime.parse("2020-02-28T14:40:00"))
                .movementType("TRN")
                .movementReason("SEC")
                .directionCode(MovementDirection.OUT)
                .build());
        }

        @Test
        public void returnsNotAuthorised() throws Exception {
            webTestClient.post()
                .uri("/api/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                       "bookingId": 1134751,
                       "fromAgencyId": "HAZLWD",
                       "toAgencyId": "OUT",
                       "movementTime": "2020-02-28T14:40:00",
                       "movementType": "TRN",
                       "movementReason": "SEC",
                       "directionCode": "OUT"
                    }
                    """)
                .exchange()
                .expectStatus().isUnauthorized();
        }

        @Test
        public void returnsNotFound() throws Exception {
            when(movementsService.createExternalMovement(anyLong(), any())).thenThrow(EntityNotFoundException.class);

            makeRequest().expectStatus().isNotFound();
        }

        @Test
        public void returnsBadRequest() throws Exception {
            when(movementsService.createExternalMovement(anyLong(), any())).thenThrow(IllegalStateException.class);

            makeRequest().expectStatus().isBadRequest();
        }

        @Test
        public void returnsInternalServerError() throws Exception {
            when(movementsService.createExternalMovement(anyLong(), any())).thenThrow(RuntimeException.class);

            makeRequest().expectStatus().is5xxServerError();
        }

        @Test
        public void handlesMissingFields() throws Exception {
            webTestClient.post()
                .uri("/api/movements")
                .headers(setAuthorisation(List.of("ROLE_INACTIVE_BOOKINGS")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                       "bookingId": 1134751
                    }
                    """)
                .exchange().expectStatus().isBadRequest();
        }

        private ResponseSpec makeRequest() throws Exception {
            return webTestClient.post()
                .uri("/api/movements")
                .headers(setAuthorisation(List.of("ROLE_INACTIVE_BOOKINGS")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                       "bookingId": 1134751,
                       "fromAgencyId": "HAZLWD",
                       "toAgencyId": "OUT",
                       "movementTime": "2020-02-28T14:40:00",
                       "movementType": "TRN",
                       "movementReason": "SEC",
                       "directionCode": "OUT"
                    }
                    """)
                .exchange();
        }
    }
}
