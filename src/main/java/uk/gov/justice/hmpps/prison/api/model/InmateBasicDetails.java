package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Offender basic detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InmateBasicDetails {

    @Schema(description = "Offender Booking Id", example = "432132")
    @NotNull
    private Long bookingId;

    @Schema(description = "Booking Number")
    @NotBlank
    private String bookingNo;

    @Schema(required = true, description = "Offender Unique Reference", example = "A1234AA")
    @NotBlank
    private String offenderNo;

    @Schema(required = true, description = "First Name")
    @NotBlank
    private String firstName;

    @Schema(description = "Middle Name(s)")
    private String middleName;

    @Schema(required = true, description = "Last Name")
    @NotBlank
    private String lastName;

    @Schema(description = "Identifier of agency to which the prisoner is associated.")
    @NotBlank
    private String agencyId;

    @Schema(description = "Identifier of living unit (e.g. cell) that prisoner is assigned to.")
    private Long assignedLivingUnitId;

    @Schema(description = "Description of living unit (e.g. cell) that offender is assigned to.", example = "MDI-1-1-3")
    private String assignedLivingUnitDesc;

    @Schema(required = true, description = "Date of Birth of prisoner", example = "1970-03-15")
    @NotNull
    private LocalDate dateOfBirth;
}
