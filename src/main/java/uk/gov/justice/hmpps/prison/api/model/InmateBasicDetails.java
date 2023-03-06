package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Offender basic detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@Builder(toBuilder = true)
@Data
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

    public InmateBasicDetails(@NotNull Long bookingId, @NotBlank String bookingNo, @NotBlank String offenderNo, @NotBlank String firstName, String middleName, @NotBlank String lastName, @NotBlank String agencyId, Long assignedLivingUnitId, String assignedLivingUnitDesc, @NotNull LocalDate dateOfBirth) {
        this.bookingId = bookingId;
        this.bookingNo = bookingNo;
        this.offenderNo = offenderNo;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.agencyId = agencyId;
        this.assignedLivingUnitId = assignedLivingUnitId;
        this.assignedLivingUnitDesc = assignedLivingUnitDesc;
        this.dateOfBirth = dateOfBirth;
    }

    public InmateBasicDetails() {
    }
}
