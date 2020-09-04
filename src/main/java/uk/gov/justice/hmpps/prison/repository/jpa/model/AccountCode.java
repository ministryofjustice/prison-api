package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ACCOUNT_CODES")
public class AccountCode {

    @Id
    @Column(name = "ACCOUNT_CODE", nullable = false, insertable = false, updatable = false)
    private Long accountCode;

    @Column(name = "CASELOAD_TYPE", nullable = false)
    private String caseLoadType;

    @Column(name = "SUB_ACCOUNT_TYPE", nullable = false)
    private String subAccountType;
}
