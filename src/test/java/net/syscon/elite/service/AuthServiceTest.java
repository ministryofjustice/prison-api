package net.syscon.elite.service;

import net.syscon.elite.repository.jpa.model.*;
import net.syscon.elite.repository.jpa.repository.StaffUserAccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthServiceTest {

    @Mock
    private StaffUserAccountRepository repository;

    private AuthService authService;

    @Before
    public void setUp() {
        authService = new AuthService(repository);
    }

    @Test
    public void getUserPersonDetails() {
        when(repository.findById(anyString())).thenReturn(buildStaffUserAccountRepositoryResponse());

        authService.getNomisUserByUsername("BOB");

        verify(repository).findById("BOB");
    }


    Optional<StaffUserAccount> buildStaffUserAccountRepositoryResponse() {
        return Optional.ofNullable(StaffUserAccount.builder()
                .username("BOB")
                .staff(Staff.builder()
                        .staffId(1234567L)
                        .firstName("BOB")
                        .lastName("SMITH")
                        .status("ACTIVE")
                        .build())
                .type("SPECIAL")
                .activeCaseLoadId("SOME")
                .roles(List.of(UserCaseloadRole.builder()
                                .id(UserCaseloadRoleIdentity.builder()
                                        .username("BOB").caseload("WEB").roleId(1L).build())
                                .role(Role.builder().id(1L).code("Role1").build())
                                .build(),
                        UserCaseloadRole.builder()
                                .id(UserCaseloadRoleIdentity.builder()
                                        .username("BOB").caseload("WEB").roleId(2L).build())
                                .role(Role.builder().id(2L).code("Role2").build())
                                .build()
                ))
                .accountDetail(AccountDetail.builder()
                        .username("BOB")
                        .accountStatus("OPEN")
                        .profile("TAG")
                        .passwordExpiry(LocalDateTime.of(2021, 2, 1, 10, 11, 12)).build())
                .build());
    }
}
