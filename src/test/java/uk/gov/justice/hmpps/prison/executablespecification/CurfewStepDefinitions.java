package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.CurfewSteps;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CurfewStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    CurfewSteps curfewSteps;

    @When("^that user requests an update of the HDC status of the latest Offender Curfew for booking \"([^\"]*)\" to \"([^\"]*)\" at \"([^\"]*)\"$")
    public void updateHDCStatus(final String bookingIdString, final String checksPassed, final String dateString) {
        curfewSteps.updateHdcStatus(bookingIdString, checksPassed, dateString);
    }

    @When("^that user requests an update of the HDC approval status of the latest Offender Curfew for booking \"([^\"]*)\" with \"([^\"]*)\" to \"([^\"]*)\" and \"([^\"]*)\" at \"([^\"]*)\"$")
    public void thatUserRequestsAnUpdateOfTheHDCApprovalStatusOfTheLatestOffenderCurfewForBookingWithToAndAt(String bookingIdString, String checksPassed, String approvalStatus, String refusedReason, String dateString) {
        curfewSteps.updateHdcApprovalStatus(bookingIdString, checksPassed, approvalStatus, refusedReason, dateString);
    }

    @Then("^the response HTTP status should be \"([^\"]*)\"$")
    public void theResponseHTTPStatusIs(final String statusString) {
        curfewSteps.verifyHttpStatusCode(Integer.valueOf(statusString));
    }

    @And("^the latest home detention curfew for booking \"([^\"]*)\" should match \"([^\"]*)\", \"([^\"]*)\" if \"([^\"]*)\" is 204$")
    public void theLatestHomeDetentionCurfewForBookingShouldMatchIfIs(Long bookingId, Boolean checksPassed, String checksPassedDate, int httpStatus) {
        if (204 != httpStatus) return;
        curfewSteps.verifyLatestHomeDetentionCurfew(bookingId, checksPassed, asLocalDate(checksPassedDate));
    }

    @And("^the latest home detention curfew for booking \"([^\"]*)\" should match \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\" if \"([^\"]*)\" is 204$")
    public void theLatestHomeDetentionCurfewForBookingShouldMatchIfIs(Long bookingId, String approvalStatus, String refusedReason, String approvalStatusDate, int httpStatus) {
        if (204 != httpStatus) return;
        curfewSteps.verifyLatestHomeDetentionCurfew(
                bookingId,
                approvalStatus,
                StringUtils.defaultIfBlank(refusedReason, null),
                asLocalDate(approvalStatusDate));
    }

    private LocalDate asLocalDate(String localDateString) {
        if (StringUtils.isEmpty(localDateString)) return null;
        return LocalDate.parse(localDateString, DateTimeFormatter.ISO_DATE);
    }
}
