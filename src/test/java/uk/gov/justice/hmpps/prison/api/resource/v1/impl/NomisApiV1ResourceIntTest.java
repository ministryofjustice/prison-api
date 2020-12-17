package uk.gov.justice.hmpps.prison.api.resource.v1.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import uk.gov.justice.hmpps.prison.api.model.v1.ContactList;
import uk.gov.justice.hmpps.prison.api.model.v1.CreateTransaction;
import uk.gov.justice.hmpps.prison.api.model.v1.Events;
import uk.gov.justice.hmpps.prison.api.model.v1.Hold;
import uk.gov.justice.hmpps.prison.api.model.v1.LiveRoll;
import uk.gov.justice.hmpps.prison.api.model.v1.Offender;
import uk.gov.justice.hmpps.prison.api.model.v1.StorePaymentRequest;
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest;
import uk.gov.justice.hmpps.prison.repository.v1.model.AccountTransactionSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.AliasSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.AvailableDatesSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.ContactPersonSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.EventSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.HoldSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.LiveRollSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.OffenderSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.UnavailabilityReasonSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.VisitSlotsSP;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.EventProcs.GetEvents;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetAccountBalances;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetAccountTransactions;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetHolds;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetTransactionByClientUniqueRef;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.PostStorePayment;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.PostTransaction;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.PostTransfer;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.OffenderProcs.GetOffenderDetails;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.OffenderProcs.GetOffenderImage;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.OffenderProcs.GetOffenderPssDetail;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.PrisonProcs.GetLiveRoll;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.core.ResolvableType.forType;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.CoreProcs.GetActiveOffender;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.EventProcs.P_EVENT_TYPE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.EventProcs.P_FROM_TS;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.EventProcs.P_LIMIT;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_AGY_LOC_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CASH_BALANCE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CLIENT_UNIQUE_REF;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CONTACT_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CURRENT_AGY_DESC;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_CURRENT_AGY_LOC_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_DATE_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_DETAILS_CLOB;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_EVENTS_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_HOLDS_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_IMAGE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_OFFENDER_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_REASON_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_ROLL_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_ROOT_OFFENDER_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_SAVINGS_BALANCE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_SINGLE_OFFENDER_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_SPENDS_BALANCE;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TIMESTAMP;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TRANS_CSR;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TXN_ENTRY_SEQ;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TXN_ID;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetAvailableDates;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetContactList;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetUnavailability;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetVisitSlotsWithCapacity;


@SuppressWarnings("ConstantConditions")
public class NomisApiV1ResourceIntTest extends ResourceTest {
    @MockBean
    private PostTransaction postTransaction;

    @MockBean
    private PostTransfer postTransfer;

    @MockBean
    private GetOffenderPssDetail offenderPssDetail;

    @MockBean
    private GetOffenderDetails offenderDetails;

    @MockBean
    private GetOffenderImage offenderImage;

    @MockBean
    private GetHolds getHolds;

    @MockBean
    private GetEvents getEvents;

    @MockBean
    private GetLiveRoll getLiveRoll;

    @MockBean
    private PostStorePayment postStorePayment;

    @MockBean
    private GetAccountBalances getAccountBalances;

    @MockBean
    private GetAccountTransactions getAccountTransactions;

    @MockBean
    private GetTransactionByClientUniqueRef getTransactionByClientUniqueRef;

    @MockBean
    private GetActiveOffender getActiveOffender;

    @MockBean
    private GetAvailableDates getAvailableDates;

    @MockBean
    private GetContactList getContactList;

    @MockBean
    private GetUnavailability getUnavailability;

    @MockBean
    private GetVisitSlotsWithCapacity getVisitSlotsWithCapacity;

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
    public void transferTransaction_duplicate() {
        final var transaction = new CreateTransaction();
        transaction.setAmount(1234L);
        transaction.setClientUniqueRef("clientRef");
        transaction.setDescription("desc");
        transaction.setType("type");
        transaction.setClientTransactionId("transId");

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);

        when(postTransfer.execute(any(SqlParameterSource.class))).thenThrow(new DuplicateKeyException("Duplicate key"));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transfer_transactions", HttpMethod.POST, requestEntity, String.class);

        assertThatJson(responseEntity.getBody()).isEqualTo("{status: 409, userMessage: \"Duplicate key\", developerMessage: \"Duplicate key\"}");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
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

