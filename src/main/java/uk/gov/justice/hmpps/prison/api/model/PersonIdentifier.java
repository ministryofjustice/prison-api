package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * PersonIdentifier
 **/
@SuppressWarnings("unused")
@Schema(description = "PersonIdentifier")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
public class PersonIdentifier {
    @NotBlank
    private String identifierType;

    @NotBlank
    private String identifierValue;

    public PersonIdentifier(@NotBlank String identifierType, @NotBlank String identifierValue) {
        this.identifierType = identifierType;
        this.identifierValue = identifierValue;
    }

    public PersonIdentifier() {
    }

    /**
     * The identifier type
     */
    @Schema(required = true, description = "The identifier type")
    @JsonProperty("identifierType")
    public String getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(final String identifierType) {
        this.identifierType = identifierType;
    }

    /**
     * The most recent identifier value of that type.
     */
    @Schema(required = true, description = "The most recent identifier value of that type.")
    @JsonProperty("identifierValue")
    public String getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(final String identifierValue) {
        this.identifierValue = identifierValue;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class PersonIdentifier {\n");

        sb.append("  identifierType: ").append(identifierType).append("\n");
        sb.append("  identifierValue: ").append(identifierValue).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
