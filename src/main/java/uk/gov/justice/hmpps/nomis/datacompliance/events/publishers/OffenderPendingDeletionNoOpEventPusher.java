package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnExpression("!{'aws', 'localstack'}.contains('${data.compliance.outbound.referral.sqs.provider}')")
public class OffenderPendingDeletionNoOpEventPusher implements OffenderPendingDeletionEventPusher {

    public OffenderPendingDeletionNoOpEventPusher() {
        log.info("Configured to ignore offender pending deletion events");
    }

    @Override
    public void sendEvent(final String offenderIdDisplay) {
        log.warn("Pretending to push pending offender deletion for {} to event topic", offenderIdDisplay);
    }
}
