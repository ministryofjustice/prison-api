package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Arrays;

public class BookingResourceCountImplIntTest extends ResourceTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Sql(scripts = {"/sql/addingHealthProblems_init.sql"},
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    @Sql(scripts = {"/sql/addingHealthProblems_clean.sql"},
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))

    @Test
    public void countPersonalCareNeedsForOffenders() {

        webTestClient.post()
            .uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01")
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .headers(setAuthorisation(Arrays.asList("ITAG_USER")))
            .accept(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromValue("[\"A1234AA\",\"A1234AD\"]")
            ).exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo("[{\"offenderNo\":\"A1234AA\",\"size\":4},{\"offenderNo\":\"A1234AD\",\"size\":1}]");

    }

    @Test
    public void countPersonalCareNeedsForOffenders_missingProblemType() {

        webTestClient.post()
            .uri("/api/bookings/offenderNo/personal-care-needs/count?fromStartDate=2010-01-01&toStartDate=2011-01-01")
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .headers(setAuthorisation(Arrays.asList("ITAG_USER")))
            .accept(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromValue("[\"A1234AA\",\"A1234AD\"]")
            ).exchange()
            .expectStatus().isBadRequest();


    }

    @Test
    public void countPersonalCareNeedsForOffenders_emptyBody() {

        webTestClient.post()
            .uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01")
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .headers(setAuthorisation(Arrays.asList("ITAG_USER")))
            .accept(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromValue("")
            ).exchange()
            .expectStatus().isBadRequest();

    }
}
