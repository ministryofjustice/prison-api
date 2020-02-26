package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.deletion.complete;

import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionCompleteEvent;

public interface OffenderDeletionCompleteEventPusher {
    void sendEvent(OffenderDeletionCompleteEvent event);
}
