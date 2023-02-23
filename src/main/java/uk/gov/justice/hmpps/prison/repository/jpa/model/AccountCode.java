package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
