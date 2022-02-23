package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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
    @Schema(required = true, description = "Number of prisoners arrived so far on given date")
    private Integer in;

    @NotNull
    @Schema(required = true, description = "Number of prisoners that have left so far on given date")
    public Integer out;
}
