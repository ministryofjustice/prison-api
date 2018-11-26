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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Physical Attributes
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Physical Attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PhysicalAttributes {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String gender;

    @NotBlank
    private String raceCode;

    @NotBlank
    private String ethnicity;

    @NotNull
    private Integer heightFeet;

    @NotNull
    private Integer heightInches;

    @NotNull
    private BigDecimal heightMetres;

    @NotNull
    private Integer heightCentimetres;

    @NotNull
    private Integer weightPounds;

    @NotNull
    private Integer weightKilograms;

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
      * Gender
      */
    @ApiModelProperty(required = true, value = "Gender")
    @JsonProperty("gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
      * Ethnicity Code
      */
    @ApiModelProperty(required = true, value = "Ethnicity Code")
    @JsonProperty("raceCode")
    public String getRaceCode() {
        return raceCode;
    }

    public void setRaceCode(String raceCode) {
        this.raceCode = raceCode;
    }

    /**
      * Ethnicity
      */
    @ApiModelProperty(required = true, value = "Ethnicity")
    @JsonProperty("ethnicity")
    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    /**
      * Height in Feet
      */
    @ApiModelProperty(required = true, value = "Height in Feet")
    @JsonProperty("heightFeet")
    public Integer getHeightFeet() {
        return heightFeet;
    }

    public void setHeightFeet(Integer heightFeet) {
        this.heightFeet = heightFeet;
    }

    /**
      * Height in Inches
      */
    @ApiModelProperty(required = true, value = "Height in Inches")
    @JsonProperty("heightInches")
    public Integer getHeightInches() {
        return heightInches;
    }

    public void setHeightInches(Integer heightInches) {
        this.heightInches = heightInches;
    }

    /**
      * Height in Metres (to 2dp)
      */
    @ApiModelProperty(required = true, value = "Height in Metres (to 2dp)")
    @JsonProperty("heightMetres")
    public BigDecimal getHeightMetres() {
        return heightMetres;
    }

    public void setHeightMetres(BigDecimal heightMetres) {
        this.heightMetres = heightMetres;
    }

    /**
      * Height in Centimetres
      */
    @ApiModelProperty(required = true, value = "Height in Centimetres")
    @JsonProperty("heightCentimetres")
    public Integer getHeightCentimetres() {
        return heightCentimetres;
    }

    public void setHeightCentimetres(Integer heightCentimetres) {
        this.heightCentimetres = heightCentimetres;
    }

    /**
      * Weight in Pounds
      */
    @ApiModelProperty(required = true, value = "Weight in Pounds")
    @JsonProperty("weightPounds")
    public Integer getWeightPounds() {
        return weightPounds;
    }

    public void setWeightPounds(Integer weightPounds) {
        this.weightPounds = weightPounds;
    }

    /**
      * Weight in Kilograms
      */
    @ApiModelProperty(required = true, value = "Weight in Kilograms")
    @JsonProperty("weightKilograms")
    public Integer getWeightKilograms() {
        return weightKilograms;
    }

    public void setWeightKilograms(Integer weightKilograms) {
        this.weightKilograms = weightKilograms;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class PhysicalAttributes {\n");
        
        sb.append("  gender: ").append(gender).append("\n");
        sb.append("  raceCode: ").append(raceCode).append("\n");
        sb.append("  ethnicity: ").append(ethnicity).append("\n");
        sb.append("  heightFeet: ").append(heightFeet).append("\n");
        sb.append("  heightInches: ").append(heightInches).append("\n");
        sb.append("  heightMetres: ").append(heightMetres).append("\n");
        sb.append("  heightCentimetres: ").append(heightCentimetres).append("\n");
        sb.append("  weightPounds: ").append(weightPounds).append("\n");
        sb.append("  weightKilograms: ").append(weightKilograms).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
