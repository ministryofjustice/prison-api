package uk.gov.justice.hmpps.prison.api.model.v1;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ApiModel(description = "offender ID")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OffenderId {

    @ApiModelProperty(value = "ID", name = "id", example = "1234567")
    private Long id;
}
