package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderDeletionComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;

public interface DataComplianceEventPusher {
    void sendPendingDeletionEvent(OffenderPendingDeletion event);
    void sendReferralCompleteEvent(OffenderPendingDeletionReferralComplete event);
    void sendDeletionCompleteEvent(OffenderDeletionComplete event);
    void sendDataDuplicateResult(DataDuplicateResult event);
}
