package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "AdjudicationCreationRequest")
@JsonInclude(Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class AdjudicationCreationRequestData {
    @Schema(description = "Adjudication number", example = "123")
    @NotNull
    private Long adjudicationNumber;

    @Schema(description = "Booking number", example = "123456")
    @NotNull
    private Long bookingId;
}
