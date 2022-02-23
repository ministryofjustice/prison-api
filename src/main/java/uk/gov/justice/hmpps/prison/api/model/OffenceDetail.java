package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "Offence Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenceDetail {

    @Schema(required = true, description = "Prisoner booking id", example = "1123456")
    @NotNull
    private Long bookingId;

    @Schema(required = true, description = "Description of offence")
    @NotBlank
    private String offenceDescription;

    @Schema(required = true, description = "Reference Code", example = "RR84070")
    @NotBlank
    private String offenceCode;

    @Schema(required = true, description = "Statute code", example = "RR84")
    @NotBlank
    private String statuteCode;
}
