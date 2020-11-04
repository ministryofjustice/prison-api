package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImprisonmentStatusRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;


public class SmokeTestHelperResourceIntTest extends ResourceTest {
    @Autowired
    private OffenderImprisonmentStatusRepository repository;
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @DisplayName("requires ROLE_SMOKE_TEST")
    public void requiresCorrectRole() {
        final var request = createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE), null);

        final var response = testRestTemplate.exchange(
                "/api/smoketest/offenders/A1234AA/imprisonment-status",
                HttpMethod.POST,
                request,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("not found")
    public void notFound() {

        final var response = testRestTemplate.exchange(
                "/api/smoketest/offenders/NOT_AN_OFFENDER/imprisonment-status",
                HttpMethod.POST,
                createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.SMOKE_TEST), null),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    @DisplayName("will add new imprisonment status to active booking")
    public void willAddImprisonmentStatus() {
        // GIVEN the offender booking as a single imprisonment status
        final var bookingId = -1;
        assertThat(repository.findByOffenderBookId(bookingId)).hasSize(1);

        // WHEN I setup the smoke test data
        final var response = testRestTemplate.exchange(
                "/api/smoketest/offenders/A1234AA/imprisonment-status",
                HttpMethod.POST,
                createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.SMOKE_TEST), null),
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(OK);


        // THEN I have twp imprisonment statuses
        final List<OffenderImprisonmentStatus> statuses = repository.findByOffenderBookId(bookingId);
        assertThat(statuses)
                .hasSize(2)
                .extracting(OffenderImprisonmentStatus::getImprisonmentStatus, OffenderImprisonmentStatus::getCommentText, OffenderImprisonmentStatus::getLatestStatus)
                .containsExactly(
                        tuple("SENT", "Some Comment Text", "N"),
                        tuple("SENT", "Some Comment Text", "Y"));

        assertThat(statuses.get(0).getExpiryDate()).isCloseTo(LocalDateTime.now(), within(60, ChronoUnit.SECONDS));
        assertThat(statuses.get(1).getExpiryDate()).isNull();
    }


}
