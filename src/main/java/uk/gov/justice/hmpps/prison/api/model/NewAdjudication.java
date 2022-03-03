package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.service.validation.MaximumTextSize;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Creation details for a new adjudication")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewAdjudication {

    @Schema(required = true, description = "Offender number (NOMS ID)", example = "G3878UK")
    @NotBlank
    private String offenderNo;

    @Schema(required = true, description = "When the incident took place", example = "15-06-2020T09:03:11")
    @NotNull
    private LocalDateTime incidentTime;

    @Schema(required = true, description = "The id of the agency related to this incident. Note: If omitted then it will use the agency related to the incidentLocationId that is provided", example = "MDI")
    @NotNull
    private String agencyId;

    @Schema(required = true, description = "The id to indicate where the incident took place. Note: This will be an agency's internal location id")
    @NotNull
    private Long incidentLocationId;

    @Schema(required = true, description = "The adjudication statement", example = "The offence involved ...")
    @NotNull
    @MaximumTextSize
    @Size(max = 4000)
    private String statement;

    @Schema(description = "The list of offence codes the offender may be charged with", example = "51:80,51:25A")
    private List<String> offenceCodes;

    @Schema(description = "The list of staff usernames who were victims", example = "NGK33Y")
    private List<String> victimStaffUsernames = new ArrayList<>();

    @Schema(description = "The list of offender numbers of offenders who were victims")
    private List<String> victimOffenderIds = new ArrayList<>();

    @Schema(description = "The list of offender numbers of the offenders who were connected")
    private List<String> connectedOffenderIds = new ArrayList<>();
}
