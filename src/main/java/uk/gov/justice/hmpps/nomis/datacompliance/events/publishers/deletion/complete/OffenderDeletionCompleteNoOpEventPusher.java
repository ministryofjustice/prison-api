package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.deletion.complete;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionCompleteEvent;

@Slf4j
@Component
@ConditionalOnExpression("!{'aws', 'localstack'}.contains('${data.compliance.sns.provider}')")
public class OffenderDeletionCompleteNoOpEventPusher implements OffenderDeletionCompleteEventPusher {

    public OffenderDeletionCompleteNoOpEventPusher() {
        log.info("Configured to ignore offender deletion complete events");
    }

    @Override
    public void sendEvent(final OffenderDeletionCompleteEvent event) {
        log.warn("Pretending to push offender deletion completed event for '{}' to queue", event.getOffenderIdDisplay());
    }
}
