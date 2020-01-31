package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.resource.StaffResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.StaffService;
import net.syscon.elite.service.StaffService.GetStaffRoleRequest;
import org.springframework.beans.factory.annotation.Value;

import javax.ws.rs.Path;
import java.util.List;

@RestResource
@Path("staff")
public class StaffResourceImpl implements StaffResource {
    private final String apiCaseloadId;
    private final StaffService staffService;

    public StaffResourceImpl(final StaffService staffService,
                             @Value("${application.caseload.id}") final String apiCaseloadId) {
        this.staffService = staffService;
        this.apiCaseloadId = apiCaseloadId;
    }

    @Override
    public GetStaffDetailResponse getStaffDetail(final Long staffId) {
        return GetStaffDetailResponse.respond200WithApplicationJson(staffService.getStaffDetail(staffId));
    }

    @Override
    public GetStaffEmailResponse getStaffEmailAddresses(final Long staffId) {
        return GetStaffEmailResponse.respond200WithApplicationJson(staffService.getStaffEmailAddresses(staffId));
    }

    @Override
    public List<CaseLoad> getStaffCaseloads(final Long staffId) {
        return staffService.getStaffCaseloads(staffId);
    }

    @Override
    public GetStaffByAgencyPositionRoleResponse getStaffByAgencyPositionRole(
            final String agencyId, final String position, final String role, final String nameFilter, final Long staffId, final Boolean activeOnly,
            final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {

        final var defaultedActiveOnly = activeOnly != null ? activeOnly : Boolean.TRUE;

        final var staffRoleRequest = new GetStaffRoleRequest(agencyId, position, role, nameFilter, defaultedActiveOnly, staffId);
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var staffDetails = staffService.getStaffByAgencyPositionRole(staffRoleRequest, pageRequest);

        return GetStaffByAgencyPositionRoleResponse.respond200WithApplicationJson(staffDetails);
    }

    @Override
    public GetStaffByAgencyRoleResponse getStaffByAgencyRole(
            final String agencyId, final String role, final String nameFilter, final Long staffId, final Boolean activeOnly,
            final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {

        final var defaultedActiveOnly = activeOnly != null ? activeOnly : Boolean.TRUE;

        final var staffRoleRequest = new GetStaffRoleRequest(agencyId, null, role, nameFilter, defaultedActiveOnly, staffId);
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var staffDetails = staffService.getStaffByAgencyPositionRole(staffRoleRequest, pageRequest);

        return GetStaffByAgencyRoleResponse.respond200WithApplicationJson(staffDetails);
    }

    @Override
    public GetStaffAccessRolesResponse getStaffAccessRoles(final Long staffId) {
        final var staffUserRoleList = staffService.getStaffRoles(staffId);
        return GetStaffAccessRolesResponse.respond200WithApplicationJson(staffUserRoleList);
    }

    @Override
    public GetAccessRolesByCaseloadResponse getAccessRolesByCaseload(final Long staffId, final String caseload) {
        final var staffUserRoleList = staffService.getRolesByCaseload(staffId, caseload);
        return GetAccessRolesByCaseloadResponse.respond200WithApplicationJson(staffUserRoleList);
    }

    @Override
    public GetAllRolesForAgencyResponse getAllRolesForAgency(final Long staffId, final String agencyId) {
        final var roles = staffService.getAllRolesForAgency(staffId, agencyId);
        return GetAllRolesForAgencyResponse.respond200WithApplicationJson(roles);
    }

    @Override
    @ProxyUser
    public AddStaffAccessRoleForApiCaseloadResponse addStaffAccessRoleForApiCaseload(final Long staffId, final String body) {
        final var staffUserRole = staffService.addStaffRole(staffId, apiCaseloadId, body);
        return AddStaffAccessRoleForApiCaseloadResponse.respond201WithApplicationJson(staffUserRole);
    }

    @Override
    @ProxyUser
    public AddStaffAccessRoleResponse addStaffAccessRole(final Long staffId, final String caseload, final String body) {
        final var staffUserRole = staffService.addStaffRole(staffId, caseload, body);
        return AddStaffAccessRoleResponse.respond201WithApplicationJson(staffUserRole);
    }

    @Override
    @ProxyUser
    public RemoveStaffAccessRoleResponse removeStaffAccessRole(final Long staffId, final String caseload, final String roleCode) {
        staffService.removeStaffRole(staffId, caseload, roleCode);
        return RemoveStaffAccessRoleResponse.respond200WithApplicationJson(roleCode);
    }

    @Override
    public GetAllStaffAccessRolesForCaseloadResponse getAllStaffAccessRolesForCaseload(final String caseload, final String roleCode) {
        final var staffUserRoleList = staffService.getAllStaffRolesForCaseload(caseload, roleCode);
        return GetAllStaffAccessRolesForCaseloadResponse.respond200WithApplicationJson(staffUserRoleList);
    }
}
