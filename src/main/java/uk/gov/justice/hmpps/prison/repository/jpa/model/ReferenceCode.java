package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "REFERENCE_CODES")
@DiscriminatorColumn(name = "domain")
@Inheritance
@IdClass(ReferenceCode.Pk.class)
@EqualsAndHashCode(of = {"domain", "code"})
@ToString(of = {"domain", "code", "description"})
public abstract class ReferenceCode implements Serializable {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private String domain;
        private String code;
    }

    @Id
    @Column(insertable = false, updatable = false)
    private String domain;

    @Id
    private String code;

    private String description;

    public static String getDescriptionOrNull(final ReferenceCode referenceCode) {
        return referenceCode != null ? referenceCode.getDescription() : null;
    }

    public static String getCodeOrNull(final ReferenceCode referenceCode) {
        return referenceCode != null ? referenceCode.getCode() : null;
    }
}
