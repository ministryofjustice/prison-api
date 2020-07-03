package net.syscon.prison.repository;

import net.syscon.prison.repository.support.IdempotentRequestControl;

public interface IdempotentRequestRepository {
    IdempotentRequestControl getAndSet(String correlationId);

    void updateResponse(String correlationId, String responseData, Integer responseStatus);
}
