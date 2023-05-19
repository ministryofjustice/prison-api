package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Update Offender Dates Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestToUpdateOffenderDates {

    @Schema(required = true, description = "UUID of the calculation performed by CRD.")
    private UUID calculationUuid;

    @Schema(description = "Timestamp when the calculation was performed")
    private LocalDateTime calculationDateTime;

    @Schema(required = true, description = "DPS/NOMIS user who submitted the calculated dates.")
    private String submissionUser;

    @Schema(required = true, description = "Key dates to be updated for the offender.")
    private OffenderKeyDates keyDates;

    @Schema(description = "Comment to be associated with the sentence calculation, if not set a default comment is used")
    private String comment;
}
