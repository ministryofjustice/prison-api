package net.syscon.elite.executableSpecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executableSpecification.steps.ReferenceDomainsSteps;
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
    private ReferenceDomainsSteps referenceDomainsSteps;

    @When("^all types are requested$")
    public void allTypesAreRequested() {
        referenceDomainsSteps.getAllTypes(false);
    }

    @Then("^all types are returned$")
    public void allTypesAreReturned() {
        referenceDomainsSteps.verifySomeSampleTypeData();
    }

    @When("^all types with subtypes are requested$")
    public void allTypesAndSubTypesAreRequested() {
        referenceDomainsSteps.getAllTypes(true);
    }

    @Then("^all types with subtypes are returned$")
    public void allTypesAndSubTypesAreReturned() {
        referenceDomainsSteps.verifySomneTypesAndSubtypes();
    }

    @When("^all alert types are requested$")
    public void allAlertTypesAreRequested() {
        referenceDomainsSteps.getAllAlertTypes();
    }

    @Then("^all alert types are returned$")
    public void allAlertTypesAreReturned() {
        referenceDomainsSteps.verifySomeAlertTypes();
    }

    @When("^all sources are requested$")
    public void allSourcesAreRequested() {
        referenceDomainsSteps.getAllSources();
    }

    @Then("^all sources are returned$")
    public void allSourcesAreReturned() {
        referenceDomainsSteps.verifyAllSources();
    }

    @When("^alert type with code \"([^\"]*)\" is requested$")
    public void anAlertTypeIsRequested(String code) {
        referenceDomainsSteps.getAlertType(code);
    }

    @When("^type with code \"([^\"]*)\" is requested$")
    public void aTypeIsRequested(String code) {
        referenceDomainsSteps.getType(code);
    }

    @When("^subtype with code \"([^\"]*)\" and subCode \"([^\"]*)\" is requested$")
    public void aSubtypeIsRequested(String code, String subCode) {
        referenceDomainsSteps.getSubtype(code, subCode);
    }

    @When("^alert code with code \"([^\"]*)\" and subCode \"([^\"]*)\" is requested$")
    public void anAlertCodeIsRequested(String code, String subCode) {
        referenceDomainsSteps.getAlertCode(code, subCode);
    }

    @When("^source with code \"([^\"]*)\" is requested$")
    public void aSourceIsRequested(String code) {
        referenceDomainsSteps.getSource(code);
    }

    @Then("^the .+ returned ([^\"]+) is \"([^\"]*)\"$")
    public void theFieldIs(String field, String value) throws ReflectiveOperationException {
        referenceDomainsSteps.verifyField(field, value);
    }

    @When("^subtype list with code \"([^\"]*)\" is requested$")
    public void aSubtypeListIsRequested(String code) {
        referenceDomainsSteps.getSubtypeList(code);
    }

    @When("^alert code list with code \"([^\"]*)\" is requested$")
    public void anAlertCodeListIsRequested(String code) {
        referenceDomainsSteps.getAlertCodeList(code);
    }

    @Then("^the list size is \"([0-9]+)\"$")
    public void listSize(int size) {
        referenceDomainsSteps.verifyListSize(size);
    }

    @And("^the list domain is \"([^\"]*)\"$")
    public void listDomain(String domain) {
        referenceDomainsSteps.verifyListDomain(domain);
    }

    @And("^the list first code is \"([^\"]*)\"$")
    public void listFirstCode(String code) {
        referenceDomainsSteps.verifyListFirstCode(code);
    }

    @And("^the list first description is \"([^\"]*)\"$")
    public void listfirstDescription(String description) {
        referenceDomainsSteps.verifyListFirstDescription(description);
    }


}
