package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.StaffLocationRole;
import net.syscon.elite.api.model.StaffUserRole;
import net.syscon.elite.api.resource.StaffResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.StaffService;
import net.syscon.elite.service.StaffService.GetStaffRoleRequest;

import javax.ws.rs.Path;
import java.util.List;

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
            String agencyId, String position, String role, String nameFilter, Long staffId,
            Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {

        GetStaffRoleRequest staffRoleRequest = new GetStaffRoleRequest(agencyId, position, role, nameFilter, staffId);
        PageRequest pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        Page<StaffLocationRole> staffDetails = staffService.getStaffByAgencyPositionRole(staffRoleRequest, pageRequest);

        return GetStaffByAgencyPositionRoleResponse.respond200WithApplicationJson(staffDetails);
    }

    @Override
    public GetStaffByAgencyRoleResponse getStaffByAgencyRole(
            String agencyId, String role, String nameFilter, Long staffId,
            Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {

        GetStaffRoleRequest staffRoleRequest = new GetStaffRoleRequest(agencyId, null, role, nameFilter, staffId);
        PageRequest pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        Page<StaffLocationRole> staffDetails = staffService.getStaffByAgencyPositionRole(staffRoleRequest, pageRequest);

        return GetStaffByAgencyRoleResponse.respond200WithApplicationJson(staffDetails);
    }

    @Override
    public GetStaffAccessRolesResponse getStaffAccessRoles(Long staffId) {
        List<StaffUserRole> staffUserRoleList = staffService.getStaffRoles(staffId);
        return GetStaffAccessRolesResponse.respond200WithApplicationJson(staffUserRoleList);
    }

    @Override
    public GetAccessRolesByCaseloadResponse getAccessRolesByCaseload(Long staffId, String caseload) {
        List<StaffUserRole> staffUserRoleList = staffService.getRolesByCaseload(staffId, caseload);
        return GetAccessRolesByCaseloadResponse.respond200WithApplicationJson(staffUserRoleList);
    }

    @Override
    public AddStaffAccessRoleResponse addStaffAccessRole(Long staffId, String caseload, String body) {
        StaffUserRole staffUserRole = staffService.addStaffRole(staffId, caseload, body);
        return AddStaffAccessRoleResponse.respond201WithApplicationJson(staffUserRole);
    }

    @Override
    public RemoveStaffAccessRoleResponse removeStaffAccessRole(Long staffId, String caseload, String roleCode) {
        staffService.removeStaffRole(staffId, caseload, roleCode);
        return RemoveStaffAccessRoleResponse.respond200WithApplicationJson(roleCode);
    }

    @Override
    public GetAllStaffAccessRolesForCaseloadResponse getAllStaffAccessRolesForCaseload(String caseload, String roleCode) {
        List<StaffUserRole> staffUserRoleList = staffService.getAllStaffRolesForCaseload(caseload, roleCode);
        return GetAllStaffAccessRolesForCaseloadResponse.respond200WithApplicationJson(staffUserRoleList);
    }
}
