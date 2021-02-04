package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.FreeTextSearchResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderDeletionComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderRestrictionResult;

@Slf4j
@Component
@ConditionalOnExpression("!{'aws', 'localstack'}.contains('${data.compliance.response.sqs.provider}')")
public class DataComplianceNoOpEventPusher implements DataComplianceEventPusher {

    public DataComplianceNoOpEventPusher() {
        log.info("Configured to ignore data compliance events");
    }

    @Override
    public void send(final OffenderPendingDeletion event) {
        log.warn("Pretending to push pending offender deletion for '{}' to queue", event.getOffenderIdDisplay());
    }

    @Override
    public void send(final OffenderPendingDeletionReferralComplete event) {
        log.warn("Pretending to push process completed event for request '{}' to queue", event.getBatchId());
    }

    @Override
    public void send(final OffenderDeletionComplete event) {
        log.warn("Pretending to push offender deletion complete event for '{}' to queue", event.getOffenderIdDisplay());
    }

    @Override
    public void sendDuplicateIdResult(final DataDuplicateResult event) {
        log.warn("Pretending to push duplicate ID result for '{}' to queue", event.getOffenderIdDisplay());
    }

    @Override
    public void sendDuplicateDataResult(final DataDuplicateResult event) {
        log.warn("Pretending to push duplicate data result for '{}' to queue", event.getOffenderIdDisplay());
    }

    @Override
    public void send(OffenderRestrictionResult event) {
        log.warn("Pretending to push offender restriction result for '{}' to queue", event.getOffenderIdDisplay());
    }

    @Override
    public void send(final FreeTextSearchResult event) {
        log.warn("Pretending to push free text search result for '{}' to queue", event.getOffenderIdDisplay());
    }
}
