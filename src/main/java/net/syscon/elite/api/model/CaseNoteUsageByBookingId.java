package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@ApiModel(description = "Case Note Type Usage By Booking Id")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class CaseNoteUsageByBookingId {
    @ApiModelProperty(required = true, value = "Booking Id", example = "123456")
    @NotBlank
    private Integer bookingId;

    @ApiModelProperty(required = true, value = "Case Note Type", position = 1, example = "KA")
    @NotBlank
    private String caseNoteType;

    @ApiModelProperty(required = true, value = "Case Note Sub Type", position = 2, example = "KS")
    @NotBlank
    private String caseNoteSubType;

    @ApiModelProperty(required = true, value = "Number of case notes of this type/subtype", position = 3, example = "5")
    @NotNull
    private Integer numCaseNotes;

    @ApiModelProperty(required = true, value = "Last case note of this type", position = 4, example = "2018-12-01T14:55:23")
    @NotNull
    private LocalDateTime latestCaseNote;
}
