package net.syscon.elite.service;

import net.syscon.elite.repository.support.IdempotentRequestControl;

public interface IdempotentRequestService {
    IdempotentRequestControl getAndSet(String correlationId);

    void updateResponse(String correlationId, String responseData, Integer responseStatus);

    <T> T extractJsonResponse(IdempotentRequestControl irc, Class<T> responseType);

    void convertAndStoreResponse(String correlationId, Object response, Integer responseStatus);
}
