package net.syscon.prison.executablespecification.steps;

import com.google.common.collect.ImmutableList;
import net.syscon.prison.api.model.PrisonerSchedule;
import net.syscon.prison.api.support.Order;
import net.syscon.prison.api.support.TimeSlot;
import net.syscon.prison.test.EliteClientException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Schedules service.
 */
public class SchedulesSteps extends CommonSteps {
    private static final String API_LOCATION_URL = API_PREFIX + "schedules/{agencyId}/locations/{locationId}/usage/{usage}";
    private static final String API_ACTIVITIES = API_PREFIX + "schedules/{agencyId}/activities";
    private static final String API_VISITS = API_PREFIX + "schedules/{agencyId}/visits";
    private static final String API_APPOINTMENTS = API_PREFIX + "schedules/{agencyId}/appointments";
    private static final String API_COURT_EVENTS = API_PREFIX + "schedules/{agencyId}/courtEvents";
    private static final String API_EXTERNAL_TRANSFERS = API_PREFIX + "schedules/{agencyId}/externalTransfers";

    private List<PrisonerSchedule> results;
    private String agency;
    private String groupName;
    private Long location;
    private String usage;

    @Override
    protected void init() {
        super.init();
        results = null;
        agency = null;
        groupName = null;
        location = null;
        usage = null;
    }

    public void givenScheduledEventsForCurrentDay() {
        // setup in data
    }

    public void givenSchedulesAreOnlyForOffendersLocated() {
        // setup in data
    }

    public void givenNoScheduledEventsForCurrentDay() {
        agency = "RNI";
        groupName = "Segregation Unit";
        location = -24L;
        usage = "APP";
    }

    public void givenAnExistingAgencyAndLocationGroup() {
        agency = "RNI";
        groupName = "Segregation Unit";
    }

    public void givenAgencyDoesNotBelongToCaseload() {
        agency = "ZZGHI";
        groupName = "A-Wing";
    }

    public void givenAgencyBelongsToCaseload() {
        agency = "RNI";
        usage = "APP";
    }

    public void givenNonExistentAgency() {
        agency = "TEST_AGENCY";
        location = -4L;
        usage = "APP";
    }

    // public void verifyField(String field, String value) throws
    // ReflectiveOperationException {
    // super.verifyField(result, field, value);
    // }

    public void givenLocationGroupDoesNotExistForTheAgency() {
        groupName = "doesnotexist";
    }

    public void givenLocationGroupDoesNotDefineAnyLocations() {
        groupName = "BlockE";
    }

    public void verifyListOfOffendersSchedules(final int size) {
        assertThat(results).asList().hasSize(size);
    }

    public void verifySchedulesAreOrdered() {
        // Check donald matthews is at the end, cell 10, so
        // just ensure cell A-1-1 comes before A-1-10
        assertThat(results).isSortedAccordingTo((o1, o2) -> o1.getCellLocation().compareTo(o2.getCellLocation()));
    }

    public void verifyOffendersAreLocatedInALocationThatBelongsToRequestedAgencyAndLocationGroup(final String locations) {
        final var cells = StringUtils.split(locations, ",");
        assertThat(results).allMatch(p -> {
            for (final var cell : cells) {
                if (p.getCellLocation().equals(cell)) {
                    return true;
                }
            }
            return false;
        });
    }

    public void schedulesAreOnlyBefore12h00() {
        assertThat(results.get(0).getStartTime().getHour()).isEqualTo(0);
        assertThat(results.get(1).getStartTime().getHour()).isEqualTo(0);
        assertThat(results.get(2).getStartTime().getHour()).isEqualTo(1);
        assertThat(results.get(3).getStartTime().getHour()).isEqualTo(3);
        assertThat(results.get(4).getStartTime().getHour()).isEqualTo(4);
    }

    public void schedulesAreOnlyBetween12And17() {
        assertThat(results.get(0).getStartTime().getHour()).isEqualTo(12);
        assertThat(results.get(1).getStartTime().getHour()).isEqualTo(13);
    }

