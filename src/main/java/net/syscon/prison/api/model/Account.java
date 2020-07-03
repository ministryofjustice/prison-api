package net.syscon.prison.api.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Prisoner Account Balance
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Prisoner Account Balance")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Account {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    private BigDecimal spends;

    @NotNull
    private BigDecimal cash;

    @NotNull
    private BigDecimal savings;

    @NotBlank
    private String currency;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(final Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
     * Spends sub account balance.
     */
    @ApiModelProperty(required = true, value = "Spends sub account balance.")
    @JsonProperty("spends")
    public BigDecimal getSpends() {
        return spends;
    }

    public void setSpends(final BigDecimal spends) {
        this.spends = spends;
    }

    /**
     * Cash sub account balance.
     */
    @ApiModelProperty(required = true, value = "Cash sub account balance.")
    @JsonProperty("cash")
    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(final BigDecimal cash) {
        this.cash = cash;
    }

    /**
     * Saves sub account balance.
     */
    @ApiModelProperty(required = true, value = "Saves sub account balance.")
    @JsonProperty("savings")
    public BigDecimal getSavings() {
        return savings;
    }

    public void setSavings(final BigDecimal savings) {
        this.savings = savings;
    }

    /**
     * Currency of these balances.
     */
    @ApiModelProperty(required = true, value = "Currency of these balances.")
    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        sb.append("class Account {\n");

        sb.append("  spends: ").append(spends).append("\n");
        sb.append("  cash: ").append(cash).append("\n");
        sb.append("  savings: ").append(savings).append("\n");
        sb.append("  currency: ").append(currency).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
