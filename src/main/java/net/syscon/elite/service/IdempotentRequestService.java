package net.syscon.elite.service;

import net.syscon.elite.repository.support.IdempotentRequestControl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface IdempotentRequestService {
    IdempotentRequestControl getAndSet(String correlationId);

    void updateResponse(String correlationId, String responseData, Integer responseStatus);

    <T> T extractJsonResponse(IdempotentRequestControl irc, Class<T> responseType);

    void convertAndStoreResponse(String correlationId, Object response, Integer responseStatus);
}
