package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.StaffLocationRole;
import net.syscon.elite.api.resource.StaffResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.StaffService;
import net.syscon.elite.service.StaffService.GetStaffRoleRequest;

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
            String agencyId, String position, String role, String nameFilter,
            Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {

        GetStaffRoleRequest staffRoleRequest = new GetStaffRoleRequest(agencyId, position, role, nameFilter);
        PageRequest pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        Page<StaffLocationRole> staffDetails = staffService.getStaffByAgencyPositionRole(staffRoleRequest, pageRequest);

        return GetStaffByAgencyPositionRoleResponse.respond200WithApplicationJson(staffDetails);
    }

    @Override
    public GetStaffByAgencyRoleResponse getStaffByAgencyRole(
            String agencyId, String role, String nameFilter,
            Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {

        GetStaffRoleRequest staffRoleRequest = new GetStaffRoleRequest(agencyId, null, role, nameFilter);
        PageRequest pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        Page<StaffLocationRole> staffDetails = staffService.getStaffByAgencyPositionRole(staffRoleRequest, pageRequest);

        return GetStaffByAgencyRoleResponse.respond200WithApplicationJson(staffDetails);
    }
}
