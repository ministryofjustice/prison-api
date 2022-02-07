package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity(name = "REFERENCE_CODES")
@DiscriminatorColumn(name = "domain")
@Inheritance
@IdClass(ReferenceCode.Pk.class)
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

    @Column(name = "LIST_SEQ")
    private Integer listSequence;

    @Column(name = "ACTIVE_FLAG")
    @Type(type="yes_no")
    private boolean active;

    public ReferenceCode(final String domain, final String code, final String description) {
        this(domain, code, description, 1, true);
    }

    public static String getDescriptionOrNull(final ReferenceCode referenceCode) {
        return referenceCode != null ? referenceCode.getDescription() : null;
    }

    public static String getCodeOrNull(final ReferenceCode referenceCode) {
        return referenceCode != null ? referenceCode.getCode() : null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final ReferenceCode that = (ReferenceCode) o;

        if (!Objects.equals(getDomain(), that.getDomain())) return false;
        return Objects.equals(getCode(), that.getCode());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getDomain());
        result = 31 * result + (Objects.hashCode(getCode()));
        return result;
    }
}
