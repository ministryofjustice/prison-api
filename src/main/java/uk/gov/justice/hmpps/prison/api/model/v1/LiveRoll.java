package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ApiModel(description = "Live Roll")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LiveRoll {
    @ApiModelProperty(value = "Noms Ids", allowEmptyValue = true)
    private List<String> noms_ids;
}
