package net.syscon.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.syscon.prison.api.model.ErrorResponse;
import net.syscon.prison.api.model.ImageDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Api(tags = {"/images"})

public interface ImageResource {

    @GetMapping("/offenders/{offenderNo}")
    @ApiOperation(value = "Image details related to offender.", nickname = "getImagesByOffender")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ImageDetail.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<ImageDetail> getImagesByOffender(@PathVariable("offenderNo") final String offenderNo);

    @GetMapping("/{imageId}")
    @ApiOperation(value = "Image detail (with image data).", notes = "Image detail (with image data).", nickname = "getImage")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ImageDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ImageDetail getImage(@ApiParam(value = "The image id of offender", required = true) @PathVariable("imageId") Long imageId);

    @GetMapping(value = "/{imageId}/data", produces = "image/jpeg")
    @ApiOperation(value = "Image data (as bytes).", notes = "Image data (as bytes).", nickname = "getImageData")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Requested resource not found."),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.")})
    ResponseEntity<byte[]> getImageData(@ApiParam(value = "The image id of offender", required = true) @PathVariable("imageId") Long imageId,
                                  @ApiParam(value = "Return full size image", defaultValue = "false") @RequestParam(value = "fullSizeImage", defaultValue = "false")  boolean fullSizeImage);



}
