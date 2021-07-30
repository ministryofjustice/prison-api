package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.api.model.UserRole;

import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class UserCaseloadAndRoleMaintenanceIntTest {

    @Autowired
    private UserService userService;

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"MAINTAIN_ACCESS_ROLES"})
    public void addAndRemoveRoleFromStaffMember() {
        final var roles = userService.getRolesByUsername("ITAG_USER", true).stream()
            .map(UserRole::getRoleCode).collect(Collectors.toList());
        assertThat(roles).hasSizeGreaterThan(5);
        assertThat(roles).contains("NWEB_MAINTAIN_ACCESS_ROLES").doesNotContain("NWEB_LICENCE_CA");

        final var added = userService.addAccessRole("ITAG_USER", "LICENCE_CA");
        assertThat(added).isTrue();

        final var rolesAfterAdd = userService.getRolesByUsername("ITAG_USER", true).stream()
            .map(UserRole::getRoleCode).collect(Collectors.toList());
        assertThat(rolesAfterAdd).contains("NWEB_MAINTAIN_ACCESS_ROLES").contains("NWEB_LICENCE_CA");

        userService.removeUsersAccessRoleForCaseload("ITAG_USER", "NWEB", "LICENCE_CA");

        final var rolesAfterRemove = userService.getRolesByUsername("ITAG_USER", true).stream()
            .map(UserRole::getRoleCode).collect(Collectors.toList());

        assertThat(rolesAfterRemove).isEqualTo(roles);
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"MAINTAIN_ACCESS_ROLES"})
    public void addUsersToNwebCaseload() {

        var numberAlloc = userService.addDefaultCaseloadForPrison("LEI");
        assertThat(numberAlloc.getNumUsersEnabled()).isEqualTo(2);

        numberAlloc = userService.addDefaultCaseloadForPrison("LEI");
        assertThat(numberAlloc.getNumUsersEnabled()).isEqualTo(0);
    }

}
