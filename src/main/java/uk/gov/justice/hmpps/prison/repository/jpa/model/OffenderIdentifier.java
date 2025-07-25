package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(exclude = {"offenderIdentifierPK"}, callSuper = false)
@ToString(exclude = {"offenderIdentifierPK"})
@Table(name = "OFFENDER_IDENTIFIERS")
public class OffenderIdentifier extends AuditableEntity {

    public static OffenderIdentifierBuilder builder() {
        return new OffenderIdentifierBuilder();
    }

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

    @Column(name = "ISSUED_AUTHORITY_TEXT")
    private String issuedAuthorityText;

    public LocalDateTime getCreateDatetime() {
        return super.getCreateDatetime();
    }

    public void setCreateDatetime(final LocalDateTime createDatetime) {
        super.setCreateDatetime(createDatetime);
    }

    public boolean isPnc() {
        return "PNC".equalsIgnoreCase(identifierType);
    }

    public boolean isCro() {
        return "CRO".equalsIgnoreCase(identifierType);
    }

    public static class OffenderIdentifierBuilder {
        private OffenderIdentifierPK offenderIdentifierPK;
        private Offender offender;
        private String identifierType;
        private String identifier;
        private LocalDate issuedDate;
        private Long rootOffenderId;
        private String caseloadType;
        private String issuedAuthorityText;
        private LocalDateTime createDatetime;

        OffenderIdentifierBuilder() {
        }

        public OffenderIdentifierBuilder offenderIdentifierPK(OffenderIdentifierPK offenderIdentifierPK) {
            this.offenderIdentifierPK = offenderIdentifierPK;
            return this;
        }

        public OffenderIdentifierBuilder offender(Offender offender) {
            this.offender = offender;
            return this;
        }

        public OffenderIdentifierBuilder identifierType(String identifierType) {
            this.identifierType = identifierType;
            return this;
        }

        public OffenderIdentifierBuilder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public OffenderIdentifierBuilder issuedDate(LocalDate issuedDate) {
            this.issuedDate = issuedDate;
            return this;
        }

        public OffenderIdentifierBuilder rootOffenderId(Long rootOffenderId) {
            this.rootOffenderId = rootOffenderId;
            return this;
        }

        public OffenderIdentifierBuilder caseloadType(String caseloadType) {
            this.caseloadType = caseloadType;
            return this;
        }

        public OffenderIdentifierBuilder issuedAuthorityText(String issuedAuthorityText) {
            this.issuedAuthorityText = issuedAuthorityText;
            return this;
        }

        public OffenderIdentifierBuilder createDatetime(LocalDateTime createDatetime) {
            this.createDatetime = createDatetime;
            return this;
        }

        public OffenderIdentifier build() {
            final var identifier = new OffenderIdentifier(this.offenderIdentifierPK, this.offender, this.identifierType, this.identifier, this.issuedDate, this.rootOffenderId, this.caseloadType, this.issuedAuthorityText);
            identifier.setCreateDatetime(createDatetime);
            return identifier;
        }

        public String toString() {
            return "OffenderIdentifier.OffenderIdentifierBuilder(offenderIdentifierPK=" + this.offenderIdentifierPK + ", offender=" + this.offender + ", identifierType=" + this.identifierType + ", identifier=" + this.identifier + ", issuedDate=" + this.issuedDate + ", rootOffenderId=" + this.rootOffenderId + ", caseloadType=" + this.caseloadType + ", issuedAuthorityText=" + this.issuedAuthorityText + ", createDatetime=" + this.createDatetime + ")";
        }
    }
}
