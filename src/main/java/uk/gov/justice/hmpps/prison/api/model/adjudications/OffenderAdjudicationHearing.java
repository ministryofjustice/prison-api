package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Represents an adjudication hearing at the offender level.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderAdjudicationHearing {
    private String agencyId;

    @Schema(description = "Display Prisoner Number (UK is NOMS ID)")
    private String offenderNo;

    @Schema(description = "OIC Hearing ID", example = "1985937")
    private long hearingId;

    @Schema(description = "Hearing Type", example = "Governor's Hearing Adult")
    private String hearingType;

    @Schema(description = "Hearing Time", example = "2017-03-17T08:30:00")
    private LocalDateTime startTime;

    private long internalLocationId;

    private String internalLocationDescription;

    @Schema(description = "The status of the hearing, SCH, COMP or EXP", example = "COMP")
    private String eventStatus;
}
