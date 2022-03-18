package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Schema(description = "Information about an Offender's activities")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderActivities {
    @NotBlank
    @Schema(required = true, description = "Display Prisoner Number (UK is NOMS ID)")
    private String offenderNo;

    @Schema(description = "The current work activities")
    @NotNull
    private Page<OffenderActivitySummary> workActivities;
}
