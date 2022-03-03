package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Payment Response")
@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class PaymentResponse {
    @ApiModelProperty(value = "Message returned from a payment", example = "Payment accepted")
    @NotNull
    private String message;
}
