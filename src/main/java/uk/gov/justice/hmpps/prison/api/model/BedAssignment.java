package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class BedAssignment {
    private Long bookingId;
    private Long livingUnitId;
    private LocalDate assignmentDate;
    private LocalDateTime assignmentDateTime;
    private String assignmentReason;
    private LocalDate assignmentEndDate;
    private LocalDateTime assignmentEndDateTime;
    private String agencyId;
    private String description;
}
