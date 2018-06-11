package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.StaffUserRole;
import net.syscon.elite.service.StaffService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccessRoleMaintenanceIntTest {

    @Autowired
    private StaffService staffService;

    @Test
    @WithUserDetails("ITAG_USER")
    public void testGetAllRolesInCaseload() {
        List<StaffUserRole> allRolesInCaseload = staffService.getAllStaffRolesForCaseload("NWEB", "KW_ADMIN");
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
        List<StaffUserRole> roles = staffService.getRolesByCaseload(-2L, "NWEB");
        assertThat(roles).containsExactly(
                StaffUserRole.builder()
                        .roleId(-201L)
                        .roleCode("KW_ADMIN")
                        .roleName("Keyworker Admin")
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
        List<StaffUserRole> roles = staffService.getStaffRoles(-5L);
        assertThat(roles).containsExactly(
                StaffUserRole.builder()
                        .roleId(-101L)
                        .roleCode("LICENCE_RO")
                        .roleName("Responsible Officer")
                        .caseloadId("NWEB")
                        .username("RO_USER")
                        .staffId(-5L)
                        .build());
    }

    @Test
    @WithMockUser(username="ITAG_USER",roles={"MAINTAIN_ACCESS_ROLES"})
    public void addAndRemoveRoleFromStaffMember() {
        List<StaffUserRole> roles = staffService.getStaffRoles(-4L);
        assertThat(roles).hasSize(3);

        StaffUserRole addedRole = staffService.addStaffRole(-4L, "NWEB", "LICENCE_CA");
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
