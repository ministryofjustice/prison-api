package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ApiModel(description = "Offender Booking Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder=true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderBooking {

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    private Map<String, Object> additionalProperties;
    
    @NotNull
    @ApiModelProperty(required = true, value = "Unique, numeric booking id.")
    private Long bookingId;

    @ApiModelProperty(value = "Booking number.")
    private String bookingNo;

    @NotBlank
    @ApiModelProperty(required = true, value = "Offender number (e.g. NOMS Number).")
    private String offenderNo;

    @NotBlank
    @ApiModelProperty(required = true, value = "Offender first name.")
    private String firstName;

    @ApiModelProperty(value = "Offender middle name.")
    private String middleName;

    @NotBlank
    @ApiModelProperty(required = true, value = "Offender last name.")
    private String lastName;

    @NotNull
    @ApiModelProperty(required = true, value = "Offender date of birth.")
    private LocalDate dateOfBirth;

    @NotNull
    @ApiModelProperty(required = true, value = "Offender's current age.")
    private Integer age;

    @NotNull
    @Builder.Default
    @ApiModelProperty(required = true, value = "List of offender's current alert types.")
    private List<String> alertsCodes = new ArrayList<String>();

    @NotNull
    @Builder.Default
    @ApiModelProperty(required = true, value = "List of offender's current alert codes.")
    private List<String> alertsDetails = new ArrayList<String>();

    @NotBlank
    @ApiModelProperty(required = true, value = "Identifier of agency that offender is associated with.")
    private String agencyId;

    @ApiModelProperty(value = "Identifier of living unit (e.g. cell) that offender is assigned to.")
    private Long assignedLivingUnitId;

    @ApiModelProperty(value = "Description of living unit (e.g. cell) that offender is assigned to.")
    private String assignedLivingUnitDesc;

    @ApiModelProperty(value = "Identifier of facial image of offender.")
    private Long facialImageId;

    @ApiModelProperty(value = "Identifier of officer (key worker) to which offender is assigned.")
    private String assignedOfficerUserId;

    @ApiModelProperty(value = "List of offender's alias names.")
    private List<String> aliases;

    @ApiModelProperty(value = "The IEP Level of the offender (UK Only)")
    private String iepLevel;

    @ApiModelProperty(value = "The Cat A/B/C/D of the offender")
    private String categoryCode;

    @ApiModelProperty(value = "The convicted status of the offender calculated")
    private String convictedStatus;

    @ApiModelProperty(hidden = true)
    private String bandCode;

    /**
     * Specialised getter which initialises the additionalProperties if it is null
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    /**
     * Specialised setter for the 'virtual' attribute convictedStatus, an interpreted value based on the bandCode value
     * The convicted status of the offender is one of 'Remand', 'Convicted' or null
     */
    public String getConvictedStatus() {
        if (this.bandCode != null) {
            return Integer.valueOf(this.bandCode) <= 8 ? "Convicted" : "Remand";
        }
        return null;
    }
}