package net.syscon.elite.executablespecification.steps;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.test.EliteClientException;
import net.syscon.util.DateTimeConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Schedules service.
 */
public class SchedulesSteps extends CommonSteps {
    private static final String API_GROUPS_URL = API_PREFIX + "schedules/{agencyId}/groups/{name}";
    private static final String API_LOCATION_URL = API_PREFIX + "schedules/{agencyId}/locations/{locationId}/usage/{usage}";
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
        agency = "LEI";
        groupName = "LandingH1Evens";
        location = -24L;
        usage = "APP";
    }

    public void givenAnExistingAgencyAndLocationGroup() {
        agency = "LEI";
        groupName = "blockA";
    }

    public void givenAnExistingAgency() {
        agency = "LEI";
    }

    public void givenAgencyDoesNotBelongToCaseload() {
        agency = "ZZGHI";
        groupName = "blockA";
    }

    public void givenAgencyBelongsToCaseload() {
        agency = "LEI";
        usage = "APP";
    }

    public void givenNoneExistentAgency() {
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

    public void getSchedulesForLocationGroup(String agencyId, String group) {
        results = dispatchGroupRequest(API_GROUPS_URL, agencyId, group, null, null);
    }

    public void getSchedulesForLocationGroup(String agencyId, String group, String date, TimeSlot timeSlot) {
        results = dispatchGroupRequest(API_GROUPS_URL, agencyId, group, date, timeSlot);
    }

    public void getSchedulesForLocationGroup() {
        results = dispatchGroupRequest(API_GROUPS_URL, agency, groupName, null, null);
    }

    public void verifyListOfOffendersSchedules(int size) {
        assertThat(results).asList().hasSize(size);
    }

    public void verifySchedulesAreOrdered() {
        // Check donald duck is at the end, cell 10, so
        // just ensure cell A-1-1 comes before A-1-10
        assertThat(results).isSortedAccordingTo((o1,o2) -> o1.getCellLocation().compareTo(o2.getCellLocation()));
    }

    public void verifyOffendersAreLocatedInALocationThatBelongsToRequestedAgencyAndLocationGroup(String locations) {
        final String[] cells = StringUtils.split(locations, ",");
        assertThat(results).allMatch(p -> {
            for (String cell : cells) {
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

    public void givenLocationNoScheduledEventsForCurrentDay() {
        agency = "LEI";
        location = -24L;
    }

    public void givenScheduledEventsForCurrentDayAtLocation() {
        // data setup
    }

    public void getSchedulesForLocation() {
        results = dispatchLocationRequest(API_LOCATION_URL, agency, location, usage, null, null);
    }

    public void getSchedulesForLocation(String agencyId, Long loc, String usage1, TimeSlot timeSlot) {
        results = dispatchLocationRequest(API_LOCATION_URL, agencyId, loc, usage1, null, timeSlot);
    }

    public void getSchedulesForLocation(String agencyId, Long loc, String usage1, String date, TimeSlot timeSlot) {
        results = dispatchLocationRequest(API_LOCATION_URL, agencyId, loc, usage1, date, timeSlot);
    }


    public void verifySchedulesAreOrderedAlphabetically() {
        assertThat(results).isSortedAccordingTo((o1,o2) -> o1.getLastName().compareTo(o2.getLastName()));
    }

    public void schedulesAreOnlyForOffendersOnCurrentDay() {
        final LocalDate now = LocalDate.now();
        assertThat(results).allMatch(p -> p.getStartTime().toLocalDate().equals(now));
    }

    public void verifyListOfOffendersSchedulesForCurrentDayLastNames(String expectedList) {
        verifyPropertyValues(results, PrisonerSchedule::getLastName, expectedList);
    }

    public void verifyListOfScheduleEventTypes(String expectedList) {
        verifyPropertyValues(results, PrisonerSchedule::getEvent, expectedList);
    }

    public void verifyListOfScheduleStartTimes(String expectedList) {
        verifyLocalTimeValues(results, PrisonerSchedule::getStartTime, expectedList);
    }

    public void verifyVisits(String visits) {
        String actual = results.stream()
                 .map(PrisonerSchedule::getComment)
                .collect(Collectors.joining(","));

        assertThat(actual).isEqualTo(visits);
    }

    public void verifyAppointments(String appointments) {
        String actual = results.stream()
                .map(PrisonerSchedule::getEventDescription)
                .collect(Collectors.joining(","));

        assertThat(actual).isEqualTo(appointments);
    }

    public void verifyCourtEvents(String events) {
        String actual = results.stream()
                .map(ps -> String.valueOf(ps.getEventId()))
                .collect(Collectors.joining(","));

        assertThat(actual).isEqualTo(events);
    }

    public void givenUsageInvalid() {
        usage = "INVALID";
    }

    public void verifyAttendanceDetails(long eventId) {
        final Optional<PrisonerSchedule> prisonerSchedule = results.stream()
                .filter(ps -> ps.getEventId() != null && ps.getEventId() == eventId)
                .findFirst();
        assertThat(prisonerSchedule.isPresent()).isTrue();
        assertThat(prisonerSchedule.get())
                .extracting("eventOutcome", "performance", "outcomeComment")
                .contains("ATT", "STANDARD", "blah");
    }

    public void verifyTransfer(String firstName, String lastName, String transferDescription) {
        final boolean match = results.stream().anyMatch(result ->
                result.getFirstName().compareToIgnoreCase(firstName) == 0 &&
                result.getLastName().compareToIgnoreCase(lastName) == 0 &&
                result.getEventDescription().compareToIgnoreCase(transferDescription) == 0);

        assertThat(match).isTrue();
    }

    public void getVisits(String offenderNo, String timeSlot) {
      LocalDate today = LocalDate.now();

      agency = "LEI";

      results = dispatchScheduleRequest(API_VISITS, agency, offenderNo, timeSlot, today);
    }

    public void getAppointments(String offenderNo, String timeSlot) {
        LocalDate today = LocalDate.now();

        agency = "LEI";

        results = dispatchScheduleRequest(API_APPOINTMENTS, agency, offenderNo, timeSlot, today);
    }

    public void getCourtEvents(String offenderNos, String date, String timeSlot) {
        agency = "LEI";
        results = dispatchScheduleRequest(API_COURT_EVENTS, agency, offenderNos, timeSlot, DateTimeConverter.fromISO8601DateString(date));
    }

    public void getExternalTransfers(String offenderNumber, String dateInput) {
        agency = "LEI";

        String date = dateInput.toLowerCase().equals("today") ? LocalDate.now().format(DateTimeFormatter.ISO_DATE) : dateInput;

        results = dispatchTransfersRequest(offenderNumber, agency, date);
    }

    private List<PrisonerSchedule> dispatchTransfersRequest(String offenderNumber, String agencyId, String date) {
        init();

        String urlModifier = getUrlModifier(date, null);

        HttpEntity<?> httpEntity = createEntity(ImmutableList.of(offenderNumber));

        try {

            ResponseEntity<List<PrisonerSchedule>> response =
                    restTemplate.exchange(
                            SchedulesSteps.API_EXTERNAL_TRANSFERS + urlModifier,
                            HttpMethod.POST,
                            httpEntity,
                            new ParameterizedTypeReference<List<PrisonerSchedule>>() {},
                            agencyId);

            buildResourceData(response);

            return response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private List<PrisonerSchedule> dispatchGroupRequest(String url, String agencyId, String name, String date, TimeSlot timeSlot) {
        init();
        Map<String,String> headers = buildSortHeaders("cellLocation", Order.ASC);

        String urlModifier = getUrlModifier(date, timeSlot);

        HttpEntity<?> httpEntity = createEntity(null, headers);
        try {
            ResponseEntity<List<PrisonerSchedule>> response = restTemplate.exchange(url + urlModifier, HttpMethod.GET,
                    httpEntity, new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                    }, agencyId, name);
            buildResourceData(response);
            return response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private List<PrisonerSchedule> dispatchLocationRequest(String url, String agencyId, Long locationId, String usage1, String date, TimeSlot timeSlot) {
        init();
        String urlModifier = getUrlModifier(date, timeSlot);

        HttpEntity<?> httpEntity = createEntity();
        try {
            ResponseEntity<List<PrisonerSchedule>> response = restTemplate.exchange(url + urlModifier, HttpMethod.GET,
                    httpEntity, new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                    }, agencyId, locationId, usage1);
            buildResourceData(response);
            return response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private List<PrisonerSchedule> dispatchScheduleRequest(String url, String agencyId, String offenderNo, String timeSlot, LocalDate date) {

        String urlModifier = getUrlModifier(date.toString(), !timeSlot.equals("") ? TimeSlot.valueOf(timeSlot) : null);

        final String[] entity = offenderNo.contains(",") ? StringUtils.split(offenderNo, ",")
                : new String[]{offenderNo};
        HttpEntity<?> httpEntity = createEntity(entity);

        try {
            ResponseEntity<List<PrisonerSchedule>> response = restTemplate.exchange(url + urlModifier, HttpMethod.POST,
                    httpEntity, new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                    }, agencyId);
            buildResourceData(response);
            return response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private String getUrlModifier(String date, TimeSlot timeSlot) {
        String urlModifier = "";
        if (date != null) {
            urlModifier += "?date=" + date;
        }
        if (timeSlot != null) {
            urlModifier += (StringUtils.isEmpty(urlModifier) ? '?' : '&') + "timeSlot=" + timeSlot.name();
        }

        return urlModifier;
    }
}
