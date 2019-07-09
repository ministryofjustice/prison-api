package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PssCaseDetailsData {

    @ApiModelProperty(value = "Personal officer assigned", name = "personal_officer", example = "Thornhill,Graham")
    @JsonProperty("personal_officer")
    private String personalOfficer;
}