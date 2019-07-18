package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

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
