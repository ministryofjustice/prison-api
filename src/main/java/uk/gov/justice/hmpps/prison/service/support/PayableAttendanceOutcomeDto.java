package uk.gov.justice.hmpps.prison.service.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayableAttendanceOutcomeDto {
    @NotNull
    private Long payableAttendanceOutcomeId;
    @NotNull
    private String eventType;
    @NotNull
    private String outcomeCode;
    private boolean paid;
    private boolean authorisedAbsence;
}
