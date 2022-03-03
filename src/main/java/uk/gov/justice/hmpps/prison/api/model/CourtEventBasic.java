package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Schema(description = "Basic Summary data for a scheduled court event")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CourtEventBasic {

    @Schema(required = true, description = "Offender number (NOMS ID)", example = "G3878UK")
    private String offenderNo;

    @Schema(required = true, description = "The agency code of the court", example = "LEEDCC")
    private String court;

    @Schema(description = "The court description", example = "Leeds Crown Court")
    private String courtDescription;

    @Schema(required = true, description = "The planned date and time of the start of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2020-12-01T14:00:00")
    private LocalDateTime startTime;

    @Schema(required = true, description = "The court event subtype (from MOVE_RSN reference data)", example = "CRT")
    private String eventSubType;

    @Schema(description = "The event description", example = "Court Appearance")
    private String eventDescription;

    @Schema(description = "Whether hold ordered by the court at this hearing")
    private boolean hold;
}
