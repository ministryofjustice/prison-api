package uk.gov.justice.hmpps.prison.executablespecification.steps;

import com.google.common.collect.ImmutableMap;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.UserDetail;
import uk.gov.justice.hmpps.prison.api.model.UserRole;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for User domain.
 */
public class UserSteps extends CommonSteps {
    private static final String API_USERS_ME_REQUEST_URL = API_PREFIX + "users/me";
    private static final String API_USERS_ME_LOCATIONS_REQUEST_URL = API_USERS_ME_REQUEST_URL + "/locations";
    private static final String API_USERS_ME_ROLES_REQUEST_URL = API_USERS_ME_REQUEST_URL + "/roles";
    private static final String API_USERS_ME_CASE_NOTE_TYPES_REQUEST_URL = API_USERS_ME_REQUEST_URL + "/caseNoteTypes";
    private static final String API_ASSIGN_API_ROLE_TO_USER = API_PREFIX + "/users/{username}/access-role/{roleCode}";
    private static final String API_ASSIGN_ACCESS_ROLE_TO_USER_FOR_CASELOAD = API_PREFIX + "/users/{username}/caseload/{caseload}/access-role/{roleCode}";
    private static final String API_REMOVE_ROLE_FROM_USER_AT_CASELOAD = API_PREFIX + "/users/{username}/caseload/{caseload}/access-role/{roleCode}";
    private static final String API_USERS = API_PREFIX + "/users";
    private static final String API_USERS_LIST = API_PREFIX + "/users/list";
    private static final String API_LOCAL_ADMINISTRATOR_USERS = API_PREFIX + "/users/local-administrator/available";
    private static final String API_ROLES_BY_USERS_AT_CASELOAD = API_PREFIX + "/users/{username}/access-roles/caseload/{caseload}";

    private List<Location> userLocations;
    private List<UserRole> userRoles;
    private List<ReferenceCode> caseNoteTypes;
    private List<String> usernames;
    private List<UserDetail> userDetails;

    @Override
    protected void init() {
        super.init();

        userLocations = null;
        userRoles = null;
        caseNoteTypes = null;
    }

