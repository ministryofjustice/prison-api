package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.FreeTextSearchResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderDeletionComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;

public interface DataComplianceEventPusher {
    void send(FreeTextSearchResult event);
    void send(OffenderPendingDeletion event);
    void send(OffenderPendingDeletionReferralComplete event);
    void send(OffenderDeletionComplete event);
    void sendDuplicateIdResult(DataDuplicateResult event);
    void sendDuplicateDataResult(DataDuplicateResult event);
}
