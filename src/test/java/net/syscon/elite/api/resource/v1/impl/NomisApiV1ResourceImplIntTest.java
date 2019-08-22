package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.*;
import net.syscon.elite.api.resource.impl.ResourceTest;
import net.syscon.elite.repository.v1.model.*;
import net.syscon.elite.repository.v1.storedprocs.EventProcs.*;
import net.syscon.elite.repository.v1.storedprocs.FinanceProcs.*;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderDetails;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderImage;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderPssDetail;
import net.syscon.elite.repository.v1.storedprocs.PrisonProcs.GetLiveRoll;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.syscon.elite.repository.v1.storedprocs.CoreProcs.GetActiveOffender;
import static net.syscon.elite.repository.v1.storedprocs.EventProcs.*;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;
import static net.syscon.elite.repository.v1.storedprocs.VisitsProc.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.core.ResolvableType.forType;


public class NomisApiV1ResourceImplIntTest extends ResourceTest {

    @TestConfiguration
    static class Config {

        @Bean
        @Primary
        public PostTransaction postTransaction() {
            return mock(PostTransaction.class);
        }

        @Bean
        @Primary
        public PostTransfer postTransfer() {
            return mock(PostTransfer.class);
        }

        @Bean
        @Primary
        public GetOffenderPssDetail getOffenderPssDetail() {
            return mock(GetOffenderPssDetail.class);
        }

        @Bean
        @Primary
        public GetOffenderDetails getOffenderDetails() {
            return mock(GetOffenderDetails.class);
        }

        @Bean
        @Primary
        public GetOffenderImage getOffenderImage() {
            return mock(GetOffenderImage.class);
        }

        @Bean
        @Primary
        public GetHolds getHolds() {
            return mock(GetHolds.class);
        }

        @Bean
        @Primary
        public GetEvents getEvents() {
            return mock(GetEvents.class);
        }

        @Bean
        @Primary
        public GetLiveRoll getLiveRoll() {
            return mock(GetLiveRoll.class);
        }

        @Bean
        @Primary
        public PostStorePayment postStorePayment() {
            return mock(PostStorePayment.class);
        }

        @Bean
        @Primary
        public GetAccountBalances getAccountBalances() {
            return mock(GetAccountBalances.class);
        }

        @Bean
        @Primary
        public GetAccountTransactions getAccountTransactions() {
            return mock(GetAccountTransactions.class);
        }

        @Bean
        @Primary
        public GetTransactionByClientUniqueRef getTransactionByClientUniqueRef() {
            return mock(GetTransactionByClientUniqueRef.class);
        }

        @Bean
        @Primary
        public GetActiveOffender getActiveOffender() {
            return mock(GetActiveOffender.class);
        }

        @Bean
        @Primary
        public GetAvailableDates getAvailableDates() {
            return mock(GetAvailableDates.class);
        }

        @Bean
        @Primary
        public GetContactList getContactList() {
            return mock(GetContactList.class);
        }

