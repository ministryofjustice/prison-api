package uk.gov.justice.hmpps.prison.api.resource;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.service.ImageService;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
@RestController
@Validated
@RequestMapping("${api.base.path}/images")
@AllArgsConstructor
public class ImageResource {

    private final ImageService imageService;

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Requested resource not found."),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.")})
    @ApiOperation(value = "Image data (as bytes).", notes = "Image data (as bytes).", nickname = "getImageData")
    @GetMapping(value = "/{imageId}/data", produces = "image/jpeg")
    public ResponseEntity<byte[]> getImageData(@PathVariable("imageId") @ApiParam(value = "The image id of offender", required = true) final Long imageId, @RequestParam(value = "fullSizeImage", defaultValue = "false") @ApiParam(value = "Return full size image", defaultValue = "false") final boolean fullSizeImage) {
        return imageService.getImageContent(imageId, fullSizeImage)
                .map(bytes -> new ResponseEntity<>(bytes, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ImageDetail.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Image details related to offender.", nickname = "getImagesByOffender")
    @GetMapping("/offenders/{offenderNo}")
    public List<ImageDetail> getImagesByOffender(@PathVariable("offenderNo") final String offenderNo) {
        return imageService.findOffenderImagesFor(offenderNo);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ImageDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Image detail (with image data).", notes = "Image detail (with image data).", nickname = "getImage")
    @GetMapping("/{imageId}")
    public ImageDetail getImage(@PathVariable("imageId") @ApiParam(value = "The image id of offender", required = true) final Long imageId) {
        return imageService.findImageDetail(imageId);
    }
}
