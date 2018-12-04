package net.syscon.elite.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.exception.EliteRuntimeException;
import net.syscon.elite.repository.IdempotentRequestRepository;
import net.syscon.elite.repository.support.IdempotentRequestControl;
import net.syscon.elite.service.IdempotentRequestService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class IdempotentRequestServiceImpl implements IdempotentRequestService {
    private final IdempotentRequestRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotentRequestServiceImpl(IdempotentRequestRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public IdempotentRequestControl getAndSet(String correlationId) {
        Validate.notBlank(correlationId);

        return repository.getAndSet(correlationId);
    }

    @Override
    public void updateResponse(String correlationId, String responseData, Integer responseStatus) {
        Validate.notBlank(correlationId);
        Validate.notBlank(responseData);

        repository.updateResponse(correlationId, responseData, responseStatus);
    }

    @Override
    public <T> T extractJsonResponse(IdempotentRequestControl irc, Class<T> responseType) {
        Validate.notNull(irc);
        Validate.notNull(responseType);

        T response;

        if (StringUtils.isNotBlank(irc.getResponse())) {
            try {
                response = objectMapper.readValue(irc.getResponse(), responseType);
            } catch (IOException e) {
                log.error("Error converting response to JSON.");
                throw new EliteRuntimeException(e);
            }
        } else {
            response = null;
        }

        return response;
    }

    @Override
    public void convertAndStoreResponse(String correlationId, Object response, Integer responseStatus) {
        Validate.notBlank(correlationId);
        Validate.notNull(response);

        String jsonResponse;

        try {
            jsonResponse = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Error converting response to JSON.");
            throw new EliteRuntimeException(e);
        }

        updateResponse(correlationId, jsonResponse, responseStatus);
    }
}
