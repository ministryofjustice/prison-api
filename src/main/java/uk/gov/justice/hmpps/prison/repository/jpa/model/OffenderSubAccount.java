package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderSubAccount.Pk.class)
@Table(name = "OFFENDER_SUB_ACCOUNTS")
public class OffenderSubAccount {

    @Id
    @Column(name = "CASELOAD_ID", nullable = false, insertable = false, updatable = false)
    private String prisonId;

    @Id
    @Column(name = "OFFENDER_ID", nullable = false, insertable = false, updatable = false)
    private Long offenderId;

    @Id
    @Column(name = "TRUST_ACCOUNT_CODE", nullable = false, insertable = false, updatable = false)
    private Long accountCode;

    @Column(name = "BALANCE", nullable = false)
    private BigDecimal balance;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Pk implements Serializable {
        private String prisonId;
        private Long offenderId;
        private Long accountCode;
    }
}
