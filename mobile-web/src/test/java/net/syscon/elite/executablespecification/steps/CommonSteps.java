package net.syscon.elite.executablespecification.steps;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.test.ErrorResponseErrorHandler;
import net.thucydides.core.annotations.Step;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Common BDD step implementations
 */
@Slf4j
public abstract class CommonSteps {
    public static final String API_PREFIX = "/api/";

    @Autowired
    private AuthenticationSteps auth;

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
    public void verifyResourceRecordsReturned(long expectedCount) {
        assertThat(Integer.valueOf(resources.size()).longValue()).isEqualTo(expectedCount);
    };

    @Step("Verify total number of resource records available")
    public void verifyTotalResourceRecordsAvailable(long expectedCount) {
        assertThat(pageMetaData.getTotalRecords()).isEqualTo(expectedCount);
    }

    @Step("Verify no resource records returned")
    public void verifyNoResourceRecordsReturned() {
        assertThat(resources.isEmpty()).isTrue();
    }

    @Step("User {0} authenticates with password {1}")
    public void authenticates(String username, String password, boolean clientCredentials, String clientId) {
        errorResponse = auth.authenticate(StringUtils.upperCase(username), password, clientCredentials, clientId);
    }

    @Step("Refreshes with token")
    public void refresh(OAuth2AccessToken token) {
        errorResponse = auth.refresh(token);
    }

    @Step("Verify authentication token")
    public void verifyToken() {
        assertThat(auth.getToken().getValue()).isNotEmpty();
    }

    @Step("Verify authentication refresh token")
    public void verifyRefreshToken() {
        assertThat(auth.getToken().getRefreshToken().getValue()).isNotEmpty();
    }

