package uk.gov.justice.hmpps.prison.api.model.digitalwarrant;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "A new sentence from a digital warrant")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sentence {
    private String sentenceType;
    private String sentenceCategory;

    private LocalDate sentenceDate;

    private Integer days;
    private Integer weeks;
    private Integer months;
    private Integer years;

    private Long offenderChargeId;
    private Long courtCaseId;
}
