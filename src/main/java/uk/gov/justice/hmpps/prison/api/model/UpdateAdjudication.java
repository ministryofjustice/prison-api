package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.service.validation.MaximumTextSize;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Update details for an existing adjudication")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdjudication {

    @Schema(requiredMode = REQUIRED, description = "When the incident took place", example = "15-06-2020T09:03:11")
    @NotNull
    private LocalDateTime incidentTime;

    @Schema(requiredMode = REQUIRED, description = "The id to indicate where the incident took place. Note: This will be an agency's internal location id")
    @NotNull
    private Long incidentLocationId;

    @Schema(requiredMode = REQUIRED, description = "The adjudication statement", example = "The offence involved ...")
    @NotNull
    @MaximumTextSize
    @Size(max = 4000)
    private String statement;

    @Schema(description = "The list of offence codes the offender may be charged with. Note: If this value is not specified then the existing offence codes will be kept", example = "51:80,51:25A")
    private List<String> offenceCodes;

    @Schema(description = "The list of staff usernames who were victims", example = "NGK33Y")
    @Default
    private List<String> victimStaffUsernames = new ArrayList<>();

    @Schema(description = "The list of offender numbers of offenders who were victims")
    @Default
    private List<String> victimOffenderIds = new ArrayList<>();

    @Schema(description = "The list of offender numbers of the offenders who were connected")
    @Default
    private List<String> connectedOffenderIds = new ArrayList<>();
}
