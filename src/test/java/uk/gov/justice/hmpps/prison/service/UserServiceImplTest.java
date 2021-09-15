package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableList;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.api.support.Order;
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
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRoleFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRoleRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.filters.NameFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link UserService}.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    private static final String USERNAME_GEN = "HH_GEN";
    private static final String LEEDS_CASELOAD_ID = "LEI";
    private static final String API_CASELOAD_ID = "NWEB";
    private static final String ROLE_CODE = "A_ROLE";
    private static final long ROLE_ID = 1L;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCaseloadRoleRepository userCaseloadRoleRepository;
    @Mock
    private StaffUserAccountRepository staffUserAccountRepository;
    @Mock
    private UserCaseloadRepository userCaseloadRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CaseLoadService caseLoadService;
    @Mock
    private AuthenticationFacade securityUtils;
    @Mock
    private TelemetryClient telemetryClient;

    private UserService userService;

    @BeforeEach
    public void init() {
        userService = new UserService(caseLoadService, roleRepository, userRepository, userCaseloadRoleRepository, userCaseloadRepository, staffUserAccountRepository, securityUtils, API_CASELOAD_ID, 100, telemetryClient);
    }

    @Test
    public void testGetUsers() {
        final var pr = new PageRequest("lastName,firstName", Order.ASC, 0L, 10L);  //the default if non provided
        final var nameFilterDto = new NameFilter("A");
        userService.getUsers("A", List.of(ROLE_CODE), Status.ALL, "CASE", "LOAD", null);

        verify(userRepository).findUsers(eq(List.of(ROLE_CODE)), refEq(nameFilterDto), eq(Status.ALL), eq("CASE"), eq("LOAD"), refEq(pr));
    }

    @Test
    public void testGetUsersWithFullNameSearch() {
        final var pr = new PageRequest("lastName,firstName", Order.ASC, 0L, 10L);  //the default if non provided
        final var nameFilterDto = new NameFilter("Brown James");
        userService.getUsers("Brown James", List.of(ROLE_CODE), Status.ALL, null, null, null);

        verify(userRepository).findUsers(eq(List.of(ROLE_CODE)), refEq(nameFilterDto), eq(Status.ALL), isNull(), isNull(), refEq(pr));
    }

    @Test
    public void testGetUsersFilterActive() {
        final var pr = new PageRequest("lastName,firstName", Order.ASC, 0L, 10L);  //the default if non provided
        final var nameFilterDto = new NameFilter("A");
        userService.getUsers("A", List.of(ROLE_CODE), Status.ACTIVE, null, null, null);

        verify(userRepository).findUsers(eq(List.of(ROLE_CODE)), refEq(nameFilterDto), eq(Status.ACTIVE), isNull(), isNull(), refEq(pr));
    }

    @Test
    public void testGetUsersFilterInactive() {
        final var pr = new PageRequest("lastName,firstName", Order.ASC, 0L, 10L);  //the default if non provided
        final var nameFilterDto = new NameFilter("A");
        userService.getUsers("A", List.of(ROLE_CODE), Status.INACTIVE, null, null, null);

        verify(userRepository).findUsers(eq(List.of(ROLE_CODE)), refEq(nameFilterDto), eq(Status.INACTIVE), isNull(), isNull(), refEq(pr));
    }

    @Test
    public void testGetRolesByUserAndCaseload() {
        final List<UserCaseloadRole> list = List.of(UserCaseloadRole.builder()
            .id(UserCaseloadRoleIdentity.builder().caseload(LEEDS_CASELOAD_ID).username(USERNAME_GEN).build())
            .role(Role.builder().roleFunction("GENERAL").code("TEST_CODE").name("Test Role").build())
            .build());  //the default if none provided

        when(caseLoadService.getCaseLoad(Mockito.eq(LEEDS_CASELOAD_ID))).thenReturn(Optional.of(CaseLoad.builder().build()));
        final var spec = UserCaseloadRoleFilter.builder().username(USERNAME_GEN).caseload(LEEDS_CASELOAD_ID).build();
        when(userCaseloadRoleRepository.findAll(spec)).thenReturn(list);

        userService.getAccessRolesByUserAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID, true);

        verify(userCaseloadRoleRepository).findAll(spec);
    }

    @Test
    public void testGetRolesByUserAndCaseloadCaseloadDoesNotExist() {
        when(caseLoadService.getCaseLoad(Mockito.eq(LEEDS_CASELOAD_ID))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getAccessRolesByUserAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID, true)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testGetRolesByUserAndCaseloadUsernameNotProvided() {
        assertThatThrownBy(() -> userService.getAccessRolesByUserAndCaseload("", LEEDS_CASELOAD_ID, false)).isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    public void testAddAccessRoleForApiCaseloadWithUserAccessibleCaseloadEntry() {
        final var role = Role.builder().code(ROLE_CODE).id(ROLE_ID).roleFunction("GENERAL").build();
        final var user = StaffUserAccount.builder().username(USERNAME_GEN).build();

        when(roleRepository.findByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userCaseloadRoleRepository.findById(UserCaseloadRoleIdentity.builder().caseload(API_CASELOAD_ID).username(USERNAME_GEN).roleId(ROLE_ID).build())).thenReturn(Optional.empty());
        when(userCaseloadRepository.findById(UserCaseloadId.builder().caseload(API_CASELOAD_ID).username(USERNAME_GEN).build())).thenReturn(Optional.empty());
        when(staffUserAccountRepository.findById(USERNAME_GEN)).thenReturn(Optional.of(user));
        when(securityUtils.getCurrentUsername()).thenReturn("adminuser");

        userService.addAccessRole(USERNAME_GEN, ROLE_CODE);

        verify(userCaseloadRepository).save(UserCaseload.builder().id(UserCaseloadId.builder().username(USERNAME_GEN).caseload(API_CASELOAD_ID).build()).startDate(LocalDate.now()).build());

        assertThat(user.getRoles()).hasSize(1);

        assertThat(user.getRoles().get(0)).isEqualTo(UserCaseloadRole.builder()
            .id(UserCaseloadRoleIdentity.builder()
                .caseload(API_CASELOAD_ID)
                .roleId(ROLE_ID)
                .username(USERNAME_GEN)
                .build())
            .role(role)
            .build());
    }

    @Test
    public void testAddAccessRoleCreatesTelemetryEvent() {
        final var role = Role.builder().code(ROLE_CODE).id(ROLE_ID).roleFunction("GENERAL").build();
        final var user = StaffUserAccount.builder().username(USERNAME_GEN).build();

        when(roleRepository.findByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userCaseloadRoleRepository.findById(UserCaseloadRoleIdentity.builder().caseload(API_CASELOAD_ID).username(USERNAME_GEN).roleId(ROLE_ID).build())).thenReturn(Optional.empty());
        when(userCaseloadRepository.findById(UserCaseloadId.builder().caseload(API_CASELOAD_ID).username(USERNAME_GEN).build())).thenReturn(Optional.empty());
        when(staffUserAccountRepository.findById(USERNAME_GEN)).thenReturn(Optional.of(user));
        when(securityUtils.getCurrentUsername()).thenReturn("adminuser");

        userService.addAccessRole(USERNAME_GEN, ROLE_CODE);

        verify(telemetryClient).trackEvent(
                "PrisonUserRoleAddSuccess",
                Map.of("username", USERNAME_GEN, "role", ROLE_CODE, "admin", "adminuser"),
                null);
    }

    @Test
    public void testAddAccessRoleForApiCaseloadWithoutUserAccessibleCaseloadEntry() {
        final var role = Role.builder().code(ROLE_CODE).id(ROLE_ID).roleFunction("GENERAL").build();
        final var user = StaffUserAccount.builder().username(USERNAME_GEN).build();

        when(roleRepository.findByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userCaseloadRoleRepository.findById(UserCaseloadRoleIdentity.builder().caseload(API_CASELOAD_ID).username(USERNAME_GEN).roleId(ROLE_ID).build())).thenReturn(Optional.empty());
        final var userCaseloadId = UserCaseloadId.builder().caseload(API_CASELOAD_ID).username(USERNAME_GEN).build();
        when(userCaseloadRepository.findById(userCaseloadId)).thenReturn(Optional.of(UserCaseload.builder().id(userCaseloadId).build()));
        when(staffUserAccountRepository.findById(USERNAME_GEN)).thenReturn(Optional.of(user));
        when(securityUtils.getCurrentUsername()).thenReturn("adminuser");

        userService.addAccessRole(USERNAME_GEN, ROLE_CODE);

        assertThat(user.getRoles()).hasSize(1);

        assertThat(user.getRoles().get(0)).isEqualTo(UserCaseloadRole.builder()
            .id(UserCaseloadRoleIdentity.builder()
                .caseload(API_CASELOAD_ID)
                .roleId(ROLE_ID)
                .username(USERNAME_GEN)
                .build())
            .role(role)
            .build());

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testAddAccessRoleForCaseloadWithUserAccessibleCaseloadEntry() {
        final var role = Role.builder().code(ROLE_CODE).id(ROLE_ID).roleFunction("GENERAL").build();
        final var user = StaffUserAccount.builder().username(USERNAME_GEN).build();

        when(roleRepository.findByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userCaseloadRoleRepository.findById(UserCaseloadRoleIdentity.builder().caseload(LEEDS_CASELOAD_ID).username(USERNAME_GEN).roleId(ROLE_ID).build())).thenReturn(Optional.empty());
        final var userCaseloadId = UserCaseloadId.builder().caseload(LEEDS_CASELOAD_ID).username(USERNAME_GEN).build();
        when(userCaseloadRepository.findById(userCaseloadId)).thenReturn(Optional.of(UserCaseload.builder().id(userCaseloadId).build()));
        when(staffUserAccountRepository.findById(USERNAME_GEN)).thenReturn(Optional.of(user));
        when(securityUtils.getCurrentUsername()).thenReturn("adminuser");

        userService.addAccessRole(USERNAME_GEN, ROLE_CODE, LEEDS_CASELOAD_ID);

        verify(userCaseloadRepository, never()).save(isA(UserCaseload.class));

        assertThat(user.getRoles()).hasSize(1);

        assertThat(user.getRoles().get(0)).isEqualTo(UserCaseloadRole.builder()
            .id(UserCaseloadRoleIdentity.builder()
                .caseload(LEEDS_CASELOAD_ID)
                .roleId(ROLE_ID)
                .username(USERNAME_GEN)
                .build())
            .role(role)
            .build());
    }

    @Test
    public void testAddAccessRoleForCaseloadWithoutUserAccessibleCaseloadEntry() {
        final var role = Role.builder().code(ROLE_CODE).id(ROLE_ID).roleFunction("GENERAL").build();
        final var user = StaffUserAccount.builder().username(USERNAME_GEN).build();

        when(roleRepository.findByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userCaseloadRoleRepository.findById(UserCaseloadRoleIdentity.builder().caseload(LEEDS_CASELOAD_ID).username(USERNAME_GEN).roleId(ROLE_ID).build())).thenReturn(Optional.empty());
        final var userCaseloadId = UserCaseloadId.builder().caseload(LEEDS_CASELOAD_ID).username(USERNAME_GEN).build();
        when(userCaseloadRepository.findById(userCaseloadId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addAccessRole(USERNAME_GEN, ROLE_CODE, LEEDS_CASELOAD_ID)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testAddAccessAccessRoleWithoutCorrectPrivileges() {
        final var role = Role.builder().code(ROLE_CODE).id(ROLE_ID).roleFunction("ADMIN").build();
        when(roleRepository.findByCode(ROLE_CODE)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> userService.addAccessRole(USERNAME_GEN, ROLE_CODE, LEEDS_CASELOAD_ID)).isInstanceOf(AccessDeniedException.class);
        verify(securityUtils).isOverrideRole("MAINTAIN_ACCESS_ROLES_ADMIN");
    }

    @Test
    public void testRemoveAccessRole() {
        final var role = Role.builder().code(ROLE_CODE).id(ROLE_ID).roleFunction("GENERAL").build();

        when(roleRepository.findByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userCaseloadRoleRepository.findById(UserCaseloadRoleIdentity.builder().caseload(API_CASELOAD_ID).username(USERNAME_GEN).roleId(ROLE_ID).build())).thenReturn(Optional.of(UserCaseloadRole.builder().build()));
        when(securityUtils.getCurrentUsername()).thenReturn("adminuser");

        userService.removeUsersAccessRoleForCaseload(USERNAME_GEN, API_CASELOAD_ID, ROLE_CODE);

        verify(userCaseloadRoleRepository).deleteRole(USERNAME_GEN, API_CASELOAD_ID, ROLE_ID);
    }

    @Test
    public void testRemoveAccessRole_telemetryEvent() {
        final var role = Role.builder().code(ROLE_CODE).id(ROLE_ID).roleFunction("GENERAL").build();

        when(roleRepository.findByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userCaseloadRoleRepository.findById(UserCaseloadRoleIdentity.builder().caseload(API_CASELOAD_ID).username(USERNAME_GEN).roleId(ROLE_ID).build())).thenReturn(Optional.of(UserCaseloadRole.builder().build()));
        when(securityUtils.getCurrentUsername()).thenReturn("adminuser");

        userService.removeUsersAccessRoleForCaseload(USERNAME_GEN, API_CASELOAD_ID, ROLE_CODE);

        verify(telemetryClient).trackEvent(
                "PrisonUserRoleRemoveSuccess",
                Map.of("username", USERNAME_GEN, "role", ROLE_CODE, "admin", "adminuser"),
                null);
    }

    @Test
    public void testGetOffenderCategorisationsBatching() {
        final var setOf150Strings = IntStream.range(1, 150).mapToObj(String::valueOf)
                .collect(Collectors.toSet());

        final var detail2 = UserDetail.builder().staffId(-3L).lastName("B").build();
        final var detail1 = UserDetail.builder().staffId(-2L).lastName("C").build();

        when(userRepository.getUserListByUsernames(anyList())).thenReturn(ImmutableList.of(detail2, detail1));

        final var results = userService.getUserListByUsernames(setOf150Strings);

        assertThat(results).hasSize(4);

        verify(userRepository, times(2)).getUserListByUsernames(anyList());
    }
}
