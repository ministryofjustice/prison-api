package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
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
    private Long bookingId;
    private LocalDate startDate;
    private Integer years;
    private Integer months;
    private Integer weeks;
    private Integer days;
    private boolean lifeSentence;
}
