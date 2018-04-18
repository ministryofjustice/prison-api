package net.syscon.elite.executablespecification.steps;


import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.test.EliteClientException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Schedules service.
 */
public class SchedulesSteps extends CommonSteps {
    private static final String API_GROUPS_URL = API_PREFIX + "schedules/{agencyId}/groups/{name}";
    private static final String API_LOCATION_URL = API_PREFIX + "schedules/{agencyId}/locations/{locationId}/usage/{usage}";

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

    private List<PrisonerSchedule> dispatchGroupRequest(String url, String agencyId, String name, TimeSlot timeSlot) {
        init();
        String urlModifier = "";
        if (timeSlot != null) {
            urlModifier += "?timeSlot=" + timeSlot.name();
        }
        HttpEntity<?> httpEntity = createEntity();
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
    
    private List<PrisonerSchedule> dispatchLocationRequest(String url, String agencyId, Long locationId, String usage1, TimeSlot timeSlot) {
        init();
        String urlModifier = "";
        if (timeSlot != null) {
            urlModifier += "?timeSlot=" + timeSlot.name();
        }
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
        results = dispatchGroupRequest(API_GROUPS_URL, agencyId, group, null);
    }

    public void getSchedulesForLocationGroup(String agencyId, String group, TimeSlot timeSlot) {
        results = dispatchGroupRequest(API_GROUPS_URL, agencyId, group, timeSlot);
    }

    public void getSchedulesForLocationGroup() {
        results = dispatchGroupRequest(API_GROUPS_URL, agency, groupName, null);
    }

    public void verifyListOfOffendersSchedulesForCurrentDay(int size) {
        assertThat(results).asList().hasSize(size);
        /*
 {
        "offenderNo": "A1234AC",
        "firstName": "NORMAN",
        "lastName": "BATES",
        "cellLocation": "LEI-A-1-1",
        "event": "VISIT",
        "eventDescription": "Visits",
        "comment": "Official Visit",
        "startTime": "2017-12-29T00:00:00",
        "endTime": "2017-12-29T00:00:00"
    },
    {
        "offenderNo": "A1234AC",
        "firstName": "NORMAN",
        "lastName": "BATES",
        "cellLocation": "LEI-A-1-1",
        "event": "VISIT",
        "eventDescription": "Visits",
        "comment": "Social Contact",
        "startTime": "2017-12-29T01:00:00",
        "endTime": "2017-12-29T01:00:00"
    },
    {
        "offenderNo": "A1234AC",
        "firstName": "NORMAN",
        "lastName": "BATES",
        "cellLocation": "LEI-A-1-1",
        "event": "MEDE",
        "eventDescription": "Medical - Dentist",
        "comment": "comment17",
        "startTime": "2017-12-29T03:00:00",
        "endTime": "2017-12-29T03:00:00"
    },
    {
        "offenderNo": "A1234AC",
        "firstName": "NORMAN",
        "lastName": "BATES",
        "cellLocation": "LEI-A-1-1",
        "event": "EDUC",
        "eventDescription": "Education",
        "comment": "comment18",
        "startTime": "2017-12-29T04:00:00",
        "endTime": "2017-12-29T04:00:00"
    },
    {
        "offenderNo": "A1234AC",
        "firstName": "NORMAN",
        "lastName": "BATES",
        "cellLocation": "LEI-A-1-1",
        "event": "EDUC",
        "eventDescription": "Education",
        "comment": "Woodwork",
        "startTime": "2017-12-29T12:00:00",
        "endTime": "2017-12-29T12:00:00"
    },
    {
        "offenderNo": "A1234AC",
        "firstName": "NORMAN",
        "lastName": "BATES",
        "cellLocation": "LEI-A-1-1",
        "event": "EDUC",
        "eventDescription": "Education",
        "comment": "Woodwork",
        "startTime": "2017-12-29T13:00:00",
        "endTime": "2017-12-29T13:00:00"
    },
    {
        "offenderNo": "A1234AE",
        "firstName": "DONALD",
        "lastName": "DUCK",
        "cellLocation": "LEI-A-1-10",
        "event": "EDUC",
        "eventDescription": "Education",
        "comment": "comment23",
        "startTime": "2017-12-29T01:00:00",
        "endTime": "2017-12-29T01:00:00"
    }
         */
    }

    public void verifySchedulesAreOrdered() {
        // Check donald duck is at the end, cell 10, so
        // just ensure cell A-1-1 comes before A-1-10
        assertThat(results).isSortedAccordingTo((o1,o2) -> o1.getCellLocation().compareTo(o2.getCellLocation()));
    }

    public void verifyOffendersAreLocatedInALocationThatBelongsToRequestedAgencyAndLocationGroup() {
        assertThat(results).allMatch(p -> p.getCellLocation().equals("LEI-A-1-1") || p.getCellLocation().equals("LEI-A-1-10"));
    }

    public void schedulesAreOnlyBefore12h00() {
        assertThat(results.get(0).getStartTime().getHour()).isEqualTo(0);
        assertThat(results.get(1).getStartTime().getHour()).isEqualTo(1);
        assertThat(results.get(2).getStartTime().getHour()).isEqualTo(3);
        assertThat(results.get(3).getStartTime().getHour()).isEqualTo(4);
        assertThat(results.get(4).getStartTime().getHour()).isEqualTo(1);
    }

    public void schedulesAreOnlyOnOrAfter12h00() {
        assertThat(results.get(0).getStartTime().getHour()).isEqualTo(12);
        assertThat(results.get(1).getStartTime().getHour()).isEqualTo(13);
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
        results = dispatchLocationRequest(API_LOCATION_URL, agency, location, usage, null);
    }

    public void getSchedulesForLocation(String agencyId, Long loc, String usage1, TimeSlot timeSlot) {
        results = dispatchLocationRequest(API_LOCATION_URL, agencyId, loc, usage1, timeSlot);
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

    public void givenUsageInvalid() {
        usage = "INVALID";
    }
}