    @Step("Verify current user details")
    public void verifyDetails(final String username, final String firstName, final String lastName) {
        try {
            final var response = restTemplate.exchange(
                    API_USERS_ME_REQUEST_URL,
                    HttpMethod.GET,
                    createEntity(),
                    UserDetail.class);

            final var userDetails = response.getBody();

            assertThat(userDetails.getUsername()).isEqualToIgnoringCase(username);
            assertThat(userDetails).hasFieldOrPropertyWithValue("firstName", firstName);
            assertThat(userDetails).hasFieldOrPropertyWithValue("lastName", lastName);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Retrieve user locations")
    public void retrieveUserLocations() {
        dispatchUserLocationsRequest();
    }

    @Step("Verify location agency ids")
    public void verifyLocationAgencies(final String agencies) {
        verifyPropertyValues(userLocations, Location::getAgencyId, agencies);
    }

    @Step("Verify location desscriptions")
    public void verifyLocationDescriptions(final String descriptions) {
        verifyPropertyValues(userLocations, Location::getDescription, descriptions);
    }

    @Step("Verify location prefixes")
    public void verifyLocationPrefixes(final String prefixes) {
        verifyPropertyValues(userLocations, Location::getLocationPrefix, prefixes);
    }

    @Step("Get roles for current user")
    public void getUserRoles(final boolean allRoles) {
        dispatchUserRolesRequest(allRoles);
    }

    @Step("Verify roles retrieved for current user")
    public void verifyRoles(final String roles) {
        verifyPropertyValues(userRoles, UserRole::getRoleCode, roles);
    }

    @Step("Get case note types for current user")
    public void getUserCaseNoteTypes() {
        dispatchUserCaseNoteTypesRequest();
    }

    @Step("Verify case note types have sub-types")
    public void verifyCaseNoteTypesHaveSubTypes() {
        assertThat(caseNoteTypes.isEmpty()).isFalse();

        assertThat(caseNoteTypes.stream().filter(type -> type.getSubCodes().isEmpty()).count()).isEqualTo(0);
    }

    public void getUsersByLaa(final String roleCode, final String nameFilter) {
        dispatchUsersByCaseloadRequest(null, roleCode, nameFilter);
    }

    public void getUsers(final String roleCode, final String nameFilter) {
        dispatchUsersRequest(roleCode, nameFilter);
    }

    public void getUsers(final List<String> usernames) {
        dispatchPostUsersRequest(usernames);
    }

    public void getRolesByUserAndCaseload(final String username, final String caseload) {
        dispatchRolesByUserAndCaseloadRequest(username, caseload);
    }

    //    @Step("Verify usernames")
    public void verifyUsernames(final String expectedUsernames) {
        verifyIdentical(usernames, csv2list(expectedUsernames));
    }

    public void assignApiRoleToUser(final String role, final String username) {
        dispatchAssignApiRoleToUser(role, username);
    }

    public void assignAccessRoleToUser(final String role, final String username, final String caseloadId) {
        dispatchAssignAccessRoleToUserForCaseload(role, username, caseloadId);
    }

    public void removeRole(final String role, final String username, final String caseload) {
        dispatchRemoveRoleFromUserAtCaseload(role, username, caseload);
    }

    public void verifyApiRoleAssignment(final String username, final String role) {
        verifyAccessRoleAssignment(username, role, "NWEB");
    }

    public void verifyAccessRoleAssignment(final String username, final String role, final String caseload) {
        dispatchRolesByUserAndCaseloadRequest(username, caseload);
        assertThat(role).isIn(userRoles.stream().map(UserRole::getRoleCode).collect(Collectors.toList()));
    }

    public void userDoesNotHaveRoleAtCaseload(final String username, final String role, final String caseload) {
        dispatchRolesByUserAndCaseloadRequest(username, caseload);
        assertThat(role).isNotIn(userRoles.stream().map(UserRole::getRoleCode).collect(Collectors.toList()));
    }

    public void verifyUserList(final String expectedUsernames) {
        assertThat(userDetails).extracting("username").isSubsetOf(csv2list(expectedUsernames));
    }

    public void verifyRoleList(final String expectedRoleCodes) {
        assertThat(userRoles).extracting("roleCode").isSubsetOf(csv2list(expectedRoleCodes));
    }

    private void dispatchRemoveRoleFromUserAtCaseload(final String role, final String username, final String caseload) {
        init();

        restTemplate.exchange(
                API_REMOVE_ROLE_FROM_USER_AT_CASELOAD,
                HttpMethod.DELETE,
                createEntity(),
                Object.class,
                username,
                caseload,
                role);
    }


    private void dispatchAssignApiRoleToUser(final String role, final String username) {
        init();

        restTemplate.exchange(
                API_ASSIGN_API_ROLE_TO_USER,
                HttpMethod.PUT,
                createEntity(),
                Object.class,
                username,
                role);
    }

    private void dispatchAssignAccessRoleToUserForCaseload(final String role, final String username, final String caseloadId) {
        init();
        try {

            restTemplate.exchange(
                    API_ASSIGN_ACCESS_ROLE_TO_USER_FOR_CASELOAD,
                    HttpMethod.PUT,
                    createEntity(),
                    Object.class,
                    username,
                    caseloadId,
                    role);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchUsersByCaseloadRequest(final String caseload, final String role, final String nameFilter) {
        init();
        var url = API_LOCAL_ADMINISTRATOR_USERS;

        if (StringUtils.isNotBlank(role) || StringUtils.isNotBlank(nameFilter)) {
            final var queryParameters = buildQueryStringParameters(ImmutableMap.of("accessRole", role, "nameFilter", nameFilter));
            if (StringUtils.isNotBlank(queryParameters))
                url += String.format("?=%s", queryParameters);
        }

        applyPagination(0L, 100L);

        final var response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createEntity(null, addPaginationHeaders()),
                new ParameterizedTypeReference<List<UserDetail>>() {
                },
                caseload);

        userDetails = response.getBody();
    }

    private void dispatchUsersRequest(final String role, final String nameFilter) {
        init();
        var url = API_USERS;

        if (StringUtils.isNotBlank(role) || StringUtils.isNotBlank(nameFilter)) {
            final var queryParameters = buildQueryStringParameters(ImmutableMap.of("accessRole", role, "nameFilter", nameFilter));
            if (StringUtils.isNotBlank(queryParameters))
                url += String.format("?=%", queryParameters);
        }

        applyPagination(0L, 100L);
        final var httpEntity = createEntity(null, addPaginationHeaders());

        final var response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<UserDetail>>() {
                });

        userDetails = response.getBody();
    }

    private void dispatchPostUsersRequest(final List<String> usernames) {
        init();

        final var response = restTemplate.exchange(
                API_USERS_LIST,
                HttpMethod.POST,
                createEntity(usernames),
                new ParameterizedTypeReference<List<UserDetail>>() {
                });

        userDetails = response.getBody();
    }

    private void dispatchRolesByUserAndCaseloadRequest(final String username, final String caseload) {
        init();

        final var response = restTemplate.exchange(
                API_ROLES_BY_USERS_AT_CASELOAD,
                HttpMethod.GET,
                createEntity(),
                new ParameterizedTypeReference<List<UserRole>>() {
                },
                username,
                caseload);

        userRoles = response.getBody();
    }

    private void dispatchUserRolesRequest(final boolean allRoles) {
        init();

        try {
            final var response = restTemplate.exchange(
                    API_USERS_ME_ROLES_REQUEST_URL + (allRoles ? "?allRoles=true" : ""),
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<List<UserRole>>() {
                    });

            userRoles = response.getBody();

            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchUserLocationsRequest() {
        init();

        try {
            final var response = restTemplate.exchange(
                    API_USERS_ME_LOCATIONS_REQUEST_URL,
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<List<Location>>() {
                    });

            userLocations = response.getBody();

            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchUserCaseNoteTypesRequest() {
        init();

        final ResponseEntity<List<ReferenceCode>> response;

        try {
            response = restTemplate.exchange(
                    API_USERS_ME_CASE_NOTE_TYPES_REQUEST_URL,
                    HttpMethod.GET,
                    createEntity(),
                new ParameterizedTypeReference<>() {
                });

            caseNoteTypes = response.getBody();

            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

}
