package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@ApiModel(description = "Case Note Type Usage By Booking Id")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class CaseNoteUsageByBookingId {
    @ApiModelProperty(required = true, value = "Booking Id", position = 1, example = "123456")
    private Integer bookingId;

    @ApiModelProperty(required = true, value = "Case Note Type", position = 2, example = "KA")
    private String caseNoteType;

    @ApiModelProperty(required = true, value = "Case Note Sub Type", position = 3, example = "KS")
    private String caseNoteSubType;

    @ApiModelProperty(required = true, value = "Number of case notes of this type/subtype", position = 4, example = "5")
    private Integer numCaseNotes;

    @ApiModelProperty(required = true, value = "Last case note of this type", position = 5, example = "2018-12-01T14:55:23")
    private LocalDateTime latestCaseNote;
}
