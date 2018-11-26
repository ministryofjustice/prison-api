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
import java.util.HashMap;
import java.util.Map;

/**
 * Contact
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Contact")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Contact {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotBlank
    private String lastName;

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String contactType;

    private String contactTypeDescription;

    @NotBlank
    private String relationship;

    private String relationshipDescription;

    @NotNull
    private boolean emergencyContact;

    @NotNull
    private boolean nextOfKin;

    private Long relationshipId;

    private Long personId;

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
      * Surname
      */
    @ApiModelProperty(required = true, value = "Surname")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
      * First Name
      */
    @ApiModelProperty(required = true, value = "First Name")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
      * Middle Names
      */
    @ApiModelProperty(value = "Middle Names")
    @JsonProperty("middleName")
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
      * Contact type
      */
    @ApiModelProperty(required = true, value = "Contact type")
    @JsonProperty("contactType")
    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    /**
      * Contact type text
      */
    @ApiModelProperty(value = "Contact type text")
    @JsonProperty("contactTypeDescription")
    public String getContactTypeDescription() {
        return contactTypeDescription;
    }

    public void setContactTypeDescription(String contactTypeDescription) {
        this.contactTypeDescription = contactTypeDescription;
    }

    /**
      * Relationship to inmate
      */
    @ApiModelProperty(required = true, value = "Relationship to inmate")
    @JsonProperty("relationship")
    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    /**
      * Relationship text
      */
    @ApiModelProperty(value = "Relationship text")
    @JsonProperty("relationshipDescription")
    public String getRelationshipDescription() {
        return relationshipDescription;
    }

    public void setRelationshipDescription(String relationshipDescription) {
        this.relationshipDescription = relationshipDescription;
    }

    /**
      * Is an emergency contact
      */
    @ApiModelProperty(required = true, value = "Is an emergency contact")
    @JsonProperty("emergencyContact")
    public boolean getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(boolean emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    /**
      * Indicates that the contact is Next of Kin Type
      */
    @ApiModelProperty(required = true, value = "Indicates that the contact is Next of Kin Type")
    @JsonProperty("nextOfKin")
    public boolean getNextOfKin() {
        return nextOfKin;
    }

    public void setNextOfKin(boolean nextOfKin) {
        this.nextOfKin = nextOfKin;
    }

    /**
      * id of the relationship (internal)
      */
    @ApiModelProperty(value = "id of the relationship (internal)")
    @JsonProperty("relationshipId")
    public Long getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(Long relationshipId) {
        this.relationshipId = relationshipId;
    }

    /**
      * id of the person contact
      */
    @ApiModelProperty(value = "id of the person contact")
    @JsonProperty("personId")
    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class Contact {\n");
        
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("  middleName: ").append(middleName).append("\n");
        sb.append("  contactType: ").append(contactType).append("\n");
        sb.append("  contactTypeDescription: ").append(contactTypeDescription).append("\n");
        sb.append("  relationship: ").append(relationship).append("\n");
        sb.append("  relationshipDescription: ").append(relationshipDescription).append("\n");
        sb.append("  emergencyContact: ").append(emergencyContact).append("\n");
        sb.append("  nextOfKin: ").append(nextOfKin).append("\n");
        sb.append("  relationshipId: ").append(relationshipId).append("\n");
        sb.append("  personId: ").append(personId).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
