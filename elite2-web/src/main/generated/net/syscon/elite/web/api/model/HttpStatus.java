
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
 * HttpStatus
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "httpStatus",
    "code",
    "message",
    "developerMessage",
    "moreInfo"
})
public class HttpStatus {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("httpStatus")
    private String httpStatus;
    @JsonProperty("code")
    private String code;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    private String message;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("developerMessage")
    private String developerMessage;
    @JsonProperty("moreInfo")
    private String moreInfo;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public HttpStatus() {
    }

    /**
     * 
     * @param code
     * @param developerMessage
     * @param httpStatus
     * @param message
     * @param moreInfo
     */
    public HttpStatus(String httpStatus, String code, String message, String developerMessage, String moreInfo) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.developerMessage = developerMessage;
        this.moreInfo = moreInfo;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The httpStatus
     */
    @JsonProperty("httpStatus")
    public String getHttpStatus() {
        return httpStatus;
    }

    /**
     * 
     * (Required)
     * 
     * @param httpStatus
     *     The httpStatus
     */
    @JsonProperty("httpStatus")
    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus withHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    /**
     * 
     * @return
     *     The code
     */
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    /**
     * 
     * @param code
     *     The code
     */
    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    public HttpStatus withCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The message
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * 
     * (Required)
     * 
     * @param message
     *     The message
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The developerMessage
     */
    @JsonProperty("developerMessage")
    public String getDeveloperMessage() {
        return developerMessage;
    }

    /**
     * 
     * (Required)
     * 
     * @param developerMessage
     *     The developerMessage
     */
    @JsonProperty("developerMessage")
    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public HttpStatus withDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
        return this;
    }

    /**
     * 
     * @return
     *     The moreInfo
     */
    @JsonProperty("moreInfo")
    public String getMoreInfo() {
        return moreInfo;
    }

    /**
     * 
     * @param moreInfo
     *     The moreInfo
     */
    @JsonProperty("moreInfo")
    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public HttpStatus withMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
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

    public HttpStatus withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(httpStatus).append(code).append(message).append(developerMessage).append(moreInfo).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof HttpStatus) == false) {
            return false;
        }
        HttpStatus rhs = ((HttpStatus) other);
        return new EqualsBuilder().append(httpStatus, rhs.httpStatus).append(code, rhs.code).append(message, rhs.message).append(developerMessage, rhs.developerMessage).append(moreInfo, rhs.moreInfo).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
