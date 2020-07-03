package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.AccessRole;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

/**
 * BDD step implementations for access role domain.
 */
public class AccessRoleSteps extends CommonSteps {
    private static final String API_ACCESS_ROLE_REQUEST_URL = API_PREFIX + "/access-roles";
    private ResponseEntity<?> createUpdateResponse;
    private List<AccessRole> accessRoles;

    @Step("create access role")
    public void createAccessRole(final String roleCode, final String roleName, final String parentRoleCode) {
        dispatchCreateOrUpdateAccessRoleRequest(roleCode, roleName, parentRoleCode, true);
    }

    @Step("Verify role created")
    public void verifyCreated() {
        assertThat(createUpdateResponse).isNotNull();
            assertThat(createUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Step("Verify role updated")
    public void verifyUpdated() {
        assertThat(createUpdateResponse).isNotNull();
        assertThat(createUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Step("Verify access roles returned")
    public void verifyAccessRoles() {
        assertThat(accessRoles).isNotNull();
        assertThat(accessRoles).extracting("roleCode").contains("ACCESS_ROLE_GENERAL");
    }

    @Step("Verify Admin access roles are not returned")
    public void verifyAccessRolesDoNotIncludeAdminRoles() {
        assertThat(accessRoles).isNotNull();
        assertThat(accessRoles).extracting("roleCode").doesNotContain("ACCESS_ROLE_ADMIN");
    }

    @Step("Verify access role not found")
    public void verifyAccessRoleNotFound() {
        assertThat(createUpdateResponse).isNull();
        assertErrorResponse(HttpStatus.NOT_FOUND);
    }

    private void dispatchCreateOrUpdateAccessRoleRequest(final String roleCode, final String roleName, final String parentRoleCode, final boolean create) {
        init();
        try {
            createUpdateResponse =
                    restTemplate.exchange(
                            API_ACCESS_ROLE_REQUEST_URL,
                            create ? POST : PUT,
                            createEntity(AccessRole.builder().roleCode(roleCode).roleName(roleName).parentRoleCode(parentRoleCode).build()), ResponseEntity.class);


        } catch (final PrisonApiClientException ex) {
            createUpdateResponse = null;
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchAccessRolesGet(final boolean includeAdmin) {
        init();
        var url = API_ACCESS_ROLE_REQUEST_URL;
        if (includeAdmin) {
            url = API_ACCESS_ROLE_REQUEST_URL + "?includeAdmin=true";
        }
        try {
            final var response = restTemplate.exchange(url, HttpMethod.GET, createEntity(null, null),
                    new ParameterizedTypeReference<List<AccessRole>>() {
                    });
            accessRoles = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void updateAccessRole(final String roleCode, final String roleName) {
        dispatchCreateOrUpdateAccessRoleRequest(roleCode, roleName, null, false);
    }

    public void getAccessRoles(final boolean includeAdmin) {
        dispatchAccessRolesGet(includeAdmin);
    }
}
