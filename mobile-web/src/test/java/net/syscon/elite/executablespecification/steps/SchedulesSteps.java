package net.syscon.elite.executablespecification.steps;


import static org.assertj.core.api.Assertions.assertThat;

import net.syscon.elite.api.model.PrisonerSchedule;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.test.EliteClientException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * BDD step implementations for Schedules service.
 */
public class SchedulesSteps extends CommonSteps {
    private static final String API_GROUPS_URL = API_PREFIX + "schedules/{agencyId}/groups/{name}";

    private List<PrisonerSchedule> results;
    private String agency;
    private String groupName;

    @Override
    protected void init() {
        super.init();
        results = null;
        agency = null;
        groupName = null;
    }

    private List<PrisonerSchedule> dispatchListRequest(String url, String agencyId, String name, TimeSlot timeSlot) {
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

    public void givenScheduledEventsForCurrentDay() {
        // setup in data
    }

    public void givenSchedulesAreOnlyForOffendersLocated() {
        // setup in data
    }

    public void givenNoScheduledEventsForCurrentDay() {
        agency = "LEI";
        groupName = "LandingH1Evens";
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
    }

    // public void verifyField(String field, String value) throws
    // ReflectiveOperationException {
    // super.verifyField(result, field, value);
    // }

    public void givenLocationGroupDoesNotExistForTheAgency() {
        groupName = "doesnotexist";
    }

    public void getSchedules(String agencyId, String name) {
        results = dispatchListRequest(API_GROUPS_URL, agencyId, name, null);
    }

    public void getSchedules(String agencyId, String name, TimeSlot timeSlot) {
        results = dispatchListRequest(API_GROUPS_URL, agencyId, name, timeSlot);
    }

    public void getSchedules() {
        results = dispatchListRequest(API_GROUPS_URL, agency, groupName, null);
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
        assertThat(results).isSortedAccordingTo((o1,o2) -> {return o1.getCellLocation().compareTo(o2.getCellLocation());});
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
}
