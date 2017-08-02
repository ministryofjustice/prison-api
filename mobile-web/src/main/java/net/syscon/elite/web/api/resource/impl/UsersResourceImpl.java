package net.syscon.elite.web.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.AssignmentService;
import net.syscon.elite.service.AuthenticationService;
import net.syscon.elite.service.ReferenceDomainService;
import net.syscon.elite.service.UserService;
import net.syscon.elite.web.api.model.*;
import net.syscon.elite.web.api.resource.UsersResource;
import net.syscon.util.MetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;

import javax.ws.rs.Path;
import java.util.List;

@RestResource
@Path("/users")
public class UsersResourceImpl implements UsersResource {

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
	public GetUsersByUsernameResponse getUsersByUsername(String username) throws Exception {
        UserDetails userDetails = userService.getUserByUsername(upperCaseUsername ? username.toUpperCase() : username);
		return GetUsersByUsernameResponse.withJsonOK(userDetails);
	}

    @Override
    public GetUsersStaffByStaffIdResponse getUsersStaffByStaffId(String staffId) {
        StaffDetails staffDetails = userService.getUserByStaffId(Long.valueOf(staffId));
		return GetUsersStaffByStaffIdResponse.withJsonOK(staffDetails);
    }

    @Override
	public GetUsersMeResponse getUsersMe() throws Exception {
		final UserDetails user = getCurrentUser();
		return GetUsersMeResponse.withJsonOK(user);
	}

	@Override
	public GetUsersMeBookingAssignmentsResponse getUsersMeBookingAssignments(int offset, int limit) throws Exception {
		final List<InmateAssignmentSummary> assignments = assignmentService.findMyAssignments(offset, limit);
		final InmateAssignmentSummaries assignmentSummaries = new InmateAssignmentSummaries(assignments, MetaDataFactory.createMetaData(limit, offset, assignments));
		return GetUsersMeBookingAssignmentsResponse.withJsonOK(assignmentSummaries);
	}

	@Override
	public PostUsersLoginResponse postUsersLogin(final String credentials, final AuthLogin authLogin) throws Exception {
		Token token = authenticationService.getAuthenticationToken(credentials, authLogin);

		if (token != null) {
			return PostUsersLoginResponse.withJsonCreated(token.getToken(), token);
		} else {
			String message = "Authentication Error";
			HttpStatus httpStatus = new HttpStatus("401", "401", message, message, "");

			return PostUsersLoginResponse.withJsonUnauthorized(httpStatus);
		}
	}

	@Override
	public PostUsersTokenResponse postUsersToken(final String header) throws Exception {
		Token token = authenticationService.refreshToken(header);

		if (token != null) {
			return PostUsersTokenResponse.withJsonCreated(token.getToken(), token);
		} else {
			String message = "Authentication Error";
			HttpStatus httpStatus = new HttpStatus("401", "401", message, message, "");

			return PostUsersTokenResponse.withJsonUnauthorized(httpStatus);
		}
	}

	@Override
	public GetUsersMeCaseLoadsResponse getUsersMeCaseLoads(final int offset, final int limit) throws Exception {
		final List<CaseLoad> caseLoads = userService.getCaseLoads(UserSecurityUtils.getCurrentUsername());
		return GetUsersMeCaseLoadsResponse.withJsonOK(caseLoads);
	}

	private UserDetails getCurrentUser() {
		return userService.getUserByUsername(UserSecurityUtils.getCurrentUsername());
	}

	@Override
	public PutUsersMeActiveCaseLoadResponse putUsersMeActiveCaseLoad(final CaseLoad entity) throws Exception {
		try {
			userService.setActiveCaseLoad(UserSecurityUtils.getCurrentUsername(), entity.getCaseLoadId());
			return PutUsersMeActiveCaseLoadResponse.withOK();
		} catch (final AccessDeniedException ex) {
			final HttpStatus httpStatus = new HttpStatus("403",  "403", "Not Authorized", "The current user does not have acess to this CaseLoad", "");
			return PutUsersMeActiveCaseLoadResponse.withJsonUnauthorized(httpStatus);
		}
	}

	@Override
	public GetUsersMeActiveCaseLoadResponse getUsersMeActiveCaseLoad() throws Exception {
		try {
			final CaseLoad caseLoad = userService.getActiveCaseLoad(UserSecurityUtils.getCurrentUsername());
			return GetUsersMeActiveCaseLoadResponse.withJsonOK(caseLoad);
		} catch (final DataAccessException ex) {
			final HttpStatus httpStatus = new HttpStatus("500",  "500", "Internal Error", "Internal Error", "");
			return GetUsersMeActiveCaseLoadResponse.withJsonBadRequest(httpStatus);
		}
	}

	@Override
	public GetUsersMeCaseNoteTypesResponse getUsersMeCaseNoteTypes(String query, String orderBy, Order order, int offset, int limit) throws Exception {
		List<CaseNoteType> caseNoteTypes = referenceDomainService.getCaseNoteTypeByCurrentCaseLoad(query, orderBy, order.toString(), offset, limit);
		CaseNoteTypes codes = new CaseNoteTypes(caseNoteTypes, MetaDataFactory.createMetaData(limit, offset, caseNoteTypes));
		return GetUsersMeCaseNoteTypesResponse.withJsonOK(codes);
	}

	@Override
	public GetUsersMeCaseNoteTypesByTypeCodeResponse getUsersMeCaseNoteTypesByTypeCode(String typeCode, String query,
			String orderBy, Order order, int offset, int limit) throws Exception {
		List<CaseNoteType> caseNoteTypes = referenceDomainService.getCaseNoteSubType(typeCode, query, orderBy, order.toString(), offset, limit);
		CaseNoteSubTypes caseNoyeSubTypes = new CaseNoteSubTypes(caseNoteTypes, MetaDataFactory.createMetaData(limit, offset, caseNoteTypes));
		return GetUsersMeCaseNoteTypesByTypeCodeResponse.withJsonOK(caseNoyeSubTypes);
	}
}
