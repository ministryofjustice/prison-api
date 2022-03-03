package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Schema(description = "IEP Level")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class IepLevel {
    @Schema(description = "The IEP level. Must be one of the reference data values in domain 'IEP_LEVEL'. Typically BAS (Basic), ENH (Enhanced), ENT (Entry) or STD (Standard)", required = true, example = "BAS")
    @NotBlank(message = "The IEP level must not be blank")
    private String iepLevel;

    @Schema(description = "A long description of the IEP level value", required = true, example = "Basic")
    @NotBlank(message = "The IEP description must not be blank")
    private String iepDescription;

    @Schema(description = "Sequence Order of IEP", example = "1")
    private Integer sequence;
}
