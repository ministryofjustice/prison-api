package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderDeletionComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;

@Slf4j
@Component
@ConditionalOnExpression("!{'aws', 'localstack'}.contains('${data.compliance.response.sqs.provider}')")
public class DataComplianceNoOpEventPusher implements DataComplianceEventPusher {

    public DataComplianceNoOpEventPusher() {
        log.info("Configured to ignore offender pending deletion events");
    }

    @Override
    public void sendPendingDeletionEvent(final OffenderPendingDeletion event) {
        log.warn("Pretending to push pending offender deletion for '{}' to queue", event.getOffenderIdDisplay());
    }

    @Override
    public void sendReferralCompleteEvent(final OffenderPendingDeletionReferralComplete event) {
        log.warn("Pretending to push process completed event for request '{}' to queue", event.getBatchId());
    }

    @Override
    public void sendDeletionCompleteEvent(final OffenderDeletionComplete event) {
        log.warn("Pretending to push offender deletion complete event for '{}' to queue", event.getOffenderIdDisplay());
    }

    @Override
    public void sendDataDuplicateResult(DataDuplicateResult event) {
        log.warn("Pretending to push data duplicate result for '{}' to queue", event.getOffenderIdDisplay());
    }
}
