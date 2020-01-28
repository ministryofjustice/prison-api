package net.syscon.elite.api.resource.impl;


import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.LocationGroup;
import net.syscon.elite.repository.LocationRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.LocationGroupService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * TODO DT-526 Replace calls to .../locations/groupsNew with .../locations/groups once the new endpoint has been tested from whereabouts
 *      API and prisonstaffhub UI has been changed to no longer point to this API (replaced with call to whereabouts API).
 */

public class AgencyResourceImplIntTest extends ResourceTest {

    private final Location L1 = Location.builder().locationId(-1L).locationType("WING").description("LEI-A").userDescription("BLOCK A").internalLocationCode("A").build();

    @MockBean
    private LocationRepository repository;

    @SpyBean
    @Qualifier("defaultLocationGroupService")
    private LocationGroupService locationGroupService;

    @Test
    public void locationGroups_allOk_returnsSuccessAndData() {
        when(repository.getLocationGroupData("LEI")).thenReturn(List.of(L1));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/agencies/LEI/locations/groupsNew", HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<LocationGroup>>() {});

        assertThatStatus(responseEntity, 200);
        assertThat(responseEntity.getBody()).containsExactly(LocationGroup.builder().key("A").name("Block A").build());

    }

    @Test
    public void locationGroups_randomError_returnsErrorFromControllerAdvice() {
        when(locationGroupService.getLocationGroups("LEI")).thenThrow(new EntityNotFoundException("test ex"));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/agencies/LEI/locations/groupsNew", HttpMethod.GET, requestEntity, ErrorResponse.class);

        assertThatStatus(responseEntity, 404);
        assertThat(responseEntity.getBody().getUserMessage()).isEqualTo("test ex");
    }
}
