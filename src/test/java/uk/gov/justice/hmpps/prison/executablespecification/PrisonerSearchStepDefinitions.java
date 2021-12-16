package uk.gov.justice.hmpps.prison.executablespecification;


import com.google.common.collect.ImmutableMap;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.executablespecification.steps.PrisonerSearchSteps;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * BDD step definitions for the following Offender Search API endpoints:
 * <ul>
 *     <li>/v2/prisoners</li>
 * </ul>
 * <p>
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class PrisonerSearchStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private PrisonerSearchSteps prisonerSearch;

    @Then("^\"([^\"]*)\" prisoner records are returned$")
    public void bookingRecordsAreReturned(final String expectedCount) throws Throwable {
        prisonerSearch.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @Then("^\"([^\"]*)\" total prisoner records are available$")
    public void totalBookingRecordsAreAvailable(final String expectedCount) throws Throwable {
        prisonerSearch.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @And("^prisoner offender numbers match \"([^\"]*)\"$")
    public void offenderNumbersMatch(final String offenderNoList) throws Throwable {
        prisonerSearch.verifyOffenderNumbers(offenderNoList);
    }

    @And("^prisoner internal location match \"([^\"]*)\"$")
    public void offenderInternalLocationMatch(final String internalLocation) {
        prisonerSearch.verifyInternalLocation(internalLocation);
    }


    @And("^the prisoners first names match \"([^\"]*)\"$")
    public void offenderFirstNamesMatch(final String firstNames) throws Throwable {
        prisonerSearch.verifyFirstNames(firstNames);
    }

    @And("^the prisoners middle names match \"([^\"]*)\"$")
    public void offenderMiddleNamesMatch(final String middleNames) throws Throwable {
        prisonerSearch.verifyMiddleNames(middleNames);
    }

    @And("^the prisoners last names match \"([^\"]*)\"$")
    public void offenderLastNamesMatch(final String lastNames) throws Throwable {
        prisonerSearch.verifyLastNames(lastNames);
    }

    @And("^the prisoners working last names match \"([^\"]*)\"$")
    public void offenderWorkingLastNamesMatch(final String workingLastNames) throws Throwable {
        prisonerSearch.verifyWorkingLastNames(workingLastNames);
    }

    @And("^the prisoners working first names match \"([^\"]*)\"$")
    public void offenderWorkingFirstNamesMatch(final String workingFirstNames) throws Throwable {
        prisonerSearch.verifyWorkingFirstNames(workingFirstNames);
    }

    @And("^the prisoners dob matches \"([^\"]*)\"$")
    public void dateOfBirthMatch(final String dobs) throws Throwable {
        prisonerSearch.verifyDobs(dobs);
    }

    @And("^the prisoners working dob matches \"([^\"]*)\"$")
    public void workingDateOfBirthMatch(final String dobs) throws Throwable {
        prisonerSearch.verifyWorkingBirthDate(dobs);
    }

    @When("^a search is made for prisoners with DOB on or after (\\d+-\\d+-\\d+) for range ([0-9]*) -> ([0-9]*)$")
    public void aSearchIsMadeForPrisonersWithDOBOnOrAfterForRange(final String dobFrom, final long offset, final long limit) throws Throwable {
        prisonerSearch.search(ImmutableMap.of("dobFrom", dobFrom), offset, limit, HttpStatus.OK);
    }

    @When("^a search is made for prisoners with DOB between \"([^\"]*)\" and \"([^\"]*)\" for range ([0-9]*) -> ([0-9]*)$")
    public void aSearchIsMadeForPrisonersBetweenTwoDates(final String dobFrom, final String dobTo, final int offset, final int limit) throws Throwable {
        final Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotBlank(dobFrom)) {
            params.put("dobFrom", dobFrom);
        }
        if (StringUtils.isNotBlank(dobTo)) {
            params.put("dobTo", dobTo);
        }
        prisonerSearch.search(params, offset, limit, HttpStatus.OK);
    }

    @When("^a search is made for prisoners$")
    public void aSearchIsMadeForPrisoners() throws Throwable {
        prisonerSearch.search(ImmutableMap.of("firstName", "DUMMY"), 0, 100, HttpStatus.FORBIDDEN);
    }

    @When("^a search is made for prisoners with first name \"([^\"]*)\", middle names \"([^\"]*)\" and last name \"([^\"]*)\"$")
    public void aSearchIsMadeForPrisonersWithFirstNameMiddleNamesAndLastName(final String firstName, final String middleNames, final String lastName) throws Throwable {
        final var params = buildNameSearch(firstName, middleNames, lastName, false);

        prisonerSearch.search(params, 0, 100, HttpStatus.OK);
    }

    @When("^a partial name search is made for prisoners with first name \"([^\"]*)\", middle names \"([^\"]*)\" and last name \"([^\"]*)\"$")
    public void aPartialNameSearchIsMadeForPrisonersWithFirstNameMiddleNamesAndLastName(final String firstName, final String middleNames, final String lastName) throws Throwable {
        final var params = buildNameSearch(firstName, middleNames, lastName, true);

        prisonerSearch.search(params, 0, 100, HttpStatus.OK);
    }

    private Map<String, String> buildNameSearch(final String firstName, final String middleNames, final String lastName, final boolean partialNameMatch) {
        final Map<String, String> params = new HashMap<>();

        Optional.ofNullable(StringUtils.trimToNull(firstName)).ifPresent(name -> params.put("firstName", name));
        Optional.ofNullable(StringUtils.trimToNull(middleNames)).ifPresent(name -> params.put("middleNames", name));
        Optional.ofNullable(StringUtils.trimToNull(lastName)).ifPresent(name -> params.put("lastName", name));

        if (partialNameMatch) {
            params.put("partialNameMatch", Boolean.TRUE.toString());
        }

        return params;
    }

    @When("^a search is made for prisoners with date of birth of \"([^\"]*)\"$")
    public void aSearchIsMadeForPrisonersWithDateOfBirthOf(final String dob) throws Throwable {
        prisonerSearch.search(ImmutableMap.of("dob", dob), 0, 100, HttpStatus.OK);
    }

    @When("^a search is made for prisoners with an offender number of \"([^\"]*)\"$")
    public void aSearchIsMadeForPrisonersWithAnOffenderNumberOf(final String offenderNo) throws Throwable {
        prisonerSearch.search(ImmutableMap.of("offenderNo", offenderNo), 0, 100, HttpStatus.OK);
    }

    @When("^a search is made for prisoners with an offender number of \"([^\"]*)\" expecting failure$")
    public void aSearchIsMadeForPrisonersWithFailure(final String offenderNo) throws Throwable {
        prisonerSearch.search(ImmutableMap.of("offenderNo", offenderNo), 0, 100, HttpStatus.FORBIDDEN);
    }

    @When("^a search is made for prisoners with offender numbers of \"([^\"]*)\" using simple endpoint$")
    public void aSimpleSearchIsMadeForPrisonersWithAnOffenderNumberOf(final String offenderNos) throws Throwable {
        prisonerSearch.simpleSearch(Arrays.asList(StringUtils.split(offenderNos, ",")), HttpStatus.OK);
    }

    @When("^a search is made for prisoners with CRO number of \"([^\"]*)\"$")
    public void aSearchIsMadeForPrisonersWithCRONumberOf(final String cro) throws Throwable {
        final Map<String, String> params = new HashMap<>();

        params.put("croNumber", cro);

        prisonerSearch.search(params, 0, 100, HttpStatus.OK);
    }

    @When("^a search is made for prisoners with PNC number of \"([^\"]*)\"$")
    public void aSearchIsMadeForPrisonersWithPNCNumberOf(final String pnc) throws Throwable {
        final Map<String, String> params = new HashMap<>();

        params.put("pncNumber", pnc);

        prisonerSearch.search(params, 0, 100, HttpStatus.OK);
    }

    @When("^an invalid search is made for prisoners with PNC number of \"([^\"]*)\"$")
    public void anInvalidSearchIsMadeForPrisonersWithPNCNumberOf(final String pnc) throws Throwable {
        final Map<String, String> params = new HashMap<>();

        params.put("pncNumber", pnc);

        prisonerSearch.search(params, 0, 100, HttpStatus.BAD_REQUEST);
    }

    @Then("^access is denied$")
    public void accessIsDenied() throws Throwable {
        prisonerSearch.verifyAccessDenied();
    }

    @Then("^bad request response is received from prisoner search API$")
    public void badRequestResponseIsReceivedFromPrisonerSearchAPI() {
        prisonerSearch.verifyBadRequest("Incorrectly formatted PNC number");
    }

    @Given("^That each search below returns all matching aliases$")
    public void thatEachSearchBelowReturnsAllMatchingAliases() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        prisonerSearch.includeAliases();

    }

    @When("^a search is made for prisoners with location of \"([^\"]*)\" and lastname of \"([^\"]*)\"$")
    public void aSearchIsMadeForPrisonersWithLocationOfAndLastnameOf(final String arg0, final String arg1) throws Throwable {
        final Map<String, String> params = new HashMap<>();

        params.put("location", arg0);
        params.put("lastName", arg1);

        prisonerSearch.search(params, 0, 100, HttpStatus.OK);
    }

    @When("^a search is made for prisoners with gender code of \"([^\"]*)\" and lastname of \"([^\"]*)\"$")
    public void aSearchIsMadeForPrisonersWithGenderCodeOfAndLastnameOf(final String gender, final String lastName) throws Throwable {
        final Map<String, String> params = new HashMap<>();

        params.put("gender", gender);
        params.put("lastName", lastName);

        prisonerSearch.search(params, 0, 100, HttpStatus.OK);
    }

    @When("^a search is made for prisoners with invalid gender code of \"([^\"]*)\" and lastname of \"([^\"]*)\"$")
    public void aSearchIsMadeForPrisonersWithInvalidGenderCodeOfAndLastnameOf(final String gender, final String lastName) throws Throwable {
        final Map<String, String> params = new HashMap<>();

        params.put("gender", gender);
        params.put("lastName", lastName);

        prisonerSearch.search(params, 0, 100, HttpStatus.BAD_REQUEST);
    }

    @Then("^a bad request response is received from the prisoner search api with message \"([^\"]*)\"$")
    public void aBadRequestResponseIsReceivedFromThePrisonerSearchApiWithMessage(final String message) throws Throwable {
        prisonerSearch.verifyBadRequest(message);
    }
}
