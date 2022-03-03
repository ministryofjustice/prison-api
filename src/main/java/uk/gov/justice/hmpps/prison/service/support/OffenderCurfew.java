package uk.gov.justice.hmpps.prison.service.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Offender Curfew")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderCurfew {

    @NotNull
    private Long offenderCurfewId;

    @NotNull
    private Long offenderBookId;

    private LocalDate assessmentDate;
    private String approvalStatus;
    private LocalDate ardCrdDate;
}
