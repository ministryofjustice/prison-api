package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.test.ErrorResponseErrorHandler;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common BDD step implementations
 */
public abstract class CommonSteps {
    public static final String API_PREFIX = "/api/";

    @Autowired
    private AuthTokenHelper auth;

    @Autowired
    protected TestRestTemplate restTemplate;

    private List<?> resources;
    private Page<?> pageMetaData;
    private ErrorResponse errorResponse;

    @PostConstruct
    protected void postConstruct() {
        restTemplate.getRestTemplate().setErrorHandler(new ErrorResponseErrorHandler());
    }

    @Step("Verify number of resource records returned")
    public void verifyResourceRecordsReturned(final long expectedCount) {
        assertThat(resources).hasSize(Math.toIntExact(expectedCount));
    }

    @Step("Verify some resource records are returned")
    public void verifyResourceRecordsNotEmpty() {
        assertThat(resources).isNotEmpty();
    }

    @Step("Verify some resource records returned")
    public void verifySomeResourceRecordsReturned() {
        assertThat(resources).hasSizeGreaterThan(0);
    }

    @Step("Verify total number of resource records available")
    public void verifyTotalResourceRecordsAvailable(final long expectedCount) {
        assertThat(pageMetaData.getTotalRecords()).isEqualTo(expectedCount);
    }

    public void authenticateAsClient(final AuthTokenHelper.AuthToken clientId) {
        auth.setToken(clientId);
    }

    @Step("Verify resource not found")
    public void verifyResourceNotFound() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Step("Verify no error")
    public void verifyNoError() {
        assertThat(errorResponse).isNull();
    }

    protected void init() {
        errorResponse = null;
        resources = null;
        pageMetaData = null;
    }

    protected <T> void buildResourceData(final ResponseEntity<List<T>> receivedResponse) {
        this.pageMetaData = buildPageMetaData(receivedResponse.getHeaders());
        this.resources = receivedResponse.getBody();
    }

    protected void setErrorResponse(final ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    protected HttpEntity<?> createEntity(final Object entity) {
        return createEntity(entity, null);
    }

    protected HttpEntity<?> createEntity(final Object entity, final Map<String, String> extraHeaders) {
        final var headers = new HttpHeaders();

        if (auth.getToken() != null) {
            headers.add("Authorization", "bearer " + auth.getToken());
        }

        if (extraHeaders != null) {
            extraHeaders.forEach(headers::add);
        }

        return new HttpEntity<>(entity, headers);
    }

    void verifyLocalDate(final LocalDate actual, final String expected) {
        if (Objects.nonNull(actual)) {
            assertThat(actual).isEqualTo(expected);
        } else {
            assertThat(StringUtils.EMPTY).isEqualTo(StringUtils.trimToEmpty(expected));
        }
    }

    void verifyEnum(final Enum<?> actual, final String expected) {
        if (Objects.nonNull(actual)) {
            assertThat(actual.toString()).isEqualTo(expected);
        } else {
            assertThat(StringUtils.EMPTY).isEqualTo(StringUtils.trimToEmpty(expected));
        }
    }

    private Page<?> buildPageMetaData(final HttpHeaders headers) {
        final Page<?> metaData;

        final var totals = headers.get("Total-Records");

        if ((totals != null) && !totals.isEmpty()) {
            final var totalRecords = Long.parseLong(totals.get(0));
            final var offsets = headers.get("Page-Offset");
            final var returnedOffset = Long.parseLong(offsets.get(0));
            final var limits = headers.get("Page-Limit");
            final var returnedLimit = Long.parseLong(limits.get(0));

            metaData = new Page<>(null, totalRecords, returnedOffset, returnedLimit);
        } else {
            metaData = null;
        }

        return metaData;
    }
}
