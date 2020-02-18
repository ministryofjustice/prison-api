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

public class AgencyResourceImplIntTest extends ResourceTest {

    private final Location L1 = Location.builder().locationId(-1L).locationType("WING").description("LEI-A").userDescription("BLOCK A").internalLocationCode("A").build();

    @MockBean
    private LocationRepository repository;

    @SpyBean
    private LocationGroupService locationGroupService;

    @Test
    public void locationGroups_allOk_returnsSuccessAndData() {
        when(repository.getLocationGroupData("LEI")).thenReturn(List.of(L1));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/agencies/LEI/locations/groups", HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<LocationGroup>>() {});

        assertThatStatus(responseEntity, 200);
        assertThat(responseEntity.getBody()).containsExactly(LocationGroup.builder().key("A").name("Block A").build());

    }

    @Test
    public void locationGroups_randomError_returnsErrorFromControllerAdvice() {
        when(locationGroupService.getLocationGroups("LEI")).thenThrow(new EntityNotFoundException("test ex"));

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/agencies/LEI/locations/groups", HttpMethod.GET, requestEntity, ErrorResponse.class);

        assertThatStatus(responseEntity, 404);
        assertThat(responseEntity.getBody().getUserMessage()).isEqualTo("test ex");
    }

    @Test
    public void locationsByType_singleResult_returnsSuccessAndData() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/agencies/SYI/locations/type/AREA", HttpMethod.GET, requestEntity, String.class);

        assertThatJsonFileAndStatus(responseEntity, 200, "get_locations_for_agency_by_type.json");
    }

    @Test
    public void locationsByType_multipleResults_returnsAllLocations() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/agencies/SYI/locations/type/CELL", HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<Location>>() {});

        assertThatStatus(responseEntity, 200);
        assertThat(responseEntity.getBody()).extracting("locationId").containsExactlyInAnyOrder(-202L, -204L, -207L);
    }

    @Test
    public void locationsByType_agencyNotFound_returnsNotFound() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/agencies/XYZ/locations/type/AREA", HttpMethod.GET, requestEntity, ErrorResponse.class);

        assertThatStatus(responseEntity, 404);
        assertThat(responseEntity.getBody().getUserMessage()).contains("XYZ");
    }

    @Test
    public void locationsByType_locationTypeNotFound_returnsNotFound() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

        final var responseEntity = testRestTemplate.exchange("/api/agencies/SYI/locations/type/WXYZ", HttpMethod.GET, requestEntity, ErrorResponse.class);

        assertThatStatus(responseEntity, 404);
        assertThat(responseEntity.getBody().getUserMessage()).contains("WXYZ");
    }
}
