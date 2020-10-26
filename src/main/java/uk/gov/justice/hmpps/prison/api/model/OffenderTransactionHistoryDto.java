package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@ApiModel(description = "Offender transaction details")
@Data
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OffenderTransactionHistoryDto {

    @ApiModelProperty(value = "Offender Id", example = "1", position = 1)
    private Long offenderId;

    @ApiModelProperty(value = "Transaction Id", example = "1", position = 2)
    private Long transactionId;

    @ApiModelProperty(value = "Transaction Sequence", example = "1", position = 3)
    private Long transactionEntrySequence;

    @ApiModelProperty(value = "Transaction Date", example = "2020-12-11", position = 4)
    private LocalDate entryDate;

    @ApiModelProperty(value = "Transaction Type", example = "???", position = 5)
    private String transactionType;

    @ApiModelProperty(value = "Transaction Description", example = "some textual description here", position = 6)
    private String entryDescription;

    @ApiModelProperty(value = "Transaction Reference Number", example = "???", position = 7)
    private String referenceNumber;

    @ApiModelProperty(value = "Transaction Amount", example = "60", position = 8)
    private Long penceAmount;

    @ApiModelProperty(value = "Offender Sub Account", example = "???", position = 9)
    private String accountType;
}