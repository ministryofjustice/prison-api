package uk.gov.justice.hmpps.prison.repository.v1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HoldSP {
    private Long holdNumber;
    private String clientUniqueRef;
    private String txnReferenceNumber;
    private String txnEntryDesc;
    private LocalDate txnEntryDate;
    private BigDecimal txnEntryAmount;
    private LocalDate holdUntilDate;
}