        final var procedureResponse = Map.<String, Object>of(
                P_NOMS_ID, "G7806VO",
                P_ROOT_OFFENDER_ID, 0L,
                P_SINGLE_OFFENDER_ID, "",
                P_AGY_LOC_ID, "LEI",
                P_DETAILS_CLOB, testClob,
                P_TIMESTAMP, timestamp);

        when(offenderPssDetail.execute(any(SqlParameterSource.class))).thenReturn(procedureResponse);

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/G7806VO/pss_detail", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "pss-detail.json");
    }

    @Test
    public void offenderDetail() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var expectedSurname = "HALIBUT";
        final var alias = new AliasSP();
        alias.setLastName("PLAICE");
        final var procedureResponse = Map.of(P_OFFENDER_CSR, (Object) List.of(OffenderSP.builder().lastName(expectedSurname)
                .offenderAliases(List.of(alias))
                .build()));

        when(offenderDetails.execute(any(SqlParameterSource.class))).thenReturn(procedureResponse);

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/A1404AE", HttpMethod.GET, requestEntity, Offender.class);

        assertThatStatus(responseEntity, 200);

        final var offenderActual = (Offender) responseEntity.getBody();

        assertThat(offenderActual).isNotNull();
        assertThat(offenderActual.getSurname()).isNotNull();
        assertThat(offenderActual.getSurname()).isEqualToIgnoringCase(expectedSurname);
        assertThat(offenderActual.getAliases()).hasSize(1);
    }

    @Test
    public void offenderImage() throws SQLException {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var imageBytes = "XXX".getBytes();
        final Blob blob = new javax.sql.rowset.serial.SerialBlob(imageBytes);
        final var procedureResponse = Map.of(P_IMAGE, (Object) blob);

        when(offenderImage.execute(any(SqlParameterSource.class))).thenReturn(procedureResponse);

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/A1404AE/image", HttpMethod.GET, requestEntity, String.class);
        assertThatStatus(responseEntity, 200);

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
    public void getEvents_WithSpacesBetweenEventData() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var events = List.of(
            new EventSP(3L, LocalDateTime.parse("2019-03-31T00:01:00.12456"), "LEI", "AB1256B", "ALERT", "{\"case_note\":{\"id\":47004657,\"contact_datetime\":\"2019-03-31 ", null,
                "00:00:00\"\n" +
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
            new EventSP(4L, LocalDateTime.parse("2019-04-30T00:00:01.234567"), "MDI", "BC1256B", "INTERNAL_LOCATION_CHANGED", "{\"account\":{\"code\":\"REG\"\n" +
                ",\"desc\":\"Private",
                " Cash\"\n" +
                    "},\"balance\":0}", null),
            new EventSP(5L, LocalDateTime.parse("2019-03-31T00:00:01"), "MDI", "CD1256B", "PERSONAL_DETAILS_CHANGED", null, null, null)
        );

        when(getEvents.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_EVENTS_CSR, events));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/events?prison_id=MDI&offender_id=A1492AE&event_type=e&from_datetime=2019-07-07 07:15:20.090&limit=100", HttpMethod.GET, requestEntity, String.class);

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
        when(postStorePayment.execute(any(SqlParameterSource.class))).thenReturn(Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/payment", HttpMethod.POST, requestEntity, String.class);

        assertThatJson(responseEntity.getBody()).isEqualTo("{ \"message\": \"Payment accepted\"}");
    }

    @Test
    public void storePaymentInvalidDetailsSupplied() {

        // Invalid request - client transaction too long - 12 character max
        final var request = StorePaymentRequest.builder().type("ADJ").amount(123L).clientTransactionId("This-is-too-long").description("bad payment args").build();
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), request);

        // No response parameters for this method so return an emtpy map to satisfy Mockito stub
        when(postStorePayment.execute(any(SqlParameterSource.class))).thenReturn(Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/payment", HttpMethod.POST, requestEntity, String.class);
        assertThatStatus(responseEntity, 400);
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
        assertThatJson(responseEntity.getBody()).isEqualTo("{\"status\":400,\"userMessage\":\"Start date cannot be in the past\",\"developerMessage\":\"400 Start date cannot be in the past\"}");
    }

    @Test
    public void getAvailableDatesInvalidOffenderId() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/AB2425215C/visits/available_dates?start_date=2017-01-01&end_date=2017-02-01", HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(400);
        assertThatJson(responseEntity.getBody()).isEqualTo("{\"status\":400,\"userMessage\":\"For input string: \\\"AB2425215C\\\"\",\"developerMessage\":\"For input string: \\\"AB2425215C\\\"\"}");
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
    public void getVisitUnavailabilityFoundCourtForDate() {
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
    public void getVisitUnavailabilityFoundOutOfVOForDates() {
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
                        .visitId(10309199L)
                        .slotStart(LocalDateTime.of(day1.getYear(), day1.getMonthValue(), day1.getDayOfMonth(), 9, 0))
                        .slotEnd(LocalDateTime.of(day1.getYear(), day1.getMonthValue(), day1.getDayOfMonth(), 12, 0))
                        .build(),
                UnavailabilityReasonSP
                        .builder()
                        .reason("VISIT")
                        .eventDate(day1)
                        .visitId(10309200L)
                        .slotStart(LocalDateTime.of(day1.getYear(), day1.getMonthValue(), day1.getDayOfMonth(), 13, 0))
                        .slotEnd(LocalDateTime.of(day1.getYear(), day1.getMonthValue(), day1.getDayOfMonth(), 16, 0))
                        .build());

        when(getUnavailability.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_REASON_CSR, unavailability));

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=" + day1 + "," + day2, HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{" +
                "\"" + day1 + "\":{\"external_movement\":false,\"existing_visits\":[{\"id\":10309199,\"slot\":\"" + visitSlot1Json + "\"},{\"id\":10309200,\"slot\":\"" + visitSlot2Json + "\"}],\"out_of_vo\":false,\"banned\":false}," +
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
        assertThatJson(responseEntity.getBody()).isEqualTo("{\"status\":400,\"userMessage\":\"Dates requested must be in future\",\"developerMessage\":\"400 Dates requested must be in future\"}");
    }

    @Test
    public void getVisitSlotsWithCapacity() {
        final var visitSlot1Json = LocalDate.now().plusDays(1) + "T13:30/16:00";
        final var visitSlot2Json = LocalDate.now().plusDays(2) + "T13:30/16:00";
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var visitSlotsSP = List.of(
                VisitSlotsSP
                        .builder()
                        .slotStart(LocalDateTime.now().plusDays(1).withHour(13).withMinute(30))
                        .slotEnd(LocalDateTime.now().plusDays(1).withHour(16).withMinute(0))
                        .capacity(402L)
                        .maxGroups(999L)
                        .maxAdults(999L)
                        .groupsBooked(1L)
                        .visitorsBooked(2L)
                        .adultsBooked(3L)
                        .build(),
                VisitSlotsSP
                        .builder()
                        .slotStart(LocalDateTime.now().plusDays(2).withHour(13).withMinute(30))
                        .slotEnd(LocalDateTime.now().plusDays(2).withHour(16).withMinute(0))
                        .capacity(402L)
                        .maxGroups(999L)
                        .maxAdults(999L)
                        .groupsBooked(4L)
                        .visitorsBooked(5L)
                        .adultsBooked(6L)
                        .build()
        );

        when(getVisitSlotsWithCapacity.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_DATE_CSR, visitSlotsSP));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/MDI/slots?start_date=" + LocalDate.now().plusDays(1) + "&end_date=" + LocalDate.now().plusDays(2), HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThatJson(responseEntity.getBody()).isEqualTo("{\"slots\":[" +
                "{\"time\":\"" + visitSlot1Json + "\",\"capacity\":402,\"max_groups\":999,\"max_adults\":999,\"groups_booked\":1,\"visitors_booked\":2,\"adults_booked\":3}," +
                "{\"time\":\"" + visitSlot2Json + "\",\"capacity\":402,\"max_groups\":999,\"max_adults\":999,\"groups_booked\":4,\"visitors_booked\":5,\"adults_booked\":6}" +
                "]}\n");
    }

    @Test
    public void getVisitSlotsWithCapacityInvalidDate() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/MDI/slots?start_date=3000-01-01&end_date=3017-01-01", HttpMethod.GET, requestEntity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(400);
        assertThatJson(responseEntity.getBody()).isEqualTo("{\"status\":400,\"userMessage\":\"End date cannot be more than 60 days in the future\",\"developerMessage\":\"400 End date cannot be more than 60 days in the future\"}");
    }

}
