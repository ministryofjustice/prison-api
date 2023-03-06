package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Summary of an offender 'currently out' according to Establishment Roll")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
public class OffenderOut {
    @NotBlank
    @Schema(required = true, description = "Display Prisoner Number")
    private String offenderNo;

    @NotNull
    private Long bookingId;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    @Schema(required = true, description = "The prisoner's internal location (Cell)")
    private String location;

    public OffenderOut(@NotBlank String offenderNo, @NotNull Long bookingId, @NotNull LocalDate dateOfBirth, @NotBlank String firstName, @NotBlank String lastName, @NotNull String location) {
        this.offenderNo = offenderNo;
        this.bookingId = bookingId;
        this.dateOfBirth = dateOfBirth;
        this.firstName = firstName;
        this.lastName = lastName;
        this.location = location;
    }

    public OffenderOut() {}
}
