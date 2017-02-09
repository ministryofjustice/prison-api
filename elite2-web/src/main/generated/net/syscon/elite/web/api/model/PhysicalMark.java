
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
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * physicalMark
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "type",
    "side",
    "bodyPart",
    "orientation",
    "comment"
})
public class PhysicalMark {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private String type;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("side")
    private String side;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bodyPart")
    private String bodyPart;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orientation")
    private String orientation;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("comment")
    private String comment;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    public PhysicalMark withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The side
     */
    @JsonProperty("side")
    public String getSide() {
        return side;
    }

    /**
     * 
     * (Required)
     * 
     * @param side
     *     The side
     */
    @JsonProperty("side")
    public void setSide(String side) {
        this.side = side;
    }

    public PhysicalMark withSide(String side) {
        this.side = side;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The bodyPart
     */
    @JsonProperty("bodyPart")
    public String getBodyPart() {
        return bodyPart;
    }

    /**
     * 
     * (Required)
     * 
     * @param bodyPart
     *     The bodyPart
     */
    @JsonProperty("bodyPart")
    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public PhysicalMark withBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The orientation
     */
    @JsonProperty("orientation")
    public String getOrientation() {
        return orientation;
    }

    /**
     * 
     * (Required)
     * 
     * @param orientation
     *     The orientation
     */
    @JsonProperty("orientation")
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public PhysicalMark withOrientation(String orientation) {
        this.orientation = orientation;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The comment
     */
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    /**
     * 
     * (Required)
     * 
     * @param comment
     *     The comment
     */
    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    public PhysicalMark withComment(String comment) {
        this.comment = comment;
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

    public PhysicalMark withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
