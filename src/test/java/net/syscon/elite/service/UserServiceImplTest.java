package net.syscon.elite.service;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.AccessRole;
import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.filters.NameFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link UserService}.
 */
@ExtendWith(SpringExtension.class)
public class UserServiceImplTest {
    private static final String USERNAME_GEN = "HH_GEN";
    private static final String LEEDS_CASELOAD_ID = "LEI";
    private static final String API_CASELOAD_ID = "NWEB";
    private static final String ROLE_CODE = "A_ROLE";
    private static final long ROLE_ID = 1L;
    @Mock
    private UserRepository userRepository;

    @Mock
    private StaffService staffService;

    @Mock
    private CaseLoadService caseLoadService;

    @Mock
    private AuthenticationFacade securityUtils;

    private UserService userService;

    @BeforeEach
    public void init() {
        userService = new UserService(caseLoadService, staffService, userRepository, securityUtils, API_CASELOAD_ID, 100);
    }

    @Test
    public void testGetUsersByCaseload() {
        final var pr = new PageRequest("lastName,firstName", Order.ASC, 0L, 10L);  //the default if non provided
        final var nameFilterDto = new NameFilter("A");
        userService.getUsersByCaseload(LEEDS_CASELOAD_ID, "A", ROLE_CODE, null);

        verify(userRepository, times(1)).findUsersByCaseload(eq(LEEDS_CASELOAD_ID), eq(ROLE_CODE), refEq(nameFilterDto), refEq(pr));
    }

    @Test
    public void testGetUsers() {
        final var pr = new PageRequest("lastName,firstName", Order.ASC, 0L, 10L);  //the default if non provided
        final var nameFilterDto = new NameFilter("A");
        userService.getUsers("A", ROLE_CODE, null);

        verify(userRepository, times(1)).findUsers(eq(ROLE_CODE), refEq(nameFilterDto), refEq(pr));
    }

    @Test
    public void testGetUsersWithFullNameSearch() {
        final var pr = new PageRequest("lastName,firstName", Order.ASC, 0L, 10L);  //the default if non provided
        final var nameFilterDto = new NameFilter("Brown James");
        userService.getUsers("Brown James", ROLE_CODE, null);

        verify(userRepository, times(1)).findUsers(eq(ROLE_CODE), refEq(nameFilterDto), refEq(pr));
    }

    @Test
    public void testGetUsersByCaseloadWithSortFieldDifferentToDefault() {
        final var pr = new PageRequest("firstName", Order.ASC, 10L, 20L);
        final var nameFilterDto = new NameFilter("A");
        userService.getUsersByCaseload(LEEDS_CASELOAD_ID, "A", ROLE_CODE, pr);

        verify(userRepository, times(1)).findUsersByCaseload(eq(LEEDS_CASELOAD_ID), eq(ROLE_CODE), refEq(nameFilterDto), refEq(pr));
    }

    @Test
    public void testGetRolesByUserAndCaseload() {
        final List<AccessRole> list = ImmutableList.of(AccessRole.builder().roleCode("TEST_CODE").roleName("Test Role").roleFunction("GENERAL").build());  //the default if non provided
        when(caseLoadService.getCaseLoad(Mockito.eq(LEEDS_CASELOAD_ID))).thenReturn(Optional.of(CaseLoad.builder().build()));
        when(userRepository.findAccessRolesByUsernameAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID, true)).thenReturn(list);

