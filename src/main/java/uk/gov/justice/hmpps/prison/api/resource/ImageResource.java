package uk.gov.justice.hmpps.prison.api.resource;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.BadRequestException;
import uk.gov.justice.hmpps.prison.service.ImageService;

import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(tags = {"images"})
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
    public ResponseEntity<byte[]> getImageData(
        @PathVariable("imageId") @ApiParam(value = "The image id of offender", required = true) final Long imageId,
        @RequestParam(value = "fullSizeImage", defaultValue = "false") @ApiParam(value = "Return full size image", defaultValue = "false") final boolean fullSizeImage
    ) {
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
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "VIEW_PRISONER_DATA"})
    public List<ImageDetail> getImagesByOffender(@PathVariable("offenderNo") final String offenderNo) {
        return imageService.findOffenderImagesFor(offenderNo);
    }

    @GetMapping("/offenders")
    @ApiOperation(value = "Get offenders with images captured in provided range")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @PreAuthorize("hasRole('SYSTEM_USER')")
    public Page<OffenderNumber> getOffendersWithImagesCapturedInRange(
        @ApiParam(value = "fromDateTime", required = true) @DateTimeFormat(iso = DATE_TIME) @RequestParam("fromDateTime") final LocalDateTime fromDate,
        @PageableDefault(direction = ASC, sort = "nomsId") final Pageable pageable) {
        return imageService.getOffendersWithImagesCapturedAfter(fromDate, pageable);
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

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 403, message = "IMAGE_UPLOAD role required to access endpoint", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "The offender number could not be found or has no bookings.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "DEV USE ONLY *** Upload a new image for a prisoner.", notes = "Requires ROLE_IMAGE_UPLOAD.")
    @PostMapping(value = "/offenders/{offenderNo}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ImageDetail putImageMultiPart(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Offender Number format incorrect") @PathVariable("offenderNo") @ApiParam(value = "The offender number relating to this image.", required = true) final String offenderNo,
        @ApiParam(value = "The image as a file to upload", required = true) @RequestPart("file") MultipartFile file
    )  {
        try {
            return imageService.putImageForOffender(offenderNo, file.getInputStream());
        } catch (IOException e) {
            throw new BadRequestException("Image Data cannot be processed");
        }
    }
}
