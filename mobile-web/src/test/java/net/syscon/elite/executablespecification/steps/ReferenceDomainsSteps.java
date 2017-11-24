package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Reference Domains service.
 */
public class ReferenceDomainsSteps extends CommonSteps {
    private static final String API_REF_PREFIX = API_PREFIX + "reference-domains/";
    private static final String API_ALERTS_URL = API_REF_PREFIX + "alertTypes";
    private static final String API_SOURCES_URL = API_REF_PREFIX + "caseNoteSources";

    private List<ReferenceCode> referenceCodes;
    private ReferenceCode referenceCode;

    @Step("Submit request for all alert types (with alert codes)")
    public void getAllAlertTypes() {
        doListApiCall(API_ALERTS_URL);
    }

    @Step("Submit request for all case note sources")
    public void getAllSources() {
        doListApiCall(API_SOURCES_URL);
    }

    @Step("Submit request for reference codes in specified domain")
    public void getRefCodesForDomain(String domain) {
        // wip
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
            assertThat(rc.getParentDomainId())
                    .as("Check parent domain for code [%s]", rc.getCode())
                    .isEqualTo(expectedParentDomain);
        });
    }

    @Step("Verify code for specific reference code item")
    public void verifyCode(int index, String expectedCode) {
        assertThat(referenceCodes.get(index).getCode()).isEqualTo(expectedCode);
    }

    @Step("Verify parent code for specific reference code item")
    public void verifyParentCode(int index, String expectedParentCode) {
        assertThat(referenceCodes.get(index).getParentCode()).isEqualTo(expectedParentCode);
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
            assertThat(rc.getParentDomainId())
                    .as("Check code [%s] has no parent domain", rc.getCode())
                    .isNull();
        });
    }

    @Step("Verify list of reference code item codes")
    public void verifyCodeList(String expectedCodes) {
        verifyPropertyValues(referenceCodes, ReferenceCode::getCode, expectedCodes);
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

    private void doListApiCall(String url) {
        init();

        applyPagination(0L, 1000L);

        try {
            ResponseEntity<List<ReferenceCode>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createEntity(null, addPaginationHeaders()),
                    new ParameterizedTypeReference<List<ReferenceCode>>() {});

            referenceCodes = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

      // @wip - this will be reinstated on next commit.
//    private void doSingleResultApiCall(String url) {
//        init();
//
//        try {
//            ResponseEntity<ReferenceCode> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    createEntity(),
//                    new ParameterizedTypeReference<ReferenceCode>() {});
//
//            referenceCode = response.getBody();
//        } catch (EliteClientException ex) {
//            setErrorResponse(ex.getErrorResponse());
//        }
//    }

    protected void init() {
        super.init();

        referenceCodes = null;
    }
}
