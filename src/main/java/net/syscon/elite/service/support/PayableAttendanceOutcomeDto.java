package net.syscon.elite.service.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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
