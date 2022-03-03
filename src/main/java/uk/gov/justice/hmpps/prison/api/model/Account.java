package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
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
@Schema(description = "Prisoner Account Balance")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class Account {

    @Schema(required = true, description = "Spends sub account balance.")
    @NotNull
    @JsonDeserialize(using = MoneyDeserializer.class)
    private BigDecimal spends;

    @Schema(required = true, description = "Cash sub account balance.")
    @NotNull
    @JsonDeserialize(using = MoneyDeserializer.class)
    private BigDecimal cash;

    @Schema(required = true, description = "Saves sub account balance.")
    @NotNull
    @JsonDeserialize(using = MoneyDeserializer.class)
    private BigDecimal savings;

    @Schema(required = true, description = "Damage obligation balance.")
    @NotNull
    @JsonDeserialize(using = MoneyDeserializer.class)
    private BigDecimal damageObligations;

    @Schema(required = true, description = "Currency of these balances.")
    @NotBlank
    private String currency;
}
