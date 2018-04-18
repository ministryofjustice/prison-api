package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
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

    private List<Location> userLocations;
    private List<UserRole> userRoles;
    private List<ReferenceCode> caseNoteTypes;

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

    private void dispatchUserRolesRequest(boolean allRoles) {
        init();

        try {
            ResponseEntity<List<UserRole>> response = restTemplate.exchange(
                    API_USERS_ME_ROLES_REQUEST_URL + (allRoles ? "?allRoles=true" : ""),
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<List<UserRole>>() {});

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
                    new ParameterizedTypeReference<List<Location>>() {});

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
                    new ParameterizedTypeReference<List<ReferenceCode>>() {});

            caseNoteTypes = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
}
