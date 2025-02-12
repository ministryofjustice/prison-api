package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.IdentifyingMark
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.IdentifyingMarkService
import uk.gov.justice.hmpps.prison.service.ImageService
import java.io.IOException

@RestController
@Tag(name = "identifying-marks")
@Validated
@RequestMapping(value = ["\${api.base.path}/identifying-marks"], produces = ["application/json"])
class IdentifyingMarkResource(
  private val service: IdentifyingMarkService,
  private val imageService: ImageService,
  private val identifyingMarkService: IdentifyingMarkService,
) {

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "Get all identifying marks associated with a prisoner's latest booking",
    description = "Requires role ROLE_VIEW_PRISONER_DATA",
  )
  @VerifyOffenderAccess(overrideRoles = ["VIEW_PRISONER_DATA"])
  @GetMapping("/prisoner/{offenderNo}")
  fun getIdentifyingMarksForLatestBooking(
    @PathVariable("offenderNo") @Parameter(
      description = "The offenderNo of offender",
      required = true,
    ) offenderNo: String,
  ): List<IdentifyingMark> = service.findIdentifyingMarksForLatestBooking(offenderNo)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "Get a specific identifying mark associated with a prisoner",
    description = "Requires role ROLE_VIEW_PRISONER_DATA",
  )
  @VerifyOffenderAccess(overrideRoles = ["VIEW_PRISONER_DATA"])
  @GetMapping("/prisoner/{offenderNo}/mark/{markId}")
  fun getIdentifyingMark(
    @PathVariable("offenderNo") @Parameter(
      description = "The offenderNo of offender",
      required = true,
    ) offenderNo: String,
    @PathVariable("markId") @Parameter(description = "The id of the mark", required = true) markId: Int,
  ): IdentifyingMark = service.getIdentifyingMarkForLatestBooking(offenderNo, markId)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "Get the content of an image",
    description = "Requires role ROLE_VIEW_PRISONER_DATA",
  )
  @PreAuthorize("hasRole('VIEW_PRISONER_DATA')")
  @GetMapping("/photo/{photoId}")
  fun getImage(
    @PathVariable("photoId") @Parameter(
      description = "The id of the image",
      required = true,
    ) photoId: Long,
  ): ResponseEntity<ByteArray> {
    val image = imageService.getImageContent(photoId, true)
      .orElseThrow(EntityNotFoundException("Unable to find image with id $photoId"))
    return ResponseEntity.ok()
      .header("Content-Type", MediaType.IMAGE_JPEG_VALUE)
      .body(image)
  }

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "403",
      description = "IMAGE_UPLOAD role required to access endpoint",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "404",
      description = "The offender number could not be found or has no bookings.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  )
  @Operation(
    summary = "Add a new photo to an identifying mark",
    description = "Requires role ROLE_VIEW_PRISONER_DATA",
  )
  @VerifyOffenderAccess(overrideRoles = ["VIEW_PRISONER_DATA"])
  @PostMapping(
    value = ["/prisoner/{offenderNo}/mark/{markId}/photo"],
    consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun addMarkPhoto(
    @PathVariable("offenderNo") @Parameter(
      description = "The offender number relating to the mark",
      required = true,
    ) offenderNo: String,
    @PathVariable("markId") @Parameter(
      description = "The mark id",
      required = true,
    ) markId: Int,
    @Parameter(description = "The image as a file to upload", required = true) @RequestPart("file") file: MultipartFile,
  ): IdentifyingMark {
    try {
      identifyingMarkService.addPhotoToMark(offenderNo, markId, file.inputStream)
      return identifyingMarkService.getIdentifyingMarkForLatestBooking(offenderNo, markId)
    } catch (e: IOException) {
      throw BadRequestException("Image Data cannot be processed")
    }
  }
}
