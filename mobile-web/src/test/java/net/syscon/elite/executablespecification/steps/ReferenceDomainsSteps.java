package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.test.EliteClientException;
import net.syscon.util.QueryOperator;
import net.thucydides.core.annotations.Step;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * BDD step implementations for Reference Domains service.
 */
public class ReferenceDomainsSteps extends CommonSteps {
    private static final String API_QUERY_PREFIX = API_PREFIX + "reference-domains?query=";
    private static final String API_REF_PREFIX = API_PREFIX + "reference-domains/";
    private static final String API_TYPES_URL = API_REF_PREFIX + "caseNoteTypes";
    private static final String API_SUBTYPES_URL = API_REF_PREFIX + "caseNoteSubTypes";
    private static final String API_ALERTS_URL = API_REF_PREFIX + "alertTypes";
    private static final String API_SOURCES_URL = API_REF_PREFIX + "caseNoteSources";

    private List<ReferenceCode> results;
    private ReferenceCode result;

    @Step("Perform search using full last name")
    public void fullLastNameSearch(String criteria) {
        String query = buildSimpleQuery("lastName", criteria);
        dispatchQuery(query);
    }

    @Step("Perform search using partial first name")
    public void partialFirstNameSearch(String criteria) {
        String query = buildSimpleQuery("firstName", criteria);
        dispatchQuery(query);
    }

    @Step("Perform reference domain type search without subtypes")
    public void getAllTypes(boolean includeSubTypes) {

        String url = API_TYPES_URL;
        if (includeSubTypes) {
            url += "?includeSubTypes=true";
        }
        doListApiCall(url);
    }

    public void getAllAlertTypes() {
        doListApiCall(API_ALERTS_URL);
    }

    public void getAllSources() {
        doListApiCall(API_SOURCES_URL);
    }

    @Step("Verify reference domain type data without subtypes")
    public void verifySomeSampleTypeData() {
        assertEquals(52, results.size());
        assertEquals("VICTIM", results.get(0).getCode());
        assertEquals("Victim", results.get(0).getDescription());
        assertEquals("TASK_TYPE", results.get(0).getDomain());
        assertEquals("Y", results.get(0).getActiveFlag());
        assertEquals("RR", results.get(10).getCode());
        assertEquals("Accredited Programme", results.get(51).getDescription());
    }

    public void verifySomeTypesAndSubtypes() {
        // verifySomeSampleTypeData(); different order !
        assertEquals(52, results.size());
        assertEquals("ACP", results.get(0).getCode());
        assertEquals(16, results.get(0).getSubCodes().size());
        assertEquals("CPS", results.get(0).getSubCodes().get(0).getCode());
        assertEquals("Core Programme Session", results.get(0).getSubCodes().get(0).getDescription());
        assertEquals("TASK_SUBTYPE", results.get(0).getSubCodes().get(0).getDomain());
        assertEquals("Y", results.get(0).getSubCodes().get(0).getActiveFlag());
    }

    public void verifySomeAlertTypes() {
        assertEquals(13, results.size());
        assertEquals("X", results.get(0).getCode());
        assertEquals("Security", results.get(0).getDescription());
        assertEquals("ALERT", results.get(0).getDomain());
        assertEquals("Y", results.get(0).getActiveFlag());
        assertEquals("H", results.get(10).getCode());
        assertEquals("Social Care", results.get(12).getDescription());
    }

    public void verifyAllSources() {
        assertEquals(4, results.size());
        assertEquals("INST", results.get(0).getCode());
        assertEquals("Prison", results.get(0).getDescription());
        assertEquals("NOTE_SOURCE", results.get(0).getDomain());
        assertEquals("Y", results.get(0).getActiveFlag());
        assertEquals("COMM", results.get(2).getCode());
        assertEquals("System", results.get(3).getDescription());
    }

    public void getAlertType(String code) {
        doSingleResultApiCall(API_ALERTS_URL + '/' + code);
    }

    public void getType(String code) {
        doSingleResultApiCall(API_TYPES_URL + '/' + code);
    }

    public void getSubtype(String code, String subCode) {
        doSingleResultApiCall(API_SUBTYPES_URL + '/' + code + "/subTypes/" + subCode);
    }

    public void getAlertCode(String code, String subCode) {
        doSingleResultApiCall(API_ALERTS_URL + '/' + code + "/codes/" + subCode);
    }

    public void getSource(String code) {
        doSingleResultApiCall(API_SOURCES_URL + '/' + code);
    }

    public void verifyField(String fieldName, String expected) throws ReflectiveOperationException {
        final String actual = BeanUtilsBean.getInstance().getProperty(result, fieldName);
        if (StringUtils.isBlank(expected)) {
            assertNull(actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    private void dispatchQuery(String query) {
        init();

        String queryUrl = API_QUERY_PREFIX + StringUtils.trimToEmpty(query);

        ResponseEntity<List<ReferenceCode>> response = restTemplate.exchange(queryUrl, HttpMethod.GET,
                createEntity(null, addPaginationHeaders()), new ParameterizedTypeReference<List<ReferenceCode>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        results = response.getBody();
        buildResourceData(response);
    }

    private void doListApiCall(String url) {
        init();
        applyPagination(0L, 1000L);
        ResponseEntity<List<ReferenceCode>> response = restTemplate.exchange(url, HttpMethod.GET,
                createEntity(null, addPaginationHeaders()), new ParameterizedTypeReference<List<ReferenceCode>>() {
                });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        results = response.getBody();
    }

    private void doSingleResultApiCall(String url) {
        init();
        try {
            ResponseEntity<ReferenceCode> response = restTemplate.exchange(url, HttpMethod.GET,
                    createEntity(null, null), new ParameterizedTypeReference<ReferenceCode>() {
                    });
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            result = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private String buildSimpleQuery(String fieldName, Object criteria) {
        StringJoiner sj = new StringJoiner(":");

        sj.add(fieldName);
        sj.add(deriveOperator(criteria).getJsonOperator());
        sj.add(parseCriteria(criteria));

        return sj.toString();
    }

    private QueryOperator deriveOperator(Object criteria) {
        QueryOperator operator;

        if (criteria instanceof String) {
            operator = (((String) criteria).contains("%") ? QueryOperator.LIKE : QueryOperator.EQUAL);
        } else {
            operator = QueryOperator.EQUAL;
        }

        return operator;
    }

    private String parseCriteria(Object criteria) {
        String formatter;

        if (criteria instanceof String) {
            formatter = "'%s'";
        } else {
            formatter = "%s";
        }

        return String.format(formatter, criteria);
    }

    protected void init() {
        super.init();

        results = null;
    }

    public void getSubtypeList(String code) {
        doListApiCall(API_SUBTYPES_URL + '/' + code);
    }

    public void getAlertCodeList(String code) {
        doListApiCall(API_ALERTS_URL + '/' + code + "/codes");
    }

    public void verifyListSize(int size) {
        assertEquals(size, results.size());
    }

    public void verifyListDomain(String domain) {
        assertEquals(domain, results.get(0).getDomain());
    }

    public void verifyListFirstCode(String code) {
        assertEquals(code, results.get(0).getCode());
    }

    public void verifyListFirstDescription(String description) {
        assertEquals(description, results.get(0).getDescription());
    }
}
