package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@ApiModel(description = "Basic Summary data for a scheduled court event")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CourtEventBasic {

    @ApiModelProperty(required = true, value = "Offender number (NOMS ID)", example = "G3878UK", position = 1)
    private String offenderNo;

    @ApiModelProperty(required = true, value = "The agency code of the court", example = "LEEDCC", position = 2)
    private String court;

    @ApiModelProperty(value = "The court description", example = "Leeds Crown Court", position = 3)
    private String courtDescription;

    @ApiModelProperty(required = true, value = "The planned date and time of the start of the event in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2020-12-01T14:00:00", position = 4)
    private LocalDateTime startTime;

    @ApiModelProperty(required = true, value = "The court event subtype (from MOVE_RSN reference data)", example = "CRT", position = 5)
    private String eventSubType;

    @ApiModelProperty(value = "The event description", example = "Court Appearance", position = 6)
    private String eventDescription;

    @ApiModelProperty(value = "Whether hold ordered by the court at this hearing", position = 7)
    private boolean hold;
}
