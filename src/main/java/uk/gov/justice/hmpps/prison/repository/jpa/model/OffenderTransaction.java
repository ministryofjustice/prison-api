package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderTransaction.Pk.class)
@Table(name = "OFFENDER_TRANSACTIONS")
public class OffenderTransaction {

    @Id
    @Column(name = "TXN_ID", nullable = false, insertable = false, updatable = false)
    private Long transactionId;

    @Id
    @Column(name = "TXN_ENTRY_SEQ", nullable = false, insertable = false, updatable = false)
    private Long transactionEntrySequence;

    @Column(name = "CASELOAD_ID", nullable = false)
    private String prisonId;

    @Column(name = "TXN_REFERENCE_NUMBER")
    private String transactionReferenceNumber;

    @Column(name = "CLIENT_UNIQUE_REF")
    private String clientUniqueRef;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private Long transactionId;
        private Long transactionEntrySequence;
    }
}
