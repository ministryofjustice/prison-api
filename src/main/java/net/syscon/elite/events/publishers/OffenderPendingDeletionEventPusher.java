package net.syscon.elite.events.publishers;

public interface OffenderPendingDeletionEventPusher {
    void sendEvent(final String offenderIdDisplay);
}