    @Step("Verify resource not found")
    public void verifyResourceNotFound() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        assertThat(errorResponse.getDeveloperMessage()).as("Test is calling incorrect path/uri").isEmpty();
    }

    @Step("Verify user message in error response")
    public void verifyErrorUserMessage(String expectedUserMessage) {
        assertThat(errorResponse.getUserMessage()).isEqualTo(expectedUserMessage);
    }

    @Step("Verify 500 error")
    public void verify500Error() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Step("Verify bad request")
    public void verifyBadRequest(String expectedUserMessage) {
        verifyBadRequest(Collections.singletonList(expectedUserMessage));
    }

    public void verifyBadRequest(List<String> expectedUserMessages) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(errorResponse.getUserMessage()).contains(expectedUserMessages);
    }

    @Step("Verify access denied")
    public void verifyAccessDenied() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Step("Verify access denied")
    public void verifyAccessDenied(String expectedUserMessage) {
        verifyAccessDenied(Collections.singletonList(expectedUserMessage));
    }

    public void verifyAccessDenied(List<String> expectedUserMessages) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(errorResponse.getUserMessage()).contains(expectedUserMessages);
    }

    @Step("Verify not authorised")
    public void verifyNotAuthorised() {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Step("Verify resource conflict")
    public void verifyResourceConflict(String expectedUserMessage) {
        verifyResourceConflict(Collections.singletonList(expectedUserMessage));
    }

    public void verifyResourceConflict(List<String> expectedUserMessages) {
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus().intValue()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(errorResponse.getUserMessage()).contains(expectedUserMessages);
    }

    @Step("Verify no error")
    public void verifyNoError() {
        assertThat(errorResponse).isNull();
    }

    @Step("Apply pagination")
    public void applyPagination(Long offset, Long limit) {
        if (limit == null) {
            paginationLimit = 10L;
        } else {
            paginationLimit = limit;
        }

        if (offset == null) {
            paginationOffset = 0;
        } else {
            paginationOffset = offset;
        }
    }

    protected void init() {
        paginationLimit = 10;
        paginationOffset = 0;
        errorResponse = null;
        resources = null;
        pageMetaData = null;
    }

    protected <T> void buildResourceData(ResponseEntity<List<T>> receivedResponse) {
        this.pageMetaData = buildPageMetaData(receivedResponse.getHeaders());
        this.resources = receivedResponse.getBody();
    }

    protected void setResourceMetaData(List<?> resources) {
        this.resources = resources;
    }

    protected void setErrorResponse(ErrorResponse errorResponse) {
        log.error("API error: {}", errorResponse.toString());
        this.errorResponse = errorResponse;
    }

    protected HttpEntity<?> createEntity() {
        return createEntity(null, null);
    }

    protected HttpEntity<?> createEntity(Object entity) {
        return createEntity(entity, null);
    }

    protected HttpEntity<?> createEntity(Object entity, Map<String, String> extraHeaders) {
        HttpHeaders headers = new HttpHeaders();

        if (auth.getToken() != null) {
            headers.add(auth.getAuthenticationHeader(), "bearer "+auth.getToken().getValue());
        }

        if (extraHeaders != null) {
            extraHeaders.forEach(headers::add);
        }

        return new HttpEntity<>(entity, headers);
    }

    protected Map<String,String> csv2map(String commaSeparatedList) {
        Map<String,String> out;

        if (StringUtils.isBlank(commaSeparatedList)) {
            out = Collections.emptyMap();
        } else {
            out = Pattern.compile("\\s*,\\s*")
                    .splitAsStream(commaSeparatedList.trim())
                    .map(s -> s.split("=", 2))
                    .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1]: ""));
        }

        return out;
    }

    protected List<String> csv2list(String commaSeparatedList) {
        List<String> out;

        if (StringUtils.isBlank(commaSeparatedList)) {
            out = Collections.emptyList();
        } else {
            out = Arrays.asList(commaSeparatedList.split("\\s*,\\s*"));
        }

        return out;
    }

    protected void verifyIdentical(List<String> listActual, List<String> listExpected) {
        // Both lists are expected to be provided (i.e. non-null). Empty lists are ok.
        // Sorting and converting back to String so that details of non-matching lists are clearly disclosed
        Collections.sort(listActual);
        Collections.sort(listExpected);

        String actual = String.join(",", listActual);
        String expected = String.join(",", listExpected);

        assertThat(actual).isEqualTo(expected);
    }

    protected void verifyIdentical(Map<String,String> mapActual, Map<String,String> mapExpected) {
        // Both maps are expected to be provided (i.e. non-null). Empty maps are ok.
        // Key/Value pairs converted to String so that details of non-matching entries are clearly disclosed
        for (Map.Entry<String,String> entry : mapExpected.entrySet()) {
            assertThat(StringUtils.join(entry.getKey(), " = ", mapActual.remove(entry.getKey())))
                    .isEqualTo(StringUtils.join(entry.getKey(), " = ", entry.getValue()));
        }

        // Finally, assert that there are no entries left in actual map (which indicates map contents are identical)
        // Again, using String comparison so any discrepancies are made clear
        String actualRemaining =
                mapActual.entrySet().stream()
                        .map(entry -> entry.getKey() + " = " + entry.getValue())
                        .collect(Collectors.joining(", "));

        assertThat(actualRemaining).isEqualTo("");
    }

    protected <T> List<String> extractPropertyValues(Collection<T> actualCollection, Function<T,String> mapper) {
        List<String> extractedVals = new ArrayList<>();

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

    protected <T> Map<String,String> extractPropertyValuesToMap(Collection<T> actualCollection,
                                                                 Function<T,String> keyMapper,
                                                                 Function<T,String> valMapper) {
        Map<String,String> extractedPropMap = new HashMap<>();

        if (actualCollection != null) {
            extractedPropMap.putAll(actualCollection.stream().collect(Collectors.toMap(keyMapper, valMapper)));
        }

        return extractedPropMap;
    }

    protected <T> List<String> extractLocalDateValues(Collection<T> actualCollection, Function<T,LocalDate> mapper) {
        List<String> extractedVals = new ArrayList<>();
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    protected <T> List<String> extractLocalDateTimeValues(Collection<T> actualCollection, Function<T,LocalDateTime> mapper) {
        List<String> extractedVals = new ArrayList<>();
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    protected <T> List<String> extractLocalTimeValues(Collection<T> actualCollection, Function<T,LocalDateTime> mapper) {
        List<String> extractedVals = new ArrayList<>();
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

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

    protected <T> List<String> extractDateValues(Collection<T> actualCollection, Function<T,Date> mapper) {
        List<String> extractedVals = new ArrayList<>();
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    protected <T> List<String> extractLongValues(Collection<T> actualCollection, Function<T,Long> mapper) {
        List<String> extractedVals = new ArrayList<>();

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

    protected <T> void verifyPropertyValues(Collection<T> actualCollection,
                                            Function<T,String> mapper,
                                            String expectedValues) {
        List<String> actualValList = extractPropertyValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLongValues(Collection<T> actualCollection,
                                        Function<T,Long> mapper,
                                        String expectedValues) {
        List<String> actualValList = extractLongValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLocalDateValues(Collection<T> actualCollection,
                                        Function<T,LocalDate> mapper,
                                        String expectedValues) {
        List<String> actualValList = extractLocalDateValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLocalDateTimeValues(Collection<T> actualCollection, Function<T, LocalDateTime> mapper,
            String expectedValues) {
        List<String> actualValList = extractLocalDateTimeValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLocalTimeValues(Collection<T> actualCollection, Function<T, LocalDateTime> mapper,
            String expectedValues) {
        List<String> actualValList = extractLocalTimeValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyDateValues(Collection<T> actualCollection,
                                            Function<T,Date> mapper,
                                            String expectedValues) {
        List<String> actualValList = extractDateValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyPropertyMapValues(Collection<T> actualCollection,
                                               Function<T,String> keyMapper,
                                               Function<T,String> valMapper,
                                               String expectedMapValues) {
        Map<String,String> actualPropertyMap = extractPropertyValuesToMap(actualCollection, keyMapper, valMapper);
        Map<String,String> expectedPropertyMap = csv2map(expectedMapValues);

        verifyIdentical(actualPropertyMap, expectedPropertyMap);
    }

    protected void verifyPropertyValue(Object bean, String propertyName, String expectedValue) throws ReflectiveOperationException {
        verifyField(bean, propertyName, expectedValue);
    }

    protected void verifyField(Object bean, String fieldName, String expectedValue) throws ReflectiveOperationException {
        assertNotNull(bean);
        PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
        final Object actual = propertyUtilsBean.getProperty(bean, fieldName);

        if (StringUtils.isBlank(expectedValue)) {
            assertNull(actual);
        } else {
            if (actual instanceof BigDecimal) {
                // Assume a monetary value with 2dp
                assertEquals(expectedValue, ((BigDecimal) actual).setScale(2).toString());
            } else {
                assertEquals(expectedValue, actual.toString());
            }
        }

    }

    protected void verifyLocalDate(LocalDate actual, String expected) {
        if (Objects.nonNull(actual)) {
            assertThat(actual).isEqualTo(expected);
        } else {
            assertThat(StringUtils.EMPTY).isEqualTo(StringUtils.trimToEmpty(expected));
        }
    }

    protected void verifyLocalDateTime(LocalDateTime actual, String expected) {
        if (Objects.nonNull(actual)) {
            assertThat(actual).isEqualTo(StringUtils.replaceFirst(expected, "\\s{1}", "T"));
        } else {
            assertThat(StringUtils.EMPTY).isEqualTo(StringUtils.trimToEmpty(expected));
        }
    }

    protected void verifyEnum(Enum<?> actual, String expected) {
        if (Objects.nonNull(actual)) {
            assertThat(actual.toString()).isEqualTo(expected);
        } else {
            assertThat(StringUtils.EMPTY).isEqualTo(StringUtils.trimToEmpty(expected));
        }
    }

    protected String buildQuery(String queryParam) {
        return "?query=" + StringUtils.trimToEmpty(queryParam);
    }

    protected Map<String,String> addPaginationHeaders() {
        return ImmutableMap.of("Page-Offset", String.valueOf(paginationOffset), "Page-Limit", String.valueOf(paginationLimit));
    }

    protected Map<String,String> buildSortHeaders(String sortFields, Order sortOrder) {
        Map<String,String> sortHeaders = new HashMap<>();

        if (StringUtils.isNotBlank(sortFields)) {
            sortHeaders.put("Sort-Fields", sortFields);
        }

        if (Objects.nonNull(sortOrder)) {
            sortHeaders.put("Sort-Order", sortOrder.toString());
        }

        return sortHeaders.isEmpty() ? null : ImmutableMap.copyOf(sortHeaders);
    }

    protected void validateResourcesIndex(int index) {
        assertThat(index).isGreaterThan(-1);
        assertThat(index).isLessThan(resources.size());
    }

    private Page<?> buildPageMetaData(HttpHeaders headers) {
        Page<?> metaData;

        List<String> totals = headers.get("Total-Records");

        if ((totals != null) && !totals.isEmpty()) {
            Long totalRecords = Long.valueOf(totals.get(0));
            List<String> offsets = headers.get("Page-Offset");
            Long returnedOffset = Long.valueOf(offsets.get(0));
            List<String> limits = headers.get("Page-Limit");
            Long returnedLimit = Long.valueOf(limits.get(0));

            metaData = new Page<>(null, totalRecords, returnedOffset, returnedLimit);
        } else {
            metaData = null;
        }

        return metaData;
    }

    /**
     * Equality assertion where blank and null are treated as equal
     */
    protected static void assertEqualsBlankIsNull(String expected, String actual) {
        if (StringUtils.isBlank(actual) && StringUtils.isBlank(expected) ) {
            return;
        }
        assertEquals(expected, actual);
    }

    public AuthenticationSteps getAuth() {
        return auth;
    }

    public   void authenticateAsClient(String clientId){
        auth.authenticateAsClient(clientId);
    }
}
