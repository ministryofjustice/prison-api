package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "HO Code")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HOCodeDto {

    @Schema(requiredMode = REQUIRED, description = "HO code", example = "825/99")
    private String code;

    @Schema(requiredMode = REQUIRED, description = "HO code description", example = "Ho Code 825/99")
    private String description;

    @Schema(requiredMode = REQUIRED, description = "Active Y/N", example = "Y")
    private String activeFlag;

    @Schema(description = "Expiry Date", example = "2021-01-05")
    private LocalDate expiryDate;
}
