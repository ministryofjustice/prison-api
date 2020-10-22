package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;

import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

@ApiModel(description = "Offender transaction details")
@Data
@EqualsAndHashCode
@ToString
@Builder
public class OffenderTransactionHistoryDto {

    @ApiModelProperty(value = "Transaction Id")
    private Long transactionId;
    @ApiModelProperty(value = "Transaction Sequence")
    private Long transactionEntrySequence;
    @ApiModelProperty(value = "Offender Id")
    private Long offenderId;
    @ApiModelProperty(value = "Transaction Date")
    private LocalDate entryDate;
    @ApiModelProperty(value = "Transaction Type")
    private String transactionType;
    @ApiModelProperty(value = "Transaction Description")
    private String entryDescription;
    @ApiModelProperty(value = "Transaction Reference Number")
    private String referenceNumber;
    @ApiModelProperty(value = "Transaction Amount")
    private BigDecimal entryAmount;
    @ApiModelProperty(value = "Transaction Type")
    private String accountType;
}