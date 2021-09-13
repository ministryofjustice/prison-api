package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AccountDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Role;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRole;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRoleIdentity;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private StaffUserAccountRepository repository;

    private AuthService authService;

    @BeforeEach
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
                        .id(UserCaseloadRoleIdentity.builder().caseload("NWEB").build())
                        .role(Role.builder().code("Role1").build())
                        .build()
                ))
                .accountDetail(new AccountDetail())
                .build());
    }
}
