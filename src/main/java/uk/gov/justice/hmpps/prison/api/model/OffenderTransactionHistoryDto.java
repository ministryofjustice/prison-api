package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Offender transaction details")
@Data
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OffenderTransactionHistoryDto {

    @Schema(description = "Offender Id", example = "1")
    private Long offenderId;

    @Schema(description = "Transaction Id", example = "1")
    private Long transactionId;

    @Schema(description = "Transaction Sequence", example = "1")
    private Long transactionEntrySequence;

    @Schema(description = "Transaction Date", example = "2020-12-11")
    private LocalDate entryDate;

    @Schema(description = "Transaction Type")
    private String transactionType;

    @Schema(description = "Transaction Description", example = "some textual description here")
    private String entryDescription;

    @Schema(description = "Transaction Reference Number")
    private String referenceNumber;

    @Schema(description = "Currency of these amounts.", example = "GBP")
    @NotBlank
    private String currency;

    @Schema(description = "Transaction Amount", example = "60")
    private Long penceAmount;

    @Schema(description = "Offender Sub Account", example = "savings,spends,cash")
    private String accountType;

    @Schema(description = "Posting type. Denotes the direction of money moving in or out of the account", example = "CR,DR")
    private String postingType;

    @Schema(description = "Offender number", example = "G6123VU")
    private String offenderNo;

    @Schema(description = "The place the transaction took place", example = "MDI")
    private String agencyId;

    @Schema(description = "List of related transaction details")
    @Default
    private List<RelatedTransactionDetails> relatedOffenderTransactions = new ArrayList<>();

    @Schema(description = "Balance at a point in time")
    private Long currentBalance;

    @Schema(description = "Indicates that the amount has been cleared from holding")
    private Boolean holdingCleared;

    @Schema(description = "Creation date time", example = "2020-12-11T:20:00")
    private LocalDateTime createDateTime;
}

