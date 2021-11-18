package uk.gov.justice.hmpps.prison.api.resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.api.support.Status;
import uk.gov.justice.hmpps.prison.repository.UserRepository;
import uk.gov.justice.hmpps.prison.service.filters.NameFilter;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserResourceIntTest extends ResourceTest {

    public static final String USERNAME = "joe";
    public static final long ROLE_ID = 1L;
    @MockBean
    private UserRepository userRepository;

    @Test
    public void getUser_statusUsesDefaultValueAllWhenNonSupplied() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN"),
            Map.of());
        final var pageRequest = new PageRequest(null, Order.ASC, 0L, 20L);
        final var userDetails = new Page<UserDetail>(List.of(), 0, pageRequest);
        when(userRepository.findUsers(any(), any(), any(), isNull(), isNull(), any())).thenReturn(userDetails);
        final var responseEntity = testRestTemplate.exchange("/api/users", HttpMethod.GET, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).findUsers(eq(null), any(NameFilter.class), eq(Status.ALL), isNull(), isNull(), any(PageRequest.class));
    }

    @Test
    public void getUser_statusUsesDefaultValueAllWhenBlankSupplied() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN"),
            Map.of());
        final var pageRequest = new PageRequest(null, Order.ASC, 0L, 20L);
        final var userDetails = new Page<UserDetail>(List.of(), 0, pageRequest);
        when(userRepository.findUsers(any(), any(), any(), isNull(), isNull(), any())).thenReturn(userDetails);
        final var responseEntity = testRestTemplate.exchange("/api/users?accessRole=&nameFilter=&caseload=&status=&activeCaseload=", HttpMethod.GET, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).findUsers(eq(List.of()), any(NameFilter.class), eq(Status.ALL), isNull(), isNull(), any(PageRequest.class));
    }

    @Test
    public void getUser_caseloadSearch() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN"),
            Map.of());
        final var pageRequest = new PageRequest(null, Order.ASC, 0L, 20L);
        final var userDetails = new Page<UserDetail>(List.of(), 0, pageRequest);
        when(userRepository.findUsers(any(), any(), any(), anyString(), isNull(), any())).thenReturn(userDetails);
        final var responseEntity = testRestTemplate.exchange("/api/users?caseload=MDI", HttpMethod.GET, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).findUsers(eq(null), any(NameFilter.class), eq(Status.ALL), eq("MDI"), isNull(), any(PageRequest.class));
    }

    @Test
    public void getUser_allFields() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN"),
            Map.of());
        final var pageRequest = new PageRequest(null, Order.ASC, 0L, 20L);
        final var userDetails = new Page<UserDetail>(List.of(), 0, pageRequest);
        when(userRepository.findUsers(any(), any(), any(), anyString(), anyString(), any())).thenReturn(userDetails);
        final var responseEntity = testRestTemplate.exchange("/api/users?accessRole=SOME_ROLE&nameFilter=BOB&caseload=MDI&status=ACTIVE&activeCaseload=LEI", HttpMethod.GET, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).findUsers(eq(List.of("SOME_ROLE")), eq(new NameFilter("BOB")), eq(Status.ACTIVE), eq("MDI"), eq("LEI"), any(PageRequest.class));
    }

    @Test
    public void getUser_MultipleRoles() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN"),
            Map.of());
        final var pageRequest = new PageRequest(null, Order.ASC, 0L, 20L);
        final var userDetails = new Page<UserDetail>(List.of(), 0, pageRequest);
        when(userRepository.findUsers(any(), any(), any(), anyString(), anyString(), any())).thenReturn(userDetails);
        final var responseEntity = testRestTemplate.exchange("/api/users?accessRole=SOME_ROLE1&accessRole=SOME_ROLE2&nameFilter=BOB&caseload=MDI&status=ACTIVE&activeCaseload=LEI", HttpMethod.GET, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).findUsers(eq(List.of("SOME_ROLE1","SOME_ROLE2")), eq(new NameFilter("BOB")), eq(Status.ACTIVE), eq("MDI"), eq("LEI"), any(PageRequest.class));
    }
    @Test
    public void getStaffUsersForLocalAdministrator_statusUsesDefaultValueAllWhenNonSupplied() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN"),
            Map.of());
        final var pageRequest = new PageRequest(null, Order.ASC, 0L, 20L);
        final var userDetails = new Page<UserDetail>(List.of(), 0, pageRequest);
        when(userRepository.getUsersAsLocalAdministrator(any(), any(), any(), any(), any())).thenReturn(userDetails);
        final var responseEntity = testRestTemplate.exchange("/api/users/local-administrator/available", HttpMethod.GET, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).getUsersAsLocalAdministrator(eq("BOB"),eq(null), any(NameFilter.class), eq(Status.ALL), any(PageRequest.class));
    }

    @Test
    public void getStaffUsersForLocalAdministrator_MultipleRoles() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES_ADMIN"),
            Map.of());
        final var pageRequest = new PageRequest(null, Order.ASC, 0L, 20L);
        final var userDetails = new Page<UserDetail>(List.of(), 0, pageRequest);
        when(userRepository.getUsersAsLocalAdministrator(any(), any(), any(), any(), any())).thenReturn(userDetails);
        final var responseEntity = testRestTemplate.exchange("/api/users/local-administrator/available?accessRole=ROLE_CODE1&accessRole=ROLE_CODE2", HttpMethod.GET, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).getUsersAsLocalAdministrator(eq("BOB"),eq(List.of("ROLE_CODE1", "ROLE_CODE2")), any(NameFilter.class), eq(Status.ALL), any(PageRequest.class));
    }
}
