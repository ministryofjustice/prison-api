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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMark
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMarkDetails
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.DistinguishingMarkService
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.ImageService
import java.io.IOException

@RestController
@Tag(name = "distinguishing-marks")
@Validated
@RequestMapping(value = ["\${api.base.path}/person"], produces = ["application/json"])
class DistinguishingMarkResource(
  private val service: DistinguishingMarkService,
  private val imageService: ImageService,
  private val distinguishingMarkService: DistinguishingMarkService,
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
    summary = "Get all distinguishing marks associated with a prisoner's latest booking",
    description = "Requires role ROLE_VIEW_PRISONER_DATA",
  )
  @VerifyOffenderAccess(overrideRoles = ["VIEW_PRISONER_DATA"])
  @GetMapping("/{prisonerNumber}/distinguishing-marks")
  fun getIdentifyingMarksForLatestBooking(
    @PathVariable("prisonerNumber") @Parameter(
      description = "Prisoner unique reference",
      example = "A1234AA",
      required = true,
    ) prisonerNumber: String,
  ): List<DistinguishingMark> = service.findMarksForLatestBooking(prisonerNumber)

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
    summary = "Get a specific distinguishing mark associated with a prisoner",
    description = "Requires role ROLE_VIEW_PRISONER_DATA",
  )
  @VerifyOffenderAccess(overrideRoles = ["VIEW_PRISONER_DATA"])
  @GetMapping("/{prisonerNumber}/distinguishing-mark/{seqId}")
  fun getIdentifyingMark(
    @PathVariable("prisonerNumber") @Parameter(
      description = "Prisoner unique reference",
      example = "A1234AA",
      required = true,
    ) prisonerNumber: String,
    @PathVariable("seqId") @Parameter(description = "The sequence id of the mark", required = true) seqId: Int,
  ): DistinguishingMark = service.getMarkForLatestBooking(prisonerNumber, seqId)

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
      description = "PRISON_API__PRISONER_PROFILE__RW role required to access endpoint",
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
    summary = "Add a new photo to an distinguishing mark",
    description = "Requires role PRISON_API__PRISONER_PROFILE__RW",
  )
  @VerifyOffenderAccess(overrideRoles = ["VIEW_PRISONER_DATA"])
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @PostMapping(
    value = ["/{prisonerNumber}/distinguishing-mark/{seqId}/photo"],
    consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun addMarkPhoto(
    @PathVariable("prisonerNumber") @Parameter(
      description = "Prisoner unique reference",
      example = "A1234AA",
      required = true,
    ) prisonerNumber: String,
    @PathVariable("seqId") @Parameter(
      description = "The sequence id of the mark",
      required = true,
    ) seqId: Int,
    @Parameter(description = "The image as a file to upload", required = true) @RequestPart("file") file: MultipartFile,
  ): DistinguishingMark {
    try {
      distinguishingMarkService.addPhotoToMark(prisonerNumber, seqId, file.inputStream)
      return distinguishingMarkService.getMarkForLatestBooking(prisonerNumber, seqId)
    } catch (e: IOException) {
      throw BadRequestException("Image Data cannot be processed")
    }
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
      description = "PRISON_API__PRISONER_PROFILE__RW role required to access endpoint",
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
    summary = "Update an existing distinguishing mark",
    description = "Requires role PRISON_API__PRISONER_PROFILE__RW",
  )
  @VerifyOffenderAccess(overrideRoles = ["VIEW_PRISONER_DATA"])
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @PutMapping(
    value = ["/{prisonerNumber}/distinguishing-mark/{seqId}"],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun updateMark(
    @PathVariable("prisonerNumber") @Parameter(
      description = "Prisoner unique reference",
      example = "A1234AA",
      required = true,
    ) prisonerNumber: String,
    @PathVariable("seqId") @Parameter(
      description = "The sequence id of the mark",
      required = true,
    ) seqId: Int,
    @RequestBody
    @Parameter(
      description = "The update request",
      required = true,
    ) request: DistinguishingMarkDetails,
  ): DistinguishingMark = distinguishingMarkService.updateMark(prisonerNumber, seqId, request)

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "403",
      description = "PRISON_API__PRISONER_PROFILE__RW role required to access endpoint",
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
    summary = "Create a new distinguishing mark, optionally providing a photo",
    description = "Requires role PRISON_API__PRISONER_PROFILE__RW",
  )
  @VerifyOffenderAccess(overrideRoles = ["VIEW_PRISONER_DATA"])
  @PreAuthorize("hasRole('PRISON_API__PRISONER_PROFILE__RW')")
  @PostMapping(
    value = ["/{prisonerNumber}/distinguishing-mark"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun createMark(
    @PathVariable("prisonerNumber") @Parameter(
      description = "Prisoner unique reference",
      example = "A1234AA",
      required = true,
    ) prisonerNumber: String,
    @Parameter(
      description = "The create request",
      required = true,
    )
    request: DistinguishingMarkDetails,
    @Parameter(
      description = "The image as a file to upload (optional)",
      required = false,
    )
    @RequestPart("file") file: MultipartFile?,
  ): DistinguishingMark {
    try {
      val markId = distinguishingMarkService.createMark(prisonerNumber, request, file?.inputStream).id
      return distinguishingMarkService.getMarkForLatestBooking(prisonerNumber, markId)
    } catch (e: IOException) {
      throw BadRequestException("Image Data cannot be processed")
    }
  }
}
