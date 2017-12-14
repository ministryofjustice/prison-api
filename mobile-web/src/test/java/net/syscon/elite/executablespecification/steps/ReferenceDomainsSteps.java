package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.ScheduleReason;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * BDD step implementations for Reference Domains service.
 */
public class ReferenceDomainsSteps extends CommonSteps {
    private static final String API_REF_PREFIX = API_PREFIX + "reference-domains/";
    private static final String API_ALERT_TYPES_URL = API_REF_PREFIX + "alertTypes";
    private static final String API_CASE_NOTE_SOURCES_URL = API_REF_PREFIX + "caseNoteSources";
    private static final String API_CASE_NOTE_TYPES_URL = API_REF_PREFIX + "caseNoteTypes";
    private static final String API_DOMAINS_URL = API_REF_PREFIX + "domains/{domain}";
    private static final String API_DOMAINS_CODES_URL = API_REF_PREFIX + "domains/{domain}/codes/{code}";
    private static final String API_SCHEDULE_REASONS_URL = API_REF_PREFIX + "scheduleReasons";

    private List<ReferenceCode> referenceCodes;
    private ReferenceCode referenceCode;
    private List<ScheduleReason> scheduleReasons;

    @Step("Submit request for all alert types (with alert codes)")
    public void getAllAlertTypes() {
        dispatchListRequest(API_ALERT_TYPES_URL, null, null, 0L, 1000L);
    }

    @Step("Submit request for all case note sources")
    public void getAllSources() {
        dispatchListRequest(API_CASE_NOTE_SOURCES_URL, null, null, 0L, 1000L);
    }

    @Step("Submit request for used case note types")
    public void getUsedCaseNoteTypes() {
        dispatchListRequest(API_CASE_NOTE_TYPES_URL, null, null, null, null);
    }

    @Step("Submit request for reference codes in specified domain")
    public void getRefCodesForDomain(String domain, boolean withSubCodes) {
        dispatchListRequest(API_DOMAINS_URL, domain, withSubCodes, 0L, 1000L);
    }

    @Step("Submit request for reference code with specified domain and code")
    public void getRefCodeForDomainAndCode(String domain, String code, boolean withSubCodes) {
        dispatchObjectRequest(API_DOMAINS_CODES_URL, domain, code, withSubCodes);
    }

    @Step("Verify reference code property value")
    public void verifyRefCodePropertyValue(String propertyName, String propertyValue) throws Exception {
        verifyPropertyValue(referenceCode, propertyName, propertyValue);
    }

    @Step("Verify returned item has no sub-codes")
    public void verifyRefCodeNoSubCodes() {
        assertThat(referenceCode.getSubCodes()).isNull();
    }

    @Step("Verify sub-code count for returned item")
    public void verifySubCodeCount(long expectedCount) {
        assertThat(Integer.valueOf(referenceCode.getSubCodes().size()).longValue()).isEqualTo(expectedCount);
    }

    @Step("Verify domain for all sub-codes of specific reference code item")
    public void verifySubCodeDomain(int index, String expectedSubCodeDomain) {
        ReferenceCode refCode = referenceCodes.get(index);

        refCode.getSubCodes().forEach(sc -> {
            assertThat(sc.getDomain())
                    .as("Check domain for sub-code [%s] of code [%s]", sc.getCode(), refCode.getCode())
                    .isEqualTo(expectedSubCodeDomain);
        });
    }

    @Step("Verify domains for sub-codes of specific reference code item")
    public void verifySubCodeDomains(int index, String expectedSubCodeDomains) {
        ReferenceCode refCode = referenceCodes.get(index);

        List<String> actualSubCodeDomains = refCode.getSubCodes()
                .stream().map(ReferenceCode::getDomain).distinct().collect(Collectors.toList());

        verifyIdentical(actualSubCodeDomains, csv2list(expectedSubCodeDomains));
    }

    @Step("Verify code for specific sub-code of specific reference code item")
    public void verifyCodeForSubCode(int subCodeIndex, int refCodeIndex, String expectedSubCodeCode) {
        assertThat(referenceCodes.get(refCodeIndex).getSubCodes().get(subCodeIndex).getCode())
                .isEqualTo(expectedSubCodeCode);
    }

    @Step("Verify description for specific sub-code of specific reference code item")
    public void verifyDescriptionForSubCode(int subCodeIndex, int refCodeIndex, String expectedSubCodeDescription) {
        assertThat(referenceCodes.get(refCodeIndex).getSubCodes().get(subCodeIndex).getDescription())
                .isEqualTo(expectedSubCodeDescription);
    }

    @Step("Verify domain for all returned reference code items")
    public void verifyDomain(String expectedDomain) {
        referenceCodes.forEach(rc -> {
            assertThat(rc.getDomain())
                    .as("Check domain for code [%s]", rc.getCode())
                    .isEqualTo(expectedDomain);
        });
    }

    @Step("Verify parent domain for all returned reference code items")
    public void verifyParentDomain(String expectedParentDomain) {
        referenceCodes.forEach(rc -> {
            assertThat(rc.getParentDomain())
                    .as("Check parent domain for code [%s]", rc.getCode())
                    .isEqualTo(expectedParentDomain);
        });
    }

    @Step("Verify code for specific reference code item")
    public void verifyCode(int index, String expectedCode) {
        assertThat(referenceCodes.get(index).getCode()).isEqualTo(expectedCode);
    }

    @Step("Verify description for specific reference code item")
    public void verifyDescription(int index, String expectedDescription) {
        assertThat(referenceCodes.get(index).getDescription()).isEqualTo(expectedDescription);
    }