        @Bean
        @Primary
        public GetUnavailability getUnavailability() {
            return mock(GetUnavailability.class);
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

    @Autowired
    private GetHolds getHolds;

    @Autowired
    private GetEvents getEvents;

    @Autowired
    private GetLiveRoll getLiveRoll;

    @Autowired
    private PostStorePayment postStorePayment;

    @Autowired
    private GetAccountBalances getAccountBalances;

    @Autowired
    private GetAccountTransactions getAccountTransactions;

    @Autowired
    private GetTransactionByClientUniqueRef getTransactionByClientUniqueRef;

    @Autowired
    private GetActiveOffender getActiveOffender;

    @Autowired
    private GetAvailableDates getAvailableDates;

    @Autowired
    private GetContactList getContactList;

    @Autowired
    private GetUnavailability getUnavailability;

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
    public void getOffenderPssDetail() throws SQLException {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var eventData = "{\n" +
                "        \"offender_details\": {\n" +
                "            \"personal_details\": {\n" +
                "                \"offender_surname\": \"ABDORIA\",\n" +
                "                \"offender_given_name_1\": \"ONGMETAIN\",\n" +
                "                \"offender_dob\": \"1990-12-06 00:00:00\",\n" +
                "                \"gender\": {\n" +
                "                    \"code\": \"M\",\n" +
                "                    \"desc\": \"Male\"\n" +
                "                },\n" +
                "                \"religion\": {\n" +
                "                    \"code\": \"NIL\",\n" +
                "                    \"desc\": \"EfJSmIEfJSm\"\n" +
                "                },\n" +
                "                \"security_category\": {\n" +
                "                    \"code\": \"C\",\n" +
                "                    \"desc\": \"Cat C\"\n" +
                "                },\n" +
                "                \"nationality\": {\n" +
                "                    \"code\": \"BRIT\",\n" +
                "                    \"desc\": \"sxiVsxi\"\n" +
                "                },\n" +
                "                \"ethnicity\": {\n" +
                "                    \"code\": \"W1\",\n" +
                "                    \"desc\": \"White: Eng./Welsh/Scot./N.Irish/British\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"sentence_information\": {\n" +
                "                \"reception_arrival_date_and_time\": \"2017-05-03 15:50:00\",\n" +
                "                \"status\": \"Convicted\",\n" +
                "                \"imprisonment_status\": {\n" +
                "                    \"code\": \"LR\",\n" +
                "                    \"desc\": \"Recalled to Prison from Parole (Non HDC)\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"location\": {\n" +
                "                \"agency_location\": \"LEI\",\n" +
                "                \"internal_location\": \"LEI-E-5-004\",\n" +
                "                \"location_type\": \"CELL\"\n" +
                "            },\n" +
                "            \"warnings\": [\n" +
                "                {\n" +
                "                    \"warning_type\": {\n" +
                "                        \"code\": \"P\",\n" +
                "                        \"desc\": \"MAPPP Case\"\n" +
                "                    },\n" +
                "                    \"warning_sub_type\": {\n" +
                "                        \"code\": \"P2\",\n" +
                "                        \"desc\": \"MAPPA Level 2 Case\"\n" +
                "                    },\n" +
                "                    \"warning_date\": \"2015-06-03 00:00:00\",\n" +
                "                    \"status\": \"ACTIVE\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"warning_type\": {\n" +
                "                        \"code\": \"R\",\n" +
                "                        \"desc\": \"Risk\"\n" +
                "                    },\n" +
                "                    \"warning_sub_type\": {\n" +
                "                        \"code\": \"RCS\",\n" +
                "                        \"desc\": \"Risk to Children - Custody\"\n" +
                "                    },\n" +
                "                    \"warning_date\": \"2013-06-04 00:00:00\",\n" +
                "                    \"status\": \"ACTIVE\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"entitlement\": {\n" +
                "                \"canteen_adjudication\": false,\n" +
                "                \"iep_level\": {\n" +
                "                    \"code\": \"STD\",\n" +
                "                    \"desc\": \"Standard\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"case_details\": {\n" +
                "                \"personal_officer\": \"Griffine, Ymmnatpher\"\n" +
                "            }\n" +
                "        }\n" +
                "     }";

        final var testClob = new javax.sql.rowset.serial.SerialClob(eventData.toCharArray());
        final var timestamp = Timestamp.valueOf("2019-07-09 00:00:00.000");

        final var procedureResponse = Map.of(
                P_NOMS_ID, (Object) "G7806VO",
                P_ROOT_OFFENDER_ID, (Object) 0L,
                P_SINGLE_OFFENDER_ID, (Object) "",
                P_AGY_LOC_ID, (Object) "LEI",
                P_DETAILS_CLOB, (Object) testClob,
                P_TIMESTAMP, (Object) timestamp);

        when(offenderPssDetail.execute(any(SqlParameterSource.class))).thenReturn(procedureResponse);

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/G7806VO/pss_detail", HttpMethod.GET, requestEntity, String.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            fail("PSS detail call failed. Response body : " + responseEntity.getBody());
            return;
        }

        // noinspection ConstantConditions
        final var json = new JsonContent<Event>(getClass(), forType(Event.class), responseEntity.getBody());

        assertThat(json).isEqualToJson("pss-detail.json");
    }

