package net.syscon.elite.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OffenderOutToday {
    @NotBlank
    private String directionCode;

    @NotBlank
    private String fromAgency;

    @NotBlank
    private String movementType;

    @NotBlank
    private LocalDate movementDate;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String offenderNo;

    @NotBlank
    private LocalDate birthDate;

    @NotBlank
    private LocalTime timeOut;

    private Integer facialImageId;

    private String reasonDescription;
}
