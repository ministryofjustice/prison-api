package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;

@ApiModel(description = "Statute")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StatuteDto {
    @ApiModelProperty(required = true, value = "Statute code", example = "RR84", position = 1)
    private String code;

    @ApiModelProperty(required = true, value = "Statute code description", example = "Statute RV98", position = 2)
    private String description;

    @ApiModelProperty(required = true, value = "Legislating Body Code", position = 3)
    private String legislatingBodyCode;

    @ApiModelProperty(required = true, value = "Active Y/N", example = "Y", position = 4)
    private ActiveFlag activeFlag;

}
