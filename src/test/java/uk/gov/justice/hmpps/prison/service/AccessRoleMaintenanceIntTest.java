package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.api.model.StaffUserRole;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class AccessRoleMaintenanceIntTest {

    @Autowired
    private StaffService staffService;

    @Test
    public void testGetSpecificRoles() {
        final var roles = staffService.getRolesByCaseload(-2L, "NWEB");
        assertThat(roles).containsExactly(
            StaffUserRole.builder()
                .roleId(-301L)
                .roleCode("ACCESS_ROLE_ADMIN")
                .roleName("Access Role Admin")
                .caseloadId("NWEB")
                .username("ITAG_USER")
                .staffId(-2L)
                .build(),
            StaffUserRole.builder()
                .roleId(-201L)
                .roleCode("KW_ADMIN")
                .roleName("Keyworker Admin")
                .caseloadId("NWEB")
                .username("ITAG_USER")
                .staffId(-2L)
                .build(),
            StaffUserRole.builder()
                .roleId(-303L)
                .roleCode("MAINTAIN_ACCESS_ROLES")
                .roleName("Maintain access roles")
                .caseloadId("NWEB")
                .username("ITAG_USER")
                .staffId(-2L)
                .build(),
            StaffUserRole.builder()
                .roleId(-302L)
                .roleCode("MAINTAIN_ACCESS_ROLES_ADMIN")
                .roleName("Maintain access roles admin")
                .caseloadId("NWEB")
                .username("ITAG_USER")
                .staffId(-2L)
                .build(),
            StaffUserRole.builder()
                .roleId(-203L)
                .roleCode("OMIC_ADMIN")
                .roleName("Omic Admin")
                .caseloadId("NWEB")
                .username("ITAG_USER")
                .staffId(-2L)
                .build());
    }

    @Test
    public void testGetAllRolesForStaffMember() {
        final var roles = staffService.getStaffRoles(-5L);
        assertThat(roles).containsExactly(
            StaffUserRole.builder()
                .roleId(-101L)
                .roleCode("LICENCE_RO")
                .roleName("Responsible Officer")
                .caseloadId("NWEB")
                .username("RO_USER")
                .staffId(-5L)
                .build(),
            StaffUserRole.builder()
                .roleId(-316L)
                .roleCode("VIEW_PRISONER_DATA")
                .roleName("View Prisoner Data")
                .caseloadId("NWEB")
                .username("RO_USER")
                .staffId(-5L)
                .build()
        );
    }
}
