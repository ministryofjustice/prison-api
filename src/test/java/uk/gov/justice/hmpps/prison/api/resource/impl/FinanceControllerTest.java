package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.TransferTransaction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionRepository;
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.InsertIntoOffenderTrans;
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.ProcessGlTransNew;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class FinanceControllerTest extends ResourceTest {
    @MockBean
    private InsertIntoOffenderTrans insertIntoOffenderTrans;
    @MockBean
    private ProcessGlTransNew processGlTransNew;
    @MockBean
    private OffenderTransactionRepository offenderTransactionRepository;

    @Test
    public void transferToSavingsNomisV1Role() {
        when(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L);
        when(offenderTransactionRepository.findById(any()))
                .thenReturn(Optional.of(OffenderTransaction.builder().build()))
                .thenReturn(Optional.of(OffenderTransaction.builder().build()));
        final var transaction = createTransferTransaction(124L);

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);
        final var responseEntity = testRestTemplate.exchange("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
                HttpMethod.POST, requestEntity, String.class, "LEI", "A1234AA");

        assertThatJsonAndStatus(responseEntity, 200, "{\"debitTransaction\":{\"id\":\"12345-1\"},\"creditTransaction\":{\"id\":\"12345-2\"},\"transactionId\":12345}");
    }

    @Test
    public void transferToSavingsUnilinkRole() {
        when(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L);
        when(offenderTransactionRepository.findById(any()))
                .thenReturn(Optional.of(OffenderTransaction.builder().build()))
                .thenReturn(Optional.of(OffenderTransaction.builder().build()));
        final var transaction = createTransferTransaction(124L);

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("UNILINK"), transaction);
        final var responseEntity = testRestTemplate.exchange("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
                HttpMethod.POST, requestEntity, String.class, "LEI", "A1234AA");

        assertThatJsonAndStatus(responseEntity, 200, "{\"debitTransaction\":{\"id\":\"12345-1\"},\"creditTransaction\":{\"id\":\"12345-2\"},\"transactionId\":12345}");
    }

    @Test
    public void transferToSavings_setXClientNameHeader() {
        when(offenderTransactionRepository.getNextTransactionId()).thenReturn(12345L);
        final var transaction1 = OffenderTransaction.builder().build();
        when(offenderTransactionRepository.findById(any()))
                .thenReturn(Optional.of(transaction1))
                .thenReturn(Optional.of(OffenderTransaction.builder().build()));
        final var transaction = createTransferTransaction(124L);

        final var jwt = createJwt("ITAG_USER", List.of("ROLE_NOMIS_API_V1"));
        final var requestEntity = createHttpEntity(jwt, transaction, Map.of("X-Client-Name", "clientName"));
        final var responseEntity = testRestTemplate.exchange("/api/finance/prison/{prisonId}/offenders/{offenderNo}/transfer-to-savings",
                HttpMethod.POST, requestEntity, String.class, "LEI", "A1234AA");

        assertThatJsonAndStatus(responseEntity, 200, "{\"debitTransaction\":{\"id\":\"12345-1\"},\"creditTransaction\":{\"id\":\"12345-2\"},\"transactionId\":12345}");

        assertThat(transaction1.getClientUniqueRef()).isEqualTo("clientName-clientRef");
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

        assertThatJsonAndStatus(responseEntity, 403, "{\"status\":403,\"userMessage\":\"Access Denied\"}");
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
