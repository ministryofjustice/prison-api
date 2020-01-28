package net.syscon.elite.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.exception.EliteRuntimeException;
import net.syscon.elite.repository.IdempotentRequestRepository;
import net.syscon.elite.repository.support.IdempotentRequestControl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Slf4j
public class IdempotentRequestService {
    private final IdempotentRequestRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotentRequestService(final IdempotentRequestRepository repository, final ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public IdempotentRequestControl getAndSet(final String correlationId) {
        Validate.notBlank(correlationId);

        return repository.getAndSet(correlationId);
    }

    @Transactional
    public void updateResponse(final String correlationId, final String responseData, final Integer responseStatus) {
        Validate.notBlank(correlationId);
        Validate.notBlank(responseData);

        repository.updateResponse(correlationId, responseData, responseStatus);
    }

    public <T> T extractJsonResponse(final IdempotentRequestControl irc, final Class<T> responseType) {
        Validate.notNull(irc);
        Validate.notNull(responseType);

        final T response;

        if (StringUtils.isNotBlank(irc.getResponse())) {
            try {
                response = objectMapper.readValue(irc.getResponse(), responseType);
            } catch (final IOException e) {
                log.error("Error converting response to JSON.");
                throw new EliteRuntimeException(e);
            }
        } else {
            response = null;
        }

        return response;
    }

    public void convertAndStoreResponse(final String correlationId, final Object response, final Integer responseStatus) {
        Validate.notBlank(correlationId);
        Validate.notNull(response);

        final String jsonResponse;

        try {
            jsonResponse = objectMapper.writeValueAsString(response);
        } catch (final JsonProcessingException e) {
            log.error("Error converting response to JSON.");
            throw new EliteRuntimeException(e);
        }

        updateResponse(correlationId, jsonResponse, responseStatus);
    }
}