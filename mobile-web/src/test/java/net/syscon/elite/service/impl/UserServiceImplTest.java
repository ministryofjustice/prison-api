package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.StaffService;
import net.syscon.elite.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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
    @Mock
    private UserRepository userRepository;

    @Mock
    private StaffService staffService;

    @Mock
    private CaseLoadService caseLoadService;

    private UserService userService;

    @Before
    public void init() {
        userService = new UserServiceImpl(caseLoadService, staffService, userRepository, null);
    }

    @Test
    public void testGetUsersByCaseload() {
        PageRequest pr = new PageRequest("lastName", Order.ASC, 0L, 10L);  //the default if non provided
        when(userRepository.findUsersByCaseload(LEEDS_CASELOAD_ID, "A_ROLE", "A", null)).thenReturn(pageResponse(2));

        userService.getUsersByCaseload(LEEDS_CASELOAD_ID, "A", "A_ROLE", null);

        verify(userRepository, times(1)).findUsersByCaseload(eq(LEEDS_CASELOAD_ID), eq("A_ROLE"), eq("A"), refEq(pr));
    }

    @Test
    public void testGetUsersByCaseloadWithSortFieldDifferentToDefault() {
        PageRequest pr = new PageRequest("firstName", Order.ASC, 10L, 20L);
        when(userRepository.findUsersByCaseload(LEEDS_CASELOAD_ID, "A_ROLE", "A", pr)).thenReturn(pageResponse(2));

        userService.getUsersByCaseload(LEEDS_CASELOAD_ID, "A", "A_ROLE", pr);

        verify(userRepository, times(1)).findUsersByCaseload(eq(LEEDS_CASELOAD_ID), eq("A_ROLE"), eq("A"), eq(pr));
    }

    @Test
    public void testGetRolesByUserAndCaseload() {
        List<UserRole> list = ImmutableList.of(UserRole.builder().roleCode("TEST_CODE").roleName("Test Role").caseloadId(LEEDS_CASELOAD_ID).roleId(123L).build());  //the default if non provided
        when(caseLoadService.getCaseLoad(Mockito.eq(LEEDS_CASELOAD_ID))).thenReturn(Optional.of(CaseLoad.builder().build()));
        when(userRepository.findAccessRolesByUsernameAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID)).thenReturn(list);

        userService.getAccessRolesByUserAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID);

        verify(userRepository, times(1)).findAccessRolesByUsernameAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetRolesByUserAndCaseloadCaseloadDoesNotExist() {
        when(caseLoadService.getCaseLoad(Mockito.eq(LEEDS_CASELOAD_ID))).thenReturn(Optional.empty());
        userService.getAccessRolesByUserAndCaseload(USERNAME_GEN, LEEDS_CASELOAD_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRolesByUserAndCaseloadUsernameNotProvided() {
        userService.getAccessRolesByUserAndCaseload("", LEEDS_CASELOAD_ID);
    }

    private Page<UserDetail> pageResponse(int userCount) {
        List<UserDetail> users = new ArrayList<>();

        for (int i = 1; i <= userCount; i++) {
            users.add(UserDetail.builder().username(String.format("A%4dAA", i)).build());
        }

        return new Page<>(users, userCount, 0, 10);
    }

}
