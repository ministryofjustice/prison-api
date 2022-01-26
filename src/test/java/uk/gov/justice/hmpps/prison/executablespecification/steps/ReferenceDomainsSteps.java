package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCodeInfo;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

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
    private List<ReferenceCode> scheduleReasons;

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
    public void getRefCodesForDomain(final String domain, final boolean withSubCodes) {
        dispatchListRequest(API_DOMAINS_URL, domain, withSubCodes, 0L, 1000L);
    }

    @Step("Submit request for reference code with specified domain and code")
    public void getRefCodeForDomainAndCode(final String domain, final String code, final boolean withSubCodes) {
        dispatchObjectRequest(API_DOMAINS_CODES_URL, domain, code, withSubCodes);
    }

    @Step("Verify reference code property value")
    public void verifyRefCodePropertyValue(final String propertyName, final String propertyValue) throws Exception {
        verifyPropertyValue(referenceCode, propertyName, propertyValue);
    }

    @Step("Verify returned item has no sub-codes")
    public void verifyRefCodeNoSubCodes() {
        assertThat(referenceCode.getSubCodes()).isEmpty();
    }

    @Step("Verify sub-code count for returned item")
    public void verifySubCodeCount(final long expectedCount) {
        assertThat(Integer.valueOf(referenceCode.getSubCodes().size()).longValue()).isEqualTo(expectedCount);
    }

    @Step("Verify domain for all sub-codes of specific reference code item")
    public void verifySubCodeDomain(final int index, final String expectedSubCodeDomain) {
        final var refCode = referenceCodes.get(index);

        refCode.getSubCodes().forEach(sc -> {
            assertThat(sc.getDomain())
                    .as("Check domain for sub-code [%s] of code [%s]", sc.getCode(), refCode.getCode())
                    .isEqualTo(expectedSubCodeDomain);
        });
    }

    @Step("Verify domains for sub-codes of specific reference code item")
    public void verifySubCodeDomains(final int index, final String expectedSubCodeDomains) {
        final var refCode = referenceCodes.get(index);

        final var actualSubCodeDomains = refCode.getSubCodes()
                .stream().map(ReferenceCode::getDomain).distinct().toList();

        verifyIdentical(actualSubCodeDomains, csv2list(expectedSubCodeDomains));
    }

    @Step("Verify code for specific sub-code of specific reference code item")
    public void verifyCodeForSubCode(final int subCodeIndex, final int refCodeIndex, final String expectedSubCodeCode) {
        assertThat(referenceCodes.get(refCodeIndex).getSubCodes().get(subCodeIndex).getCode())
                .isEqualTo(expectedSubCodeCode);
    }

    @Step("Verify description for specific sub-code of specific reference code item")
    public void verifyDescriptionForSubCode(final int subCodeIndex, final int refCodeIndex, final String expectedSubCodeDescription) {
        assertThat(referenceCodes.get(refCodeIndex).getSubCodes().get(subCodeIndex).getDescription())
                .isEqualTo(expectedSubCodeDescription);
    }

    @Step("Verify item contains sub-code with description")
    public void verifyItemContainsSubCodeWithDescription(final String itemDescription, final String expectedSubCodeDescription) {
        final var item = referenceCodes.stream().filter(it -> it.getDescription().equals(itemDescription)).findFirst();
        assertThat(item).isPresent().withFailMessage(format("Item %s not found", itemDescription));
        assertThat(item.get().getSubCodes()).extracting(ReferenceCodeInfo::getDescription).contains(expectedSubCodeDescription);
    }

    @Step("Verify domain for all returned reference code items")
    public void verifyDomain(final String expectedDomain) {
        referenceCodes.forEach(rc -> {
            assertThat(rc.getDomain())
                    .as("Check domain for code [%s]", rc.getCode())
                    .isEqualTo(expectedDomain);
        });
    }

    @Step("Verify parent domain for all returned reference code items")
    public void verifyParentDomain(final String expectedParentDomain) {
        referenceCodes.forEach(rc -> {
            assertThat(rc.getParentDomain())
                    .as("Check parent domain for code [%s]", rc.getCode())
                    .isEqualTo(expectedParentDomain);
        });
    }

    @Step("Verify code for specific reference code item")
    public void verifyCode(final int index, final String expectedCode) {
        assertThat(referenceCodes.get(index).getCode()).isEqualTo(expectedCode);
    }

    @Step("Verify description for specific reference code item")
    public void verifyDescription(final int index, final String expectedDescription) {
        assertThat(referenceCodes.get(index).getDescription()).isEqualTo(expectedDescription);
    }

    @Step("Verify description exists in the reference codes")
    public void verifyDescriptionExists(final String expectedDescription) {
        assertThat(referenceCodes).extracting(ReferenceCodeInfo::getDescription).contains(expectedDescription);
    }

    @Step("Verify parent code for specific reference code item")
    public void verifyParentCode(final int index, final String expectedParentCode) {
        assertThat(referenceCodes.get(index).getParentCode()).isEqualTo(expectedParentCode);
    }

    @Step("Verify sub-code count for specific reference code item")
    public void verifySubCodeCount(final int index, final long expectedCount) {
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
    public void verifyAlwaysSubCodesExcept(final String exceptedCodes) {
        final var exceptedCodesList = csv2list(exceptedCodes);

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
    public void verifyCodeList(final String expectedCodes) {
        verifyPropertyValues(referenceCodes, ReferenceCode::getCode, expectedCodes);
    }

    @Step("Verify list of reference code item domains")
    public void verifyDomainList(final String expectedDomains) {
        verifyPropertyValues(referenceCodes, ReferenceCode::getDomain, expectedDomains);
    }

    @Step("Verify list of reference code item descriptions")
    public void verifyDescriptionList(final String expectedDescriptions) {
        verifyPropertyValues(referenceCodes, ReferenceCode::getDescription, expectedDescriptions);
    }

    @Step("Verify sub-code domain for all returned reference code items")
    public void verifySubCodeDomain(final String expectedSubCodeDomain) {
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

    private void dispatchListRequest(final String resourcePath, final String domain, final Boolean withSubCodes, final Long offset, final Long limit) {
        init();

        var urlModifier = "";
        final HttpEntity<?> httpEntity;

        if (Objects.nonNull(offset) && Objects.nonNull(limit)) {
            applyPagination(offset, limit);
            httpEntity = createEntity(null, addPaginationHeaders());
        } else {
            httpEntity = createEntity();
        }

        if (Objects.nonNull(withSubCodes)) {
            urlModifier = "?withSubCodes=" + withSubCodes.toString();
        }

        final var url = resourcePath + urlModifier;

        try {
            final var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    new ParameterizedTypeReference<List<ReferenceCode>>() {
                    },
                    domain);

            referenceCodes = response.getBody();

            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchObjectRequest(final String resourcePath, final String domain, final String code, final Boolean withSubCodes) {
        init();

        var urlModifier = "";

        if (Objects.nonNull(withSubCodes)) {
            urlModifier = "?withSubCodes=" + withSubCodes.toString();
        }

        final var url = resourcePath + urlModifier;

        try {
            final var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<ReferenceCode>() {
                    },
                    domain,
                    code);

            referenceCode = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchScheduleReasonsListRequest(final String resourcePath, final String eventType) {
        init();
        final var httpEntity = createEntity();
        final var urlModifier = "?eventType=" + eventType;
        final var url = resourcePath + urlModifier;
        try {
            final var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    new ParameterizedTypeReference<List<ReferenceCode>>() {
                    });

            scheduleReasons = response.getBody();
            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
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

    public void getReasonCodes(final String eventType) {
        dispatchScheduleReasonsListRequest(API_SCHEDULE_REASONS_URL, eventType);
    }

    public void verifyReasonCodes(final List<ReferenceCode> expected) {

        final var expectedIterator = expected.iterator();
        final var actualIterator = scheduleReasons.iterator();
        while (expectedIterator.hasNext()) {
            final var expectedThis = expectedIterator.next();
            final var actualThis = actualIterator.next();
            assertThat(actualThis.getCode()).isEqualTo(expectedThis.getCode());
            assertThat(actualThis.getDescription()).isEqualTo(expectedThis.getDescription());
        }
        assertThat(actualIterator.hasNext()).as("Too many actual events").isFalse();
    }
}
