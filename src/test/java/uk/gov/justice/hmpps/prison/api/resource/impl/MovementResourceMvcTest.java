package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.justice.hmpps.prison.api.model.CreateExternalMovement;
import uk.gov.justice.hmpps.prison.api.resource.MovementResource;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.MovementsService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MovementResource.class)
public class MovementResourceMvcTest extends TestController {

    @MockBean
    private MovementsService movementsService;

    @Nested
    class CreateExternalMovements {
        @Test
        @WithMockUser(username = "ITAG_USER")
        public void createExternalMovement() throws Exception {
            makeRequest().andExpect(status().isCreated());

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
            makeRequest().andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "ITAG_USER")
        public void returnsNotFound() throws Exception {
            when(movementsService.createExternalMovement(anyLong(), any())).thenThrow(EntityNotFoundException.class);

            makeRequest().andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "ITAG_USER")
        public void returnsBadRequest() throws Exception {
            when(movementsService.createExternalMovement(anyLong(), any())).thenThrow(IllegalStateException.class);

            makeRequest().andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "ITAG_USER")
        public void returnsInternalServerError() throws Exception {
            when(movementsService.createExternalMovement(anyLong(), any())).thenThrow(RuntimeException.class);

            makeRequest().andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(username = "ITAG_USER")
        public void handlesMissingFields() throws Exception {
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/movements")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                           "bookingId": 1134751,
                        }
                        """)
            ).andExpect(status().isBadRequest());
        }

        private ResultActions makeRequest() throws Exception {
            return mockMvc.perform(
                MockMvcRequestBuilders.post("/api/movements")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
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
            );
        }
    }
}
