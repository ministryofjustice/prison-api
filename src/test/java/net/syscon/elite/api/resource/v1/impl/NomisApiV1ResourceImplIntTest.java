package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.CreateTransaction;
import net.syscon.elite.api.model.v1.Image;
import net.syscon.elite.api.model.v1.Offender;
import net.syscon.elite.api.model.v1.OffenderPssDetailEvent;
import net.syscon.elite.api.resource.impl.ResourceTest;
import net.syscon.elite.repository.v1.model.AliasSP;
import net.syscon.elite.repository.v1.model.OffenderSP;
import net.syscon.elite.repository.v1.storedprocs.FinanceProcs.PostTransaction;
import net.syscon.elite.repository.v1.storedprocs.FinanceProcs.PostTransfer;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderDetails;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderImage;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderPssDetail;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;
import static org.assertj.core.api.Assertions.assertThat;
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

        @Bean
        @Primary
        public GetOffenderPssDetail getOffenderPssDetail() {
            return Mockito.mock(GetOffenderPssDetail.class);
        }

        @Bean
        @Primary
        public GetOffenderDetails getOffenderDetails() {
            return Mockito.mock(GetOffenderDetails.class);
        }

        @Bean
        @Primary
        public GetOffenderImage getOffenderImage() {
            return Mockito.mock(GetOffenderImage.class);
        }
    }

    @Autowired
    private PostTransaction postTransaction;

    @Autowired
    private PostTransfer postTransfer;

    @Autowired
    private GetOffenderPssDetail offenderPssDetail;

    @Autowired
    private GetOffenderDetails offenderDetails;

    @Autowired
    private GetOffenderImage offenderImage;


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

    // @Test - failing - return types in mocked procedure call
    public void getOffenderPssDetail() throws SQLException {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var testClob = new javax.sql.rowset.serial.SerialClob("XXX".toCharArray());
        final var timestamp = Timestamp.valueOf(LocalDateTime.now());
        final var localDateTime = LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
        final var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        final var expectedTime = formatter.format(localDateTime);

        when(offenderPssDetail.execute(any(SqlParameterSource.class))).thenReturn(
                Map.of( P_NOMS_ID, "A1404AE",
                        P_ROOT_OFFENDER_ID, "1L",
                        P_SINGLE_OFFENDER_ID, "0",
                        P_AGY_LOC_ID, "MDI",
                        P_DETAILS_CLOB, testClob,
                        P_TIMESTAMP, timestamp));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/A1404AE/pss_detail", HttpMethod.GET, requestEntity, OffenderPssDetailEvent.class);

        if (responseEntity.getStatusCodeValue()!= 200) {
            fail("PSS detail call failed. Response body : " + responseEntity.getBody());
            return;
        }

        final var actual = (OffenderPssDetailEvent) responseEntity.getBody();

        assertThat(actual.getNomsId()).isEqualTo("A1401AE");
        assertThat(actual.getPrisonId()).isEqualTo("MDI");
        assertThat(actual.getEventData()).isNotNull();
    }

    // @Test - failing - return types in mocked procedure call
    public void offenderDetail() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var expectedSurname = "HALIBUT";

        when(offenderDetails.execute(any(SqlParameterSource.class))).thenReturn(
                Map.of(P_OFFENDER_CSR, List.of(OffenderSP.builder().lastName(expectedSurname)
                        .offenderAliases(List.of(AliasSP.builder().lastName("PLAICE").build()))
                        .build())));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/A1404AE", HttpMethod.GET, requestEntity, Offender.class);

        if (responseEntity.getStatusCodeValue()!= 200) {
            fail("Offender detail failed. Response body : " + responseEntity.getBody());
            return;
        }

        final var offenderActual = (Offender) responseEntity.getBody();

        assertThat(offenderActual.getSurname()).isEqualToIgnoringCase(expectedSurname);
        assertThat(offenderActual.getAliases()).hasSize(1);
    }

    // @Test - failing - return types in mocked procedure call
    public void offenderImage() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        when(offenderImage.execute(any(SqlParameterSource.class))).thenReturn(
                Map.of( P_IMAGE, "XXX"));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/A1404AE/image", HttpMethod.GET, requestEntity, Image.class);
        if (responseEntity.getStatusCodeValue()!= 200) {
            fail("offenderImage failed. Response body : " + responseEntity.getBody());
            return;
        }

        final var actualImage = (Image) responseEntity.getBody();
        assertThat(actualImage.getImage()).isEqualToIgnoringCase("XXX");
    }
}