    public void schedulesAreOnlyOnOrAfter17h00() {
        assertThat(results.get(0).getStartTime().getHour()).isEqualTo(18);
    }

// --------------------------------------------------------------------------------

    public void givenAnExistingAgencyAndLocation() {
        agency = "LEI";
        location = -28L;
        usage = "APP";
    }

    public void givenLocationDoesNotExistForTheAgency() {
        location = -99L;
    }

    public void givenScheduledEventsForCurrentDayAtLocation() {
        // data setup
    }

    public void getSchedulesForLocation() {
        results = dispatchLocationRequest(API_LOCATION_URL, agency, location, usage, null, null);
    }

    public void getSchedulesForLocation(final String agencyId, final Long loc, final String usage1, final TimeSlot timeSlot) {
        results = dispatchLocationRequest(API_LOCATION_URL, agencyId, loc, usage1, null, timeSlot);
    }

    public void getSchedulesForLocation(final String agencyId, final Long loc, final String usage1, final String date, final TimeSlot timeSlot) {
        results = dispatchLocationRequest(API_LOCATION_URL, agencyId, loc, usage1, date, timeSlot);
    }


    public void verifySchedulesAreOrderedAlphabetically() {
        assertThat(results).isSortedAccordingTo((o1, o2) -> o1.getLastName().compareTo(o2.getLastName()));
    }

    public void schedulesAreOnlyForOffendersOnCurrentDay() {
        final var now = LocalDate.now();
        assertThat(results).allMatch(p -> p.getStartTime().toLocalDate().equals(now));
    }

    public void verifyListOfOffendersSchedulesForCurrentDayLastNames(final String expectedList) {
        verifyPropertyValues(results, PrisonerSchedule::getLastName, expectedList);
    }

    public void verifyListOfScheduleEventTypes(final String expectedList) {
        verifyPropertyValues(results, PrisonerSchedule::getEvent, expectedList);
    }

    public void verifyListOfScheduleStartTimes(final String expectedList) {
        verifyLocalTimeValues(results, PrisonerSchedule::getStartTime, expectedList);
    }

    public void verifyEventComments(final String expectedList) {
        final var actual = results.stream()
                .map(PrisonerSchedule::getComment)
                .collect(Collectors.joining(","));

        assertThat(actual).isEqualTo(expectedList);
    }

    public void verifyEventDescriptionsWithLocation(final String expectedList) {
        final var actual = results.stream()
                .map(event -> event.getEventDescription() + parseLocation(event))
                .collect(Collectors.joining(","))
                .trim();

        assertThat(actual).isEqualTo(expectedList);
    }

    private String parseLocation(final PrisonerSchedule event) {
        return event.getEventLocation() == null ? "" : " (" + event.getEventLocation() + ")";
    }

    public void verifyEventDescriptionAndLocation(final String eventDescription, final String eventLocation) {
        final var match = results.stream()
                .anyMatch(event -> event.getEventDescription().equals(eventDescription) && event.getEventLocation().equals(eventLocation));

        assertThat(match).isEqualTo(true);
    }

    public void verifyCourtEvents(final String events) {
        final var actual = results.stream()
                .map(ps -> String.valueOf(ps.getEventId()))
                .collect(Collectors.joining(","));

        assertThat(actual).isEqualTo(events);
    }

    public void givenUsageInvalid() {
        usage = "INVALID";
    }

    public void verifyAttendanceDetails(final long eventId) {
        final var prisonerSchedule = results.stream()
                .filter(ps -> ps.getEventId() != null && ps.getEventId() == eventId)
                .findFirst();
        assertThat(prisonerSchedule.isPresent()).isTrue();
        assertThat(prisonerSchedule.get())
                .extracting("eventOutcome", "performance", "outcomeComment")
                .contains("ATT", "STANDARD", "blah");
    }

    public void verifyTransfer(final String firstName, final String lastName, final String transferDescription) {
        final var match = results.stream().anyMatch(result ->
                result.getFirstName().equalsIgnoreCase(firstName) &&
                        result.getLastName().equalsIgnoreCase(lastName) &&
                        result.getEventDescription().equalsIgnoreCase(transferDescription));

        assertThat(match).isTrue();
    }

