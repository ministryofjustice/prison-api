package uk.gov.justice.hmpps.nomis.datacompliance.events.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto.AdHocReferralRequest;
import uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto.DataDuplicateCheck;
import uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto.FreeTextCheck;
import uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto.OffenderDeletionGranted;
import uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto.OffenderRestrictionRequest;
import uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto.ProvisionalDeletionReferralRequest;
import uk.gov.justice.hmpps.nomis.datacompliance.events.listeners.dto.ReferralRequest;
import uk.gov.justice.hmpps.nomis.datacompliance.service.DataComplianceReferralService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.DataDuplicateService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.FreeTextSearchService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderDeletionService;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderDeletionService.OffenderDeletionGrant;
import uk.gov.justice.hmpps.nomis.datacompliance.service.OffenderRestrictionService;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.data.domain.Pageable.unpaged;

@Slf4j
@Service
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.request.sqs.provider}')")
public class DataComplianceEventListener {

    private static final String REFERRAL_REQUEST = "DATA_COMPLIANCE_REFERRAL-REQUEST";
    private static final String AD_HOC_REFERRAL_REQUEST = "DATA_COMPLIANCE_AD-HOC-REFERRAL-REQUEST";
    private static final String  PROVISIONAL_DELETION_REFERRAL_REQUEST = "PROVISIONAL_DELETION_REFERRAL_REQUEST";
    private static final String DATA_DUPLICATE_ID_CHECK = "DATA_COMPLIANCE_DATA-DUPLICATE-ID-CHECK";
    private static final String DATA_DUPLICATE_DB_CHECK = "DATA_COMPLIANCE_DATA-DUPLICATE-DB-CHECK";
    private static final String FREE_TEXT_MORATORIUM_CHECK = "DATA_COMPLIANCE_FREE-TEXT-MORATORIUM-CHECK";
    private static final String OFFENDER_RESTRICTION_CHECK = "DATA_COMPLIANCE_OFFENDER-RESTRICTION-CHECK";
    private static final String OFFENDER_DELETION_GRANTED = "DATA_COMPLIANCE_OFFENDER-DELETION-GRANTED";

    private final Map<String, MessageHandler> messageHandlers = Map.of(
            REFERRAL_REQUEST, this::handleReferralRequest,
            AD_HOC_REFERRAL_REQUEST, this::handleAdHocReferralRequest,
            PROVISIONAL_DELETION_REFERRAL_REQUEST, this::handleProvisionalDeletionReferralRequest,
            DATA_DUPLICATE_ID_CHECK, this::handleDuplicateIdCheck,
            DATA_DUPLICATE_DB_CHECK, this::handleDuplicateDataCheck,
            FREE_TEXT_MORATORIUM_CHECK, this::handleFreeTextMoratoriumCheck,
            OFFENDER_RESTRICTION_CHECK, this::handleOffenderRestrictionCheck,
            OFFENDER_DELETION_GRANTED, this::handleDeletionGranted);

    private final DataComplianceReferralService dataComplianceReferralService;
    private final DataDuplicateService dataDuplicateService;
    private final OffenderDeletionService offenderDeletionService;
    private final FreeTextSearchService freeTextSearchService;
    private final OffenderRestrictionService offenderRestrictionService;
    private final ObjectMapper objectMapper;

