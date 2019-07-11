package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.CreateTransaction;
import net.syscon.elite.api.model.v1.Events;
import net.syscon.elite.api.model.v1.Hold;
import net.syscon.elite.api.model.v1.LiveRoll;
import net.syscon.elite.api.resource.impl.ResourceTest;
import net.syscon.elite.repository.v1.model.EventSP;
import net.syscon.elite.repository.v1.model.HoldSP;
import net.syscon.elite.repository.v1.model.LiveRollSP;
import net.syscon.elite.repository.v1.storedprocs.EventProcs.*;
import net.syscon.elite.repository.v1.storedprocs.FinanceProcs.*;
import net.syscon.elite.repository.v1.storedprocs.PrisonProcs.GetLiveRoll;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.syscon.elite.repository.v1.storedprocs.EventProcs.*;
import static net.syscon.elite.repository.v1.storedprocs.FinanceProcs.*;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;
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

        @Bean
        @Primary
        public GetEvents getEvents() {
            return Mockito.mock(GetEvents.class);
        }

        @Bean
        @Primary
        public GetLiveRoll getLiveRoll() {
            return Mockito.mock(GetLiveRoll.class);
        }
    }

    @Autowired
    private PostTransaction postTransaction;

    @Autowired
    private PostTransfer postTransfer;

    @Autowired
    private GetHolds getHolds;

    @Autowired
    private GetEvents getEvents;

    @Autowired
    private GetLiveRoll getLiveRoll;

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
}
