package uk.gov.justice.hmpps.nomis.prison.datacompliance.repository.jpa.model;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(exclude = {"offenderIdentifierPK", "offenderAlias"}, callSuper = false)
@ToString(exclude = {"offenderIdentifierPK", "offenderAlias"})
@Table(name = "OFFENDER_IDENTIFIERS")
public class OffenderIdentifierPendingDeletion {

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
    private OffenderAliasPendingDeletion offenderAlias;

    @Column(name = "IDENTIFIER_TYPE", nullable = false)
    private String identifierType;

    @Column(name = "IDENTIFIER", nullable = false)
    private String identifier;

    public boolean isPnc() {
        return "PNC".equalsIgnoreCase(identifierType);
    }

    public boolean isCro() {
        return "CRO".equalsIgnoreCase(identifierType);
    }
}
