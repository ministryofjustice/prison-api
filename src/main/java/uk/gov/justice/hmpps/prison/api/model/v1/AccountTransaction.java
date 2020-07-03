package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@ApiModel(description = "Account Transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"id", "type", "description", "amount", "date"})
public class AccountTransaction {

    @ApiModelProperty(value = "Transaction ID", required = true, example = "204564839-3", position = 1)
    private String id;

    @ApiModelProperty(value = "The type of transaction", required = true, position = 2)
    private CodeDescription type;

    @ApiModelProperty(value = "Transaction description", example = "Transfer In Regular from caseload PVR", required = true, position = 3)
    private String description;

    @ApiModelProperty(value = "Amount in pence", example = "12345", required = true, position = 4)
    private Long amount;

    @ApiModelProperty(value = "Date of the transaction", example = "2016-10-21", required = true, position = 5)
    private LocalDate date;
}