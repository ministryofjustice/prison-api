package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Statute")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StatuteDto {
    @Schema(requiredMode = REQUIRED, description = "Statute code", example = "RR84")
    private String code;

    @Schema(requiredMode = REQUIRED, description = "Statute code description", example = "Statute RV98")
    private String description;

    @Schema(requiredMode = REQUIRED, description = "Legislating Body Code", example = "UK")
    private String legislatingBodyCode;

    @Schema(requiredMode = REQUIRED, description = "Active Y/N", example = "Y")
    private String activeFlag;

}
