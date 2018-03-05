package net.syscon.elite.service;

import lombok.Getter;
import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.support.AgencyRequest;
import org.apache.commons.lang3.Validate;

public interface StaffService {
    StaffDetail getStaffDetail(Long staffId);

    Page<StaffDetail> getStaffByAgencyPositionRole(GetStaffRoleRequest request, PageRequest pageRequest);

    @Getter
    class GetStaffRoleRequest extends AgencyRequest {
        private final String position;
        private final String role;
        private final String nameFilter;

        public GetStaffRoleRequest(String agencyId, String role) {
            this(agencyId, null, role, null);
        }

        public GetStaffRoleRequest(String agencyId, String position, String role) {
            this(agencyId, position, role,null);
        }

        public GetStaffRoleRequest(String agencyId, String position, String role, String nameFilter) {
            super(agencyId);

            Validate.notBlank(role, "Role is required.");

            this.position = position;
            this.role = role;
            this.nameFilter = nameFilter;
        }
    }
}
