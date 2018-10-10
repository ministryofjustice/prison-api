package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.AccessRole;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

/**
 * BDD step implementations for access role domain.
 */
public class AccessRoleSteps extends CommonSteps {
    private static final String API_ACCESS_ROLE_REQUEST_URL = API_PREFIX + "/access-roles";
    private ResponseEntity createUpdateResponse;
    private List<AccessRole> accessRoles;

    @Step("create access role")
    public void createAccessRole(String roleCode, String roleName, String parentRoleCode) {
        dispatchCreateOrUpdateAccessRoleRequest(roleCode, roleName, parentRoleCode, true);
    }

    @Step("Verify role created")
    public void verifyCreated() {
        assertThat(createUpdateResponse).isNotNull();
        assertThat(createUpdateResponse.getStatusCode().value()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Step("Verify role updated")
    public void verifyUpdated() {
        assertThat(createUpdateResponse).isNotNull();
        assertThat(createUpdateResponse.getStatusCode().value()).isEqualTo(Response.Status.OK.getStatusCode());
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

    private void dispatchCreateOrUpdateAccessRoleRequest(String roleCode, String roleName, String parentRoleCode, boolean create) {
        init();

        try {
            createUpdateResponse =
                    restTemplate.exchange(
                            API_ACCESS_ROLE_REQUEST_URL,
                            create ? POST : PUT,
                            createEntity(AccessRole.builder().roleCode(roleCode).roleName(roleName).parentRoleCode(parentRoleCode).build()), ResponseEntity.class);


        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchAccessRolesGet(boolean includeAdmin) {
        init();
        String url = API_ACCESS_ROLE_REQUEST_URL;
        if (includeAdmin) {
            url = API_ACCESS_ROLE_REQUEST_URL + "?includeAdmin=true";
        }
        try {
            ResponseEntity<List<AccessRole>> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(null, null),
                    new ParameterizedTypeReference<List<AccessRole>>() {});
            accessRoles = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void updateAccessRole(String roleCode, String roleName) {
        dispatchCreateOrUpdateAccessRoleRequest(roleCode, roleName, null, false);
    }

    public void getAccessRoles(boolean includeAdmin) {
        dispatchAccessRolesGet(includeAdmin);
    }
}