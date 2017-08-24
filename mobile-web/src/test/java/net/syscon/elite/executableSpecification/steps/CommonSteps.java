package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.PageMetaData;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common BDD step implementations
 */
public abstract class CommonSteps {
    public static final String API_PREFIX = "/";
    public static final String V2_API_PREFIX = "/v2/";

    @Autowired
    private AuthenticationSteps auth;

    @Autowired
    protected TestRestTemplate restTemplate;

    private List<?> resources;
    private PageMetaData pageMetaData;
    private Map<String, Object> additionalProperties = new HashMap<>();
    private ResponseEntity receievedResponse;
    private long paginationLimit;
    private long paginationOffset;

    @Step("Verify number of resource records returned")
    public void verifyResourceRecordsReturned(long expectedCount) {
        assertThat(Integer.valueOf(resources.size()).longValue()).isEqualTo(expectedCount);
    };

    @Step("Verify total number of resource records available")
    public void verifyTotalResourceRecordsAvailable(long expectedCount) {
        assertThat(pageMetaData.getTotalRecords()).isEqualTo(expectedCount);
    }

    @Step("User {0} authenticates with password {1}")
    public void authenticates(String username, String password) {
        auth.authenticate(username, password);
    }

    @Step("Verify authentication token")
    public void verifyToken() {
        assertThat(auth.getToken()).isNotEmpty();
    }

    @Step("Verify HTTP status response")
    public void verifyHttpStatusResponse(int statusCode) {
        assertThat(receievedResponse.getStatusCode().value()).isEqualTo(statusCode);
    }

    @Step("Apply pagination")
    public void applyPagination(String limit, String offset) {
        if (StringUtils.isBlank(limit)) {
            paginationLimit = 0;
        } else {
            paginationLimit = Long.valueOf(limit);
        }

        if (StringUtils.isBlank(offset)) {
            paginationOffset = 0;
        } else {
            paginationOffset = Long.valueOf(offset);
        }
    }

    protected void init() {
        paginationLimit = 0;
        paginationOffset = 0;
    }

    protected void setResourceMetaData(List<?> resources, PageMetaData pageMetaData) {
        this.resources = resources;
        this.pageMetaData = pageMetaData;
    }

    protected void setAdditionalResponseProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties.putAll(additionalProperties);
    }

    protected void setReceivedResponse(ResponseEntity receievedResponse) {
        this.receievedResponse = receievedResponse;
    }

    protected HttpEntity createEntity() {
        return createEntity(null, null);
    }

    protected HttpEntity createEntity(Object entity) {
        return createEntity(entity, null);
    }

    protected HttpEntity createEntity(Object entity, Map<String, String> extraHeaders) {
        HttpHeaders headers = new HttpHeaders();

        if (auth.getToken() != null) {
            headers.add(auth.getAuthenticationHeader(), auth.getToken());
        }

        if (extraHeaders != null) {
            extraHeaders.forEach(headers::add);
        }

        return new HttpEntity<>(entity, headers);
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

    protected <T> List<String> extractPropertyValues(Collection<T> actualCollection, Function<T, String> mapper) {
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

    protected <T> List<String> extractLongValues(Collection<T> actualCollection, Function<T, Long> mapper) {
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
                                            Function<T, String> mapper,
                                            String expectedValues) {
        List<String> actualValList = extractPropertyValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected <T> void verifyLongValues(Collection<T> actualCollection,
                                        Function<T, Long> mapper,
                                        String expectedValues) {
        List<String> actualValList = extractLongValues(actualCollection, mapper);
        List<String> expectedValList = csv2list(expectedValues);

        verifyIdentical(actualValList, expectedValList);
    }

    protected String buildQuery(String queryParam) {
        return addPaginationParams("?query=" + StringUtils.trimToEmpty(queryParam));
    }

    private String addPaginationParams(String query) {
        StringBuilder params = new StringBuilder(StringUtils.trimToEmpty(query));

        if ((paginationLimit > 0 ) || (paginationOffset > 0)) {
            params.append(StringUtils.isBlank(query) ? "?" : "&")
                    .append("limit=")
                    .append(paginationLimit)
                    .append("&")
                    .append("offset=")
                    .append(paginationOffset);
        }

        return params.toString();
    }
}
