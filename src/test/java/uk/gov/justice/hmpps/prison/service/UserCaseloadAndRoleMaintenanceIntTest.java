package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.util.WithMockNomisUser;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class UserCaseloadAndRoleMaintenanceIntTest {

    @Autowired
    private UserService userService;

    @Test
    @WithMockNomisUser(username = "ITAG_USER", roles = {"MAINTAIN_ACCESS_ROLES"})
    public void addUsersToNwebCaseload() {

        var numberAlloc = userService.addDefaultCaseloadForPrison("LEI");
        assertThat(numberAlloc.getNumUsersEnabled()).isEqualTo(2);

        numberAlloc = userService.addDefaultCaseloadForPrison("LEI");
        assertThat(numberAlloc.getNumUsersEnabled()).isEqualTo(0);
    }

}
