package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AccountDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Role;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRole;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRoleIdentity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")

@AutoConfigureTestDatabase(replace = NONE)
public class StaffUserAccountRepositoryTest {

    @Autowired
    private StaffUserAccountRepository repository;

    @Test
    public void getUserDetails() {
        var user = repository.findById("ITAG_USER");

        assertThat(user).get().usingRecursiveComparison()
            .ignoringFields("staff", "roles")
            .isEqualTo(staffUserAccountRepositoryResponse());
    }


    StaffUserAccount staffUserAccountRepositoryResponse() {
        return StaffUserAccount.builder()
                .username("ITAG_USER")
                .staff(Staff.builder()
                    .staffId(-2L)
                    .firstName("API")
                    .lastName("USER")
                        .status("ACTIVE")
                        .build())
                .type("GENERAL")
                .activeCaseLoadId("LEI")
                .roles(List.of(UserCaseloadRole.builder()
                                .id(UserCaseloadRoleIdentity.builder()
                                        .username("ITAG_USER").caseload("NWEB").roleId(-303L).build())
                                .role(Role.builder().id(-303L).code("MAINTAIN_ACCESS_ROLES").build())
                                .build(),
                        UserCaseloadRole.builder()
                                .id(UserCaseloadRoleIdentity.builder()
                                        .username("ITAG_USER").caseload("NWEB").roleId(-302L).build())
                                .role(Role.builder().id(-302L).code("MAINTAIN_ACCESS_ROLES_ADMIN").build())
                                .build(),
                        UserCaseloadRole.builder()
                                .id(UserCaseloadRoleIdentity.builder()
                                        .username("ITAG_USER").caseload("NWEB").roleId(-301L).build())
                                .role(Role.builder().id(-301L).code("ACCESS_ROLE_ADMIN").build())
                                .build(),
                        UserCaseloadRole.builder()
                                .id(UserCaseloadRoleIdentity.builder()
                                        .username("ITAG_USER").caseload("NWEB").roleId(-203L).build())
                                .role(Role.builder().id(-203L).code("OMIC_ADMIN").build())
                                .build(),
                        UserCaseloadRole.builder()
                                .id(UserCaseloadRoleIdentity.builder()
                                        .username("ITAG_USER").caseload("NWEB").roleId(-201L).build())
                                .role(Role.builder().id(-201L).code("KW_ADMIN").build())
                                .build()
                ))
                .accountDetail(AccountDetail.builder()
                        .username("ITAG_USER")
                        .accountStatus("OPEN")
                        .profile("TAG_GENERAL")
                        .passwordExpiry(LocalDateTime.parse("2021-10-12T21:22:23")).build())
                .build();
    }
}


