package net.syscon.elite.executablespecification.steps;

import com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for User domain.
 */
public class UserSteps extends CommonSteps {
    private static final String API_USERS_ME_REQUEST_URL = API_PREFIX + "users/me";
    private static final String API_USERS_ME_LOCATIONS_REQUEST_URL = API_USERS_ME_REQUEST_URL + "/locations";
    private static final String API_USERS_ME_ROLES_REQUEST_URL = API_USERS_ME_REQUEST_URL + "/roles";
    private static final String API_USERS_ME_CASE_NOTE_TYPES_REQUEST_URL = API_USERS_ME_REQUEST_URL + "/caseNoteTypes";
    private static final String API_USERS_USERNAMES_HAVING_ROLE_AT_CASELOAD = API_PREFIX + "/users/access-roles/caseload/{caseload}/access-role/{roleCode}";
    private static final String API_ASSIGN_API_ROLE_TO_USER = API_PREFIX + "/users/{username}/access-role/{roleCode}";
    private static final String API_ASSIGN_ACCESS_ROLE_TO_USER_FOR_CASELOAD = API_PREFIX + "/users/{username}/caseload/{caseload}/access-role/{roleCode}";
    private static final String API_REMOVE_ROLE_FROM_USER_AT_CASELOAD = API_PREFIX + "/users/{username}/caseload/{caseload}/access-role/{roleCode}";
    private static final String API_USERS_AT_CASELOAD = API_PREFIX + "/users/caseload/{caseload}";
    private static final String API_USERS = API_PREFIX + "/users";
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
    public void verifyDetails(String username, String firstName, String lastName) {
        try {
            ResponseEntity<UserDetail> response = restTemplate.exchange(
                    API_USERS_ME_REQUEST_URL,
                    HttpMethod.GET,
                    createEntity(),
                    UserDetail.class);

            UserDetail userDetails = response.getBody();

            assertThat(userDetails.getUsername()).isEqualToIgnoringCase(username);
            assertThat(userDetails).hasFieldOrPropertyWithValue("firstName", firstName);
            assertThat(userDetails).hasFieldOrPropertyWithValue("lastName", lastName);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Retrieve user locations")
    public void retrieveUserLocations() {
        dispatchUserLocationsRequest();
    }

    @Step("Verify location agency ids")
    public void verifyLocationAgencies(String agencies) {
        verifyPropertyValues(userLocations, Location::getAgencyId, agencies);
    }

    @Step("Verify location desscriptions")
    public void verifyLocationDescriptions(String descriptions) {
        verifyPropertyValues(userLocations, Location::getDescription, descriptions);
    }

    @Step("Verify location prefixes")
    public void verifyLocationPrefixes(String prefixes) {
        verifyPropertyValues(userLocations, Location::getLocationPrefix, prefixes);
    }

    @Step("Get roles for current user")
    public void getUserRoles(boolean allRoles) {
        dispatchUserRolesRequest(allRoles);
    }

    @Step("Verify roles retrieved for current user")
    public void verifyRoles(String roles) {
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

    public void getUsersByCaseload(String caseloadId, String roleCode, String nameFilter, boolean localAdministratorUsersOnly) {
        dispatchUsersByCaseloadRequest(caseloadId, roleCode, nameFilter, localAdministratorUsersOnly);
    }

    public void getUsersByLaa(String roleCode, String nameFilter) {
        dispatchUsersByCaseloadRequest(null, roleCode, nameFilter, true);
    }

    public void getUsers(String roleCode, String nameFilter, boolean localAdministratorUsersOnly) {
        dispatchUsersRequest(roleCode, nameFilter);
    }

    public void getRolesByUserAndCaseload(String username, String caseload) {
        dispatchRolesByUserAndCaseloadRequest(username, caseload);
    }

    //    @Step("Find usernames having role at caseload")
    public void findUsernamesHavingRoleAtCaseload(String role, String caseload) {
        dispatchUsernamesHavingRoleAtCaseloadRequest(role, caseload);
    }

    //    @Step("Verify usernames")
    public void verifyUsernames(String expectedUsernames) {
        verifyIdentical(usernames, csv2list(expectedUsernames));
    }

    public void assignApiRoleToUser(String role, String username) {
        dispatchAssignApiRoleToUser(role, username);
    }

    public void assignAccessRoleToUser(String role, String username, String caseloadId) {
        dispatchAssignAccessRoleToUserForCaseload(role, username, caseloadId);
    }

    public void removeRole(String role, String username, String caseload) {
        dispatchRemoveRoleFromUserAtCaseload(role, username, caseload);
    }

    public void verifyApiRoleAssignment(String username, String role) {
        dispatchUsernamesHavingRoleAtCaseloadRequest(role, "NWEB");
        assertThat(username).isIn(usernames);
    }

    public void verifyAccessRoleAssignment(String username, String role, String caseload) {
        dispatchUsernamesHavingRoleAtCaseloadRequest(role,caseload);
        assertThat(username).isIn(usernames);
    }

    public void userDoesNotHaveRoleAtCaseload(String username, String role, String caseload) {
        dispatchUsernamesHavingRoleAtCaseloadRequest(role, caseload);
        assertThat(username).isNotIn(usernames);

    }

    public void verifyUserList(String expectedUsernames) {
        assertThat(userDetails).extracting("username").containsOnlyElementsOf(csv2list(expectedUsernames));
    }

    public void verifyRoleList(String expectedRoleCodes) {
        assertThat(userRoles).extracting("roleCode").containsOnlyElementsOf(csv2list(expectedRoleCodes));
    }

    private void dispatchRemoveRoleFromUserAtCaseload(String role, String username, String caseload) {
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


    private void dispatchAssignApiRoleToUser(String role, String username) {
        init();

        restTemplate.exchange(
                API_ASSIGN_API_ROLE_TO_USER,
                HttpMethod.PUT,
                createEntity(),
                Object.class,
                username,
                role);
    }

    private void dispatchAssignAccessRoleToUserForCaseload(String role, String username, String caseloadId) {
        init();
        try{

            restTemplate.exchange(
                    API_ASSIGN_ACCESS_ROLE_TO_USER_FOR_CASELOAD,
                    HttpMethod.PUT,
                    createEntity(),
                    Object.class,
                    username,
                    caseloadId,
                    role);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }


    private void dispatchUsernamesHavingRoleAtCaseloadRequest(String role, String caseload) {
        init();

        ResponseEntity<List<String>> response = restTemplate.exchange(
                API_USERS_USERNAMES_HAVING_ROLE_AT_CASELOAD,
                HttpMethod.GET,
                createEntity(),
                new ParameterizedTypeReference<List<String>>() {
                },
                caseload,
                role);

        usernames = response.getBody();
    }

    private void dispatchUsersByCaseloadRequest(String caseload, String role, String nameFilter, boolean localAdministratorUsers) {
        init();
        var url = localAdministratorUsers ? API_LOCAL_ADMINISTRATOR_USERS : API_USERS_AT_CASELOAD;

        if(StringUtils.isNotBlank(role) || StringUtils.isNotBlank(nameFilter)) {
            String queryParameters = buildQueryStringParameters(ImmutableMap.of("accessRole", role,"nameFilter", nameFilter));
            if(StringUtils.isNotBlank(queryParameters))
                url += String.format("?=%s", queryParameters);
        }

        var response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createEntity(),
                new ParameterizedTypeReference<List<UserDetail>>() {
                },
                caseload);

        userDetails = response.getBody();
    }

    private void dispatchUsersRequest(String role, String nameFilter) {
        init();
        String url = API_USERS;

        if(StringUtils.isNotBlank(role) || StringUtils.isNotBlank(nameFilter)) {
            String queryParameters = buildQueryStringParameters(ImmutableMap.of("accessRole", role,"nameFilter", nameFilter));
            if(StringUtils.isNotBlank(queryParameters))
                url += String.format("?=%", queryParameters);
        }

        applyPagination(0L, 100L);
        HttpEntity<?> httpEntity = createEntity(null, addPaginationHeaders());

        ResponseEntity<List<UserDetail>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<UserDetail>>() { });

        userDetails = response.getBody();
    }

    private void dispatchRolesByUserAndCaseloadRequest(String username, String caseload) {
        init();

        ResponseEntity<List<UserRole>> response = restTemplate.exchange(
                API_ROLES_BY_USERS_AT_CASELOAD,
                HttpMethod.GET,
                createEntity(),
                new ParameterizedTypeReference<List<UserRole>>() {
                },
                username,
                caseload);

        userRoles = response.getBody();
    }

    private void dispatchUserRolesRequest(boolean allRoles) {
        init();

        try {
            ResponseEntity<List<UserRole>> response = restTemplate.exchange(
                    API_USERS_ME_ROLES_REQUEST_URL + (allRoles ? "?allRoles=true" : ""),
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<List<UserRole>>() {
                    });

            userRoles = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchUserLocationsRequest() {
        init();

        try {
            ResponseEntity<List<Location>> response = restTemplate.exchange(
                    API_USERS_ME_LOCATIONS_REQUEST_URL,
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<List<Location>>() {
                    });

            userLocations = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchUserCaseNoteTypesRequest() {
        init();

        ResponseEntity<List<ReferenceCode>> response;

        try {
            response = restTemplate.exchange(
                    API_USERS_ME_CASE_NOTE_TYPES_REQUEST_URL,
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<List<ReferenceCode>>() {
                    });

            caseNoteTypes = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

}
