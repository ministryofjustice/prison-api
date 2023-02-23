package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Person
 **/
@SuppressWarnings("unused")
@Schema(description = "Person")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class Person {
    @NotNull
    private Long personId;

    @NotBlank
    private String lastName;

    @NotBlank
    private String firstName;

    public Person(@NotNull Long personId, @NotBlank String lastName, @NotBlank String firstName) {
        this.personId = personId;
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public Person() {
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
