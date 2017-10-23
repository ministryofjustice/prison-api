package net.syscon.elite.repository;

import net.syscon.elite.api.model.MainSentence;

public interface SentenceRepository {
    MainSentence getMainSentence(Long bookingId);
}
