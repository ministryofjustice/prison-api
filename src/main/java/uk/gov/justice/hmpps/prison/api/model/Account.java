package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static uk.gov.justice.hmpps.prison.util.MoneySupport.MoneyDeserializer;

/**
 * Prisoner Account Balance
 **/
@ApiModel(description = "Prisoner Account Balance")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class Account {

    @ApiModelProperty(required = true, value = "Spends sub account balance.")
    @NotNull
    private BigDecimal spends;

    @ApiModelProperty(required = true, value = "Cash sub account balance.")
    @NotNull
    private BigDecimal cash;

    @ApiModelProperty(required = true, value = "Saves sub account balance.")
    @NotNull
    private BigDecimal savings;

    @ApiModelProperty(required = true, value = "Damage obligation balance.")
    @NotNull
    @JsonDeserialize(using = MoneyDeserializer.class)
    private BigDecimal damageObligations;

    @ApiModelProperty(required = true, value = "Currency of these balances.")
    @NotBlank
    private String currency;
}
