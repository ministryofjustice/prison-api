package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Visitor")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Visitor {

    @Schema(description = "Person id of visitor")
    @JsonProperty("personId")
    @NotNull
    private Long personId;

    @Schema(description = "Last name of visitor")
    @JsonProperty("lastName")
    @NotBlank
    private String lastName;

    @Schema(description = "First name of visitor")
    @JsonProperty("firstName")
    @NotBlank
    private String firstName;

    @Schema(description = "Date of birth of visitor")
    @JsonProperty("dateOfBirth")
    @NotNull
    private LocalDate dateOfBirth;

    @Schema(description = "Flag marking the visitor as lead visitor or not (only set for visit orders)", example = "true")
    @JsonProperty("leadVisitor")
    @NotNull
    private boolean leadVisitor;

    @Schema(description = "Relationship of visitor to offender")
    @NotBlank
    private String relationship;

    @Schema(description = "Whether the visitor attended.  Defaults in NOMIS to true when the visit is created so of limited value.")
    @NotBlank
    private boolean attended;
}
