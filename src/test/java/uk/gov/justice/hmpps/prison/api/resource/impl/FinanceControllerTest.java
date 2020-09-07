package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction;
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.InsertIntoOffenderTrans;
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.ProcessGlTransNew;

import java.util.List;

public class FinanceControllerTest extends ResourceTest {
    @MockBean
    private InsertIntoOffenderTrans insertIntoOffenderTrans;
    @MockBean
    private ProcessGlTransNew processGlTransNew;

    @Test
    public void transferToSavings() {
        final var transaction = createTransferTransaction(124L);

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);
        final var responseEntity = testRestTemplate.exchange("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
                HttpMethod.POST, requestEntity, String.class, "LEI", "A1234AA");

        assertThatJsonAndStatus(responseEntity, 200, "{\"debitTransaction\":{\"id\":\"12345-1\"},\"creditTransaction\":{\"id\":\"12345-2\"},\"transactionId\":\"12345\"}");
    }

    @Test
    public void transferToSavings_validatePrisonId() {
        final var transaction = createTransferTransaction(12345678L);

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);
        final var responseEntity = testRestTemplate.exchange("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
                HttpMethod.POST, requestEntity, String.class, "1234", "A1234AA");

        assertThatJsonAndStatus(responseEntity, 400,
                "{\"status\":400,\"userMessage\":\"transferToSavings.prisonId: Value is too long: max length is 3\",\"developerMessage\":\"transferToSavings.prisonId: Value is too long: max length is 3\"}");
    }

    @Test
    public void transferToSavings_validateOffenderNo() {
        final var transaction = createTransferTransaction(12345678L);

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);
        final var responseEntity = testRestTemplate.exchange("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
                HttpMethod.POST, requestEntity, String.class, "LEI", "123ABC");

        assertThatJsonAndStatus(responseEntity, 400,
                "{\"status\":400,\"userMessage\":\"transferToSavings.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'\",\"developerMessage\":\"transferToSavings.offenderNo: Value contains invalid characters: must match '[a-zA-Z][0-9]{4}[a-zA-Z]{2}'\"}");
    }

    @Test
    public void transferToSavings_validateTransferTransaction() {
        final var transaction = createTransferTransaction(0L);

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);
        final var responseEntity = testRestTemplate.exchange("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
                HttpMethod.POST, requestEntity, String.class, "LEI", "A1234AA");

        assertThatJsonAndStatus(responseEntity, 400,
                "{\"status\":400,\"userMessage\":\"Field: amount - The amount must be greater than 0\",\"developerMessage\":\"Field: amount - The amount must be greater than 0\"}");
    }

    @Test
    public void transferToSavings_wrongRole() {
        final var transaction = createTransferTransaction(1L);

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_BOB"), transaction);
        final var responseEntity = testRestTemplate.exchange("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
                HttpMethod.POST, requestEntity, String.class, "LEI", "A1234AA");

        assertThatJsonAndStatus(responseEntity, 403, "{\"status\":403,\"userMessage\":\"Access is denied\"}");
    }

    @NotNull
    private TransferTransaction createTransferTransaction(final long amount) {
        return TransferTransaction.builder()
                .amount(amount)
                .clientUniqueRef("clientRef")
                .description("desc")
                .clientTransactionId("transId")
                .build();
    }
}
