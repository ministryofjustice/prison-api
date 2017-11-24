package net.syscon.elite.executablespecification;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.ReferenceDomainsSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for reference domains endpoints:
 * <ul>
 * <li>/reference-domains/alertTypes</li>
 * <li>/reference-domains/caseNoteSources</li>
 * <li>/reference-domains/caseNoteTypes</li>
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

    @And("^codes of returned items are \"([^\"]*)\"$")
    public void codesOfReturnedItemsAre(String expectedCodes) throws Throwable {
        referenceDomains.verifyCodeList(expectedCodes);
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
        referenceDomains.getRefCodesForDomain(domain);
    }

    @When("^request submitted to retrieve all reference codes, with sub-codes, for domain \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveAllReferenceCodesWithSubCodesForDomain(String domain) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^reference code with domain \"([^\"]*)\" and code \"([^\"]*)\" is requested$")
    public void referenceCodeWithDomainAndCodeIsRequested(String domain, String code) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
