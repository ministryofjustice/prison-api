package net.syscon.elite.repository;

import net.syscon.elite.repository.support.IdempotentRequestControl;

public interface IdempotentRequestRepository {
    IdempotentRequestControl getAndSet(String correlationId);

    void updateResponse(String correlationId, String responseData, Integer responseStatus);
}