    public DataComplianceEventListener(final DataComplianceReferralService dataComplianceReferralService,
                                       final DataDuplicateService dataDuplicateService,
                                       final OffenderDeletionService offenderDeletionService,
                                       final FreeTextSearchService freeTextSearchService,
                                       final OffenderRestrictionService offenderRestrictionService,
                                       final ObjectMapper objectMapper) {

        log.info("Configured to listen to data compliance events");

        this.dataComplianceReferralService = dataComplianceReferralService;
        this.dataDuplicateService = dataDuplicateService;
        this.offenderDeletionService = offenderDeletionService;
        this.freeTextSearchService = freeTextSearchService;
        this.offenderRestrictionService = offenderRestrictionService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${data.compliance.request.sqs.queue.name}")
    public void handleEvent(final Message<String> message) {

        final var eventType = getEventType(message.getHeaders());

        log.debug("Handling incoming data compliance event of type: {}", eventType);

        messageHandlers.get(eventType).handle(message);
    }

    private String getEventType(final MessageHeaders messageHeaders) {

        final var eventType = messageHeaders.get("eventType", String.class);

        checkNotNull(eventType, "Message event type not found");
        checkState(messageHandlers.containsKey(eventType),
                "Unexpected message event type: '%s', expecting one of: %s", eventType, messageHandlers.keySet());

        return eventType;
    }

    private void handleReferralRequest(final Message<String> message) {
        final var event = parseEvent(message.getPayload(), ReferralRequest.class);

        checkNotNull(event.getBatchId(), "No batch ID specified in request: %s", message.getPayload());
        checkNotNull(event.getDueForDeletionWindowStart(), "No window start date specified in request: %s", message.getPayload());
        checkNotNull(event.getDueForDeletionWindowEnd(), "No window end date specified in request: %s", message.getPayload());

        final var pageRequest = Optional.ofNullable(event.getLimit())
                .map(limit -> (Pageable) PageRequest.of(0, limit))
                .orElse(unpaged());

        dataComplianceReferralService.referOffendersForDeletion(
                event.getBatchId(),
                event.getDueForDeletionWindowStart(),
                event.getDueForDeletionWindowEnd(),
                pageRequest);
    }

    private void handleAdHocReferralRequest(final Message<String> message) {
        final var event = parseEvent(message.getPayload(), AdHocReferralRequest.class);

        checkState(isNotEmpty(event.getOffenderIdDisplay()), "No offender specified in request: %s", message.getPayload());
        checkNotNull(event.getBatchId(), "No batch ID specified in request: %s", message.getPayload());

        dataComplianceReferralService.referAdHocOffenderDeletion(event.getOffenderIdDisplay(), event.getBatchId());
    }

    private void handleProvisionalDeletionReferralRequest(final Message<String> message) {
        final var event = parseEvent(message.getPayload(), ProvisionalDeletionReferralRequest.class);

        checkState(isNotEmpty(event.getOffenderIdDisplay()), "No offender specified in request: %s", message.getPayload());
        checkState(isNotEmpty(event.getReferralId()), "No referralId specified in request: %s", message.getPayload());

        dataComplianceReferralService.referProvisionalDeletion(event.getOffenderIdDisplay(), event.getReferralId());
    }

    private void handleFreeTextMoratoriumCheck(final Message<String> message) {
        final var event = parseEvent(message.getPayload(), FreeTextCheck.class);

        validateFreeTextCheck(event, message.getPayload());

        freeTextSearchService.checkForMatchingContent(event.getOffenderIdDisplay(), event.getRetentionCheckId(), event.getRegex());
    }

    private void handleOffenderRestrictionCheck(final Message<String> message) {
        final var event = parseEvent(message.getPayload(), OffenderRestrictionRequest.class);

        validateOffenderRestrictionCheck(event, message.getPayload());

        offenderRestrictionService.checkForOffenderRestrictions(event);
    }

    private void validateFreeTextCheck(final FreeTextCheck event, final String payload) {
        checkState(isNotEmpty(event.getOffenderIdDisplay()), "No offender specified in request: %s", payload);
        checkNotNull(event.getRetentionCheckId(), "No retention check ID specified in request: %s", payload);
        checkState(isNotEmpty(event.getRegex()), "No regex provided in request: %s", payload);
        event.getRegex().forEach(regex -> validateRegex(regex, payload));
    }

    private void validateOffenderRestrictionCheck(final OffenderRestrictionRequest event, final String payload) {
        checkState(isNotEmpty(event.getOffenderIdDisplay()), "No offender specified in request: %s", payload);
        checkNotNull(event.getRetentionCheckId(), "No retention check ID specified in request: %s", payload);
        checkState(isNotEmpty(event.getRestrictionCodes()), "No restriction code specified in request: %s", payload);
        checkState(isNotEmpty(event.getRegex()), "No regex provided in request: %s", payload);
        validateRegex(event.getRegex(), payload);
    }

    private void validateRegex(final String regex, final String payload) {
        checkState(isNotEmpty(regex), "Empty regex provided in request: %s", payload);
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException ex) {
            throw new IllegalStateException(format("Invalid regex provided in request: %s", payload), ex);
        }
    }

    private void handleDuplicateIdCheck(final Message<String> message) {
        final var event = parseDataDuplicateEvent(message);
        dataDuplicateService.checkForDuplicateIds(event.getOffenderIdDisplay(), event.getRetentionCheckId());
    }

    private void handleDuplicateDataCheck(final Message<String> message) {
        final var event = parseDataDuplicateEvent(message);
        dataDuplicateService.checkForDataDuplicates(event.getOffenderIdDisplay(), event.getRetentionCheckId());
    }

    private DataDuplicateCheck parseDataDuplicateEvent(final Message<String> message) {
        final var event = parseEvent(message.getPayload(), DataDuplicateCheck.class);

        checkState(isNotEmpty(event.getOffenderIdDisplay()), "No offender specified in request: %s", message.getPayload());
        checkNotNull(event.getRetentionCheckId(), "No retention check ID specified in request: %s", message.getPayload());

        return event;
    }

    private void handleDeletionGranted(final Message<String> message) {
        final var event = parseEvent(message.getPayload(), OffenderDeletionGranted.class);

        checkState(isNotEmpty(event.getOffenderIdDisplay()), "No offender specified in request: %s", message.getPayload());
        checkNotNull(event.getReferralId(), "No referral ID specified in request: %s", message.getPayload());

        offenderDeletionService.deleteOffender(OffenderDeletionGrant.builder()
                .offenderNo(event.getOffenderIdDisplay())
                .referralId(event.getReferralId())
                .offenderIds(event.getOffenderIds())
                .offenderBookIds(event.getOffenderBookIds())
                .build());
    }

    private <T> T parseEvent(final String requestJson, final Class<T> eventType) {
        try {
            return objectMapper.readValue(requestJson, eventType);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to parse request: " + requestJson, e);
        }
    }

    @FunctionalInterface
    private interface MessageHandler {
        void handle(Message<String> message);
    }
}