    public void getActivities(final String offenderNo, final String date, final String timeSlot) {
        agency = "LEI";

        results = dispatchScheduleRequest(API_ACTIVITIES, agency, offenderNo, timeSlot, date);
    }

    public void getVisits(final String offenderNo, final String timeSlot) {
        final var today = LocalDate.now();

        agency = "LEI";

        results = dispatchScheduleRequest(API_VISITS, agency, offenderNo, timeSlot, today.toString());
    }

    public void getAppointments(final String offenderNo, final String timeSlot) {
        final var today = LocalDate.now();

        agency = "LEI";

        results = dispatchScheduleRequest(API_APPOINTMENTS, agency, offenderNo, timeSlot, today.toString());
    }

    public void getCourtEvents(final String offenderNos, final String date, final String timeSlot) {
        agency = "LEI";
        results = dispatchScheduleRequest(API_COURT_EVENTS, agency, offenderNos, timeSlot, date);
    }

    public void getExternalTransfers(final String offenderNumber, final String dateInput) {
        agency = "LEI";

        final var date = dateInput.toLowerCase().equals("today") ? LocalDate.now().format(DateTimeFormatter.ISO_DATE) : dateInput;

        results = dispatchTransfersRequest(offenderNumber, agency, date);
    }

    private List<PrisonerSchedule> dispatchTransfersRequest(final String offenderNumber, final String agencyId, final String date) {
        init();

        final var urlModifier = getUrlModifier(date, null);

        final var httpEntity = createEntity(ImmutableList.of(offenderNumber));

        try {

            final var response =
                    restTemplate.exchange(
                            SchedulesSteps.API_EXTERNAL_TRANSFERS + urlModifier,
                            HttpMethod.POST,
                            httpEntity,
                            new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                            },
                            agencyId);

            buildResourceData(response);

            return response.getBody();
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private List<PrisonerSchedule> dispatchGroupRequest(final String url, final String agencyId, final String name, final String date, final TimeSlot timeSlot) {
        init();
        final var headers = buildSortHeaders("cellLocation", Order.ASC);

        final var urlModifier = getUrlModifier(date, timeSlot);

        final var httpEntity = createEntity(null, headers);
        try {
            final var response = restTemplate.exchange(url + urlModifier, HttpMethod.GET,
                    httpEntity, new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                    }, agencyId, name);
            buildResourceData(response);
            return response.getBody();
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private List<PrisonerSchedule> dispatchLocationRequest(final String url, final String agencyId, final Long locationId, final String usage1, final String date, final TimeSlot timeSlot) {
        init();
        final var urlModifier = getUrlModifier(date, timeSlot);

        final var httpEntity = createEntity();
        try {
            final var response = restTemplate.exchange(url + urlModifier, HttpMethod.GET,
                    httpEntity, new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                    }, agencyId, locationId, usage1);
            buildResourceData(response);
            return response.getBody();
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private List<PrisonerSchedule> dispatchScheduleRequest(final String url, final String agencyId, final String offenderNo, final String timeSlot, final String date) {

        final var urlModifier = getUrlModifier(date, StringUtils.isNotEmpty(timeSlot) ? TimeSlot.valueOf(timeSlot) : null);

        final var entity = offenderNo.contains(",") ? StringUtils.split(offenderNo, ",")
                : new String[]{offenderNo};
        final var httpEntity = createEntity(entity);

        try {
            final var response = restTemplate.exchange(url + urlModifier, HttpMethod.POST,
                    httpEntity, new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                    }, agencyId);
            buildResourceData(response);
            return response.getBody();
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private String getUrlModifier(final String date, final TimeSlot timeSlot) {
        var urlModifier = "";
        if (date != null) {
            urlModifier += "?date=" + date;
        }
        if (timeSlot != null) {
            urlModifier += (StringUtils.isEmpty(urlModifier) ? '?' : '&') + "timeSlot=" + timeSlot.name();
        }

        return urlModifier;
    }
}
