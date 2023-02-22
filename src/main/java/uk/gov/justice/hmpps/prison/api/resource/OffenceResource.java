package uk.gov.justice.hmpps.prison.api.resource;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.HOCodeDto;
import uk.gov.justice.hmpps.prison.api.model.OffenceDto;
import uk.gov.justice.hmpps.prison.api.model.OffenceToScheduleMappingDto;
import uk.gov.justice.hmpps.prison.api.model.StatuteDto;
import uk.gov.justice.hmpps.prison.service.reference.OffenceService;

import jakarta.validation.constraints.NotBlank;
import java.util.List;


@RestController
@Tag(name = "offences")
@RequestMapping(value = "${api.base.path}/offences", produces = "application/json")
@Validated
@AllArgsConstructor
@Slf4j
public class OffenceResource {

    private final OffenceService service;

    @GetMapping()
    @Operation(summary = "Paged List of active offences")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<OffenceDto> getActiveOffences(
        @ParameterObject @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {

        return service.getOffences(true, pageable);
    }

    @GetMapping("/all")
    @Operation(summary = "Paged List of all offences")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<OffenceDto> getOffences(
        @ParameterObject @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {

        return service.getOffences(false, pageable);
    }

    @GetMapping("/code/{offenceCode}")
    @Operation(summary = "Paged List of all offences where the offence code starts with the passed in offenceCode param")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<OffenceDto> getOffencesThatStartWith(
        @Parameter(required = true, example = "AA1256A", description = "The offence code")
        @PathVariable("offenceCode")
        final String offenceCode,
        @ParameterObject @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {
        log.info("Request received to fetch offences that start with offenceCode {}", offenceCode);
        return service.getOffencesThatStartWith(offenceCode, pageable);
    }

    @GetMapping("/ho-code")
    @Operation(summary = "Paged List of offences by HO Code")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<OffenceDto> getOffencesByHoCode(
        @Parameter(description = "HO Code", required = true, example = "825/99") @RequestParam("code") @NotBlank final String code,
        @ParameterObject @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {
        return service.findByHoCode(code, pageable);
    }

    @GetMapping("/statute")
    @Operation(summary = "Paged List of offences by Statute")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<OffenceDto> getOffencesByStatute(
        @Parameter(description = "Statute Code", required = true, example = "RR84") @RequestParam("code") @NotBlank final String code,
        @ParameterObject @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {
        return service.findByStatute(code, pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Paged List of offences matching offence description")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    public Page<OffenceDto> getOffencesByDescription(
        @Parameter(description = "Search text of the offence", required = true, example = "RR84") @RequestParam("searchText") @NotBlank final String searchText,
        @PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {
        return service.findOffences(searchText, pageable);
    }

    @PostMapping("/ho-code")
    @Operation(summary = "Create Home Office Notifiable Offence Code records if they dont already exist", description = "Requires OFFENCE_MAINTAINER role")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Home Office Notifiable Offence Codes created successfully"),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PreAuthorize("hasRole('OFFENCE_MAINTAINER') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Void> createHomeOfficeCodes(@RequestBody final List<HOCodeDto> hoCodes) {
        log.info("Request received to create Home Office Notifiable Offence Codes");
        service.createHomeOfficeCodes(hoCodes);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/statute")
    @Operation(summary = "Create statutes if they dont already exist", description = "Requires OFFENCE_MAINTAINER role")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Statutes created successfully"),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PreAuthorize("hasRole('OFFENCE_MAINTAINER') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Void> createStatute(@RequestBody final List<StatuteDto> statutes) {
        log.info("Request received to create a statutes");
        service.createStatutes(statutes);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/offence")
    @Operation(summary = "Create offences", description = "Requires OFFENCE_MAINTAINER role")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Offences created successfully"),
        @ApiResponse(responseCode = "404", description = "A dependent resource is missing (either the statute or the home office code doesnt exist)", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "409", description = "A record already exists for a passed in offence", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PreAuthorize("hasRole('OFFENCE_MAINTAINER') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Void> createOffences(@RequestBody final List<OffenceDto> offences) {
        log.info("Request received to create offences ");
        service.createOffences(offences);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/offence")
    @Operation(summary = "Update offences", description = "Requires OFFENCE_MAINTAINER role")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Offence updated successfully"),
        @ApiResponse(responseCode = "404", description = "A dependent resource is missing (either the offence or the home office code doesnt exist)", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @PreAuthorize("hasRole('OFFENCE_MAINTAINER') and hasAuthority('SCOPE_write')")
    public ResponseEntity<Void> updateOffences(@RequestBody final  List<OffenceDto> offences) {
        log.info("Request received to update offences");
        service.updateOffences(offences);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/link-to-schedule")
    @Operation(summary = "Link offence to schedule", description = "Requires UPDATE_OFFENCE_SCHEDULES role")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Offences linked to schedules successfully"),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.")
    })
    @PreAuthorize("hasRole('UPDATE_OFFENCE_SCHEDULES')")
    @ResponseStatus(HttpStatus.CREATED)
    public void linkOffencesToSchedules(@RequestBody final List<OffenceToScheduleMappingDto> offencesToSchedules) {
        log.info("Request received to link offences to schedules");
        service.linkOffencesToSchedules(offencesToSchedules);
    }

    @PostMapping("/unlink-from-schedule")
    @Operation(summary = "Unlink offence from schedule", description = "Requires UPDATE_OFFENCE_SCHEDULES role")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Offences unlinked from schedules successfully"),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.")
    })
    @PreAuthorize("hasRole('UPDATE_OFFENCE_SCHEDULES')")
    public void unlinkOffencesFromSchedules(@RequestBody final List<OffenceToScheduleMappingDto> offencesToSchedules) {
        log.info("Request received to unlink offences from schedules");
        service.unlinkOffencesFromSchedules(offencesToSchedules);
    }
}
