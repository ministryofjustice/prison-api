package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "Offender basic detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InmateBasicDetails {

    @NotNull
    private Long bookingId;

    @NotBlank
    private String bookingNo;

    @NotBlank
    private String offenderNo;

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String agencyId;

    private Long assignedLivingUnitId;

    @NotNull
    private LocalDate dateOfBirth;
}
