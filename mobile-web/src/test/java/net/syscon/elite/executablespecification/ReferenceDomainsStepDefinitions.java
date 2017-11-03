package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import net.syscon.elite.executablespecification.steps.ReferenceDomainsSteps;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for reference domains endpoints:
 * <ul>
 * <li>/reference-domains/alertTypes</li>
 * <li>/reference-domains/alertTypes/{alertType}</li>
 * <li>/reference-domains/alertTypes/{alertType}/codes</li>
 * <li>/reference-domains/alertTypes/{alertType}/codes/{alertCode}</li>
 * <li>/reference-domains/caseNoteTypes</li>
 * <li>/reference-domains/caseNoteTypes/{typeCode}</li>
 * <li>/reference-domains/caseNoteSubTypes/{typeCode}</li>
 * <li>/reference-domains/caseNoteSubTypes/{typeCode}/subTypes/{subTypeCode}</li>
 * <li>/reference-domains/caseNoteSources</li>
 * <li>/reference-domains/caseNoteSources/{sourceCode}</li>
 * </ul>
 */
public class ReferenceDomainsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private ReferenceDomainsSteps referenceDomains;

    @When("^all types are requested$")
    public void allTypesAreRequested() {
        referenceDomains.getAllTypes(false);
    }

    @Then("^all types are returned$")
    public void allTypesAreReturned() {
        referenceDomains.verifySomeSampleTypeData();
    }

    @When("^all types with subtypes are requested$")
    public void allTypesAndSubTypesAreRequested() {
        referenceDomains.getAllTypes(true);
    }

    @Then("^all types with subtypes are returned$")
    public void allTypesAndSubTypesAreReturned() {
        referenceDomains.verifySomeTypesAndSubtypes();
    }

    @When("^all alert types are requested$")
    public void allAlertTypesAreRequested() {
        referenceDomains.getAllAlertTypes();
    }

    @Then("^all alert types are returned$")
    public void allAlertTypesAreReturned() {
        referenceDomains.verifySomeAlertTypes();
    }

    @When("^all sources are requested$")
    public void allSourcesAreRequested() {
        referenceDomains.getAllSources();
    }

    @Then("^all sources are returned$")
    public void allSourcesAreReturned() {
        referenceDomains.verifyAllSources();
    }

    @When("^alert type with code \"([^\"]*)\" is requested$")
    public void anAlertTypeIsRequested(String code) {
        referenceDomains.getAlertType(code);
    }

    @When("^type with code \"([^\"]*)\" is requested$")
    public void aTypeIsRequested(String code) {
        referenceDomains.getType(code);
    }

    @When("^subtype with code \"([^\"]*)\" and subCode \"([^\"]*)\" is requested$")
    public void aSubtypeIsRequested(String code, String subCode) {
        referenceDomains.getSubtype(code, subCode);
    }

    @When("^alert code with code \"([^\"]*)\" and subCode \"([^\"]*)\" is requested$")
    public void anAlertCodeIsRequested(String code, String subCode) {
        referenceDomains.getAlertCode(code, subCode);
    }

    @When("^source with code \"([^\"]*)\" is requested$")
    public void aSourceIsRequested(String code) {
        referenceDomains.getSource(code);
    }

    @Then("^the .+ returned ([^\"]+) is \"([^\"]*)\"$")
    public void theFieldIs(String field, String value) throws ReflectiveOperationException {
        referenceDomains.verifyField(field, value);
    }

    @When("^subtype list with code \"([^\"]*)\" is requested$")
    public void aSubtypeListIsRequested(String code) {
        referenceDomains.getSubtypeList(code);
    }

    @When("^alert code list with code \"([^\"]*)\" is requested$")
    public void anAlertCodeListIsRequested(String code) {
        referenceDomains.getAlertCodeList(code);
    }

    @Then("^the list size is \"([0-9]+)\"$")
    public void listSize(int size) {
        referenceDomains.verifyListSize(size);
    }

    @And("^the list domain is \"([^\"]*)\"$")
    public void listDomain(String domain) {
        referenceDomains.verifyListDomain(domain);
    }

    @And("^the list first code is \"([^\"]*)\"$")
    public void listFirstCode(String code) {
        referenceDomains.verifyListFirstCode(code);
    }

    @And("^the list first description is \"([^\"]*)\"$")
    public void listfirstDescription(String description) {
        referenceDomains.verifyListFirstDescription(description);
    }


}
