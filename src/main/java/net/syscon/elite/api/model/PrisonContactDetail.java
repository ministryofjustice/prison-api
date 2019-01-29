package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contacts Details for prison
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Contacts Details for prison")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PrisonContactDetail {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String agencyId;

    @NotBlank
    private String addressType;

    @NotBlank
    private String premise;

    @NotBlank
    private String locality;

    @NotBlank
    private String city;

    @NotBlank
    private String country;

    @NotBlank
    private String postCode;

    @NotNull
    @Builder.Default
    private List<Telephone> phones = new ArrayList<Telephone>();

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
      * Identifier of agency/prison.
      */
    @ApiModelProperty(required = true, value = "Identifier of agency/prison.")
    @JsonProperty("agencyId")
    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    /**
      * Type of address.
      */
    @ApiModelProperty(required = true, value = "Type of address.")
    @JsonProperty("addressType")
    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    /**
      * The Prison name.
      */
    @ApiModelProperty(required = true, value = "The Prison name.")
    @JsonProperty("premise")
    public String getPremise() {
        return premise;
    }

    public void setPremise(String premise) {
        this.premise = premise;
    }

    /**
      * Describes the geographic location.
      */
    @ApiModelProperty(required = true, value = "Describes the geographic location.")
    @JsonProperty("locality")
    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    /**
      * Address city.
      */
    @ApiModelProperty(required = true, value = "Address city.")
    @JsonProperty("city")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    /**
      * Address country.
      */
    @ApiModelProperty(required = true, value = "Address country.")
    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    /**
      * Address postcode.
      */
    @ApiModelProperty(required = true, value = "Address postcode.")
    @JsonProperty("postCode")
    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    /**
      * List of Telephone details
      */
    @ApiModelProperty(required = true, value = "List of Telephone details")
    @JsonProperty("phones")
    public List<Telephone> getPhones() {
        return phones;
    }

    public void setPhones(List<Telephone> phones) {
        this.phones = phones;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class PrisonContactDetail {\n");
        
        sb.append("  agencyId: ").append(agencyId).append("\n");
        sb.append("  addressType: ").append(addressType).append("\n");
        sb.append("  premise: ").append(premise).append("\n");
        sb.append("  locality: ").append(locality).append("\n");
        sb.append("  city: ").append(city).append("\n");
        sb.append("  country: ").append(country).append("\n");
        sb.append("  postCode: ").append(postCode).append("\n");
        sb.append("  phones: ").append(phones).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