    @Test
    public void offenderDetail() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var expectedSurname = "HALIBUT";
        final var procedureResponse = Map.of(P_OFFENDER_CSR, (Object) List.of(OffenderSP.builder().lastName(expectedSurname)
                .offenderAliases(List.of(AliasSP.builder().lastName("PLAICE").build()))
                .build()));

        when(offenderDetails.execute(any(SqlParameterSource.class))).thenReturn(procedureResponse);

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/A1404AE", HttpMethod.GET, requestEntity, Offender.class);

        if (responseEntity.getStatusCodeValue() != 200) {
            fail("Offender detail failed. Response body : " + responseEntity.getBody());
            return;
        }

        final var offenderActual = (Offender) responseEntity.getBody();

        assertThat(offenderActual).isNotNull();
        assertThat(offenderActual.getSurname()).isNotNull();
        assertThat(offenderActual.getSurname()).isEqualToIgnoringCase(expectedSurname);
        assertThat(offenderActual.getAliases()).hasSize(1);
    }

    @Test
    public void offenderImage() throws SQLException {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        byte[] imageBytes = "XXX".getBytes();
        Blob blob = new javax.sql.rowset.serial.SerialBlob(imageBytes);
        final var procedureResponse = Map.of(P_IMAGE, (Object) blob);

        when(offenderImage.execute(any(SqlParameterSource.class))).thenReturn(procedureResponse);

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/A1404AE/image", HttpMethod.GET, requestEntity, String.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            fail("offenderImage failed. Response body : " + responseEntity.getBody());
            return;
        }

        // Encoded image returns this value for the test XXX value used
        final var actualJson = responseEntity.getBody();
        assertThatJson(actualJson).isEqualTo("{\"image\":\"WFhY\"}");
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

    @Test
    public void getEvents() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var events = List.of(
                new EventSP(3L, LocalDateTime.parse("2019-03-31T00:01:00.12456"), "LEI", "AB1256B", "ALERT", null, null,
                        "{\"case_note\":{\"id\":47004657,\"contact_datetime\":\"2019-03-31 00:00:00\"\n" +
                                ",\"source\":{\"code\":\"AUTO\"\n" +
                                ",\"desc\":\"System\"\n" +
                                "},\"type\":{\"code\":\"ALERT\"\n" +
                                ",\"desc\":\"Alert\"\n" +
                                "},\"sub_type\":{\"code\":\"INACTIVE\"\n" +
                                ",\"desc\":\"Made Inactive\"\n" +
                                "},\"staff_member\":{\"id\":1,\"name\":\"Cnomis, Admin&Onb\"\n" +
                                ",\"userid\":\"\"\n" +
                                "},\"text\":\"Alert Other and Charged under Harassment Act made inactive.\"\n" +
                                ",\"amended\":false}}"),
                new EventSP(4L, LocalDateTime.parse("2019-04-30T00:00:01.234567"), "MDI", "BC1256B", "INTERNAL_LOCATION_CHANGED", null,
                        "{\"account\":{\"code\":\"REG\"\n" +
                                ",\"desc\":\"Private Cash\"\n" +
                                "},\"balance\":0}", null),
                new EventSP(5L, LocalDateTime.parse("2019-03-31T00:00:01"), "MDI", "CD1256B", "PERSONAL_DETAILS_CHANGED", null, null, null)
        );

