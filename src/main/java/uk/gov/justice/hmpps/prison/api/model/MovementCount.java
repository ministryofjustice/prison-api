package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Establishment roll count in and out numbers
 **/
@SuppressWarnings("unused")
@Schema(description = "Establishment roll count in and out numbers")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MovementCount {

    @NotNull
    @Schema(requiredMode = REQUIRED, description = "Number of prisoners arrived so far on given date")
    private Integer in;

    @NotNull
    @Schema(requiredMode = REQUIRED, description = "Number of prisoners that have left so far on given date")
    public Integer out;
}
