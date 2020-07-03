package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ApiModel(description = "Language")
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"preferred_spoken", "interpreter_required"})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Language {

    @ApiModelProperty(value = "Spoken language", name = "preferred_spoken", position = 0)
    @JsonProperty("preferred_spoken")
    private CodeDescription spokenLanguage;

    @ApiModelProperty(value = "whether an interpreter is required", name = "interpreter_required", example = "true", position = 1)
    @JsonProperty("interpreter_required")
    private Boolean interpreterRequired;


}
