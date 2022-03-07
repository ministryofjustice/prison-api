package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

@Schema(description = "Offender recall return to custody date")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class ReturnToCustodyDate {

    @Schema(description = "the booking id")
    private Long bookingId;

    @Schema(description = "The date the offender returned to custody")
    private LocalDate returnToCustodyDate;
}
