package uk.gov.justice.hmpps.prison.executablespecification.steps;

import com.google.common.collect.ImmutableMap;
import net.thucydides.core.annotations.Step;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.test.ErrorResponseErrorHandler;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private long paginationLimit;
    private long paginationOffset;

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

    @Step("Verify no resource records returned")
    public void verifyNoResourceRecordsReturned() {
        assertThat(resources.isEmpty()).isTrue();
    }

    public void authenticateAsClient(final AuthTokenHelper.AuthToken clientId) {
        auth.setToken(clientId);
    }

    @Step("Verify resource not found")
    public void verifyResourceNotFound() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Step("Verify user message in error response")
    public void verifyErrorUserMessage(final String expectedUserMessage) {
        assertThat(errorResponse.getUserMessage()).isEqualTo(expectedUserMessage);
    }

    @Step("Verify 500 error")
    public void verify500Error() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Step("Verify bad request")
    public void verifyBadRequest(final String expectedUserMessage) {
        verifyBadRequest(Collections.singletonList(expectedUserMessage));
    }

    public void verifyBadRequest(final List<String> expectedUserMessages) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(errorResponse.getUserMessage()).contains(expectedUserMessages);
    }

    @Step("Verify access denied")
    public void verifyAccessDenied() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Step("Verify access denied")
    public void verifyAccessDenied(final String expectedUserMessage) {
        verifyAccessDenied(Collections.singletonList(expectedUserMessage));
    }

    private void verifyAccessDenied(final List<String> expectedUserMessages) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(errorResponse.getUserMessage()).contains(expectedUserMessages);
    }

    @Step("Verify not authorised")
    private void verifyNotAuthorised() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Step("Verify not authorised")
    public void verifyUnapprovedClient() {
        verifyNotAuthorised();
        // unfortunately not able to access the status text that is returned to the client thought the oauth2template.
    }

    @Step("Verify resource conflict")
    public void verifyResourceConflict(final String expectedUserMessage) {
        verifyResourceConflict(Collections.singletonList(expectedUserMessage));
    }

    private void verifyResourceConflict(final List<String> expectedUserMessages) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(errorResponse.getUserMessage()).contains(expectedUserMessages);
    }

    @Step("Verify no error")
    public void verifyNoError() {
        assertThat(errorResponse).isNull();
    }

    @Step("Apply pagination")
    public void applyPagination(final Long offset, final Long limit) {
        paginationLimit = Objects.requireNonNullElse(limit, 10L);
        paginationOffset = Objects.requireNonNullElse(offset, 0L);
    }

    protected void init() {
        paginationLimit = 10;
        paginationOffset = 0;
        errorResponse = null;
        resources = null;
        pageMetaData = null;
    }

    protected <T> void buildResourceData(final ResponseEntity<List<T>> receivedResponse) {
        this.pageMetaData = buildPageMetaData(receivedResponse.getHeaders());
        this.resources = receivedResponse.getBody();
    }

    protected <T> void buildResourceData(final org.springframework.data.domain.Page<T> receivedResponse) {
        this.pageMetaData = buildPageMetaData(receivedResponse);
        this.resources = receivedResponse.getContent();
    }
    void setResourceMetaData(final List<?> resources) {
        this.resources = resources;
    }

    protected void setErrorResponse(final ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    protected ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    protected HttpEntity<?> createEntity() {
        return createEntity(null, null);
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

    private Map<String, String> csv2map(final String commaSeparatedList) {
        final Map<String, String> out;

        if (StringUtils.isBlank(commaSeparatedList)) {
            out = Collections.emptyMap();
        } else {
            out = Pattern.compile("\\s*,\\s*")
                    .splitAsStream(commaSeparatedList.trim())
                    .map(s -> s.split("=", 2))
                    .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));
        }

        return out;
    }

    List<String> csv2list(final String commaSeparatedList) {
        final List<String> out;

        if (StringUtils.isBlank(commaSeparatedList)) {
            out = Collections.emptyList();
        } else {
            out = Arrays.asList(commaSeparatedList.split("\\s*,\\s*"));
        }

        return out;
    }

    void verifyIdentical(final List<String> listActual, final List<String> listExpected) {
        // Both lists are expected to be provided (i.e. non-null). Empty lists are ok.
        // Sorting and converting back to String so that details of non-matching lists are clearly disclosed
        Collections.sort(listActual);
        Collections.sort(listExpected);

        final var actual = String.join(",", listActual);
        final var expected = String.join(",", listExpected);

        assertThat(actual).isEqualTo(expected);
    }

    private void verifyIdentical(final Map<String, String> mapActual, final Map<String, String> mapExpected) {
        // Both maps are expected to be provided (i.e. non-null). Empty maps are ok.
        // Key/Value pairs converted to String so that details of non-matching entries are clearly disclosed
        for (final var entry : mapExpected.entrySet()) {
            assertThat(StringUtils.join(entry.getKey(), " = ", mapActual.remove(entry.getKey())))
                    .isEqualTo(StringUtils.join(entry.getKey(), " = ", entry.getValue()));
        }

        // Finally, assert that there are no entries left in actual map (which indicates map contents are identical)
        // Again, using String comparison so any discrepancies are made clear
        final var actualRemaining =
                mapActual.entrySet().stream()
                        .map(entry -> entry.getKey() + " = " + entry.getValue())
                        .collect(Collectors.joining(", "));

        assertThat(actualRemaining).isEqualTo("");
    }

    private <T> List<String> extractPropertyValues(final Collection<T> actualCollection, final Function<T, String> mapper) {
        final List<String> extractedVals = new ArrayList<>();

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    private <T> Map<String, String> extractPropertyValuesToMap(final Collection<T> actualCollection,
                                                               final Function<T, String> keyMapper,
                                                               final Function<T, String> valMapper) {
        final Map<String, String> extractedPropMap = new HashMap<>();

        if (actualCollection != null) {
            extractedPropMap.putAll(actualCollection.stream().collect(Collectors.toMap(keyMapper, valMapper)));
        }

        return extractedPropMap;
    }

    private <T> List<String> extractLocalDateValues(final Collection<T> actualCollection, final Function<T, LocalDate> mapper) {
        final List<String> extractedVals = new ArrayList<>();
        final var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(Objects::nonNull)
                            .map(date -> date.format(dateTimeFormatter))
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    private <T> List<String> extractLocalDateTimeValues(final Collection<T> actualCollection, final Function<T, LocalDateTime> mapper) {
        final List<String> extractedVals = new ArrayList<>();
        final var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(Objects::nonNull)
                            .map(date -> date.format(dateTimeFormatter))
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    private <T> List<String> extractLocalTimeValues(final Collection<T> actualCollection, final Function<T, LocalDateTime> mapper) {
        final List<String> extractedVals = new ArrayList<>();
        final var dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(Objects::nonNull)
                            .map(date -> date.format(dateTimeFormatter))
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    private <T> List<String> extractDateValues(final Collection<T> actualCollection, final Function<T, Date> mapper) {
        final List<String> extractedVals = new ArrayList<>();
        final var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(Objects::nonNull)
                            .map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(dateTimeFormatter))
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    private <T> List<String> extractLongValues(final Collection<T> actualCollection, final Function<T, Long> mapper) {
        final List<String> extractedVals = new ArrayList<>();

        if (actualCollection != null) {
            extractedVals.addAll(
                    actualCollection
                            .stream()
                            .map(mapper)
                            .filter(Objects::nonNull)
                            .map(String::valueOf)
                            .collect(Collectors.toList())
            );
        }

        return extractedVals;
    }

    protected <T> void verifyPropertyValues(final Collection<T> actualCollection,
                                            final Function<T, String> mapper,
                                            final String expectedValues) {
        final var actualValList = extractPropertyValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    <T> void verifyLongValues(final Collection<T> actualCollection,
                              final Function<T, Long> mapper,
                              final String expectedValues) {
        final var actualValList = extractLongValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    <T> void verifyLocalDateValues(final Collection<T> actualCollection,
                                   final Function<T, LocalDate> mapper,
                                   final String expectedValues) {
        final var actualValList = extractLocalDateValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLocalDateTimeValues(final Collection<T> actualCollection, final Function<T, LocalDateTime> mapper,
                                                 final String expectedValues) {
        final var actualValList = extractLocalDateTimeValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    <T> void verifyLocalTimeValues(final Collection<T> actualCollection, final Function<T, LocalDateTime> mapper,
                                   final String expectedValues) {
        final var actualValList = extractLocalTimeValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyDateValues(final Collection<T> actualCollection,
                                        final Function<T, Date> mapper,
                                        final String expectedValues) {
        final var actualValList = extractDateValues(actualCollection, mapper);
        final var expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    <T> void verifyPropertyMapValues(final Collection<T> actualCollection,
                                     final Function<T, String> keyMapper,
                                     final Function<T, String> valMapper,
                                     final String expectedMapValues) {
        final var actualPropertyMap = extractPropertyValuesToMap(actualCollection, keyMapper, valMapper);
        final var expectedPropertyMap = csv2map(expectedMapValues);

        verifyIdentical(actualPropertyMap, expectedPropertyMap);
    }

    void verifyPropertyValue(final Object bean, final String propertyName, final String expectedValue) throws ReflectiveOperationException {
        verifyField(bean, propertyName, expectedValue);
    }

    protected void verifyField(final Object bean, final String fieldName, final String expectedValue) throws ReflectiveOperationException {
        assertThat(bean).isNotNull();
        final var propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
        final var actual = propertyUtilsBean.getProperty(bean, fieldName);

        if (StringUtils.isBlank(expectedValue)) {
            assertThat(actual).isNull();
        } else {
            if (actual instanceof BigDecimal) {
                // Assume a monetary value with 2dp
                assertThat(((BigDecimal) actual).setScale(2, RoundingMode.HALF_UP).toString()).isEqualTo(expectedValue);
            } else {
                assertThat(actual.toString()).isEqualTo(expectedValue);
            }
        }

    }

    void verifyLocalDate(final LocalDate actual, final String expected) {
        if (Objects.nonNull(actual)) {
            assertThat(actual).isEqualTo(expected);
        } else {
            assertThat(StringUtils.EMPTY).isEqualTo(StringUtils.trimToEmpty(expected));
        }
    }

    void verifyLocalDateTime(final LocalDateTime actual, final String expected) {
        if (Objects.nonNull(actual)) {
            assertThat(actual).isEqualTo(RegExUtils.replaceFirst(expected, "\\s{1}", "T"));
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

    String buildQuery(final String queryParam) {
        return "?query=" + StringUtils.trimToEmpty(queryParam);
    }

    String buildQueryStringParameters(final Map<String, String> parameters) {
        return parameters.keySet()
                .stream()
                .map(key -> String.format("%s=%s", key, parameters.get(key)))
                .collect(Collectors.joining("&"));
    }

    protected Map<String, String> addPaginationHeaders() {
        return ImmutableMap.of("Page-Offset", String.valueOf(paginationOffset), "Page-Limit", String.valueOf(paginationLimit));
    }

    Map<String, String> buildSortHeaders(final String sortFields, final Order sortOrder) {
        final Map<String, String> sortHeaders = new HashMap<>();

        if (StringUtils.isNotBlank(sortFields)) {
            sortHeaders.put("Sort-Fields", sortFields);
        }

        if (Objects.nonNull(sortOrder)) {
            sortHeaders.put("Sort-Order", sortOrder.toString());
        }

        return sortHeaders.isEmpty() ? null : ImmutableMap.copyOf(sortHeaders);
    }

    void validateResourcesIndex(final int index) {
        assertThat(index).isGreaterThan(-1);
        assertThat(index).isLessThan(resources.size());
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

    private <T> Page<T> buildPageMetaData(final org.springframework.data.domain.Page<T> page) {
        return new Page<T>(page.getContent(), page.getTotalElements(), page.getPageable().getOffset(), page.getPageable().getPageSize());

    }
    /**
     * Equality assertion where blank and null are treated as equal
     */
    static void assertEqualsBlankIsNull(final String expected, final String actual) {
        if (StringUtils.isBlank(actual) && StringUtils.isBlank(expected)) {
            return;
        }
        assertThat(actual).isEqualTo(expected);
    }

    void assertErrorResponse(final HttpStatus expectedStatusCode) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(expectedStatusCode.value());
    }
}
