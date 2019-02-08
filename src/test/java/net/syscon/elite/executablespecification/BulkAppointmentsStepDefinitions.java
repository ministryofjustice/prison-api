package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.api.model.bulkappointments.AppointmentDefaults;
import net.syscon.elite.api.model.bulkappointments.AppointmentDetails;
import net.syscon.elite.executablespecification.steps.BulkAppointmentSteps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BulkAppointmentsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private BulkAppointmentSteps bulkAppointmentSteps;

    @When("^These appointment defaults:$")
    public void appointmentDefaults(Map<String, String> defaults) {
        var builder = AppointmentDefaults
                .builder()
                .locationId(Long.parseLong(defaults.get("locationId")))
                .appointmentType(defaults.get("appointmentType"))
                .startTime(getOptionalTime(defaults.get("startTime")))
                .comment(defaults.get("comment"));
        if (!StringUtils.isBlank(defaults.get("endTime"))) {
            builder.endTime(getOptionalTime(defaults.get("endTime")));
        }
        bulkAppointmentSteps.appointmentDefaults(builder.build());
    }

    @And("^these appointment details:$")
    public void theseAppointmentDetails(List<Map<String, String>> details) {
        List<AppointmentDetails>  appointmentDetails = details
                .stream()
                .map(row -> AppointmentDetails
                        .builder()
                        .bookingId(Long.valueOf(row.get("bookingId")))
                        .startTime(getOptionalTime(row.get("startTime")))
                        .endTime(getOptionalTime(row.get("endTime")))
                        .comment(row.get("comment"))
                        .build())
                .collect(Collectors.toList());
        bulkAppointmentSteps.appointmentDetails(appointmentDetails);
    }

    @Then("^appointments for the date <(\\d+)-(\\d+)-(\\d+)> are:$")
    public void appointmentsForTheDateAre(int yyyy, int mm, int dd, List<Map<String,String>> appointments) {
        LocalDate date = LocalDate.of(yyyy, mm, dd);
        bulkAppointmentSteps.appointmentsAre(date, appointments);
    }

    @When("^I make a request to create bulk appointments$")
    public void createBulkAppointments() {
        bulkAppointmentSteps.createBulkAppointments();
    }

    private LocalDateTime getOptionalTime(String string) {
        if (StringUtils.isBlank(string)) return null;
        return LocalDateTime.parse(string);
    }

    @Then("^the bulk appointment request is rejected$")
    public void theBulkAppointmentRequestIsRejected() {
        bulkAppointmentSteps.assertRequestRejected();
    }

    @And("^The bulk appointment request status code is <(\\d+)>$")
    public void theBulkAppointmentRequestStatusCodeIs(int expectedStatusCode) {
        bulkAppointmentSteps.assertHttpStatusCode(expectedStatusCode);
    }
}
