package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.PageMetaData;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common BDD step implementations
 */
public abstract class CommonSteps {
    public static final String API_PREFIX = "/api/";

    @Autowired
    private AuthenticationSteps auth;

    @Autowired
    protected TestRestTemplate restTemplate;

    private List<?> resources;
    private PageMetaData pageMetaData;

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

    protected void setResourceMetaData(List<?> resources, PageMetaData pageMetaData) {
        this.resources = resources;
        this.pageMetaData = pageMetaData;
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
}
