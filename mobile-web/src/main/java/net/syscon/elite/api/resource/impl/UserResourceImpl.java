package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.UserResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.ws.rs.Path;
import java.util.List;
import java.util.Optional;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/users")
public class UserResourceImpl implements UserResource {
    private final AuthenticationFacade authenticationFacade;
    private final LocationService locationService;
    private final AssignmentService assignmentService;
    private final UserService userService;
    private final CaseLoadService caseLoadService;
    private final CaseNoteService caseNoteService;

    public UserResourceImpl(AuthenticationFacade authenticationFacade, LocationService locationService,
                            AssignmentService assignmentService,
                            UserService userService, CaseLoadService caseLoadService,
                            CaseNoteService caseNoteService) {
        this.authenticationFacade = authenticationFacade;
        this.locationService = locationService;
        this.assignmentService = assignmentService;
        this.userService = userService;
        this.caseLoadService = caseLoadService;
        this.caseNoteService = caseNoteService;
    }

    @Override
    public GetMyUserInformationResponse getMyUserInformation() {
        UserDetail user = userService.getUserByUsername(authenticationFacade.getCurrentUsername());

        return GetMyUserInformationResponse.respond200WithApplicationJson(user);
    }

    @Override
    public GetMyAssignmentsResponse getMyAssignments(Long pageOffset, Long pageLimit) {
        Page<OffenderBooking> assignments = assignmentService.findMyAssignments(
                authenticationFacade.getCurrentUsername(),
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return GetMyAssignmentsResponse.respond200WithApplicationJson(assignments);
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
        return GetStaffDetailResponse.respond200WithApplicationJson(userService.getUserByStaffId(staffId));
    }

    @Override
    public GetUserDetailsResponse getUserDetails(String username) {
        UserDetail userByUsername = userService.getUserByUsername(username.toUpperCase());

        return GetUserDetailsResponse.respond200WithApplicationJson(userByUsername);
    }


}
