package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.repository.support.IdempotentRequestControl;

public interface IdempotentRequestRepository {
    IdempotentRequestControl getAndSet(String correlationId);

    void updateResponse(String correlationId, String responseData, Integer responseStatus);
}
