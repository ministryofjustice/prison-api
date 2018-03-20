package net.syscon.elite.service;

import lombok.Getter;
import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.StaffLocationRole;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.support.AgencyRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public interface StaffService {
    String STAFF_STATUS_ACTIVE = "ACTIVE";

    static boolean isStaffActive(StaffDetail staffDetail) {
        Validate.notNull(staffDetail);

        return StringUtils.equals(STAFF_STATUS_ACTIVE, staffDetail.getStatus());
    }

    StaffDetail getStaffDetail(Long staffId);

    Page<StaffLocationRole> getStaffByAgencyPositionRole(GetStaffRoleRequest request, PageRequest pageRequest);

    StaffDetail getStaffDetailByPersonnelIdentifier(String idType, String id);

    @Getter
    class GetStaffRoleRequest extends AgencyRequest {
        private final String position;
        private final String role;
        private final String nameFilter;
        private final Long staffId;

        public GetStaffRoleRequest(String agencyId, String role) {
            this(agencyId, null, role, null, null);
        }

        public GetStaffRoleRequest(String agencyId, String position, String role) {
            this(agencyId, position, role, null, null);
        }

        public GetStaffRoleRequest(String agencyId, String position, String role, String nameFilter, Long staffId) {
            super(agencyId);

            Validate.notBlank(role, "Role is required.");

            this.position = position;
            this.role = role;
            this.nameFilter = nameFilter;
            this.staffId = staffId;
        }
    }
}
