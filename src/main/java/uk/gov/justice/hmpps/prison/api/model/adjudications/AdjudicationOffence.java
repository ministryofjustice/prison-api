package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "A type of offence that can be made as part of an adjudication")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjudicationOffence {

    @ApiModelProperty(value = "Offence Id", example = "8")
    private String id;
    @ApiModelProperty(value = "Offence Code", example = "51:7")
    private String code;
    @ApiModelProperty(value = "Offence Description", example = "Escapes or absconds from prison or from legal custody")
    private String description;
}