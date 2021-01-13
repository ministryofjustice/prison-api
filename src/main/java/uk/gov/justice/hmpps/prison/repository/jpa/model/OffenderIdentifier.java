package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(exclude = {"offenderIdentifierPK"}, callSuper = false)
@ToString(exclude = {"offenderIdentifierPK"})
@Table(name = "OFFENDER_IDENTIFIERS")
public class OffenderIdentifier extends AuditableEntity {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class OffenderIdentifierPK implements Serializable {

        @Column(name = "OFFENDER_ID", nullable = false)
        private Long offenderId;

        @Column(name = "OFFENDER_ID_SEQ", nullable = false)
        private Long offenderIdSeq;
    }

    @EmbeddedId
    private OffenderIdentifierPK offenderIdentifierPK;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_ID", insertable = false, updatable = false)
    private Offender offender;

    @Column(name = "IDENTIFIER_TYPE", nullable = false)
    private String identifierType;

    @Column(name = "IDENTIFIER", nullable = false)
    private String identifier;

    @Column(name = "ISSUED_DATE")
    private LocalDate issuedDate;

    @Column(name = "ROOT_OFFENDER_ID")
    private Long rootOffenderId;

    @Column(name = "CASELOAD_TYPE")
    private String caseloadType;

    public boolean isPnc() {
        return "PNC".equalsIgnoreCase(identifierType);
    }

    public boolean isCro() {
        return "CRO".equalsIgnoreCase(identifierType);
    }
}
