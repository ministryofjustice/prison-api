package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;


@ApiModel(description = "Prisoner Movement")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderMovement {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotBlank
    @ApiModelProperty(required = true, value = "Display Prisoner Number (UK is NOMS ID)")
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
    @ApiModelProperty(required = true, value = "Agency travelling from")
    private String fromAgency;

    @NotBlank
    @ApiModelProperty(required = true, value = "Description for Agency travelling from")
    private String fromAgencyDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "Agency travelling to")
    private String toAgency;

    @NotBlank
    @ApiModelProperty(required = true, value = "Description for Agency travelling to")
    private String toAgencyDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "ADM (admission), CRT (court), REL (release), TAP (temporary absence) or TRN (transfer)", example = "ADM", allowableValues = "ADM,CRT,REL,TAP,TRN")
    private String movementType;

    @NotBlank
    @ApiModelProperty(required = true, value = "Description of the movement type")
    private String movementTypeDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "Reason code for the movement")
    private String movementReason;

    @NotBlank
    @ApiModelProperty(required = true, value = "Description of the movement reason")
    private String movementReasonDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "IN or OUT")
    private String directionCode;

    @NotNull
    @ApiModelProperty(required = true, value = "Movement time")
    private LocalTime movementTime;

    @NotNull
    @ApiModelProperty(required = true, value = "Movement date")
    private LocalDate movementDate;

}
