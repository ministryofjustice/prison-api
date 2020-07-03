package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ApiModel(description = "Account Transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AccountTransactions {

    @ApiModelProperty(value = "List of account transactions", allowEmptyValue = true)
    private List<AccountTransaction> transactions;
}
