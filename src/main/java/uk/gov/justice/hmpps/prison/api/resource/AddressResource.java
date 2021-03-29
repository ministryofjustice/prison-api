package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;


@RestController
@Validated
@Api(tags = {"Addresses"})
@RequestMapping("${api.base.path}/addresses")
@AllArgsConstructor
public class AddressResource {

    @PostMapping("/addresses")
    @ApiOperation(value = "Adds a new address to a location", consumes = "application/json", produces = "application/json")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "CREATED", response = String.class),
        @ApiResponse(code = 401, message = "Unauthorized.", response = ErrorResponse.class)})
    public AddressDto addAddress() {
        return null;
    }


}
