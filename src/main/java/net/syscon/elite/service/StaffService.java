package net.syscon.elite.service;

import lombok.Getter;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.support.AgencyRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface StaffService {
    String STAFF_STATUS_ACTIVE = "ACTIVE";

    static boolean isStaffActive(final StaffDetail staffDetail) {
        Validate.notNull(staffDetail);

        return StringUtils.equals(STAFF_STATUS_ACTIVE, staffDetail.getStatus());
    }

    StaffDetail getStaffDetail(@NotNull Long staffId);

    List<String> getStaffEmailAddresses(@NotNull Long staffId);

    Page<StaffLocationRole> getStaffByAgencyPositionRole(GetStaffRoleRequest request, PageRequest pageRequest);

    StaffDetail getStaffDetailByPersonnelIdentifier(String idType, String id);

    List<StaffUserRole> getStaffRoles(Long staffId);

    List<StaffUserRole> getRolesByCaseload(Long staffId, String caseload);

    List<StaffUserRole> getAllStaffRolesForCaseload(String caseload, String roleCode);

    StaffUserRole addStaffRole(Long staffId, String caseload, String roleCode);

    void removeStaffRole(Long staffId, String caseload, String roleCode);

    List<StaffRole> getAllRolesForAgency(Long staffId, String agencyId);

    List<CaseLoad> getStaffCaseloads(@NotNull Long staffId);

    @Getter
    class GetStaffRoleRequest extends AgencyRequest {
        private final String position;
        private final String role;
        private final String nameFilter;
        private final Boolean activeOnly;
        private final Long staffId;

        public GetStaffRoleRequest(final String agencyId, final String role, final Boolean activeOnly) {
            this(agencyId, null, role, null, activeOnly, null);
        }

        public GetStaffRoleRequest(final String agencyId, final String position, final String role, final Boolean activeOnly) {
            this(agencyId, position, role, null, activeOnly, null);
        }

        public GetStaffRoleRequest(final String agencyId, final String position, final String role, final String nameFilter, final Boolean activeOnly, final Long staffId) {
            super(agencyId);
            this.activeOnly = activeOnly;

            Validate.notBlank(role, "Role is required.");

            this.position = position;
            this.role = role;
            this.nameFilter = nameFilter;
            this.staffId = staffId;
        }
    }
}
