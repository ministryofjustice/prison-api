package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
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
    @WithUserDetails("ITAG_USER")
    public void testGetAllRolesInCaseload() {
        final var allRolesInCaseload = staffService.getAllStaffRolesForCaseload("NWEB", "KW_ADMIN");
        assertThat(allRolesInCaseload).containsExactly(
                StaffUserRole.builder()
                        .roleId(-201L)
                        .roleCode("KW_ADMIN")
                        .roleName("Keyworker Admin")
                        .caseloadId("NWEB")
                        .username("API_TEST_USER")
                        .staffId(-4L)
                        .build(),
                StaffUserRole.builder()
                        .roleId(-201L)
                        .roleCode("KW_ADMIN")
                        .roleName("Keyworker Admin")
                        .caseloadId("NWEB")
                        .username("ITAG_USER")
                        .staffId(-2L)
                        .build());
    }

    @Test
    @WithUserDetails("ITAG_USER")
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
    @WithUserDetails("API_TEST_USER")
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
                        .roleId(-304L)
                        .roleCode("VIEW_PRISONER_DATA")
                        .roleName("View Prisoner Data")
                        .caseloadId("NWEB")
                        .username("RO_USER")
                        .staffId(-5L)
                        .build()
        );
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"MAINTAIN_ACCESS_ROLES"})
    public void addAndRemoveRoleFromStaffMember() {
        var roles = staffService.getStaffRoles(-4L);
        assertThat(roles).hasSize(3);

        final var addedRole = staffService.addStaffRole(-4L, "NWEB", "LICENCE_CA");
        roles = staffService.getStaffRoles(-4L);
        assertThat(roles).hasSize(4);

        staffService.removeStaffRole(-4L, "NWEB", "LICENCE_CA");
        assertThat(addedRole).isEqualToComparingFieldByField(
                StaffUserRole.builder()
                        .roleId(-100L)
                        .roleCode("LICENCE_CA")
                        .roleName("Case Admin")
                        .caseloadId("NWEB")
                        .username("API_TEST_USER")
                        .staffId(-4L)
                        .build());

        roles = staffService.getStaffRoles(-4L);
        assertThat(roles).hasSize(3);
    }

}
