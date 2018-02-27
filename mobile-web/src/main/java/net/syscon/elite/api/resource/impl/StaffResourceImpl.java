package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.StaffResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.StaffService;

import javax.ws.rs.Path;

@RestResource
@Path("staff")
public class StaffResourceImpl implements StaffResource {
    private final StaffService staffService;

    public StaffResourceImpl(StaffService staffService) {
        this.staffService = staffService;
    }

    @Override
    public GetStaffDetailResponse getStaffDetail(Long staffId) {
        return GetStaffDetailResponse.respond200WithApplicationJson(staffService.getStaffDetail(staffId));
    }

    @Override
    public GetStaffByAgencyPositionRoleResponse getStaffByAgencyPositionRole(
            String agencyId, String position, String role, String filterByName, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        return null;
    }
}
