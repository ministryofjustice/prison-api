package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SchedulesResourceTest extends ResourceTest {

    @Test
    public void testThatScheduleActivities_IsReturnForAllActivityLocations() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/activities?timeSlot=PM&date=2017-09-11",
                HttpMethod.GET,
                createHttpEntity(token, ""),
                new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                });

        final var activities = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(activities).extracting("locationId").contains(-27L, -26L, -26L, -27L, -26L);
    }

    @Test
    public void testThatScheduleActivitiesByDateRange_ReturnsData() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/activities-by-date-range?timeSlot=PM&fromDate=2017-09-11&toDate=2017-09-12",
                HttpMethod.GET,
                createHttpEntity(token, ""),
                new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                });

        final var activities = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(activities).isNotEmpty();
    }

    @Test
    public void testThatSuspendedActivity_IsReturned() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/schedules/locations/-27/activities?timeSlot=PM&date=1985-01-01&includeSuspended=true",
                HttpMethod.GET,
                createHttpEntity(token, ""),
                new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                });

        final var activities = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(activities).hasSize(1);
    }

    @Test
    public void schedulesAgencyIdActivitiesByLocationId_NoLocationGroupScheduleEvents_ReturnsEmptyList() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var locationIds = getLocationIdsNoSchedules();

        final var response = testRestTemplate.exchange(
                "/api/schedules/RNI/events-by-location-ids",
                HttpMethod.POST,
                createHttpEntity(token, locationIds),
                new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                });

        final var schedules = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(schedules).isEmpty();

    }

    @Test
    public void schedulesAgencyIdActivitiesByLocationId_LocationGroupScheduleEventsInOrder_OffenderSchedulesAreInOrder() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var locationIds = getLocationIdsWithSchedules();

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/events-by-location-ids",
                HttpMethod.POST,
                createHttpEntity(token, locationIds),
                new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                });

        final var schedules = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(schedules).extracting("cellLocation").isSorted();
        assertThat(schedules).extracting("cellLocation").containsOnly("LEI-A-1-1", "LEI-A-1-10");

    }

    @Test
    public void schedulesAgencyIdActivitiesByLocationId_AmTimeslot_MorningSchedulesOnly() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var locationIds = getLocationIdsWithSchedules();

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/events-by-location-ids?timeSlot=AM",
                HttpMethod.POST,
                createHttpEntity(token, locationIds),
                new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                });

        final var schedules = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(schedules).allSatisfy(s ->
                assertThat(s.getStartTime().toLocalTime()).isBefore(LocalTime.of(12, 0))
        );

    }

    @Test
    public void schedulesAgencyIdActivitiesByLocationId_PmTimeslot_SchedulesBetween1200and1700Only() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var locationIds = getLocationIdsWithSchedules();

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/events-by-location-ids?timeSlot=PM",
                HttpMethod.POST,
                createHttpEntity(token, locationIds),
                new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                });

        final var schedules = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(schedules).allSatisfy(s ->
                assertThat(s.getStartTime().toLocalTime()).isAfterOrEqualTo(LocalTime.of(12, 0))
        );
        assertThat(schedules).allSatisfy(s ->
                assertThat(s.getStartTime().toLocalTime()).isBeforeOrEqualTo(LocalTime.of(17, 0))
        );

    }

    @Test
    public void schedulesAgencyIdActivitiesByLocationId_EveningTimeslot_EveningSchedulesOnly() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var locationIds = getLocationIdsWithSchedules();

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/events-by-location-ids?timeSlot=ED",
                HttpMethod.POST,
                createHttpEntity(token, locationIds),
                new ParameterizedTypeReference<List<PrisonerSchedule>>() {
                });

        final var schedules = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(schedules).allSatisfy(s ->
                assertThat(s.getStartTime().toLocalTime()).isAfter(LocalTime.of(17, 0))
        );

    }

    @Test
    public void schedulesAgencyIdActivitiesByLocationId_AgencyNotAccessible_ReturnsNotFound() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var notAnAgency = "ZZGHI";
        final var locationIds = getLocationIdsWithSchedules();

        final var response = testRestTemplate.exchange(
                "/api/schedules/ZZGHI/events-by-location-ids",
                HttpMethod.POST,
                createHttpEntity(token, locationIds),
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(error.getUserMessage()).contains(notAnAgency).contains("not found");

    }

    @Test
    public void schedulesAgencyIdActivitiesByLocationId_NoLocationsPassed_ReturnsBadRequest() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/events-by-location-ids",
                HttpMethod.POST,
                createHttpEntity(token, List.of()),
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("must not be empty");
    }

    @Test
    public void scheduledAppointmentsReturned() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var locationIds = getLocationIdsWithSchedules();
        final var date = LocalDate.of(2017, 1, 2);

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/appointments?date={date}",
                HttpMethod.GET,
                createHttpEntity(token, locationIds),
                new ParameterizedTypeReference<String>() {}, date);

        assertThatJsonFileAndStatus(response, 200, "scheduled-appointments-on-date.json");
    }

    @Test
    public void testGetScheduledActivityById() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
        final var eventIds = List.of(-1L, 91234L);

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/activities-by-event-ids",
                HttpMethod.POST,
                createHttpEntity(token, eventIds),
                new ParameterizedTypeReference<String>() {});

        assertThatJsonFileAndStatus(response, 200, "scheduled-activities.json");
    }

    @Test
    public void testThatGetScheduledActivitiesById_ReturnsBadRequest() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/activities-by-event-ids",
                HttpMethod.POST,
                createHttpEntity(token, Collections.emptyList()),
                new ParameterizedTypeReference<String>() {});

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void testThatGetScheduledActivitiesById_ReturnsNotFound_WhenUserNotInAgency() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_READ_ONLY);
        final var eventIds = List.of(-1L, 91234L);

        final var response = testRestTemplate.exchange(
                "/api/schedules/LEI/activities-by-event-ids",
                HttpMethod.POST,
                createHttpEntity(token, eventIds),
                new ParameterizedTypeReference<String>() {});

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }


    private List<Long> getLocationIdsNoSchedules() {
        return List.of(108582L, 108583L);
    }

    private List<Long> getLocationIdsWithSchedules() {
        return List.of(-3L, -12L, -1101L, -1002L, -1003L, -1004L, -1005L, -1006L, -1007L, -1008L, -4L,
                -5L, -6L, -7L, -8L, -9L, -10L, -11L, -33L);
    }
}
