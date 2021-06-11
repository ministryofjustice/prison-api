package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;

import java.time.LocalDate;

@ApiModel(description = "HO Code")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HOCodeDto {

    @ApiModelProperty(required = true, value = "HO code", example = "825/99", position = 1)
    private String code;

    @ApiModelProperty(required = true, value = "HO code description", example = "Ho Code 825/99", position = 2)
    private String description;

    @ApiModelProperty(required = true, value = "Active Y/N", example = "Y", position = 3)
    private ActiveFlag activeFlag;

    @ApiModelProperty(value = "Expiry Date", example = "2021-01-05", position = 4)
    private LocalDate expiryDate;
}
