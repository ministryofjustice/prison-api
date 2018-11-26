package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Prisoner Custody Status
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Prisoner Custody Status")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Movement {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String offenderNo;

    @NotNull
    private LocalDateTime createDateTime;

    @NotBlank
    private String fromAgency;

    @NotBlank
    private String fromAgencyDescription;

    @NotBlank
    private String toAgency;

    @NotBlank
    private String toAgencyDescription;

    @NotBlank
    private String movementType;

    @NotBlank
    private String movementTypeDescription;

    @NotBlank
    private String directionCode;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Display Prisoner Number (UK is NOMS ID)
      */
    @ApiModelProperty(required = true, value = "Display Prisoner Number (UK is NOMS ID)")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
      * Timestamp when the external movement record was created
      */
    @ApiModelProperty(required = true, value = "Timestamp when the external movement record was created")
    @JsonProperty("createDateTime")
    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    /**
      * Agency travelling from
      */
    @ApiModelProperty(required = true, value = "Agency travelling from")
    @JsonProperty("fromAgency")
    public String getFromAgency() {
        return fromAgency;
    }

    public void setFromAgency(String fromAgency) {
        this.fromAgency = fromAgency;
    }

    /**
      * Description of the agency travelling from
      */
    @ApiModelProperty(required = true, value = "Description of the agency travelling from")
    @JsonProperty("fromAgencyDescription")
    public String getFromAgencyDescription() {
        return fromAgencyDescription;
    }

    public void setFromAgencyDescription(String fromAgencyDescription) {
        this.fromAgencyDescription = fromAgencyDescription;
    }

    /**
      * Agency travelling to
      */
    @ApiModelProperty(required = true, value = "Agency travelling to")
    @JsonProperty("toAgency")
    public String getToAgency() {
        return toAgency;
    }

    public void setToAgency(String toAgency) {
        this.toAgency = toAgency;
    }

    /**
      * Description of the agency travelling to
      */
    @ApiModelProperty(required = true, value = "Description of the agency travelling to")
    @JsonProperty("toAgencyDescription")
    public String getToAgencyDescription() {
        return toAgencyDescription;
    }

    public void setToAgencyDescription(String toAgencyDescription) {
        this.toAgencyDescription = toAgencyDescription;
    }

    /**
      * ADM(ission), REL(ease) or TRN(sfer)
      */
    @ApiModelProperty(required = true, value = "ADM(ission), REL(ease) or TRN(sfer)")
    @JsonProperty("movementType")
    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    /**
      * Description of the movement type
      */
    @ApiModelProperty(required = true, value = "Description of the movement type")
    @JsonProperty("movementTypeDescription")
    public String getMovementTypeDescription() {
        return movementTypeDescription;
    }

    public void setMovementTypeDescription(String movementTypeDescription) {
        this.movementTypeDescription = movementTypeDescription;
    }

    /**
      * IN or OUT
      */
    @ApiModelProperty(required = true, value = "IN or OUT")
    @JsonProperty("directionCode")
    public String getDirectionCode() {
        return directionCode;
    }

    public void setDirectionCode(String directionCode) {
        this.directionCode = directionCode;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class Movement {\n");
        
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  createDateTime: ").append(createDateTime).append("\n");
        sb.append("  fromAgency: ").append(fromAgency).append("\n");
        sb.append("  fromAgencyDescription: ").append(fromAgencyDescription).append("\n");
        sb.append("  toAgency: ").append(toAgency).append("\n");
        sb.append("  toAgencyDescription: ").append(toAgencyDescription).append("\n");
        sb.append("  movementType: ").append(movementType).append("\n");
        sb.append("  movementTypeDescription: ").append(movementTypeDescription).append("\n");
        sb.append("  directionCode: ").append(directionCode).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