    @Step("Verify parent code for specific reference code item")
    public void verifyParentCode(int index, String expectedParentCode) {
        assertThat(referenceCodes.get(index).getParentCode()).isEqualTo(expectedParentCode);
    }

    @Step("Verify sub-code count for specific reference code item")
    public void verifySubCodeCount(int index, long expectedCount) {
        assertThat(Integer.valueOf(referenceCodes.get(index).getSubCodes().size()).longValue()).isEqualTo(expectedCount);
    }

    @Step("Verify no sub codes for any reference code item")
    public void verifyNoSubCodes() {
        referenceCodes.forEach(rc -> {
            assertThat(rc.getSubCodes())
                    .as("Check code [%s] has no sub-codes", rc.getCode())
                    .isNullOrEmpty();
        });
    }

    @Step("Verify at least one or more sub codes for every reference code item")
    public void verifyAlwaysSubCodes() {
        referenceCodes.forEach(rc -> {
            assertThat(rc.getSubCodes())
                    .as("Check code [%s] has one or more sub-codes", rc.getCode())
                    .isNotEmpty();
        });
    }

    @Step("Verify at least one or more sub codes for every non-excepted reference code item")
    public void verifyAlwaysSubCodesExcept(String exceptedCodes) {
        List<String> exceptedCodesList = csv2list(exceptedCodes);

        referenceCodes.stream().filter(rc -> !exceptedCodesList.contains(rc.getCode())).forEach(rc -> {
            assertThat(rc.getSubCodes())
                    .as("Check code [%s] has no sub-codes", rc.getCode())
                    .isNotEmpty();
        });
    }

    @Step("Verify no parent domain for any reference code item")
    public void verifyNoParentDomain() {
        referenceCodes.forEach(rc -> {
            assertThat(rc.getParentDomain())
                    .as("Check code [%s] has no parent domain", rc.getCode())
                    .isNull();
        });
    }

    @Step("Verify list of reference code item codes")
    public void verifyCodeList(String expectedCodes) {
        verifyPropertyValues(referenceCodes, ReferenceCode::getCode, expectedCodes);
    }

    @Step("Verify list of reference code item domains")
    public void verifyDomainList(String expectedDomains) {
        verifyPropertyValues(referenceCodes, ReferenceCode::getDomain, expectedDomains);
    }

    @Step("Verify list of reference code item descriptions")
    public void verifyDescriptionList(String expectedDescriptions) {
        verifyPropertyValues(referenceCodes, ReferenceCode::getDescription, expectedDescriptions);
    }

    @Step("Verify sub-code domain for all returned reference code items")
    public void verifySubCodeDomain(String expectedSubCodeDomain) {
        referenceCodes.forEach(rc -> {
            if (Objects.nonNull(rc.getSubCodes())) {
                rc.getSubCodes().forEach(sc -> {
                    assertThat(sc.getDomain())
                            .as("Check domain for sub-code [%s] of code [%s]", sc.getCode(), rc.getCode())
                            .isEqualTo(expectedSubCodeDomain);
                });
            }
        });
    }

    private void dispatchListRequest(String resourcePath, String domain, Boolean withSubCodes, Long offset, Long limit) {
        init();

        String urlModifier = "";
        HttpEntity<?> httpEntity;

        if (Objects.nonNull(offset) && Objects.nonNull(limit)) {
            applyPagination(offset, limit);
            httpEntity = createEntity(null, addPaginationHeaders());
        } else {
            httpEntity = createEntity();
        }

        if (Objects.nonNull(withSubCodes)) {
            urlModifier = "?withSubCodes=" + withSubCodes.toString();
        }

        String url = resourcePath + urlModifier;

        try {
            ResponseEntity<List<ReferenceCode>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    new ParameterizedTypeReference<List<ReferenceCode>>() {},
                    domain);

            referenceCodes = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchObjectRequest(String resourcePath, String domain, String code, Boolean withSubCodes) {
        init();

        String urlModifier = "";

        if (Objects.nonNull(withSubCodes)) {
            urlModifier = "?withSubCodes=" + withSubCodes.toString();
        }

        String url = resourcePath + urlModifier;

        try {
            ResponseEntity<ReferenceCode> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<ReferenceCode>() {},
                    domain,
                    code);

            referenceCode = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchScheduleReasonsListRequest(String resourcePath, String eventType) {
        init();
        HttpEntity<?> httpEntity = createEntity();
        String urlModifier = "?eventType=" + eventType;
        String url = resourcePath + urlModifier;
        try {
            ResponseEntity<List<ScheduleReason>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    new ParameterizedTypeReference<List<ScheduleReason>>() {});

            scheduleReasons = response.getBody();
            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
    
    @Override
    protected void init() {
        super.init();

        referenceCodes = null;
        referenceCode = null;
        scheduleReasons = null;
    }

    public void getReasonCodes(String eventType) {
        dispatchScheduleReasonsListRequest(API_SCHEDULE_REASONS_URL, eventType);
    }

    public void verifyReasonCodes(List<ScheduleReason> expected) {

        final Iterator<ScheduleReason> expectedIterator = expected.iterator();
        final Iterator<ScheduleReason> actualIterator = scheduleReasons.iterator();
        while (expectedIterator.hasNext()) {
            final ScheduleReason expectedThis = expectedIterator.next();
            final ScheduleReason actualThis = actualIterator.next();
            assertEquals(expectedThis.getCode(), actualThis.getCode());
            assertEquals(expectedThis.getDescription(), actualThis.getDescription());
        }
        assertFalse("Too many actual events", actualIterator.hasNext());
    }
}
