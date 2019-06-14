package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import lombok.*;

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

    @JsonProperty("preferred_spoken")
    private CodeDescription spokenLanguage;

    @JsonProperty("interpreter_required")
    private Boolean interpreterRequired;


}
