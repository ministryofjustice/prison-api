package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Transaction Response")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class TransactionResponse {
    @ApiModelProperty(value = "ID of created transaction", example = "6179604-1")
    @NotNull
    private String id;
}
