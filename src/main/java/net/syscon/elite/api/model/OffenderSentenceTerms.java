package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@SuppressWarnings("unused")
@ApiModel(description = "Offender Sentence start date and length for booking id")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class OffenderSentenceTerms {
    @ApiModelProperty(required = true, value = "Offender booking id.", position = 1, example = "1132400")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Start date of sentence.", position = 2, example = "2018-12-31")
    private LocalDate startDate;

    @ApiModelProperty(required = true, value = "Sentence length years.", position = 3)
    private Integer years;

    @ApiModelProperty(required = true, value = "Sentence length months.", position = 4)
    private Integer months;

    @ApiModelProperty(required = true, value = "Sentence length weeks.", position = 5)
    private Integer weeks;

    @ApiModelProperty(required = true, value = "Sentence length days.", position = 6)
    private Integer days;

    @ApiModelProperty(required = true, value = "Whether this is a life sentence.", position = 7)
    private Boolean lifeSentence;
}
