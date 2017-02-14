
package net.syscon.elite.web.api.model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "gender",
    "ethnicity",
    "heightInches",
    "heightMeters",
    "weightPounds",
    "weightKg"
})
public class PhysicalAttributes {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("gender")
    private String gender;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ethnicity")
    private String ethnicity;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("heightInches")
    private Double heightInches;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("heightMeters")
    private Double heightMeters;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("weightPounds")
    private Double weightPounds;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("weightKg")
    private Double weightKg;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public PhysicalAttributes() {
    }

    /**
     * 
     * @param gender
     * @param ethnicity
     * @param heightMeters
     * @param weightPounds
     * @param heightInches
     * @param weightKg
     */
    public PhysicalAttributes(String gender, String ethnicity, Double heightInches, Double heightMeters, Double weightPounds, Double weightKg) {
        this.gender = gender;
        this.ethnicity = ethnicity;
        this.heightInches = heightInches;
        this.heightMeters = heightMeters;
        this.weightPounds = weightPounds;
        this.weightKg = weightKg;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The gender
     */
    @JsonProperty("gender")
    public String getGender() {
        return gender;
    }

    /**
     * 
     * (Required)
     * 
     * @param gender
     *     The gender
     */
    @JsonProperty("gender")
    public void setGender(String gender) {
        this.gender = gender;
    }

    public PhysicalAttributes withGender(String gender) {
        this.gender = gender;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The ethnicity
     */
    @JsonProperty("ethnicity")
    public String getEthnicity() {
        return ethnicity;
    }

    /**
     * 
     * (Required)
     * 
     * @param ethnicity
     *     The ethnicity
     */
    @JsonProperty("ethnicity")
    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public PhysicalAttributes withEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The heightInches
     */
    @JsonProperty("heightInches")
    public Double getHeightInches() {
        return heightInches;
    }

    /**
     * 
     * (Required)
     * 
     * @param heightInches
     *     The heightInches
     */
    @JsonProperty("heightInches")
    public void setHeightInches(Double heightInches) {
        this.heightInches = heightInches;
    }

    public PhysicalAttributes withHeightInches(Double heightInches) {
        this.heightInches = heightInches;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The heightMeters
     */
    @JsonProperty("heightMeters")
    public Double getHeightMeters() {
        return heightMeters;
    }

    /**
     * 
     * (Required)
     * 
     * @param heightMeters
     *     The heightMeters
     */
    @JsonProperty("heightMeters")
    public void setHeightMeters(Double heightMeters) {
        this.heightMeters = heightMeters;
    }

    public PhysicalAttributes withHeightMeters(Double heightMeters) {
        this.heightMeters = heightMeters;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The weightPounds
     */
    @JsonProperty("weightPounds")
    public Double getWeightPounds() {
        return weightPounds;
    }

    /**
     * 
     * (Required)
     * 
     * @param weightPounds
     *     The weightPounds
     */
    @JsonProperty("weightPounds")
    public void setWeightPounds(Double weightPounds) {
        this.weightPounds = weightPounds;
    }

    public PhysicalAttributes withWeightPounds(Double weightPounds) {
        this.weightPounds = weightPounds;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The weightKg
     */
    @JsonProperty("weightKg")
    public Double getWeightKg() {
        return weightKg;
    }

    /**
     * 
     * (Required)
     * 
     * @param weightKg
     *     The weightKg
     */
    @JsonProperty("weightKg")
    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public PhysicalAttributes withWeightKg(Double weightKg) {
        this.weightKg = weightKg;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public PhysicalAttributes withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(gender).append(ethnicity).append(heightInches).append(heightMeters).append(weightPounds).append(weightKg).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PhysicalAttributes) == false) {
            return false;
        }
        PhysicalAttributes rhs = ((PhysicalAttributes) other);
        return new EqualsBuilder().append(gender, rhs.gender).append(ethnicity, rhs.ethnicity).append(heightInches, rhs.heightInches).append(heightMeters, rhs.heightMeters).append(weightPounds, rhs.weightPounds).append(weightKg, rhs.weightKg).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
