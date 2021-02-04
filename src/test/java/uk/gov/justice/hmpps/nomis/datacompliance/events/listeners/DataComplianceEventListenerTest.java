package uk.gov.justice.hmpps.nomis.datacompliance.events.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto.OffenderRestrictionRequest;
import uk.gov.justice.hmpps.nomis.datacompliance.service.DataComplianceReferralService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.DataDuplicateService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.FreeTextSearchService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderDeletionService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderRestrictionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataComplianceEventListenerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private DataComplianceReferralService dataComplianceReferralService;

    @Mock
    private DataDuplicateService dataDuplicateService;

    @Mock
    private OffenderDeletionService offenderDeletionService;

    @Mock
    private FreeTextSearchService freeTextSearchService;

    @Mock
    private OffenderRestrictionService offenderRestrictionService;

    private DataComplianceEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new DataComplianceEventListener(
                dataComplianceReferralService,
                dataDuplicateService,
                offenderDeletionService,
                freeTextSearchService,
                offenderRestrictionService,
                MAPPER);
    }

    @Test
    void handleReferralRequest() {

        handleMessage(
                "{\"batchId\":123,\"dueForDeletionWindowStart\":\"2020-01-01\",\"dueForDeletionWindowEnd\":\"2020-02-02\",\"limit\":10}",
                Map.of("eventType", "DATA_COMPLIANCE_REFERRAL-REQUEST"));

        verify(dataComplianceReferralService).referOffendersForDeletion(
                123L,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 2, 2),
                PageRequest.of(0, 10));
    }

    @Test
    void handleReferralRequestWithNoLimit() {

        handleMessage(
                "{\"batchId\":123,\"dueForDeletionWindowStart\":\"2020-01-01\",\"dueForDeletionWindowEnd\":\"2020-02-02\"}",
                Map.of("eventType", "DATA_COMPLIANCE_REFERRAL-REQUEST"));

        verify(dataComplianceReferralService).referOffendersForDeletion(
                123L,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 2, 2),
                Pageable.unpaged());
    }

    @Test
    void handleReferralRequestThrowsIfBatchIdNull() {

        assertThatThrownBy(() -> handleMessage(
                "{\"dueForDeletionWindowStart\":\"2020-01-01\",\"dueForDeletionWindowEnd\":\"2020-02-02\"}",
                Map.of("eventType", "DATA_COMPLIANCE_REFERRAL-REQUEST")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No batch ID specified in request");

        verifyNoInteractions(dataComplianceReferralService);
    }

    @Test
    void handleReferralRequestThrowsIfWindowStartNull() {

        assertThatThrownBy(() -> handleMessage(
                "{\"batchId\":123,\"dueForDeletionWindowEnd\":\"2020-02-02\"}",
                Map.of("eventType", "DATA_COMPLIANCE_REFERRAL-REQUEST")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No window start date specified in request");

        verifyNoInteractions(dataComplianceReferralService);
    }

    @Test
    void handleReferralRequestThrowsIfWindowEndNull() {

        assertThatThrownBy(() -> handleMessage(
                "{\"batchId\":123,\"dueForDeletionWindowStart\":\"2020-01-01\"}",
                Map.of("eventType", "DATA_COMPLIANCE_REFERRAL-REQUEST")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No window end date specified in request");

        verifyNoInteractions(dataComplianceReferralService);
    }

    @Test
    void handleAdHocReferralRequest() {

        handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"batchId\":123}",
                Map.of("eventType", "DATA_COMPLIANCE_AD-HOC-REFERRAL-REQUEST"));

        verify(dataComplianceReferralService).referAdHocOffenderDeletion("A1234AA", 123L);
    }

    @Test
    void handleAdHocReferralRequestThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> handleMessage(
                "{\"offenderIdDisplay\":\"\",\"batchId\":123}",
                Map.of("eventType", "DATA_COMPLIANCE_AD-HOC-REFERRAL-REQUEST")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(dataComplianceReferralService);
    }

    @Test
    void handleAdHocReferralRequestThrowsIfBatchIdNull() {

        assertThatThrownBy(() -> handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\"}",
                Map.of("eventType", "DATA_COMPLIANCE_AD-HOC-REFERRAL-REQUEST")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No batch ID specified in request");

        verifyNoInteractions(dataComplianceReferralService);
    }

    @Test
    void handleDuplicateIdCheck() {

        handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123}",
                Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-ID-CHECK"));

        verify(dataDuplicateService).checkForDuplicateIds("A1234AA", 123L);
    }

    @Test
    void handleDuplicateIdCheckThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"\",\"retentionCheckId\":123}", Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-ID-CHECK")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleDuplicateIdCheckThrowsIfRetentionCheckIdNull() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\"}", Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-ID-CHECK")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No retention check ID specified in request");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleDuplicateDataCheck() {

        handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123}",
                Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-DB-CHECK"));

        verify(dataDuplicateService).checkForDataDuplicates("A1234AA", 123L);
    }

    @Test
    void handleDuplicateDataCheckThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"\",\"retentionCheckId\":123}", Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-DB-CHECK")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(dataDuplicateService);
    }

    @Test
    void handleDuplicateDataCheckThrowsIfRetentionCheckIdNull() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\"}", Map.of("eventType", "DATA_COMPLIANCE_DATA-DUPLICATE-DB-CHECK")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No retention check ID specified in request");

        verifyNoInteractions(dataDuplicateService);
    }

    @Test
    void handleFreeTextCheck() {

        handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123,\"regex\":[\"^(regex|1)$\",\"^(regex|2)$\"]}",
                Map.of("eventType", "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-CHECK"));

        verify(freeTextSearchService).checkForMatchingContent("A1234AA", 123L, List.of("^(regex|1)$","^(regex|2)$"));
    }

    @Test
    void handleFreeTextCheckThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> handleMessage(
                "{\"offenderIdDisplay\":\"\",\"retentionCheckId\":123,\"regex\":[\"^(some|regex)$\"]}",
                Map.of("eventType", "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-CHECK")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(freeTextSearchService);
    }

    @Test
    void handleFreeTextCheckThrowsIfRetentionCheckIdNull() {

        assertThatThrownBy(() -> handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"regex\":[\"^(some|regex)$\"]}",
                Map.of("eventType", "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-CHECK")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No retention check ID specified in request");

        verifyNoInteractions(freeTextSearchService);
    }

    @Test
    void handleFreeTextCheckThrowsIfEmptyRegexList() {

        assertThatThrownBy(() -> handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123,\"regex\":[]}",
                Map.of("eventType", "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-CHECK")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No regex provided in request");

        verifyNoInteractions(freeTextSearchService);
    }

    @Test
    void handleFreeTextCheckThrowsIfRegexEmpty() {

        assertThatThrownBy(() -> handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123,\"regex\":[\"\"]}",
                Map.of("eventType", "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-CHECK")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Empty regex provided in request");

        verifyNoInteractions(freeTextSearchService);
    }

    @Test
    void handleFreeTextCheckThrowsIfRegexInvalid() {

        assertThatThrownBy(() -> handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123,\"regex\":[\".**INVALID\"]}",
                Map.of("eventType", "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-CHECK")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid regex provided in request");

        verifyNoInteractions(freeTextSearchService);
    }

    @Test
    void handleOffenderRestrictionCheck() {

        handleMessage(
            "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123,\"restrictionCodes\":[\"CHILD\"],\"regex\":\"^(regex|1)$\"}",
            Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-RESTRICTION-CHECK"));

        verify(offenderRestrictionService).checkForOffenderRestrictions(OffenderRestrictionRequest.builder()
            .offenderIdDisplay("A1234AA")
            .retentionCheckId(123L)
            .restrictionCode("CHILD")
            .regex("^(regex|1)$")
            .build());
    }

    @ParameterizedTest()
    @NullAndEmptySource
    void handleOffenderRestrictionCheckIfOffenderIdIsNullOrEmpty(String offenderId) {

        assertThatThrownBy(() -> handleMessage(
            "{\"offenderIdDisplay\":" + offenderId + ",\"retentionCheckId\":123,\"restrictionCodes\":[\"CHILD\"],\"regex\":\"^(regex|1)$\"}",
            Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-RESTRICTION-CHECK")))
            .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(offenderRestrictionService);
    }

    @ParameterizedTest()
    @NullAndEmptySource
    void handleOffenderRestrictionCheckIfRetentionCheckIdIsNullOrEmpty(String retentionCheckId) {

        assertThatThrownBy(() -> {
            handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":" + retentionCheckId + ",\"restrictionCodes\":[\"CHILD\"],\"regex\":\"^(regex|1)$\"}",
                    Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-RESTRICTION-CHECK"));
        })
            .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(offenderRestrictionService);
    }

    @ParameterizedTest()
    @NullAndEmptySource
    void handleOffenderRestrictionCheckIfRestrictionCodesAreNullOrEmpty(String restrictionCodes) {

        assertThatThrownBy(() -> {
            handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123,\"restrictionCodes\":" + restrictionCodes + ",\"regex\":\"^(regex|1)$\"}",
                Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-RESTRICTION-CHECK"));
        })
            .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(offenderRestrictionService);
    }


    @ParameterizedTest()
    @NullAndEmptySource
    @ValueSource(strings = {".**SOME_INVALID_REGEX", "..*SOME_INVALID_REGEX", "?></|'&^%$£@"})
    void handleOffenderRestrictionCheckIfRegexInvalid(String regex) {

        assertThatThrownBy(() -> handleMessage(
            "{\"offenderIdDisplay\":\"A1234AA\",\"retentionCheckId\":123,\"restrictionCodes\":[\"CHILD\"],\"regex\":" + regex + "}",
            Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-RESTRICTION-CHECK")))
            .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(offenderRestrictionService);
    }


    @Test
    void handleOffenderDeletionEvent() {

        handleMessage(
                "{\"offenderIdDisplay\":\"A1234AA\",\"referralId\":123,\"offenderIds\":[456],\"offenderBookIds\":[789]}",
                Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED"));

        verify(offenderDeletionService).deleteOffender(OffenderDeletionService.OffenderDeletionGrant.builder()
                .offenderNo("A1234AA")
                .referralId(123L)
                .offenderId(456L)
                .offenderBookId(789L)
                .build());
    }

    @Test
    void handleOffenderDeletionEventThrowsIfOffenderIdDisplayEmpty() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"\",\"referralId\":123,\"offenderIds\":[456],\"offenderBookIds\":[789]}",
                Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No offender specified in request");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleOffenderDeletionEventThrowsIfReferralIdNull() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\",\"offenderIds\":[456],\"offenderBookIds\":[789]}",
                Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No referral ID specified in request");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleEventThrowsIfMessageAttributesNotPresent() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\"}", Map.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Message event type not found");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleEventThrowsIfEventTypeUnexpected() {

        assertThatThrownBy(() -> handleMessage("{\"offenderIdDisplay\":\"A1234AA\"}", Map.of("eventType", "UNEXPECTED!")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unexpected message event type: 'UNEXPECTED!'");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleEventThrowsIfMessageNotPresent() {
        assertThatThrownBy(() -> handleMessage(null, Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("argument \"content\" is null");

        verifyNoInteractions(offenderDeletionService);
    }

    @Test
    void handleEventThrowsIfMessageUnparsable() {

        assertThatThrownBy(() -> handleMessage("BAD MESSAGE!", Map.of("eventType", "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse request");

        verifyNoInteractions(offenderDeletionService);
    }

    private void handleMessage(final String payload, final Map<String, Object> headers) {
        listener.handleEvent(mockMessage(payload, headers));
    }

    @SuppressWarnings("unchecked")
    private Message<String> mockMessage(final String payload, final Map<String, Object> headers) {
        final var message = mock(Message.class);
        when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
        lenient().when(message.getPayload()).thenReturn(payload);
        return message;
    }
}