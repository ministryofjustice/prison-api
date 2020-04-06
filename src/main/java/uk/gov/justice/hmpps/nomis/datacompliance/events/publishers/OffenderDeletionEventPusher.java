package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionCompleteEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionReferralCompleteEvent;

public interface OffenderDeletionEventPusher {
    void sendPendingDeletionEvent(OffenderPendingDeletionEvent event);
    void sendReferralCompleteEvent(OffenderPendingDeletionReferralCompleteEvent event);
    void sendDeletionCompleteEvent(OffenderDeletionCompleteEvent event);
}
