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
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BulkAppointmentsStepDefinitions extends AbstractStepDefinitions {

    private static final Pattern RELATIVE_TIME_PATTERN = Pattern.compile("^(Today_plus_)(\\d+)_days_T(\\d{2}:\\d{2})$");
    @Autowired
    private BulkAppointmentSteps bulkAppointmentSteps;

    @When("^These appointment defaults:$")
    public void appointmentDefaults(final Map<String, String> defaults) {
        final var builder = AppointmentDefaults
                .builder()
                .locationId(Long.parseLong(defaults.get("locationId")))
                .appointmentType(defaults.get("appointmentType"))
                .startTime(asLocalDateTime(defaults.get("startTime")))
                .comment(defaults.get("comment"));
        if (!StringUtils.isBlank(defaults.get("endTime"))) {
            builder.endTime(asLocalDateTime(defaults.get("endTime")));
        }
        bulkAppointmentSteps.appointmentDefaults(builder.build());
    }

    @And("^these appointment details:$")
    public void theseAppointmentDetails(final List<Map<String, String>> details) {
        final var appointmentDetails = details
                .stream()
                .map(row -> AppointmentDetails
                        .builder()
                        .bookingId(Long.valueOf(row.get("bookingId")))
                        .startTime(asLocalDateTime(row.get("startTime")))
                        .endTime(asLocalDateTime(row.get("endTime")))
                        .comment(row.get("comment"))
                        .build())
                .collect(Collectors.toList());
        bulkAppointmentSteps.appointmentDetails(appointmentDetails);
    }

    @Then("^appointments for tomorrow are:$")
    public void appointmentsForTomorrow(List<Map<String, String>> appointments) {
        LocalDate date = LocalDate.now().plusDays(1L);
        bulkAppointmentSteps.appointmentsAre(date, replaceRelativeTimes(appointments));
    }


    @Then("^appointments for the day after tomorrow are:$")
    public void appointmentsForTheDayAfterTomorrow(List<Map<String, String>> appointments) {
        LocalDate date = LocalDate.now().plusDays(2L);
        bulkAppointmentSteps.appointmentsAre(date, replaceRelativeTimes(appointments));
    }

    private List<Map<String, String>> replaceRelativeTimes(List<Map<String, String>> appointments) {
        return appointments
                .stream()
                .map(BulkAppointmentsStepDefinitions::replaceRelativeTimes)
                .collect(Collectors.toList());
    }

    @When("^I make a request to create bulk appointments$")
    public void createBulkAppointments() {
        bulkAppointmentSteps.createBulkAppointments();
    }

    @Then("^the bulk appointment request is rejected$")
    public void theBulkAppointmentRequestIsRejected() {
        bulkAppointmentSteps.assertRequestRejected();
    }

    @And("^The bulk appointment request status code is <(\\d+)>$")
    public void theBulkAppointmentRequestStatusCodeIs(final int expectedStatusCode) {
        bulkAppointmentSteps.assertHttpStatusCode(expectedStatusCode);
    }

    private static LocalDateTime asLocalDateTime(String string) {
        return asOptionalLocalDateTime(string).orElse(null);
    }

    private static Optional<LocalDateTime> asOptionalLocalDateTime(String string) {
        if (StringUtils.isBlank(string)) {
            return Optional.empty();
        }
        var matcher = RELATIVE_TIME_PATTERN.matcher(string);
        if (matcher.matches()) {
            return Optional.of(
                    LocalDateTime.of(
                            LocalDate.now().plusDays(Long.parseLong(matcher.group(2))),
                            LocalTime.parse(matcher.group(3))));
        }
        return Optional.of(LocalDateTime.parse(string));
    }

    private static Map<String, String> replaceRelativeTimes(Map<String, String> map) {
        var result = new HashMap<>(map);
        result.computeIfPresent("startTime", BulkAppointmentsStepDefinitions::asDateTimeString);
        result.computeIfPresent("endTime", BulkAppointmentsStepDefinitions::asDateTimeString);
        return result;
    }

    private static String asDateTimeString(String dontCare, String value) {
        return asOptionalLocalDateTime(value).map(LocalDateTime::toString).orElse("");
    }
}
