package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Schema(description = "Language")
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"preferred_spoken", "interpreter_required"})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Language {

    @Schema(description = "Spoken language", name = "preferred_spoken")
    @JsonProperty("preferred_spoken")
    private CodeDescription spokenLanguage;

    @Schema(description = "whether an interpreter is required", name = "interpreter_required", example = "true")
    @JsonProperty("interpreter_required")
    private Boolean interpreterRequired;


}
