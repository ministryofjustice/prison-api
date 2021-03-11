package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.FreeTextSearchResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderDeletionComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.Booking;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.OffenderAlias;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.ProvisionalDeletionReferralResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataComplianceEventPusherTest {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private AmazonSQS client;

    private DataComplianceEventPusher eventPusher;

    @BeforeEach
    void setUp() {
        eventPusher = new DataComplianceAwsEventPusher(client, "queue.url", OBJECT_MAPPER);
    }

    @Test
    void sendPendingDeletionEvent() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
                .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.send(OffenderPendingDeletion.builder()
                .offenderIdDisplay("offender1")
                .firstName("Bob")
                .middleName("Middle")
                .lastName("Jones")
                .birthDate(LocalDate.of(1990, 1, 2))
                .agencyLocationId("LEI")
                .offenderAlias(OffenderAlias.builder()
                        .offenderId(123L)
                        .booking(Booking.builder()
                                .offenderBookId(321L)
                                .offenceCode("offence1")
                                .alertCode("alert1")
                                .build())
                        .build())
                .build());

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody()).isEqualTo(
                "{" +
                        "\"offenderIdDisplay\":\"offender1\"," +
                        "\"firstName\":\"Bob\"," +
                        "\"middleName\":\"Middle\"," +
                        "\"lastName\":\"Jones\"," +
                        "\"birthDate\":\"1990-01-02\"," +
                        "\"agencyLocationId\":\"LEI\"," +
                        "\"offenderAliases\":[{" +
                            "\"offenderId\":123," +
                            "\"bookings\":[" +
                                "{\"offenderBookId\":321,\"offenceCodes\":[\"offence1\"],\"alertCodes\":[\"alert1\"]}" +
                            "]" +
                        "}]" +
                "}");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_OFFENDER-PENDING-DELETION");
    }

    @Test
    void sendProvisionalDeletionReferralResultEvent() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
            .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.send(ProvisionalDeletionReferralResult.builder()
            .referralId(123L)
            .offenderIdDisplay("offender")
            .subsequentChangesIdentified(false)
            .agencyLocationId("someAgencyLocId")
            .offenceCodes(List.of("someOffenceCode1", "someOffenceCode2"))
            .alertCodes(List.of("someAlertCode1", "someAlertCode2"))
            .build());

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody()).isEqualTo(
            "{\"referralId\":123," +
            "\"offenderIdDisplay\":\"offender\"," +
            "\"subsequentChangesIdentified\":false," +
            "\"agencyLocationId\":\"someAgencyLocId\"," +
            "\"offenceCodes\":[\"someOffenceCode1\",\"someOffenceCode2\"]," +
            "\"alertCodes\":[\"someAlertCode1\",\"someAlertCode2\"]}");

        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
            .isEqualTo("DATA_COMPLIANCE_OFFENDER_PROVISIONAL_DELETION_REFERRAL");
    }

    @Test
    void sendReferralCompleteEvent() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
                .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.send(new OffenderPendingDeletionReferralComplete(123L, 1L, 2L));

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody()).isEqualTo("{\"batchId\":123,\"numberReferred\":1,\"totalInWindow\":2}");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_OFFENDER-PENDING-DELETION-REFERRAL-COMPLETE");
    }

    @Test
    void sendDeletionCompleteEvent() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
                .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.send(new OffenderDeletionComplete("offender1", 123L));

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody()).isEqualTo("{\"offenderIdDisplay\":\"offender1\",\"referralId\":123}");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_OFFENDER-DELETION-COMPLETE");
    }

    @Test
    void sendDuplicateIdResult() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
                .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.sendDuplicateIdResult(DataDuplicateResult.builder()
                .offenderIdDisplay("offender1")
                .retentionCheckId(123L)
                .duplicateOffender("offender2")
                .build());

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody())
                .isEqualTo("{\"offenderIdDisplay\":\"offender1\",\"retentionCheckId\":123,\"duplicateOffenders\":[\"offender2\"]}");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_DATA-DUPLICATE-ID-RESULT");
    }

    @Test
    void sendDuplicateDataResult() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
                .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.sendDuplicateDataResult(DataDuplicateResult.builder()
                .offenderIdDisplay("offender1")
                .retentionCheckId(123L)
                .duplicateOffender("offender2")
                .build());

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody())
                .isEqualTo("{\"offenderIdDisplay\":\"offender1\",\"retentionCheckId\":123,\"duplicateOffenders\":[\"offender2\"]}");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_DATA-DUPLICATE-DB-RESULT");
    }

    @Test
    void sendFreeTextSearchResult() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
                .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.send(FreeTextSearchResult.builder()
                .offenderIdDisplay("offender1")
                .retentionCheckId(123L)
                .matchingTable("table1")
                .build());

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody())
                .isEqualTo("{\"offenderIdDisplay\":\"offender1\",\"retentionCheckId\":123,\"matchingTables\":[\"table1\"]}");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-RESULT");
    }

    @Test
    void sendEventPropagatesException() {
        when(client.sendMessage(any())).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> eventPusher.send(
                OffenderPendingDeletion.builder().offenderIdDisplay("offender1").build()))
                .isInstanceOf(RuntimeException.class);
    }
}
