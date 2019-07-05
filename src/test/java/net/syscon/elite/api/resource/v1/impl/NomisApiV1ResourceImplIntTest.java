package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.CreateTransaction;
import net.syscon.elite.api.model.v1.Hold;
import net.syscon.elite.api.resource.impl.ResourceTest;
import net.syscon.elite.repository.v1.model.HoldSP;
import net.syscon.elite.repository.v1.storedprocs.FinanceProcs.*;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.syscon.elite.repository.v1.storedprocs.FinanceProcs.*;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.P_HOLDS_CSR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.core.ResolvableType.forType;

public class NomisApiV1ResourceImplIntTest extends ResourceTest {
    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        public PostTransaction postTransaction() {
            return Mockito.mock(PostTransaction.class);
        }

        @Bean
        @Primary
        public PostTransfer postTransfer() {
            return Mockito.mock(PostTransfer.class);
        }

        @Bean
        @Primary
        public GetHolds getHolds() {
            return Mockito.mock(GetHolds.class);
        }
    }

    @Autowired
    private PostTransaction postTransaction;

    @Autowired
    private PostTransfer postTransfer;

    @Autowired
    private GetHolds getHolds;

    @Test
    public void transferTransaction() {
        final var transaction = new CreateTransaction();
        transaction.setAmount(1234L);
        transaction.setClientUniqueRef("clientRef");
        transaction.setDescription("desc");
        transaction.setType("type");
        transaction.setClientTransactionId("transId");

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);

        when(postTransfer.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_TXN_ID, "someId", P_TXN_ENTRY_SEQ, "someSeq", P_CURRENT_AGY_DESC, "someDesc", P_CURRENT_AGY_LOC_ID, "someLoc"));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transfer_transactions", HttpMethod.POST, requestEntity, String.class);

        assertThatJson(responseEntity.getBody()).isEqualTo("{current_location: {code: \"someLoc\", desc: \"someDesc\"}, transaction: {id:\"someId-someSeq\"}}");
    }

    @Test
    public void createTransaction() {
        final var transaction = new CreateTransaction();
        transaction.setAmount(1234L);
        transaction.setClientUniqueRef("clientRef");
        transaction.setDescription("desc");
        transaction.setType("type");
        transaction.setClientTransactionId("transId");

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);

        when(postTransaction.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_TXN_ID, "someId", P_TXN_ENTRY_SEQ, "someSeq"));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transactions", HttpMethod.POST, requestEntity, String.class);

        assertThatJson(responseEntity.getBody()).isEqualTo("{id:\"someId-someSeq\"}");
    }

    @Test
    public void getHolds() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var holds = List.of(
                new HoldSP(3L, "ref", "12345", "entry", null, new BigDecimal("123.45"), null),
                new HoldSP(4L, "ref2", "12346", "entry2", LocalDate.of(2019, 1, 2), new BigDecimal("123.46"), LocalDate.of(2018, 12, 30))
        );

        when(getHolds.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_HOLDS_CSR, holds));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/holds", HttpMethod.GET, requestEntity, String.class);

        //noinspection ConstantConditions
        assertThat(new JsonContent<Hold>(getClass(), forType(Hold.class), responseEntity.getBody())).isEqualToJson("holds.json");
    }

    @Test
    public void getHoldsWithClientReference() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", List.of("ROLE_NOMIS_API_V1"), Map.of("X-Client-Name", "some-client"));

        final var holds = List.of(
                new HoldSP(3L, "ref", "12345", "entry", null, new BigDecimal("123.45"), null),
                new HoldSP(4L, "some-client-ref2", "12346", "entry2", LocalDate.of(2019, 1, 2), new BigDecimal("123.46"), LocalDate.of(2018, 12, 30))
        );

        final var captor = ArgumentCaptor.forClass(SqlParameterSource.class);
        when(getHolds.execute(captor.capture())).thenReturn(Map.of(P_HOLDS_CSR, holds));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/holds?client_unique_ref=some-reference", HttpMethod.GET, requestEntity, String.class);

        //noinspection ConstantConditions
        assertThat(new JsonContent<Hold>(getClass(), forType(Hold.class), responseEntity.getBody())).isEqualToJson("holds.json");

        assertThat(captor.getValue().getValue(P_CLIENT_UNIQUE_REF)).isEqualTo("some-client-some-reference");
    }
}
