package uk.gov.justice.hmpps.prison.executablespecification.steps;

import com.google.common.collect.ImmutableMap;
import net.serenitybdd.annotations.Step;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for User domain.
 */
public class UserSteps extends CommonSteps {
    private static final String API_USERS_ME_REQUEST_URL = API_PREFIX + "users/me";
    private static final String API_USERS_ME_LOCATIONS_REQUEST_URL = API_USERS_ME_REQUEST_URL + "/locations";
    private static final String API_USERS_ME_ROLES_REQUEST_URL = API_USERS_ME_REQUEST_URL + "/roles";
    private static final String API_USERS_ME_CASE_NOTE_TYPES_REQUEST_URL = API_USERS_ME_REQUEST_URL + "/caseNoteTypes";
    private static final String API_USERS_LIST = API_PREFIX + "/users/list";
    private static final String API_LOCAL_ADMINISTRATOR_USERS = API_PREFIX + "/users/local-administrator/available";

    private List<Location> userLocations;
    private List<UserRole> userRoles;
    private List<ReferenceCode> caseNoteTypes;
    private List<UserDetail> userDetails;

    @Override
    protected void init() {
        super.init();

        userLocations = null;
        userRoles = null;
        caseNoteTypes = null;
    }

    @Step("Retrieve user locations")
    public void retrieveUserLocations(boolean includeNonResidential) {
        dispatchUserLocationsRequest(includeNonResidential);
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
        dispatchUsersByCaseloadRequest(roleCode, nameFilter);
    }

    public void getUsers(final List<String> usernames) {
        dispatchPostUsersRequest(usernames);
    }

    public void verifyUserList(final String expectedUsernames) {
        assertThat(userDetails).extracting("username").isSubsetOf(csv2list(expectedUsernames));
    }

    public void verifyRoleList(final String expectedRoleCodes) {
        assertThat(userRoles).extracting("roleCode").isSubsetOf(csv2list(expectedRoleCodes));
    }

    private void dispatchUsersByCaseloadRequest(final String role, final String nameFilter) {
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

    private void dispatchUserLocationsRequest(boolean includeNonResidential) {
        init();

        try {
            final var response = restTemplate.exchange(
                    API_USERS_ME_LOCATIONS_REQUEST_URL + "?include-non-residential-locations="+ includeNonResidential,
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
