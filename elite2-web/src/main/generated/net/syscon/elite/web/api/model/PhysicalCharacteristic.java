
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


/**
 * physicalCharacteristic
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "characteristic",
    "detail"
})
public class PhysicalCharacteristic {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("characteristic")
    private String characteristic;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("detail")
    private String detail;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public PhysicalCharacteristic() {
    }

    /**
     * 
     * @param detail
     * @param characteristic
     */
    public PhysicalCharacteristic(String characteristic, String detail) {
        this.characteristic = characteristic;
        this.detail = detail;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The characteristic
     */
    @JsonProperty("characteristic")
    public String getCharacteristic() {
        return characteristic;
    }

    /**
     * 
     * (Required)
     * 
     * @param characteristic
     *     The characteristic
     */
    @JsonProperty("characteristic")
    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    public PhysicalCharacteristic withCharacteristic(String characteristic) {
        this.characteristic = characteristic;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The detail
     */
    @JsonProperty("detail")
    public String getDetail() {
        return detail;
    }

    /**
     * 
     * (Required)
     * 
     * @param detail
     *     The detail
     */
    @JsonProperty("detail")
    public void setDetail(String detail) {
        this.detail = detail;
    }

    public PhysicalCharacteristic withDetail(String detail) {
        this.detail = detail;
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

    public PhysicalCharacteristic withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(characteristic).append(detail).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PhysicalCharacteristic) == false) {
            return false;
        }
        PhysicalCharacteristic rhs = ((PhysicalCharacteristic) other);
        return new EqualsBuilder().append(characteristic, rhs.characteristic).append(detail, rhs.detail).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
