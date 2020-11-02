package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.AccessRole;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.AccessRoleService;

import java.util.List;

@RestController
@Api(tags = {"access-roles"})
@Validated
@RequestMapping("${api.base.path}/access-roles")
public class AccessRoleResource {
    private final AccessRoleService accessRoleService;

    public AccessRoleResource(final AccessRoleService accessRoleService) {
        this.accessRoleService = accessRoleService;
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = AccessRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of access roles", notes = "List of access roles", nickname = "getAccessRoles")
    @GetMapping
    public List<AccessRole> getAccessRoles(@RequestParam(value = "includeAdmin", required = false, defaultValue = "false") @ApiParam(value = "Include admin roles", required = true, defaultValue = "false") final boolean includeAdmin) {
        return accessRoleService.getAccessRoles(includeAdmin);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. role code not provided.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to create an access role.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Parent access role not found.", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "A role already exists with the provided role code.", response = ErrorResponse.class)})
    @ApiOperation(value = "Create new access role.", notes = "Create new access role.", nickname = "createAccessRole")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @ProxyUser
    public ResponseEntity<Void> createAccessRole(@RequestBody @ApiParam(required = true) final AccessRole newAccessRole) {
        accessRoleService.createAccessRole(newAccessRole);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. role code not provided.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to update an access role.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Access role not found.", response = ErrorResponse.class)})
    @ApiOperation(value = "Update the access role.", notes = "Update the access role.", nickname = "updateAccessRole")
    @PutMapping
    @ProxyUser
    public ResponseEntity<Void>  updateAccessRole(@RequestBody @ApiParam(required = true) final AccessRole accessRole) {
        accessRoleService.updateAccessRole(accessRole);
        return ResponseEntity.ok().build();
    }

}
