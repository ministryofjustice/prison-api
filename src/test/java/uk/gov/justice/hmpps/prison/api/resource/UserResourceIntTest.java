package uk.gov.justice.hmpps.prison.api.resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.AccessRole;
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.api.support.Status;
import uk.gov.justice.hmpps.prison.repository.UserRepository;
import uk.gov.justice.hmpps.prison.service.filters.NameFilter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class UserResourceIntTest extends ResourceTest {

    @MockBean
    private UserRepository userRepository;


    @Test
    public void addAccessRole() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("BOB", List.of("ROLE_MAINTAIN_ACCESS_ROLES"), null);
        final var role = AccessRole.builder().roleId(1L).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(anyString())).thenReturn(Optional.of(role));

        final var responseEntity = testRestTemplate.exchange("/api/users/joe/access-role/ROLE_FRED", HttpMethod.PUT, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        verify(userRepository).addRole("joe", "NWEB", 1L);
    }

    @Test
    public void addAccessRoles() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES"),
            "[\"ROLE_FRED\",\"ROLE_GEORGE\"]");
        final var role = AccessRole.builder().roleId(1L).roleFunction("GENERAL").build();
        final var role2 = AccessRole.builder().roleId(2L).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(anyString())).thenReturn(Optional.of(role)).thenReturn(Optional.of(role2));

        final var responseEntity = testRestTemplate.exchange("/api/users/joe/access-role", HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(userRepository).addRole("joe", "NWEB", 1L);
        verify(userRepository).addRole("joe", "NWEB", 2L);
    }

    @Test
    public void addAccessRoles_noneSpecified() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES"),
            "[]");
        final var role = AccessRole.builder().roleId(1L).roleFunction("GENERAL").build();
        final var role2 = AccessRole.builder().roleId(2L).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(anyString())).thenReturn(Optional.of(role)).thenReturn(Optional.of(role2));

        final var responseEntity = testRestTemplate.exchange("/api/users/joe/access-role", HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(userRepository);
    }

    @Test
    public void addAccessRoles_noprivileges() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "BOB",
            List.of("ROLE_VIEWING"),
            "[\"ROLE_FRED\",\"ROLE_GEORGE\"]");
        final var role = AccessRole.builder().roleId(1L).roleFunction("GENERAL").build();
        final var role2 = AccessRole.builder().roleId(2L).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(anyString())).thenReturn(Optional.of(role)).thenReturn(Optional.of(role2));

        final var responseEntity = testRestTemplate.exchange("/api/users/joe/access-role", HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        verifyNoInteractions(userRepository);
    }

    @Test
    public void getUser_statusUsesDefaultValueAllWhenNonSupplied() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN"),
            Map.of());
        final var pageRequest = new PageRequest(null, Order.ASC, 0L, 20L);
        final var userDetails = new Page(List.of(), 0, pageRequest);
        when(userRepository.findUsers(any(), any(), any(), any())).thenReturn(userDetails);
        final var responseEntity = testRestTemplate.exchange("/api/users", HttpMethod.GET, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).findUsers(eq(null), any(NameFilter.class), eq(Status.ALL), any(PageRequest.class));
    }

    @Test
    public void getStaffUsersForLocalAdministrator_statusUsesDefaultValueAllWhenNonSupplied() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN"),
            Map.of());
        final var pageRequest = new PageRequest(null, Order.ASC, 0L, 20L);
        final var userDetails = new Page(List.of(), 0, pageRequest);
        when(userRepository.getUsersAsLocalAdministrator(any(),any(), any(), any(), any())).thenReturn(userDetails);
        final var responseEntity = testRestTemplate.exchange("/api/users/local-administrator/available", HttpMethod.GET, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).getUsersAsLocalAdministrator(eq("BOB"),eq(null), any(NameFilter.class), eq(Status.ALL), any(PageRequest.class));
    }

}
