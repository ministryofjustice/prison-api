package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.AccessRole;
import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.StaffService;
import net.syscon.elite.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Test cases for {@link BookingServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void init() {
        userService = new UserServiceImpl(caseLoadService, staffService, userRepository, securityUtils, API_CASELOAD_ID);
    }

    @Test
    public void testGetUsersByCaseload() {
        PageRequest pr = new PageRequest("lastName,firstName", Order.ASC, 0L, 10L);  //the default if non provided
        final NameFilter nameFilterDto = new NameFilter("A");
        userService.getUsersByCaseload(LEEDS_CASELOAD_ID, "A", ROLE_CODE, null);

        verify(userRepository, times(1)).findUsersByCaseload(eq(LEEDS_CASELOAD_ID), eq(ROLE_CODE), refEq(nameFilterDto), refEq(pr));
    }

    @Test
    public void testGetUsers() {
        PageRequest pr = new PageRequest("lastName,firstName", Order.ASC, 0L, 10L);  //the default if non provided
        final NameFilter nameFilterDto = new NameFilter("A");
        userService.getUsers("A", ROLE_CODE, null);

        verify(userRepository, times(1)).findUsers(eq(ROLE_CODE), refEq(nameFilterDto), refEq(pr));
    }

    @Test
    public void testGetUsersWithFullNameSearch() {
        PageRequest pr = new PageRequest("lastName,firstName", Order.ASC, 0L, 10L);  //the default if non provided
        final NameFilter nameFilterDto = new NameFilter("Brown James");
        userService.getUsers("Brown James", ROLE_CODE, null);

        verify(userRepository, times(1)).findUsers(eq(ROLE_CODE), refEq(nameFilterDto), refEq(pr));
    }

    @Test
    public void testGetUsersByCaseloadWithSortFieldDifferentToDefault() {
        PageRequest pr = new PageRequest("firstName", Order.ASC, 10L, 20L);
        final NameFilter nameFilterDto = new NameFilter("A");
        userService.getUsersByCaseload(LEEDS_CASELOAD_ID, "A", ROLE_CODE, pr);

        verify(userRepository, times(1)).findUsersByCaseload(eq(LEEDS_CASELOAD_ID), eq(ROLE_CODE), refEq(nameFilterDto), refEq(pr));
    }

    @Test
    public void testGetRolesByUserAndCaseload() {
        List<AccessRole> list = ImmutableList.of(AccessRole.builder().roleCode("TEST_CODE").roleName("Test Role").roleFunction("GENERAL").build());  //the default if non provided
        when(caseLoadService.getCaseLoad(Mockito.eq(LEEDS_CASELOAD_ID))).thenReturn(Optional.of(CaseLoad.builder().build()));
        when(userRepository.findAccessRolesByUsernameAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID, true)).thenReturn(list);

        userService.getAccessRolesByUserAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID, true);

        verify(userRepository, times(1)).findAccessRolesByUsernameAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID, true);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetRolesByUserAndCaseloadCaseloadDoesNotExist() {
        when(caseLoadService.getCaseLoad(Mockito.eq(LEEDS_CASELOAD_ID))).thenReturn(Optional.empty());
        userService.getAccessRolesByUserAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRolesByUserAndCaseloadUsernameNotProvided() {
        userService.getAccessRolesByUserAndCaseload("", LEEDS_CASELOAD_ID, false);
    }

    @Test
    public void testaddAccessRoleForApiCaseloadWithUserAccessibleCaseloadEntry() {
        AccessRole role = AccessRole.builder().roleId(ROLE_ID).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userRepository.isRoleAssigned(USERNAME_GEN, API_CASELOAD_ID, ROLE_ID)) .thenReturn(false);

        when(userRepository.isUserAssessibleCaseloadAvailable(API_CASELOAD_ID, USERNAME_GEN)).thenReturn(false);


        userService.addAccessRole(USERNAME_GEN, ROLE_CODE);

        verify(userRepository, times(1)).addUserAssessibleCaseload(API_CASELOAD_ID, USERNAME_GEN);
        verify(userRepository, times(1)).addRole(USERNAME_GEN, API_CASELOAD_ID, ROLE_ID);
    }

    @Test
    public void testaddAccessRoleForApiCaseloadWithoutUserAccessibleCaseloadEntry() {
        AccessRole role = AccessRole.builder().roleId(ROLE_ID).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userRepository.isRoleAssigned(USERNAME_GEN, API_CASELOAD_ID, ROLE_ID)) .thenReturn(false);

        when(userRepository.isUserAssessibleCaseloadAvailable(API_CASELOAD_ID, USERNAME_GEN)).thenReturn(true);

        userService.addAccessRole(USERNAME_GEN, ROLE_CODE);

        verify(userRepository, times(1)).addRole(USERNAME_GEN, API_CASELOAD_ID, ROLE_ID);
        verify(userRepository, times(0)).addUserAssessibleCaseload(API_CASELOAD_ID, USERNAME_GEN);
    }

    @Test
    public void testaddAccessRoleForCaseloadWithUserAccessibleCaseloadEntry() {
        AccessRole role = AccessRole.builder().roleId(ROLE_ID).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userRepository.isRoleAssigned(USERNAME_GEN, LEEDS_CASELOAD_ID, ROLE_ID)) .thenReturn(false);

        when(userRepository.isUserAssessibleCaseloadAvailable(LEEDS_CASELOAD_ID, USERNAME_GEN)).thenReturn(true);

        userService.addAccessRole(USERNAME_GEN, ROLE_CODE, LEEDS_CASELOAD_ID);

        verify(userRepository, times(0)).addUserAssessibleCaseload(API_CASELOAD_ID, USERNAME_GEN);
        verify(userRepository, times(1)).addRole(USERNAME_GEN, LEEDS_CASELOAD_ID, ROLE_ID);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testaddAccessRoleForCaseloadWithoutUserAccessibleCaseloadEntry() {
        AccessRole role = AccessRole.builder().roleId(ROLE_ID).roleFunction("GENERAL").build();
        when(userRepository.getRoleByCode(ROLE_CODE)).thenReturn(Optional.of(role));
        when(userRepository.isRoleAssigned(USERNAME_GEN, LEEDS_CASELOAD_ID, ROLE_ID)) .thenReturn(false);

        when(userRepository.isUserAssessibleCaseloadAvailable(LEEDS_CASELOAD_ID, USERNAME_GEN)).thenReturn(false);


        userService.addAccessRole(USERNAME_GEN, ROLE_CODE, LEEDS_CASELOAD_ID);
    }

    @Test(expected = AccessDeniedException.class)
    public void testaddAdminAccessRoleWithoutCorrectPriviledges() {
        AccessRole role = AccessRole.builder().roleId(ROLE_ID).roleFunction("ADMIN").build();
        when(userRepository.getRoleByCode(ROLE_CODE)).thenReturn(Optional.of(role));

        userService.addAccessRole(USERNAME_GEN, ROLE_CODE, LEEDS_CASELOAD_ID);
        verify(securityUtils.isOverrideRole("MAINTAIN_ACCESS_ROLES_ADMIN"),times(1));
    }

    private Page<UserDetail> pageResponse(int userCount) {
        List<UserDetail> users = new ArrayList<>();

        for (int i = 1; i <= userCount; i++) {
            users.add(UserDetail.builder().username(String.format("A%4dAA", i)).build());
        }

        return new Page<>(users, userCount, 0, 10);
    }

}
