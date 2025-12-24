package uk.gov.justice.hmpps.prison.test;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpMessageConverterExtractor;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles error (i.e. 4xx or 5xx) from Prison API and converts response body to {@link ErrorResponse}.
 */
public class ErrorResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    protected void handleError(
        ClientHttpResponse response, HttpStatusCode statusCode,
        @Nullable URI url, @Nullable HttpMethod method) throws IOException {
        final var errorMessageExtractor =
                new HttpMessageConverterExtractor<>(ErrorResponse.class, getMessageConverters());

        ErrorResponse errorResponse = null;
        try {
            errorResponse = errorMessageExtractor.extractData(response);
        } catch (final Exception e) {
            super.handleError(response, statusCode, url, method);
        }
        throw new PrisonApiClientException(errorResponse);
    }

    private List<HttpMessageConverter<?>> getMessageConverters() {
        final List<HttpMessageConverter<?>> converters = new ArrayList<>();

        converters.add(new MappingJackson2HttpMessageConverter());

        return converters;
    }
}
