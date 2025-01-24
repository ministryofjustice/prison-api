package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.IncidentTypeConfiguration
import uk.gov.justice.hmpps.prison.core.ReferenceData
import uk.gov.justice.hmpps.prison.core.SlowReportQuery
import uk.gov.justice.hmpps.prison.service.IncidentReportConfigurationService

@RestController
@Tag(name = "Incident Reports", description = "Configuration information for an incident report by type")
@Validated
@RequestMapping(value = ["/api/incidents/configuration"], produces = ["application/json"])
class IncidentTypeConfigurationResource(
  private val incidentReportConfigurationService: IncidentReportConfigurationService,
) {

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Returns incident type configuration data sets for incident reports",
    description = "No additional role required",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns incident type configuration",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Missing privileges to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @SlowReportQuery
  @ReferenceData(description = "NO role needed as only reading reference data")
  fun getQuestionnaires(
    @Schema(description = "Return configuration for incident type only")
    @RequestParam(name = "incident-type", required = false) incidentType: String? = null,
  ): List<IncidentTypeConfiguration> = incidentReportConfigurationService.getIncidentTypeConfiguration(incidentType = incidentType)
}
