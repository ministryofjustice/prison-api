package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

public interface OffenderPendingDeletionEventPusher {
    void sendEvent(final String offenderIdDisplay);
}
