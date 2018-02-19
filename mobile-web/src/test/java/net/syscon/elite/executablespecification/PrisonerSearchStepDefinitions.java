package net.syscon.elite.executablespecification;


import com.google.common.collect.ImmutableMap;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.PrisonerSearchSteps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * BDD step definitions for the following Offender Search API endpoints:
 * <ul>
 *     <li>/v2/prisoners</li>
 * </ul>
 *
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class PrisonerSearchStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private PrisonerSearchSteps prisonerSearch;

    @Then("^\"([^\"]*)\" prisoner records are returned$")
    public void bookingRecordsAreReturned(String expectedCount) throws Throwable {
        prisonerSearch.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @Then("^\"([^\"]*)\" total prisoner records are available$")
    public void totalBookingRecordsAreAvailable(String expectedCount) throws Throwable {
        prisonerSearch.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @And("^prisoner offender numbers match \"([^\"]*)\"$")
    public void offenderNumbersMatch(String offenderNoList) throws Throwable {
        prisonerSearch.verifyOffenderNumbers(offenderNoList);
    }

    @And("^the prisoners first names match \"([^\"]*)\"$")
    public void offenderFirstNamesMatch(String firstNames) throws Throwable {
        prisonerSearch.verifyFirstNames(firstNames);
    }

    @And("^the prisoners middle names match \"([^\"]*)\"$")
    public void offenderMiddleNamesMatch(String middleNames) throws Throwable {
        prisonerSearch.verifyMiddleNames(middleNames);
    }

    @And("^the prisoners last names match \"([^\"]*)\"$")
    public void offenderLastNamesMatch(String lastNames) throws Throwable {
        prisonerSearch.verifyLastNames(lastNames);
    }

    @And("^the prisoners dob matches \"([^\"]*)\"$")
    public void dateOfBirthMatch(String dobs) throws Throwable {
        prisonerSearch.verifyDobs(dobs);
    }

    @When("^a search is made for prisoners with DOB on or after (\\d+-\\d+-\\d+) for range ([0-9]*) -> ([0-9]*)$")
    public void aSearchIsMadeForPrisonersWithDOBOnOrAfterForRange(String dobFrom, long offset, long limit) throws Throwable {
        prisonerSearch.search(ImmutableMap.of("dobFrom", dobFrom), offset, limit, HttpStatus.OK);
    }
    @When("^a search is made for prisoners with DOB between \"([^\"]*)\" and \"([^\"]*)\" for range ([0-9]*) -> ([0-9]*)$")
    public void aSearchIsMadeForPrisonersBetweenTwoDates(String dobFrom, String dobTo, int offset, int limit) throws Throwable {
        Map<String, String> params = new HashMap<>();
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
    public void aSearchIsMadeForPrisonersWithFirstNameMiddleNamesAndLastName(String firstName, String middleNames, String lastName) throws Throwable {
        Map<String,String> params = buildNameSearch(firstName, middleNames, lastName, false);

        prisonerSearch.search(params, 0, 100, HttpStatus.OK);
    }

    @When("^a partial name search is made for prisoners with first name \"([^\"]*)\", middle names \"([^\"]*)\" and last name \"([^\"]*)\"$")
    public void aPartialNameSearchIsMadeForPrisonersWithFirstNameMiddleNamesAndLastName(String firstName, String middleNames, String lastName) throws Throwable {
        Map<String,String> params = buildNameSearch(firstName, middleNames, lastName, true);

        prisonerSearch.search(params, 0, 100, HttpStatus.OK);
    }

    private Map<String,String> buildNameSearch(String firstName, String middleNames, String lastName, boolean partialNameMatch) {
        Map<String, String> params = new HashMap<>();

        Optional.ofNullable(StringUtils.trimToNull(firstName)).ifPresent(name -> params.put("firstName", name));
        Optional.ofNullable(StringUtils.trimToNull(middleNames)).ifPresent(name -> params.put("middleNames", name));
        Optional.ofNullable(StringUtils.trimToNull(lastName)).ifPresent(name -> params.put("lastName", name));

        if (partialNameMatch) {
            params.put("partialNameMatch", Boolean.TRUE.toString());
        }

        return params;
    }

    @When("^a search is made for prisoners with date of birth of \"([^\"]*)\"$")
    public void aSearchIsMadeForPrisonersWithDateOfBirthOf(String dob) throws Throwable {
        prisonerSearch.search(ImmutableMap.of("dob", dob), 0, 100, HttpStatus.OK);
    }

    @When("a search is made for prisoners with an offender number of \"([^\"]*)\"$")
    public void aSearchIsMadeForPrisonersWithAnOffenderNumberOf(String offenderNo) throws Throwable {
        prisonerSearch.search(ImmutableMap.of("offenderNo", offenderNo), 0, 100, HttpStatus.OK);
    }

    @When("^a search is made for prisoners with PNC number of \"([^\"]*)\" and/or CRO number of \"([^\"]*)\"$")
    public void aSearchIsMadeForPrisonersWithPNCNumberOfAndOrCRONumberOf(String pnc, String cro) throws Throwable {
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotBlank(pnc)) {
            params.put("pncNumber", pnc);
        }
        if (StringUtils.isNotBlank(cro)) {
            params.put("croNumber", cro);
        }
        prisonerSearch.search(params, 0, 100, HttpStatus.OK);
    }

    @Then("^access is denied$")
    public void accessIsDenied() throws Throwable {
        prisonerSearch.verifyAccessDenied();
    }
}
