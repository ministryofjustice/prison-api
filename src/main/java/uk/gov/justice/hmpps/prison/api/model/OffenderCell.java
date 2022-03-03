package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Offender cell details")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderCell {
    @Schema(description = "The case identifier", example = "1")
    private Long id;

    @Schema(description = "Description", example = "LEI-1-1")
    private String description;

    @Schema(description = "Description", example = "LEI-1-1")
    private String userDescription;

    @Schema(description = "Capacity", example = "2")
    private Integer capacity;

    @Schema(description = "Number of occupants", example = "2")
    private Integer noOfOccupants;

    @Schema(description = "List of attributes")
    private List<OffenderCellAttribute> attributes;

}
