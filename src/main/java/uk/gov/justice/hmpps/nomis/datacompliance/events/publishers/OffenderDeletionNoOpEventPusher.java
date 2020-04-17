package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionCompleteEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionReferralCompleteEvent;

@Slf4j
@Component
@ConditionalOnExpression("!{'aws', 'localstack'}.contains('${data.compliance.outbound.referral.sqs.provider}')")
public class OffenderDeletionNoOpEventPusher implements OffenderDeletionEventPusher {

    public OffenderDeletionNoOpEventPusher() {
        log.info("Configured to ignore offender pending deletion events");
    }

    @Override
    public void sendPendingDeletionEvent(final OffenderPendingDeletionEvent event) {
        log.warn("Pretending to push pending offender deletion for '{}' to queue", event.getOffenderIdDisplay());
    }

    @Override
    public void sendReferralCompleteEvent(final OffenderPendingDeletionReferralCompleteEvent event) {
        log.warn("Pretending to push process completed event for request '{}' to queue", event.getBatchId());
    }

    @Override
    public void sendDeletionCompleteEvent(final OffenderDeletionCompleteEvent event) {
        log.warn("Pretending to push offender deletion complete event for '{}' to queue", event.getOffenderIdDisplay());
    }
}
