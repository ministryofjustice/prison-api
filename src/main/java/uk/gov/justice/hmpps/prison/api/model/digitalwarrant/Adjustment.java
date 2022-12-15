package uk.gov.justice.hmpps.prison.api.model.digitalwarrant;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "An adjustment to a calculation")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Adjustment {

    @Schema(description = "The sequence of sentence")
    private Integer sequence;

    @Schema(description = "The type of adjustment")
    private String type;

    @Schema(description = "The from date of the adjustment")
    private LocalDate from;

    @Schema(description = "The to date of the adjustment")
    private LocalDate to;

    @Schema(description = "Days of the adjustment")
    private Integer days;
}
