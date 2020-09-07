package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderTrustAccount.Pk.class)
@Table(name = "OFFENDER_TRUST_ACCOUNTS")
public class OffenderTrustAccount {

    @Id
    @Column(name = "CASELOAD_ID", nullable = false, insertable = false, updatable = false)
    private String prisonId;

    @Id
    @Column(name = "OFFENDER_ID", nullable = false, insertable = false, updatable = false)
    private Long offenderId;

    @Id
    @Column(name = "ACCOUNT_CLOSED_FLAG", nullable = false)
    private String accountClosedFlag;

    public boolean isAccountClosed() {
        return "Y".equals(accountClosedFlag);
    }

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Pk implements Serializable {
        private String prisonId;
        private Long offenderId;
    }
}
