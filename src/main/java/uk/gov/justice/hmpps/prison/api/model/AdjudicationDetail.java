package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "AdjudicationDetail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class AdjudicationDetail {
    @Schema(description = "Adjudication number", example = "123")
    @NotNull
    private Long adjudicationNumber;

    @Schema(description = "The staff id of the reporter", example = "123456")
    @NotNull
    private Long reporterStaffId;

    @Schema(description = "Booking number", example = "123456")
    @NotNull
    private Long bookingId;

    @Schema(description = "Offender number (NOMS ID)", example = "G3878UK")
    @NotNull
    private String offenderNo;

    @Schema(description = "The id of the agency related to this incident", example = "MDI")
    @NotNull
    private String agencyId;

    @Schema(description = "When the incident took place", example = "15-06-2020T09:03:11")
    @NotNull
    private LocalDateTime incidentTime;

    @Schema(description = "The id to indicate where the incident took place. Note: This will be an agency's internal location id")
    @NotNull
    private Long incidentLocationId;

    @Schema(description = "The adjudication statement", example = "The offence involved ...")
    @NotNull
    private String statement;

    @Schema(description = "The list of offence codes the offender may be charged with", example = "51:80,51:25A")
    private List<String> offenceCodes;

    @Schema(description = "The id of the user the created the adjudication", example = "ASMITH")
    private String createdByUserId;

    @Schema(description = "The list of staff ids who were victims", example = "[17381, 17515]")
    private List<Long> victimStaffIds;

    @Schema(description = "The list of offender numbers of offenders who were victims")
    private List<String> victimOffenderIds;

    @Schema(description = "The list of offender numbers of the offenders who were connected")
    private List<String> connectedOffenderIds;
}
