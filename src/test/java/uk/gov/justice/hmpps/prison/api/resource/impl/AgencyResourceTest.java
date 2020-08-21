package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class AgencyResourceTest extends ResourceTest {

    @Test
    public void testCanFindAgenciesByType() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/type/INST",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "agencies_by_type.json");
    }

    @Test
    public void testCanFindAgenciesByTypePlusInactive() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/type/INST?activeOnly={activeOnly}",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                }, "false");

        assertThatJsonFileAndStatus(response, 200, "inactive_agencies_by_type.json");
    }

    @Test
    public void testCanFindCellsWithCapacity() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/LEI/cellsWithCapacity",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });
        System.out.println(response);
        assertThatJsonFileAndStatus(response, 200, "cells_with_capacity.json");
    }

    @Test
    public void testCanFindCellsWithCapacity_filtered() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/LEI/cellsWithCapacity?attribute=DO",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });
        System.out.println(response);
        assertThatJsonFileAndStatus(response, 200, "cells_with_capacity_filtered.json");
    }

    @Test
    public void testEstablishmentTypesForMoorlandPrison() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/MDI",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathArrayValue("establishmentTypes").containsExactlyInAnyOrder("CM", "CNOMIS", "IM", "RPTR");
    }
}
