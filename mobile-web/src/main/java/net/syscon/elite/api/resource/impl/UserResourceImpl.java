package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.UserResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.*;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.Path;
import java.util.*;
import java.util.stream.Collectors;

@RestResource
@Path("/users")
public class UserResourceImpl implements UserResource {
    private final AuthenticationFacade authenticationFacade;
    private final UserService userService;
    private final InmateService inmateService;
    private final LocationService locationService;
    private final StaffService staffService;
    private final CaseLoadService caseLoadService;
    private final CaseNoteService caseNoteService;
    private final KeyWorkerAllocationService keyWorkerAllocationService;
    private final Environment env;

    public UserResourceImpl(AuthenticationFacade authenticationFacade,
                            LocationService locationService,
                            UserService userService,
                            StaffService staffService,
                            CaseLoadService caseLoadService,
                            CaseNoteService caseNoteService,
                            InmateService inmateService,
                            KeyWorkerAllocationService keyWorkerAllocationService,
                            Environment env) {
        this.authenticationFacade = authenticationFacade;
        this.locationService = locationService;
        this.userService = userService;
        this.staffService = staffService;
        this.caseLoadService = caseLoadService;
        this.caseNoteService = caseNoteService;
        this.inmateService = inmateService;
        this.keyWorkerAllocationService = keyWorkerAllocationService;
        this.env = env;
    }

    @Override
    public GetAllUsersHavingRoleAtCaseloadResponse getAllUsersHavingRoleAtCaseload(String caseload, String roleCode) {
        Set<String> users = userService.getAllUsernamesForCaseloadAndRole(caseload, roleCode);
        return GetAllUsersHavingRoleAtCaseloadResponse.respond200WithApplicationJson(new ArrayList<>(users));
    }

    @Override
    public GetUsersByCaseLoadResponse getUsersByCaseLoad(String caseload, String nameFilter, String accessRole, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {

        PageRequest pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        Page<UserDetail> userDetails = userService.getUsersByCaseload(caseload, nameFilter, accessRole, pageRequest);

        return GetUsersByCaseLoadResponse.respond200WithApplicationJson(userDetails);
    }

    @Override
    public RemoveUsersAccessRoleForCaseloadResponse removeUsersAccessRoleForCaseload(String username, String caseload, String roleCode) {
        userService.removeUsersAccessRoleForCaseload( username,  caseload,  roleCode);
        return RemoveUsersAccessRoleForCaseloadResponse.respond200WithApplicationJson();
    }

    @Override
    public AddAccessRoleResponse addAccessRole(String username, String roleCode) {
        boolean added = userService.addAccessRole(username, roleCode);
        return added? AddAccessRoleResponse.respond201WithApplicationJson() : AddAccessRoleResponse.respond200WithApplicationJson();
    }

    @Override
    public GetMyUserInformationResponse getMyUserInformation() {
        UserDetail user = userService.getUserByUsername(authenticationFacade.getCurrentUsername());

        return GetMyUserInformationResponse.respond200WithApplicationJson(user);
    }

    @Override
    public GetMyCaseLoadsResponse getMyCaseLoads(boolean allCaseloads) {
        List<CaseLoad> caseLoads = userService.getCaseLoads(authenticationFacade.getCurrentUsername(), allCaseloads);

        return GetMyCaseLoadsResponse.respond200WithApplicationJson(caseLoads);
    }

    @Override
    public GetMyCaseNoteTypesResponse getMyCaseNoteTypes(String sortFields, Order sortOrder) {
        Optional<CaseLoad> currentCaseLoad =
                caseLoadService.getWorkingCaseLoadForUser(authenticationFacade.getCurrentUsername());

        String caseLoadType = currentCaseLoad.isPresent() ? currentCaseLoad.get().getType() : "BOTH";

        List<ReferenceCode> caseNoteTypes = caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(caseLoadType);

        return GetMyCaseNoteTypesResponse.respond200WithApplicationJson(caseNoteTypes);
    }

    @Override
    public GetMyLocationsResponse getMyLocations() {
        List<Location> userLocations = locationService.getUserLocations(authenticationFacade.getCurrentUsername());

        return GetMyLocationsResponse.respond200WithApplicationJson(userLocations);
    }

    @Override
    public GetMyRolesResponse getMyRoles(boolean allRoles) {
        List<UserRole> rolesByUsername = userService.getRolesByUsername(authenticationFacade.getCurrentUsername(), allRoles);

        return GetMyRolesResponse.respond200WithApplicationJson(rolesByUsername);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write')")
    public UpdateMyActiveCaseLoadResponse updateMyActiveCaseLoad(CaseLoad caseLoad) {
        try {
            userService.setActiveCaseLoad(authenticationFacade.getCurrentUsername(), caseLoad.getCaseLoadId());
        } catch (final AccessDeniedException ex) {
            return UpdateMyActiveCaseLoadResponse.respond403WithApplicationJson(ErrorResponse.builder()
                    .userMessage("Not Authorized")
                    .developerMessage("The current user does not have acess to this CaseLoad")
                    .build());
        }

        return null;
    }

    @Override
    public GetStaffDetailResponse getStaffDetail(Long staffId) {
        return GetStaffDetailResponse.respond200WithApplicationJson(staffService.getStaffDetail(staffId));
    }

    @Override
    public GetUserDetailsResponse getUserDetails(String username) {
        UserDetail userByUsername = userService.getUserByUsername(username.toUpperCase());

        return GetUserDetailsResponse.respond200WithApplicationJson(userByUsername);
    }

    @Override
    public AddApiAccessForCaseloadResponse addApiAccessForCaseload(String caseload) {
        userService.addDefaultCaseloadForPrison(caseload);
        return AddApiAccessForCaseloadResponse.respond200WithApplicationJson();
    }

    @Override
    public GetMyAssignmentsResponse getMyAssignments(Long pageOffset, Long pageLimit) {
        boolean nomisProfile = Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.contains("nomis"));
        boolean iepLevel = false;
        List<Long> bookingIds = null;
        List<String> offenderNos = null;

        if (nomisProfile) {
            iepLevel = true;
            List<KeyWorkerAllocationDetail> allocations = keyWorkerAllocationService.getAllocationsForCurrentCaseload(authenticationFacade.getCurrentUsername());
            offenderNos = allocations.stream().map(KeyWorkerAllocationDetail::getOffenderNo).collect(Collectors.toList());
        } else {
            bookingIds = inmateService.getPersonalOfficerBookings(authenticationFacade.getCurrentUsername());
        }

        final PageRequest pageRequest = new PageRequest(null, Order.ASC, pageOffset, pageLimit);
        Page<OffenderBooking> assignments = new Page<>(Collections.emptyList(), 0, pageRequest.getOffset(), pageRequest.getLimit());

        if (!(CollectionUtils.isEmpty(bookingIds) && CollectionUtils.isEmpty(offenderNos))) {
            assignments = inmateService.findAllInmates(
                    InmateSearchCriteria.builder()
                            .username(authenticationFacade.getCurrentUsername())
                            .iepLevel(iepLevel)
                            .offenderNos(offenderNos)
                            .bookingIds(bookingIds)
                            .pageRequest(pageRequest)
                            .build());
        }
        return GetMyAssignmentsResponse.respond200WithApplicationJson(assignments);
    }

}
