package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Person
 **/
@SuppressWarnings("unused")
@Schema(description = "Person")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Person {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    private Long personId;

    @NotBlank
    private String lastName;

    @NotBlank
    private String firstName;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @Hidden
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * id of the person
     */
    @Schema(required = true, description = "id of the person")
    @JsonProperty("personId")
    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(final Long personId) {
        this.personId = personId;
    }

    /**
     * Surname
     */
    @Schema(required = true, description = "Surname")
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * First Name
     */
    @Schema(required = true, description = "First Name")
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class Person {\n");

        sb.append("  personId: ").append(personId).append("\n");
        sb.append("  lastName: ").append(lastName).append("\n");
        sb.append("  firstName: ").append(firstName).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