        final var captor = ArgumentCaptor.forClass(SqlParameterSource.class);
        when(getEvents.execute(captor.capture())).thenReturn(Map.of(P_EVENTS_CSR, events));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/events?prison_id=MDI&offender_id=A1492AE&event_type=e&from_datetime=2019-07-07 07:15:20.090&limit=100", HttpMethod.GET, requestEntity, String.class);

        assertThat(captor.getValue().getValue(P_AGY_LOC_ID)).isEqualTo("MDI");
        assertThat(captor.getValue().getValue(P_NOMS_ID)).isEqualTo("A1492AE");
        assertThat(captor.getValue().getValue(P_ROOT_OFFENDER_ID)).isNull();
        assertThat(captor.getValue().getValue(P_SINGLE_OFFENDER_ID)).isNull();
        assertThat(captor.getValue().getValue(P_EVENT_TYPE)).isEqualTo("e");
        assertThat(captor.getValue().getValue(P_FROM_TS)).isEqualTo(LocalDateTime.parse("2019-07-07T07:15:20.090"));
        assertThat(captor.getValue().getValue(P_LIMIT)).isEqualTo(100L);

        //noinspection ConstantConditions
        assertThat(new JsonContent<Events>(getClass(), forType(Events.class), responseEntity.getBody())).isEqualToJson("events.json");
    }

    @Test
    public void getLiveRoll() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var roll = List.of(new LiveRollSP("A12345B"), new LiveRollSP("B23456C"));

        final var captor = ArgumentCaptor.forClass(SqlParameterSource.class);
        when(getLiveRoll.execute(captor.capture())).thenReturn(Map.of(P_ROLL_CSR, roll));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/MDI/live_roll", HttpMethod.GET, requestEntity, String.class);

        assertThat(captor.getValue().getValue(P_AGY_LOC_ID)).isEqualTo("MDI");

        //noinspection ConstantConditions
        assertThat(new JsonContent<LiveRoll>(getClass(), forType(LiveRoll.class), responseEntity.getBody())).isEqualToJson("roll.json");
    }

    @Test
    public void storePaymentOk() {

        final var request = StorePaymentRequest.builder().type("ADJ").amount(1324L).clientTransactionId("CS123").description("Earnings for May").build();
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), request);

        // No response parameters for this method so return an emtpy map to satisfy Mockito stub
        when(postStorePayment.execute(any(SqlParameterSource.class))).thenReturn(Collections.EMPTY_MAP);

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/payment", HttpMethod.POST, requestEntity, String.class);

        assertThatJson(responseEntity.getBody()).isEqualTo("{ \"message\": \"Payment accepted\"}");
    }

    @Test
    public void storePaymentInvalidDetailsSupplied() {

        // Invalid request - client transaction too long - 12 character max
        final var request = StorePaymentRequest.builder().type("ADJ").amount(123L).clientTransactionId("This-is-too-long").description("bad payment args").build();
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), request);

        // No response parameters for this method so return an emtpy map to satisfy Mockito stub
        when(postStorePayment.execute(any(SqlParameterSource.class))).thenReturn(Collections.EMPTY_MAP);

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/payment", HttpMethod.POST, requestEntity, String.class);

        assertThatJson(responseEntity.getBody()).toString().contains("400");
    }

    @Test
    public void getAccountBalances() {

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        when(getAccountBalances.execute(any(SqlParameterSource.class))).thenReturn(
                Map.of(P_CASH_BALANCE, new BigDecimal("12.34"), P_SPENDS_BALANCE, new BigDecimal("56.78"), P_SAVINGS_BALANCE, new BigDecimal("34.34")));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/accounts", HttpMethod.GET, requestEntity, String.class);

        assertThatJson(responseEntity.getBody()).isEqualTo("{ \"spends\": 5678, \"savings\": 3434, \"cash\": 1234 }");
    }

    @Test
    public void getCashTransactions() {

        final var responseEntity = getTransactions("cash");

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{ \"transactions\": [ { \"id\": \"111-1\", \"type\": { \"code\": \"A\", \"desc\": \"AAA\" }, \"description\": \"Transaction test\", \"amount\": 1234, \"date\": \"2019-12-01\" } ] }");
    }

    @Test
    public void getSpendsTransactions() {

        final var responseEntity = getTransactions("spends");

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{ \"transactions\": [ { \"id\": \"111-1\", \"type\": { \"code\": \"A\", \"desc\": \"AAA\" }, \"description\": \"Transaction test\", \"amount\": 1234, \"date\": \"2019-12-01\" } ] }");
    }

    @Test
    public void getSavingsTransactions() {

        final var responseEntity = getTransactions("savings");

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{ \"transactions\": [ { \"id\": \"111-1\", \"type\": { \"code\": \"A\", \"desc\": \"AAA\" }, \"description\": \"Transaction test\", \"amount\": 1234, \"date\": \"2019-12-01\" } ] }");
    }

    @Test
    public void getTransactionByClientUniqueRef() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", List.of("ROLE_NOMIS_API_V1"), Map.of("X-Client-Name", "some-client"));

        final var transactions = List.of(
                AccountTransactionSP.builder()
                        .txnId(111L)
                        .txnEntrySeq(1)
                        .txnEntryDate(LocalDate.of(2019, 12, 1))
                        .txnEntryDesc("Transaction test")
                        .txnType("A")
                        .txnTypeDesc("AAA")
                        .txnEntryAmount(new BigDecimal("12.34"))
                        .build()
        );

        when(getTransactionByClientUniqueRef.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_TRANS_CSR, transactions));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/transactions/some-reference", HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{ \"id\": \"111-1\", \"type\": { \"code\": \"A\", \"desc\": \"AAA\" }, \"description\": \"Transaction test\", \"amount\": 1234, \"date\": \"2019-12-01\" }");
    }

    @Test
    public void getTransactionByClientUniqueRefTransactionNotFound() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", List.of("ROLE_NOMIS_API_V1"), Map.of("X-Client-Name", "some-client"));
        final var transactions = List.of();
        when(getTransactionByClientUniqueRef.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_TRANS_CSR, transactions));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/transactions/some-reference", HttpMethod.GET, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    public void getActiveOffender() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        when(getActiveOffender.executeFunction(eq(BigDecimal.class), any(SqlParameterSource.class))).thenReturn(
                new BigDecimal("1111111"));

        final var responseEntity = testRestTemplate.exchange("/api/v1/lookup/active_offender?noms_id=G0797UA&date_of_birth=1958-04-07", HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{ \"found\": true, \"offender\": { \"id\": 1111111 } }");
    }

    @Test
    public void getActiveOffenderNotFound() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        when(getActiveOffender.executeFunction(eq(BigDecimal.class), any(SqlParameterSource.class))).thenReturn(null);

        final var responseEntity = testRestTemplate.exchange("/api/v1/lookup/active_offender?noms_id=G0797UA&date_of_birth=1958-04-07", HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{ \"found\": false }");
    }

    @Test
    public void getAvailableDates() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var dates = List.of(
                AvailableDatesSP
                        .builder()
                        .slotDate(LocalDate.of(2019, 1, 21))
                        .build(),
                AvailableDatesSP
                        .builder()
                        .slotDate(LocalDate.of(2019, 1, 22))
                        .build());
        when(getAvailableDates.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_DATE_CSR, dates));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/available_dates?start_date=" + LocalDate.now() + "&end_date=" + LocalDate.now().plusDays(10), HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{ \"dates\": [ \"2019-01-21\", \"2019-01-22\" ] }");
    }

    @Test
    public void getAvailableDatesInvalidDate() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/available_dates?start_date=2017-01-01&end_date=2017-02-01", HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(400);
        assertThatJson(responseEntity.getBody()).isEqualTo("{\"status\":400,\"userMessage\":\"Invalid start and end date range\",\"developerMessage\":\"\"}");
    }

    @Test
    public void getContactListWithoutRestrictions() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var contacts = List.of(
                ContactPersonSP
                        .builder()
                        .personId(1111111L)
                        .firstName("first")
                        .middleName("mid")
                        .lastName("last")
                        .birthDate(LocalDate.of(2000, 1, 1))
                        .sexCode("M")
                        .sexDesc("Male")
                        .relationshipTypeCode("Other")
                        .relationshipTypeDesc("Other - Social")
                        .contactTypeCode("S")
                        .contactTypeDesc("Social/ Family")
                        .approvedVisitorFlag("Y")
                        .activeFlag("Y")
                        .build());
        when(getContactList.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_CONTACT_CSR, contacts));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/contact_list", HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{\"contacts\":[{\"id\":1111111,\"given_name\":\"first\",\"middle_names\":\"mid\",\"surname\":\"last\",\"date_of_birth\":\"2000-01-01\",\"gender\":{\"code\":\"M\",\"desc\":\"Male\"},\"relationship_type\":{\"code\":\"Other\",\"desc\":\"Other - Social\"},\"contact_type\":{\"code\":\"S\",\"desc\":\"Social/ Family\"},\"approved_visitor\":true,\"active\":true,\"restrictions\":[]}]}\n");
        assertThat(new JsonContent<ContactList>(getClass(), forType(ContactList.class), responseEntity.getBody())).isEqualToJson("contact-list-without-restrictions.json");
    }

    @Test
    public void getContactListWithRestrictions() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var contacts = List.of(
                ContactPersonSP
                        .builder()
                        .personId(1111111L)
                        .firstName("first")
                        .middleName("mid")
                        .lastName("last")
                        .birthDate(LocalDate.of(2000, 1, 1))
                        .sexCode("M")
                        .sexDesc("Male")
                        .relationshipTypeCode("Other")
                        .relationshipTypeDesc("Other - Social")
                        .contactTypeCode("S")
                        .contactTypeDesc("Social/ Family")
                        .approvedVisitorFlag("Y")
                        .activeFlag("Y")
                        .restrictionTypeCode("PREINF")
                        .restrictionTypeDesc("Previous Info")
                        .restrictionEffectiveDate(LocalDate.of(2015, 1, 1))
                        .restrictionExpiryDate(LocalDate.of(2020, 1, 1))
                        .commentText("xxxxxx")
                        .build());
        when(getContactList.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_CONTACT_CSR, contacts));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/contact_list", HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(new JsonContent<ContactList>(getClass(), forType(ContactList.class), responseEntity.getBody())).isEqualToJson("contact-list-with-restrictions.json");
    }

    private ResponseEntity getTransactions(final String accountType) {
        final var transactions = List.of(
                AccountTransactionSP.builder()
                        .txnId(111L)
                        .txnEntrySeq(1)
                        .txnEntryDate(LocalDate.of(2019, 12, 1))
                        .txnEntryDesc("Transaction test")
                        .txnType("A")
                        .txnTypeDesc("AAA")
                        .txnEntryAmount(new BigDecimal("12.34"))
                        .build()
        );

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        when(getAccountTransactions.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_TRANS_CSR, transactions));

        return testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/accounts/" + accountType + "/transactions", HttpMethod.GET, requestEntity, String.class);
    }

    @Test
    public void getVisitUnavailabilityFoundCourtforDate() {
        final var day1 = LocalDate.now().plusDays(1);
        final var day2 = LocalDate.now().plusDays(2);
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var unavailability = List.of(UnavailabilityReasonSP.builder().reason("COURT").eventDate(day1).build());

        when(getUnavailability.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_REASON_CSR, unavailability));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=" + day1 + "," + day2, HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{" +
                "\"" + day1 + "\":{\"external_movement\":true,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":false}," +
                "\"" + day2 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":false}}");
    }

    @Test
    public void getVisitUnavailabilityFoundBannedForDates() {
        final var day1 = LocalDate.now().plusDays(1);
        final var day2 = LocalDate.now().plusDays(2);
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var unavailability = List.of(UnavailabilityReasonSP.builder().reason("BAN").eventDate(day1).build(), UnavailabilityReasonSP.builder().reason("BAN").eventDate(day2).build());

        when(getUnavailability.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_REASON_CSR, unavailability));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=" + day1 + "," + day2, HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{" +
                "\"" + day1 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":true}," +
                "\"" + day2 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":true}}");
    }

    @Test
    public void getVisitUnavailabilityFoundOutOfVOforDates() {
        final var day1 = LocalDate.now().plusDays(1);
        final var day2 = LocalDate.now().plusDays(2);
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var unavailability = List.of(UnavailabilityReasonSP.builder().reason("VO").eventDate(day1).build(), UnavailabilityReasonSP.builder().reason("VO").eventDate(day2).build());

        when(getUnavailability.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_REASON_CSR, unavailability));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=" + day1 + "," + day2, HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{" +
                "\"" + day1 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":true,\"banned\":false}," +
                "\"" + day2 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":true,\"banned\":false}}");
    }

    @Test
    public void getVisitUnavailabilityFoundVisitsDates() {
        final var day1 = LocalDate.now().plusDays(1);
        final var day2 = LocalDate.now().plusDays(2);
        final var visitSlot1Json = day1 + "T09:00/12:00";
        final var visitSlot2Json = day1 + "T13:00/16:00";
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var unavailability = List.of(UnavailabilityReasonSP
                        .builder()
                        .reason("VISIT")
                        .eventDate(day1)
                        .visitId("10309199")
                        .slotStart(LocalDateTime.of(day1.getYear(), day1.getMonthValue(), day1.getDayOfMonth(), 9, 0))
                        .slotEnd(LocalDateTime.of(day1.getYear(), day1.getMonthValue(), day1.getDayOfMonth(), 12, 0))
                        .build(),
                UnavailabilityReasonSP
                        .builder()
                        .reason("VISIT")
                        .eventDate(day1)
                        .visitId("10309200")
                        .slotStart(LocalDateTime.of(day1.getYear(), day1.getMonthValue(), day1.getDayOfMonth(), 13, 0))
                        .slotEnd(LocalDateTime.of(day1.getYear(), day1.getMonthValue(), day1.getDayOfMonth(), 16, 0))
                        .build());

        when(getUnavailability.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_REASON_CSR, unavailability));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=" + day1 + "," + day2, HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{" +
                "\"" + day1 + "\":{\"external_movement\":false,\"existing_visits\":[{\"visit_id\":10309199,\"slot\":\"" + visitSlot1Json + "\"},{\"visit_id\":10309200,\"slot\":\"" + visitSlot2Json + "\"}],\"out_of_vo\":false,\"banned\":false}," +
                "\"" + day2 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":false}}");
    }

    @Test
    public void getVisitUnavailabilityNonFoundForDates() {
        final var day1 = LocalDate.now().plusDays(1);
        final var day2 = LocalDate.now().plusDays(2);
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var unavailability = List.of();
        when(getUnavailability.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_REASON_CSR, unavailability));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=" + day1 + "," + day2, HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{" +
                "\"" + day1 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":false}," +
                "\"" + day2 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":false}}");
    }

    @Test
    public void getVisitUnavailabilityInvalidDate() {
        final var day1 = LocalDate.now().minusDays(1);
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var unavailability = List.of();
        when(getUnavailability.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_REASON_CSR, unavailability));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=" + day1, HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(400);
        assertThatJson(responseEntity.getBody()).isEqualTo("{\"status\":400,\"userMessage\":\"Dates requested must be in future\",\"developerMessage\":\"\"}");
    }

}
