package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

public interface OffenderPendingDeletionEventPusher {
    void sendPendingDeletionEvent(final String offenderIdDisplay);
    void sendProcessCompletedEvent(final String requestId);
}
