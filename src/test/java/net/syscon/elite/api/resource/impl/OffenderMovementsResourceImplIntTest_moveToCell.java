package net.syscon.elite.api.resource.impl;

import net.syscon.elite.repository.jpa.repository.BedAssignmentHistoriesRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.util.JwtParameters;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;

// All data required for these tests can be found in R__8_7_MOVE_TO_CELL.sql
public class OffenderMovementsResourceImplIntTest_moveToCell extends ResourceTest {

    @Autowired
    private OffenderBookingRepository offenderBookingRepository;
    @Autowired
    private BedAssignmentHistoriesRepository bedAssignmentHistoriesRepository;

    @Test
    public void moveToCell_validRequest_updatesDataAndReturnsOk() {
        final var dateTime = LocalDateTime.now().minusHours(1);

        final var request = createHttpEntity(mtcUserToken(), null);
        final var response = testRestTemplate.exchange(
                format("/api/bookings/-56/living-unit/-301?reasonCode=ADM&dateTime=%s", dateTime.format(ISO_LOCAL_DATE_TIME)),
                PUT,
                request,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathNumberValue("$.bookingId").isEqualTo(-56);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("$.internalLocationId").isEqualTo("-301");

        final var offenderBooking = offenderBookingRepository.findById(-56L).orElseThrow();
        assertThat(offenderBooking.getLivingUnitId()).isEqualTo(-301L);

        final var bedAssignmentHistories = bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(-56L);
        assertThat(bedAssignmentHistories.get(bedAssignmentHistories.size() - 1))
                .extracting("offenderBooking.bookingId", "livingUnitId", "assignmentReason", "assignmentDate", "assignmentDateTime")
                .containsExactlyInAnyOrder(-56L, -301L, "ADM", dateTime.toLocalDate(), dateTime.withNano(0));
    }


    private String mtcUserToken() {
        return jwtAuthenticationHelper.createJwt(
                JwtParameters.builder()
                        .username("MTC_USER")
                        .scope(List.of("read", "write"))
                        .roles(emptyList())
                        .expiryTime(Duration.ofDays(365 * 10))
                        .build()
        );
    }

}
