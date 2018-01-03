package net.syscon.elite.executablespecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.executablespecification.steps.ReferenceDomainsSteps;
import org.springframework.beans.factory.annotation.Autowired;

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
    public void caseNoteSourcesAreReturned(String expectedCount) throws Throwable {
        referenceDomains.verifyResourceRecordsReturned(Long.parseLong(expectedCount));
    }

    @And("^domain for all returned items is \"([^\"]*)\"$")
    public void domainForAllReturnedItemsIs(String expectedDomain) throws Throwable {
        referenceDomains.verifyDomain(expectedDomain);
    }

    @And("^code for \"([^\"]*)\" returned item is \"([^\"]*)\"$")
    public void codeForReturnedItemIs(String ordinal, String expectedCode) throws Throwable {
        referenceDomains.verifyCode(ord2idx(ordinal), expectedCode);
    }

    @And("^description for \"([^\"]*)\" returned item is \"([^\"]*)\"$")
    public void descriptionForReturnedItemIs(String ordinal, String expectedDescription) throws Throwable {
        referenceDomains.verifyDescription(ord2idx(ordinal), expectedDescription);
    }

    @And("^codes of returned items are \"([^\"]*)\"$")
    public void codesOfReturnedItemsAre(String expectedCodes) throws Throwable {
        referenceDomains.verifyCodeList(expectedCodes);
    }

    @And("^domains of returned items are \"([^\"]*)\"$")
    public void domainsOfReturnedItemsAre(String expectedDomains) throws Throwable {
        referenceDomains.verifyDomainList(expectedDomains);
    }

    @And("^parent domain for all returned items is \"([^\"]*)\"$")
    public void parentDomainForAllReturnedItemsIs(String expectedParentDomain) throws Throwable {
        referenceDomains.verifyParentDomain(expectedParentDomain);
    }

    @And("^there are no sub codes for any returned item$")
    public void thereAreNoSubCodesForAnyReturnedItem() throws Throwable {
        referenceDomains.verifyNoSubCodes();
    }

    @And("^descriptions of returned items are \"([^\"]*)\"$")
    public void descriptionsOfReturnedItemsAre(String expectedDescriptions) throws Throwable {
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
    public void domainForAllReturnedItemSubCodesIs(String expectedSubCodeDomain) throws Throwable {
        referenceDomains.verifySubCodeDomain(expectedSubCodeDomain);
    }

    @And("^there are one or more sub codes for every returned item except \"([^\"]*)\"$")
    public void thereAreOneOrMoreSubCodesForEveryReturnedItemExcept(String exceptedCodes) throws Throwable {
        referenceDomains.verifyAlwaysSubCodesExcept(exceptedCodes);
    }

    @When("^request submitted to retrieve all reference codes, without sub-codes, for domain \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveAllReferenceCodesWithoutSubCodesForDomain(String domain) throws Throwable {
        referenceDomains.getRefCodesForDomain(domain, false);
    }

    @When("^request submitted to retrieve all reference codes, with sub-codes, for domain \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveAllReferenceCodesWithSubCodesForDomain(String domain) throws Throwable {
        referenceDomains.getRefCodesForDomain(domain, true);
    }

    @And("^\"([^\"]*)\" returned item has \"([^\"]*)\" sub-codes$")
    public void returnedItemHasSubCodes(String ordinal, String expectedCount) throws Throwable {
        referenceDomains.verifySubCodeCount(ord2idx(ordinal), Long.parseLong(expectedCount));
    }

    @And("^domain for all sub-codes of \"([^\"]*)\" returned item is \"([^\"]*)\"$")
    public void domainForAllSubCodesOfReturnedItemIs(String ordinal, String expectedSubCodeDomain) throws Throwable {
        referenceDomains.verifySubCodeDomain(ord2idx(ordinal), expectedSubCodeDomain);
    }

    @And("^domains for sub-codes of \"([^\"]*)\" returned item are \"([^\"]*)\"$")
    public void domainsForSubCodesOfReturnedItemAre(String ordinal, String expectedSubCodeDomains) throws Throwable {
        referenceDomains.verifySubCodeDomains(ord2idx(ordinal), expectedSubCodeDomains);
    }

    @And("^code for \"([^\"]*)\" sub-code of \"([^\"]*)\" returned item is \"([^\"]*)\"$")
    public void codeForSubCodeOfReturnedItemIs(String subCodeOrdinal, String refCodeOrdinal, String expectedCode) throws Throwable {
        referenceDomains.verifyCodeForSubCode(ord2idx(subCodeOrdinal), ord2idx(refCodeOrdinal), expectedCode);
    }

    @And("^description for \"([^\"]*)\" sub-code of \"([^\"]*)\" returned item is \"([^\"]*)\"$")
    public void descriptionForSubCodeOfReturnedItemIs(String subCodeOrdinal, String refCodeOrdinal, String expectedDescription) throws Throwable {
        referenceDomains.verifyDescriptionForSubCode(ord2idx(subCodeOrdinal), ord2idx(refCodeOrdinal), expectedDescription);
    }

    @When("^request submitted to retrieve reference code, without sub-codes, for domain \"([^\"]*)\" and code \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveReferenceCodeWithoutSubCodesForDomainAndCode(String domain, String code) throws Throwable {
        referenceDomains.getRefCodeForDomainAndCode(domain, code, false);
    }

    @And("^\"([^\"]*)\" for returned item is \"([^\"]*)\"$")
    public void forReturnedItemIs(String propertyName, String propertyValue) throws Throwable {
        referenceDomains.verifyRefCodePropertyValue(propertyName, propertyValue);
    }

    @And("^returned item has \"([^\"]*)\" sub-codes$")
    public void returnedItemHasSubCodes(String expectedCount) throws Throwable {
        referenceDomains.verifySubCodeCount(Long.parseLong(expectedCount));
    }

    @And("^returned item has no sub-codes$")
    public void returnedItemHasNoSubCodes() throws Throwable {
        referenceDomains.verifyRefCodeNoSubCodes();
    }

    @When("^request submitted to retrieve reference code, with sub-codes, for domain \"([^\"]*)\" and code \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveReferenceCodeWithSubCodesForDomainAndCode(String domain, String code) throws Throwable {
        referenceDomains.getRefCodeForDomainAndCode(domain, code, true);
    }

    @Then("^resource not found response is received from reference domains API$")
    public void resourceNotFoundResponseIsReceivedFromReferenceDomainsAPI() throws Throwable {
        referenceDomains.verifyResourceNotFound();
    }

    @And("^user message in resource not found response from reference domains API is \"([^\"]*)\"$")
    public void userMessageInResourceNotFoundResponseFromReferenceDomainsAPIIs(String expectedUserMessage) throws Throwable {
        referenceDomains.verifyErrorUserMessage(expectedUserMessage);
    }

    @Then("^bad request response is received from reference domains API and user message is \"([^\"]*)\"$")
    public void badRequestResponseIsReceivedFromReferenceDomainsAPIAndUserMessageIs(String expectedUserMessage) throws Throwable {
        referenceDomains.verifyBadRequest(expectedUserMessage);
    }

    @When("^a request is submitted to retrieve all reason codes for event type \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveReasonCodesForEventType(String eventType) throws Throwable {
        referenceDomains.getReasonCodes(eventType);
    }

    @Then("^the returned reason codes are as follows:$")
    public void reasonCodesAreReturnedAsFollows(DataTable table) throws Throwable {
        final List<ReferenceCode> expected = table.asList(ReferenceCode.class);
        referenceDomains.verifyReasonCodes(expected);
    }
}
