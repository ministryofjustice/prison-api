package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DeceasedOffenderDeletionResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.FreeTextSearchResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderDeletionComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderRestrictionResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.ProvisionalDeletionReferralResult;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.response.sqs.provider}')")
public class DataComplianceAwsEventPusher implements DataComplianceEventPusher {

    private static final String OFFENDER_PENDING_DELETION = "DATA_COMPLIANCE_OFFENDER-PENDING-DELETION";
    private static final String OFFENDER_PROVISIONAL_DELETION_REFERRAL = "DATA_COMPLIANCE_OFFENDER_PROVISIONAL_DELETION_REFERRAL";
    private static final String OFFENDER_PENDING_DELETION_REFERRAL_COMPLETE = "DATA_COMPLIANCE_OFFENDER-PENDING-DELETION-REFERRAL-COMPLETE";
    private static final String OFFENDER_DELETION_COMPLETE = "DATA_COMPLIANCE_OFFENDER-DELETION-COMPLETE";
    private static final String DUPLICATE_ID_RESULT = "DATA_COMPLIANCE_DATA-DUPLICATE-ID-RESULT";
    private static final String DATA_DUPLICATE_DB_RESULT = "DATA_COMPLIANCE_DATA-DUPLICATE-DB-RESULT";
    private static final String OFFENDER_RESTRICTION_RESULT = "DATA_COMPLIANCE_OFFENDER-RESTRICTION-RESULT";
    private static final String FREE_TEXT_MORATORIUM_RESULT = "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-RESULT";
    private static final String DECEASED_OFFENDER_DELETION_RESULT = "DATA_COMPLIANCE_DECEASED-OFFENDER-DELETION-RESULT";

    private final ObjectMapper objectMapper;
    private final AmazonSQS sqsClient;
    private final String queueUrl;

    public DataComplianceAwsEventPusher(
            @Autowired @Qualifier("dataComplianceResponseSqsClient") final AmazonSQS sqsClient,
            @Value("${data.compliance.response.sqs.queue.url}") final String queueUrl,
            final ObjectMapper objectMapper) {

        log.info("Configured to push data compliance events to SQS queue: {}", queueUrl);

        this.objectMapper = objectMapper;
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public void send(final OffenderPendingDeletion event) {

        log.trace("Sending referral of offender pending deletion: {}", event.getOffenderIdDisplay());

        sqsClient.sendMessage(generateRequest(OFFENDER_PENDING_DELETION, event));
    }

    @Override
    public void send(final ProvisionalDeletionReferralResult event) {
        log.trace("Sending referral of provisional deletion referral result: {}", event.getOffenderIdDisplay());

        sqsClient.sendMessage(generateRequest(OFFENDER_PROVISIONAL_DELETION_REFERRAL, event));
    }

    @Override
    public void send(final OffenderPendingDeletionReferralComplete event) {

        log.trace("Sending process completed event for request: {}", event.getBatchId());

        sqsClient.sendMessage(generateRequest(OFFENDER_PENDING_DELETION_REFERRAL_COMPLETE, event));
    }

    @Override
    public void send(final OffenderDeletionComplete event) {

        log.trace("Sending offender deletion complete event: {}", event.getOffenderIdDisplay());

        sqsClient.sendMessage(generateRequest(OFFENDER_DELETION_COMPLETE, event));
    }

    @Override
    public void sendDuplicateIdResult(final DataDuplicateResult event) {

        log.trace("Sending duplicate ID result for offender: {}", event.getOffenderIdDisplay());

        sqsClient.sendMessage(generateRequest(DUPLICATE_ID_RESULT, event));
    }

    @Override
    public void sendDuplicateDataResult(final DataDuplicateResult event) {

        log.trace("Sending duplicate data result for offender: {}", event.getOffenderIdDisplay());

        sqsClient.sendMessage(generateRequest(DATA_DUPLICATE_DB_RESULT, event));
    }

    @Override
    public void send(final OffenderRestrictionResult event) {
        log.trace("Sending offender restriction result for offender: {}", event.getOffenderIdDisplay());

        sqsClient.sendMessage(generateRequest(OFFENDER_RESTRICTION_RESULT, event));
    }

    @Override
    public void send(final FreeTextSearchResult event) {

        log.trace("Sending free text search result for offender: {}", event.getOffenderIdDisplay());

        sqsClient.sendMessage(generateRequest(FREE_TEXT_MORATORIUM_RESULT, event));
    }

    @Override
    public void send(final DeceasedOffenderDeletionResult event) {
        log.trace("Sending deceased offender result for batch: {}", event.getBatchId());

        sqsClient.sendMessage(generateRequest(DECEASED_OFFENDER_DELETION_RESULT, event));
    }

    private SendMessageRequest generateRequest(final String eventType, final Object messageBody) {
        return new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageAttributes(Map.of(
                        "eventType", stringAttribute(eventType),
                        "contentType", stringAttribute("application/json;charset=UTF-8")))
                .withMessageBody(toJson(messageBody));
    }

    private MessageAttributeValue stringAttribute(final String value) {
        return new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(value);
    }

    private String toJson(final Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
