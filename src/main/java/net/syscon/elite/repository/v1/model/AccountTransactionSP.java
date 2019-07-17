package net.syscon.elite.repository.v1.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransactionSP {
    private Long txnId;
    private int txnEntrySeq;
    private LocalDate txnEntryDate;
    private String txnType;
    private String txnTypeDesc;
    private String txnEntryDesc;
    private String txnReferenceNumber;
    private BigDecimal txnEntryAmount;
}