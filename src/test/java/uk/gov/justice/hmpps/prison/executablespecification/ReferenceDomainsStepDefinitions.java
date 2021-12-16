package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.executablespecification.steps.ReferenceDomainsSteps;

import java.util.List;

/**
 * BDD step definitions for reference domains endpoints:
 * <ul>
 * <li>/reference-domains/alertTypes</li>
 * <li>/reference-domains/caseNoteSources</li>
 * <li>/reference-domains/caseNoteTypes</li>
 * <li>/reference-domains/domains/{domain}</li>
 * <li>/reference-domains/domains/{domain}/codes/{code}</li>
 * </ul>
 */
public class ReferenceDomainsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private ReferenceDomainsSteps referenceDomains;

    @When("^request submitted to retrieve all alert types with alert codes$")
    public void requestSubmittedToRetrieveAllAlertTypesWithAlertCodes() throws Throwable {
        referenceDomains.getAllAlertTypes();
    }

    @When("^request submitted to retrieve all case note sources$")
    public void requestSubmittedToRetrieveAllCaseNoteSources() throws Throwable {
        referenceDomains.getAllSources();
    }

    @When("^request submitted to retrieve used case note types$")
    public void requestSubmittedToRetrieveUsedCaseNoteTypes() throws Throwable {
        referenceDomains.getUsedCaseNoteTypes();
    }

    @Then("^\"([^\"]*)\" reference code items are returned$")
    public void caseNoteSourcesAreReturned(final String expectedCount) throws Throwable {
        referenceDomains.verifyResourceRecordsReturned(Long.parseLong(expectedCount));
    }

    @Then("^some reference code items are returned$")
    public void someCaseNoteSourcesAreReturned() throws Throwable {
        referenceDomains.verifySomeResourceRecordsReturned();
    }

    @And("^domain for all returned items is \"([^\"]*)\"$")
    public void domainForAllReturnedItemsIs(final String expectedDomain) throws Throwable {
        referenceDomains.verifyDomain(expectedDomain);
    }

    @And("^code for \"([^\"]*)\" returned item is \"([^\"]*)\"$")
    public void codeForReturnedItemIs(final String ordinal, final String expectedCode) throws Throwable {
        referenceDomains.verifyCode(ord2idx(ordinal), expectedCode);
    }

    @And("^description for \"([^\"]*)\" returned item is \"([^\"]*)\"$")
    public void descriptionForReturnedItemIs(final String ordinal, final String expecteDescription) throws Throwable {
        referenceDomains.verifyDescription(ord2idx(ordinal), expecteDescription);
    }

    @And("^the returned items contain description \"([^\"]*)\"$")
    public void returnedItemsContainDescription(final String expectedDescription) throws Throwable {
        referenceDomains.verifyDescriptionExists(expectedDescription);
    }

    @And("^codes of returned items are \"([^\"]*)\"$")
    public void codesOfReturnedItemsAre(final String expectedCodes) throws Throwable {
        referenceDomains.verifyCodeList(expectedCodes);
    }

    @And("^domains of returned items are \"([^\"]*)\"$")
    public void domainsOfReturnedItemsAre(final String expectedDomains) throws Throwable {
        referenceDomains.verifyDomainList(expectedDomains);
    }

    @And("^parent domain for all returned items is \"([^\"]*)\"$")
    public void parentDomainForAllReturnedItemsIs(final String expectedParentDomain) throws Throwable {
        referenceDomains.verifyParentDomain(expectedParentDomain);
    }

    @And("^there are no sub codes for any returned item$")
    public void thereAreNoSubCodesForAnyReturnedItem() throws Throwable {
        referenceDomains.verifyNoSubCodes();
    }

    @And("^descriptions of returned items are \"([^\"]*)\"$")
    public void descriptionsOfReturnedItemsAre(final String expectedDescriptions) throws Throwable {
        referenceDomains.verifyDescriptionList(expectedDescriptions);
    }

    @And("^there is no parent domain for any returned item$")
    public void thereIsNoParentDomainForAnyReturnedItem() throws Throwable {
        referenceDomains.verifyNoParentDomain();
    }

    @And("^there are one or more sub codes for every returned item$")
    public void thereAreOneOrMoreSubCodesForEveryReturnedItem() throws Throwable {
        referenceDomains.verifyAlwaysSubCodes();
    }

    @And("^domain for all returned item sub-codes is \"([^\"]*)\"$")
    public void domainForAllReturnedItemSubCodesIs(final String expectedSubCodeDomain) throws Throwable {
        referenceDomains.verifySubCodeDomain(expectedSubCodeDomain);
    }

    @And("^there are one or more sub codes for every returned item except \"([^\"]*)\"$")
    public void thereAreOneOrMoreSubCodesForEveryReturnedItemExcept(final String exceptedCodes) throws Throwable {
        referenceDomains.verifyAlwaysSubCodesExcept(exceptedCodes);
    }

    @When("^request submitted to retrieve all reference codes, without sub-codes, for domain \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveAllReferenceCodesWithoutSubCodesForDomain(final String domain) throws Throwable {
        referenceDomains.getRefCodesForDomain(domain, false);
    }

    @When("^request submitted to retrieve all reference codes, with sub-codes, for domain \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveAllReferenceCodesWithSubCodesForDomain(final String domain) throws Throwable {
        referenceDomains.getRefCodesForDomain(domain, true);
    }

    @And("^\"([^\"]*)\" returned item has \"([^\"]*)\" sub-codes$")
    public void returnedItemHasSubCodes(final String ordinal, final String expectedCount) throws Throwable {
        referenceDomains.verifySubCodeCount(ord2idx(ordinal), Long.parseLong(expectedCount));
    }

    @And("^domain for all sub-codes of \"([^\"]*)\" returned item is \"([^\"]*)\"$")
    public void domainForAllSubCodesOfReturnedItemIs(final String ordinal, final String expectedSubCodeDomain) throws Throwable {
        referenceDomains.verifySubCodeDomain(ord2idx(ordinal), expectedSubCodeDomain);
    }

    @And("^domains for sub-codes of \"([^\"]*)\" returned item are \"([^\"]*)\"$")
    public void domainsForSubCodesOfReturnedItemAre(final String ordinal, final String expectedSubCodeDomains) throws Throwable {
        referenceDomains.verifySubCodeDomains(ord2idx(ordinal), expectedSubCodeDomains);
    }

    @And("^code for \"([^\"]*)\" sub-code of \"([^\"]*)\" returned item is \"([^\"]*)\"$")
    public void codeForSubCodeOfReturnedItemIs(final String subCodeOrdinal, final String refCodeOrdinal, final String expectedCode) throws Throwable {
        referenceDomains.verifyCodeForSubCode(ord2idx(subCodeOrdinal), ord2idx(refCodeOrdinal), expectedCode);
    }

    @And("^description for \"([^\"]*)\" sub-code of \"([^\"]*)\" returned item is \"([^\"]*)\"$")
    public void descriptionForSubCodeOfReturnedItemIs(final String subCodeOrdinal, final String refCodeOrdinal, final String expectedDescription) throws Throwable {
        referenceDomains.verifyDescriptionForSubCode(ord2idx(subCodeOrdinal), ord2idx(refCodeOrdinal), expectedDescription);
    }

    @And("^item description \"([^\"]*)\" contains sub-code with description \"([^\"]*)\"$")
    public void itemContainsSubCodeWithDescription(final String itemDescription, final String expectedDescription) throws Throwable {
        referenceDomains.verifyItemContainsSubCodeWithDescription(itemDescription, expectedDescription);
    }

    @When("^request submitted to retrieve reference code, without sub-codes, for domain \"([^\"]*)\" and code \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveReferenceCodeWithoutSubCodesForDomainAndCode(final String domain, final String code) throws Throwable {
        referenceDomains.getRefCodeForDomainAndCode(domain, code, false);
    }

    @And("^\"([^\"]*)\" for returned item is \"([^\"]*)\"$")
    public void forReturnedItemIs(final String propertyName, final String propertyValue) throws Throwable {
        referenceDomains.verifyRefCodePropertyValue(propertyName, propertyValue);
    }

    @And("^returned item has \"([^\"]*)\" sub-codes$")
    public void returnedItemHasSubCodes(final String expectedCount) throws Throwable {
        referenceDomains.verifySubCodeCount(Long.parseLong(expectedCount));
    }

    @And("^returned item has no sub-codes$")
    public void returnedItemHasNoSubCodes() throws Throwable {
        referenceDomains.verifyRefCodeNoSubCodes();
    }

    @When("^request submitted to retrieve reference code, with sub-codes, for domain \"([^\"]*)\" and code \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveReferenceCodeWithSubCodesForDomainAndCode(final String domain, final String code) throws Throwable {
        referenceDomains.getRefCodeForDomainAndCode(domain, code, true);
    }

    @Then("^resource not found response is received from reference domains API$")
    public void resourceNotFoundResponseIsReceivedFromReferenceDomainsAPI() throws Throwable {
        referenceDomains.verifyResourceNotFound();
    }

    @And("^user message in resource not found response from reference domains API is \"([^\"]*)\"$")
    public void userMessageInResourceNotFoundResponseFromReferenceDomainsAPIIs(final String expectedUserMessage) throws Throwable {
        referenceDomains.verifyErrorUserMessage(expectedUserMessage);
    }

    @Then("^bad request response is received from reference domains API and user message is \"([^\"]*)\"$")
    public void badRequestResponseIsReceivedFromReferenceDomainsAPIAndUserMessageIs(final String expectedUserMessage) throws Throwable {
        referenceDomains.verifyBadRequest(expectedUserMessage);
    }

    @When("^a request is submitted to retrieve all reason codes for event type \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveReasonCodesForEventType(final String eventType) throws Throwable {
        referenceDomains.getReasonCodes(eventType);
    }

    @Then("^the returned reason codes are as follows:$")
    public void reasonCodesAreReturnedAsFollows(final DataTable table) throws Throwable {
        final List<ReferenceCode> expected = table.asList(ReferenceCode.class);
        referenceDomains.verifyReasonCodes(expected);
    }
}
