package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "Summary of an offender counted as Establishment Roll - In")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data

public class OffenderIn {
    @NotBlank
    @Schema(required = true, description = "Display Prisoner Number")
    private String offenderNo;

    @NotNull
    private Long bookingId;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Schema(required = true, description = "Id for Agency travelling from")
    private String fromAgencyId;

    @NotBlank
    @Schema(required = true, description = "Description for Agency travelling from")
    private String fromAgencyDescription;

    @NotBlank
    @Schema(required = true, description = "Id for Agency travelling to")
    private String toAgencyId;

    @NotBlank
    @Schema(required = true, description = "Description for Agency travelling to")
    private String toAgencyDescription;

    @NotBlank
    @Schema(description = "City offender was received from")
    private String fromCity;

    @NotBlank
    @Schema(description = "City offender was sent to")
    private String toCity;

    @NotNull
    @Schema(required = true, description = "Movement time")
    private LocalTime movementTime;

    @NotNull
    @Schema(required = true, description = "Movement date time")
    private LocalDateTime movementDateTime;

    @NotNull
    @Schema(required = true, description = "Description of the offender's (internal) location")
    private String location;

    public OffenderIn(@NotBlank String offenderNo, @NotNull Long bookingId, @NotNull LocalDate dateOfBirth, @NotBlank String firstName, String middleName, @NotBlank String lastName, @NotBlank String fromAgencyId, @NotBlank String fromAgencyDescription, @NotBlank String toAgencyId, @NotBlank String toAgencyDescription, @NotBlank String fromCity, @NotBlank String toCity, @NotNull LocalTime movementTime, @NotNull LocalDateTime movementDateTime, @NotNull String location) {
        this.offenderNo = offenderNo;
        this.bookingId = bookingId;
        this.dateOfBirth = dateOfBirth;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.fromAgencyId = fromAgencyId;
        this.fromAgencyDescription = fromAgencyDescription;
        this.toAgencyId = toAgencyId;
        this.toAgencyDescription = toAgencyDescription;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.movementTime = movementTime;
        this.movementDateTime = movementDateTime;
        this.location = location;
    }

    public OffenderIn() {}
}
