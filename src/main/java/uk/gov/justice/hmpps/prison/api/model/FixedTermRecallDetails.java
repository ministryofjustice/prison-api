package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Schema(description = "Details relating to the fixed term recall on a booking")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class FixedTermRecallDetails {

    @Schema(description = "The booking id")
    private Long bookingId;

    @Schema(description = "The date the offender returned to custody")
    private LocalDate returnToCustodyDate;

    @Schema(description = "The length of the fixed term recall")
    private Integer recallLength;
}
