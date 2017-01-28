
package net.syscon.elite.web.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * A collection of Agencies
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "agencies"
})
public class Agencies {

    @JsonProperty("agencies")
    private List<Agency> agencies = new ArrayList<Agency>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The agencies
     */
    @JsonProperty("agencies")
    public List<Agency> getAgencies() {
        return agencies;
    }

    /**
     * 
     * @param agencies
     *     The agencies
     */
    @JsonProperty("agencies")
    public void setAgencies(List<Agency> agencies) {
        this.agencies = agencies;
    }

    public Agencies withAgencies(List<Agency> agencies) {
        this.agencies = agencies;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Agencies withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
