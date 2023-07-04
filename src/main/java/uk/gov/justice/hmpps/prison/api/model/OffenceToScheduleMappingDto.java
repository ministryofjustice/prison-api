package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Maps an offence to a schedule")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OffenceToScheduleMappingDto {
    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Offence code", example = "COML025")
    private String offenceCode;

    @NotNull
    @Schema(requiredMode = REQUIRED, description = "Schedule type", example = "SCHEDULE_15")
    private Schedule schedule;

    @JsonIgnore
    public String getStatuteCode() {
        return this.offenceCode.substring(0, 4);
    }
}
