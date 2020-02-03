package net.syscon.elite.service;

import net.syscon.elite.repository.jpa.model.*;
import net.syscon.elite.repository.jpa.repository.StaffUserAccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

        authService.getNomisUserByUsername("ITAG_USER");

        verify(repository).findById("ITAG_USER");
    }


    private Optional<StaffUserAccount> buildStaffUserAccountRepositoryResponse() {
        return Optional.ofNullable(StaffUserAccount.builder()
                .staff(new Staff())
                .roles(List.of(UserCaseloadRole.builder()
                        .role(Role.builder().code("Role1").build())
                        .build()
                ))
                .accountDetail(new AccountDetail())
                .build());
    }
}
