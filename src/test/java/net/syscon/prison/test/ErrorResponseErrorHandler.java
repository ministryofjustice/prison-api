package net.syscon.prison.test;

import net.syscon.prison.api.model.ErrorResponse;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpMessageConverterExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles error (i.e. 4xx or 5xx) from Elite2 API and converts response body to {@link ErrorResponse}.
 */
public class ErrorResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(final ClientHttpResponse response) throws IOException {
        final var errorMessageExtractor =
                new HttpMessageConverterExtractor<ErrorResponse>(ErrorResponse.class, getMessageConverters());

        ErrorResponse errorResponse = null;
        try {
            errorResponse = errorMessageExtractor.extractData(response);
        } catch (final Exception e) {
            super.handleError(response);
        }
        throw new PrisonApiClientException(errorResponse);
    }

    private List<HttpMessageConverter<?>> getMessageConverters() {
        final List<HttpMessageConverter<?>> converters = new ArrayList<>();

        converters.add(new MappingJackson2HttpMessageConverter());

        return converters;
    }
}
