package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
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
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.BadRequestException;
import uk.gov.justice.hmpps.prison.service.ImageService;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Tag(name = "images")
@Validated
@RequestMapping(value = "${api.base.path}/images", produces = "application/json")
@AllArgsConstructor
public class ImageResource {

    private final ImageService imageService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Requested resource not found."),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.")})
    @Operation(summary = "Image data (as bytes).", description = "Requires role VIEW_PRISONER_DATA.")
    @Tag(name = "integration-api")
    @PreAuthorize("hasAnyRole('VIEW_PRISONER_DATA','PRISON_API__HMPPS_INTEGRATION_API')")
    @GetMapping(value = "/{imageId}/data", produces = "image/jpeg")
    public ResponseEntity<byte[]> getImageData(
        @PathVariable("imageId") @Parameter(description = "The image id of offender", required = true) final Long imageId,
        @RequestParam(value = "fullSizeImage", defaultValue = "false") @Parameter(description = "Return full size image") final boolean fullSizeImage
    ) {
        return imageService.getImageContent(imageId, fullSizeImage)
                .map(bytes -> new ResponseEntity<>(bytes, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Image details related to offender.", description = "Requires role VIEW_PRISONER_DATA.")
    @Tag(name = "integration-api")
    @PreAuthorize("hasAnyRole('VIEW_PRISONER_DATA','PRISON_API__HMPPS_INTEGRATION_API')")
    @GetMapping("/offenders/{offenderNo}")
    @VerifyOffenderAccess(overrideRoles = {"VIEW_PRISONER_DATA"})
    public List<ImageDetail> getImagesByOffender(@PathVariable("offenderNo") final String offenderNo) {
        return imageService.findOffenderImagesFor(offenderNo);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ImageDetail.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Image detail (with image data).", description = "Requires role VIEW_PRISONER_DATA.")
    @PreAuthorize("hasRole('VIEW_PRISONER_DATA')")
    @GetMapping("/{imageId}")
    public ImageDetail getImage(@PathVariable("imageId") @Parameter(description = "The image id of offender", required = true) final Long imageId) {
        return imageService.findImageDetail(imageId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "PRISON_API__PRISONER_PROFILE__RW role required to access endpoint", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "The offender number could not be found or has no bookings.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(
        summary = "Upload a new image for a prisoner.",
        description = "Requires PRISON_API__PRISONER_PROFILE__RW. Additionally scales the image to 480 x 600 and creates a thumbnail 240 x 300 (4:5 aspect ratio)."
    )
    @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
    @PostMapping(value = "/offenders/{offenderNo}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ProxyUser
    public ImageDetail putImageMultiPart(
        @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$", message = "Offender Number format incorrect") @PathVariable("offenderNo") @Parameter(description = "The offender number relating to this image.", required = true) final String offenderNo,
        @Parameter(description = "The image as a file to upload", required = true) @RequestPart("file") MultipartFile file
    )  {
        try {
            return imageService.putImageForOffender(offenderNo, file.getInputStream());
        } catch (IOException e) {
            throw new BadRequestException("Image Data cannot be processed");
        }
    }
}
