package net.syscon.elite.test;

import net.syscon.elite.api.model.ErrorResponse;
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
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpMessageConverterExtractor<ErrorResponse> errorMessageExtractor =
                new HttpMessageConverterExtractor<>(ErrorResponse.class, getMessageConverters());

        ErrorResponse errorResponse = errorMessageExtractor.extractData(response);

        throw new EliteClientException(errorResponse);
    }

    private List<HttpMessageConverter<?>> getMessageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        converters.add(new MappingJackson2HttpMessageConverter());

        return converters;
    }
}
