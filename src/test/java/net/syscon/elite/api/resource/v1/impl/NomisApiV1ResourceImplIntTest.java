package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.CreateTransaction;
import net.syscon.elite.api.resource.impl.ResourceTest;
import net.syscon.elite.repository.v1.storedprocs.FinanceProcs.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.syscon.elite.repository.v1.storedprocs.FinanceProcs.*;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    }

    @Autowired
    private PostTransaction postTransaction;

    @Autowired
    private PostTransfer postTransfer;

    @Test
    public void transferTransaction() {
        final var transaction = new CreateTransaction();
        transaction.setAmount(1234L);
        transaction.setClientUniqueRef("clientRef");
        transaction.setDescription("desc");
        transaction.setType("type");
        transaction.setClientTransactionId("transId");

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);

        when(postTransfer.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_TXN_ID, "someId", P_TXN_ENTRY_SEQ, "someSeq", P_CURRENT_AGY_DESC, "someDesc", P_CURRENT_AGY_LOC_ID, "someLoc"));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transfer_transactions", HttpMethod.POST, requestEntity, String.class);

        if (responseEntity.getStatusCodeValue() >= 300) {
            fail("Failed to call api, response is " + responseEntity.getBody());
            return;
        }

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

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);

        when(postTransaction.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_TXN_ID, "someId", P_TXN_ENTRY_SEQ, "someSeq"));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transactions", HttpMethod.POST, requestEntity, String.class);

        if (responseEntity.getStatusCodeValue() >= 300) {
            fail("Failed to call api, response is " + responseEntity.getBody());
            return;
        }

        assertThatJson(responseEntity.getBody()).isEqualTo("{id:\"someId-someSeq\"}");
    }
}
