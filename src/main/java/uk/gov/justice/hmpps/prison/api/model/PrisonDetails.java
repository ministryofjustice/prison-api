package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Prison details
 **/
@Schema(description = "Prison details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PrisonDetails {

    @NotBlank
    @Schema(description = "ID of prison", example = "MDI")
    private String prisonId;

    @NotBlank
    @Schema(description = "Name of prison", example = "Moorland (HMP)")
    private String prison;
}
