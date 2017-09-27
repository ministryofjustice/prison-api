package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.AssignmentService;
import net.syscon.elite.service.AuthenticationService;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.service.UserService;
import net.syscon.elite.v2.api.model.*;
import net.syscon.elite.v2.api.resource.UserResource;
import net.syscon.elite.v2.api.support.Order;
import net.syscon.elite.v2.service.LocationService;
import net.syscon.util.MetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;

import javax.ws.rs.Path;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

@RestResource
@Path("/users")
public class UserResourceImpl implements UserResource {

    @Autowired
    private LocationService locationService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ReferenceDomainService referenceDomainService;

    @Autowired
    private UserService userService;

    @Value("${token.username.stored.caps:true}")
    private boolean upperCaseUsername;


    @Override
    public GetMyUserInformationResponse getMyUserInformation() {
        return GetMyUserInformationResponse.respond200WithApplicationJson(userService.getUserByUsername(UserSecurityUtils.getCurrentUsername()));
    }

    @Override
    public GetMyAssignmentsResponse getMyAssignments(Long pageOffset, Long pageLimit) {
        final List<OffenderBooking> assignments = assignmentService.findMyAssignments(nvl(pageOffset, 0L), nvl(pageLimit, 10L));
        return GetMyAssignmentsResponse.respond200WithApplicationJson(assignments, MetaDataFactory.getTotalRecords(assignments), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
    }

    @Override
    public GetMyCaseLoadsResponse getMyCaseLoads() {
        final List<CaseLoad> caseLoads = userService.getCaseLoads(UserSecurityUtils.getCurrentUsername());
        return GetMyCaseLoadsResponse.respond200WithApplicationJson(caseLoads);
    }

    @Override
    public GetMyCaseNoteTypesResponse getMyCaseNoteTypes(boolean includeSubTypes, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        final long offset = nvl(pageOffset, 0L);
        final long limit = nvl(pageLimit, 10L);
        List<ReferenceCode> caseNoteTypes = referenceDomainService.getCaseNoteTypeByCurrentCaseLoad(query, sortFields, sortOrder, offset, limit, includeSubTypes);

        if (includeSubTypes) {
            return GetMyCaseNoteTypesResponse.respond200WithApplicationJson(caseNoteTypes, (long)caseNoteTypes.size(),  0L, (long)caseNoteTypes.size());
        } else {
            return GetMyCaseNoteTypesResponse.respond200WithApplicationJson(caseNoteTypes, MetaDataFactory.getTotalRecords(caseNoteTypes), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
        }
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
        final UserDetail userByUsername = userService.getUserByUsername(upperCaseUsername ? username.toUpperCase() : username);
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
