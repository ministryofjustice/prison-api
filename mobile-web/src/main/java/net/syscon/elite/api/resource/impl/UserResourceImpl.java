package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.UserResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;

import javax.ws.rs.Path;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/users")
public class UserResourceImpl implements UserResource {
    private final LocationService locationService;
    private final AssignmentService assignmentService;
    private final AuthenticationService authenticationService;
    private final ReferenceDomainService referenceDomainService;
    private final UserService userService;

    @Value("${token.username.stored.caps:true}")
    private boolean upperCaseUsername;

    public UserResourceImpl(LocationService locationService, AssignmentService assignmentService, AuthenticationService authenticationService, ReferenceDomainService referenceDomainService, UserService userService) {
        this.locationService = locationService;
        this.assignmentService = assignmentService;
        this.authenticationService = authenticationService;
        this.referenceDomainService = referenceDomainService;
        this.userService = userService;
    }

    @Override
    public GetMyUserInformationResponse getMyUserInformation() {
        return GetMyUserInformationResponse.respond200WithApplicationJson(userService.getUserByUsername(UserSecurityUtils.getCurrentUsername()));
    }

    @Override
    public GetMyAssignmentsResponse getMyAssignments(Long pageOffset, Long pageLimit) {
        Page<OffenderBooking> assignments = assignmentService.findMyAssignments(
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return GetMyAssignmentsResponse.respond200WithApplicationJson(assignments);
    }

    @Override
    public GetMyCaseLoadsResponse getMyCaseLoads() {
        List<CaseLoad> caseLoads = userService.getCaseLoads(UserSecurityUtils.getCurrentUsername());

        return GetMyCaseLoadsResponse.respond200WithApplicationJson(caseLoads);
    }

    @Override
    public GetMyCaseNoteTypesResponse getMyCaseNoteTypes(boolean includeSubTypes, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<ReferenceCode> caseNoteTypes = referenceDomainService.getCaseNoteTypeByCurrentCaseLoad(
                query,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                includeSubTypes);

        return GetMyCaseNoteTypesResponse.respond200WithApplicationJson(caseNoteTypes);
    }

    @Override
    public GetMyLocationsResponse getMyLocations() {
        List<Location> userLocations = locationService.getUserLocations(UserSecurityUtils.getCurrentUsername());

        return GetMyLocationsResponse.respond200WithApplicationJson(userLocations);
    }

    @Override
    public UpdateMyActiveCaseLoadResponse updateMyActiveCaseLoad(CaseLoad caseLoad) {
        try {
            userService.setActiveCaseLoad(UserSecurityUtils.getCurrentUsername(), caseLoad.getCaseLoadId());
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
        UserDetail userByUsername = userService.getUserByUsername(upperCaseUsername ? username.toUpperCase() : username);

        return GetUserDetailsResponse.respond200WithApplicationJson(userByUsername);
    }

    @Override
    public LoginResponse login(AuthLogin authLogin, String authorization) {
        Token token = authenticationService.getAuthenticationToken(authorization, authLogin);

        if (token != null) {
            return LoginResponse.respond201WithApplicationJson(token, authorization);
        } else {
            return LoginResponse.respond401WithApplicationJson(ErrorResponse.builder().userMessage("Access Denied").build());
        }
    }

    @Override
    public TokenRefreshResponse tokenRefresh(String authorization) {
        Token token = authenticationService.refreshToken(authorization);

        if (token != null) {
            return TokenRefreshResponse.respond201WithApplicationJson(token, authorization);
        } else {
            return TokenRefreshResponse.respond401WithApplicationJson(ErrorResponse.builder()
                    .userMessage("Authentication Error")
                    .errorCode(401)
                    .build());
        }
    }
}
