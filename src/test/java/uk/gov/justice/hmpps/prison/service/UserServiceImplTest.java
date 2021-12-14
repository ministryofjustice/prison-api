package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.api.support.Status;
import uk.gov.justice.hmpps.prison.repository.UserRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.UserCaseloadRoleRepository;
import uk.gov.justice.hmpps.prison.service.filters.NameFilter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link UserService}.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    private static final String API_CASELOAD_ID = "NWEB";
    private static final String ROLE_CODE = "A_ROLE";

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCaseloadRoleRepository userCaseloadRoleRepository;
    @Mock
    private UserCaseloadRepository userCaseloadRepository;
    @Mock
    private CaseLoadService caseLoadService;

    private UserService userService;

    @BeforeEach
    public void init() {
        userService = new UserService(caseLoadService, userRepository, userCaseloadRoleRepository, userCaseloadRepository, API_CASELOAD_ID, 100);
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