        userService.getAccessRolesByUserAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID, true);

        verify(userRepository, times(1)).findAccessRolesByUsernameAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID, true);
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
    public void testaddAccessRoleForApiCaseloadWithUserAccessibleCaseloadEntry() {
        final var role = AccessRole.builder().roleId(ROLE_ID).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userRepository.isRoleAssigned(USERNAME_GEN, API_CASELOAD_ID, ROLE_ID)).thenReturn(false);

        when(userRepository.isUserAssessibleCaseloadAvailable(API_CASELOAD_ID, USERNAME_GEN)).thenReturn(false);


        userService.addAccessRole(USERNAME_GEN, ROLE_CODE);

        verify(userRepository, times(1)).addUserAssessibleCaseload(API_CASELOAD_ID, USERNAME_GEN);
        verify(userRepository, times(1)).addRole(USERNAME_GEN, API_CASELOAD_ID, ROLE_ID);
    }

    @Test
    public void testaddAccessRoleForApiCaseloadWithoutUserAccessibleCaseloadEntry() {
        final var role = AccessRole.builder().roleId(ROLE_ID).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userRepository.isRoleAssigned(USERNAME_GEN, API_CASELOAD_ID, ROLE_ID)).thenReturn(false);

        when(userRepository.isUserAssessibleCaseloadAvailable(API_CASELOAD_ID, USERNAME_GEN)).thenReturn(true);

        userService.addAccessRole(USERNAME_GEN, ROLE_CODE);

        verify(userRepository, times(1)).addRole(USERNAME_GEN, API_CASELOAD_ID, ROLE_ID);
        verify(userRepository, times(0)).addUserAssessibleCaseload(API_CASELOAD_ID, USERNAME_GEN);
    }

    @Test
    public void testaddAccessRoleForCaseloadWithUserAccessibleCaseloadEntry() {
        final var role = AccessRole.builder().roleId(ROLE_ID).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userRepository.isRoleAssigned(USERNAME_GEN, LEEDS_CASELOAD_ID, ROLE_ID)).thenReturn(false);

        when(userRepository.isUserAssessibleCaseloadAvailable(LEEDS_CASELOAD_ID, USERNAME_GEN)).thenReturn(true);

        userService.addAccessRole(USERNAME_GEN, ROLE_CODE, LEEDS_CASELOAD_ID);

        verify(userRepository, times(0)).addUserAssessibleCaseload(API_CASELOAD_ID, USERNAME_GEN);
        verify(userRepository, times(1)).addRole(USERNAME_GEN, LEEDS_CASELOAD_ID, ROLE_ID);
    }

    @Test
    public void testaddAccessRoleForCaseloadWithoutUserAccessibleCaseloadEntry() {
        final var role = AccessRole.builder().roleId(ROLE_ID).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userRepository.isRoleAssigned(USERNAME_GEN, LEEDS_CASELOAD_ID, ROLE_ID)).thenReturn(false);

        when(userRepository.isUserAssessibleCaseloadAvailable(LEEDS_CASELOAD_ID, USERNAME_GEN)).thenReturn(false);


        assertThatThrownBy(() -> userService.addAccessRole(USERNAME_GEN, ROLE_CODE, LEEDS_CASELOAD_ID)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testaddAdminAccessRoleWithoutCorrectPriviledges() {
        final var role = AccessRole.builder().roleId(ROLE_ID).roleFunction("ADMIN").build();
        when(userRepository.getRoleByCode(ROLE_CODE)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> userService.addAccessRole(USERNAME_GEN, ROLE_CODE, LEEDS_CASELOAD_ID)).isInstanceOf(AccessDeniedException.class);
        verify(securityUtils).isOverrideRole("MAINTAIN_ACCESS_ROLES_ADMIN");
    }

    @Test
    public void testGetOffenderCategorisationsBatching() {

        final var setOf150Strings = Stream.iterate("1", n -> String.valueOf(Integer.parseInt(n) + 1))
                .limit(150)
                .collect(Collectors.toSet());

        final var detail2 = UserDetail.builder().staffId(-3L).lastName("B").build();
        final var detail1 = UserDetail.builder().staffId(-2L).lastName("C").build();

        when(userRepository.getUserListByUsernames(anyList())).thenReturn(ImmutableList.of(detail2, detail1));

        final var results = userService.getUserListByUsernames(setOf150Strings);

        assertThat(results).hasSize(4);

        verify(userRepository, times(2)).getUserListByUsernames(anyList());
    }

    private Page<UserDetail> pageResponse(final int userCount) {
        final List<UserDetail> users = new ArrayList<>();

        for (var i = 1; i <= userCount; i++) {
            users.add(UserDetail.builder().username(String.format("A%4dAA", i)).build());
        }

        return new Page<>(users, userCount, 0, 10);
    }

}
