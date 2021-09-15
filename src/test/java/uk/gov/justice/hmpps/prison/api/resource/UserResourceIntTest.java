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
import uk.gov.justice.hmpps.prison.repository.jpa.model.Role;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseload;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadId;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRole;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRoleIdentity;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.RoleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRoleRepository;
import uk.gov.justice.hmpps.prison.service.filters.NameFilter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class UserResourceIntTest extends ResourceTest {

    public static final String USERNAME = "joe";
    public static final String ROLE_FRED = "ROLE_FRED";
    public static final String API_CASELOAD_ID = "NWEB";
    public static final long ROLE_ID = 1L;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserCaseloadRoleRepository userCaseloadRoleRepository;
    @MockBean
    private StaffUserAccountRepository staffUserAccountRepository;
    @MockBean
    private UserCaseloadRepository userCaseloadRepository;
    @MockBean
    private RoleRepository roleRepository;

    @Test
    public void addAccessRole() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("BOB", List.of("ROLE_MAINTAIN_ACCESS_ROLES"), null);

        final var role = Role.builder().code(ROLE_FRED).id(ROLE_ID).roleFunction("GENERAL").build();
        final var user = StaffUserAccount.builder().username(USERNAME).build();

        when(roleRepository.findByCode(ROLE_FRED)).thenReturn(Optional.of(role));
        when(userCaseloadRoleRepository.findById(UserCaseloadRoleIdentity.builder().caseload(API_CASELOAD_ID).username(USERNAME).roleId(ROLE_ID).build())).thenReturn(Optional.empty());
        final var userCaseloadId = UserCaseloadId.builder().caseload(API_CASELOAD_ID).username(USERNAME).build();
        when(userCaseloadRepository.findById(userCaseloadId)).thenReturn(Optional.of(UserCaseload.builder().id(userCaseloadId).build()));
        when(staffUserAccountRepository.findById(USERNAME)).thenReturn(Optional.of(user));

        final var responseEntity = testRestTemplate.exchange(format("/api/users/%s/access-role/%s", USERNAME, ROLE_FRED), HttpMethod.PUT, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        verify(userCaseloadRepository, never()).save(isA(UserCaseload.class));

        assertThat(user.getRoles()).hasSize(1);

        assertThat(user.getRoles().get(0)).isEqualTo(UserCaseloadRole.builder()
            .id(UserCaseloadRoleIdentity.builder()
                .caseload(API_CASELOAD_ID)
                .roleId(ROLE_ID)
                .username(USERNAME)
                .build())
            .role(role)
            .build());
    }


    @Test
    public void addAccessRoles() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES"),
            "[\"ROLE_FRED\",\"ROLE_GEORGE\"]");

        final var role1 = Role.builder().code(ROLE_FRED).id(ROLE_ID).roleFunction("GENERAL").build();
        final var role2 = Role.builder().code("ROLE_GEORGE").id(2L).roleFunction("GENERAL").build();
        final var user = StaffUserAccount.builder().username(USERNAME).build();

        when(roleRepository.findByCode(ROLE_FRED)).thenReturn(Optional.of(role1));
        when(roleRepository.findByCode("ROLE_GEORGE")).thenReturn(Optional.of(role2));

        when(userCaseloadRoleRepository.findById(UserCaseloadRoleIdentity.builder().caseload(API_CASELOAD_ID).username(USERNAME).roleId(ROLE_ID).build())).thenReturn(Optional.empty());
        when(userCaseloadRoleRepository.findById(UserCaseloadRoleIdentity.builder().caseload(API_CASELOAD_ID).username(USERNAME).roleId(2L).build())).thenReturn(Optional.empty());

        final var userCaseloadId = UserCaseloadId.builder().caseload(API_CASELOAD_ID).username(USERNAME).build();
        when(userCaseloadRepository.findById(userCaseloadId)).thenReturn(Optional.of(UserCaseload.builder().id(userCaseloadId).build()));
        when(staffUserAccountRepository.findById(USERNAME)).thenReturn(Optional.of(user));

        final var responseEntity = testRestTemplate.exchange(format("/api/users/%s/access-role", USERNAME), HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(user.getRoles()).hasSize(2);

        assertThat(user.getRoles().get(0)).isEqualTo(UserCaseloadRole.builder()
            .id(UserCaseloadRoleIdentity.builder()
                .caseload(API_CASELOAD_ID)
                .roleId(ROLE_ID)
                .username(USERNAME)
                .build())
            .role(role1)
            .build());

        assertThat(user.getRoles().get(1)).isEqualTo(UserCaseloadRole.builder()
            .id(UserCaseloadRoleIdentity.builder()
                .caseload(API_CASELOAD_ID)
                .roleId(2L)
                .username(USERNAME)
                .build())
            .role(role2)
            .build());
    }

    @Test
    public void addAccessRoles_noneSpecified() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "BOB",
            List.of("ROLE_MAINTAIN_ACCESS_ROLES"),
            "[]");

        final var responseEntity = testRestTemplate.exchange(format("/api/users/%s/access-role", USERNAME), HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(roleRepository, staffUserAccountRepository, userCaseloadRepository, userCaseloadRoleRepository);
    }

    @Test
    public void addAccessRoles_noprivileges() {
        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
            "BOB",
            List.of("ROLE_VIEWING"),
            "[\"ROLE_FRED\",\"ROLE_GEORGE\"]");

        final var responseEntity = testRestTemplate.exchange(format("/api/users/%s/access-role", USERNAME), HttpMethod.POST, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        verifyNoInteractions(roleRepository, staffUserAccountRepository, userCaseloadRepository, userCaseloadRoleRepository);
    }


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
