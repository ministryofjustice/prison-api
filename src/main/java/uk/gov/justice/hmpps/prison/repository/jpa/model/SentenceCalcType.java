package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.YesNoConverter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SENTENCE_CALC_TYPES")
@IdClass(SentenceCalcType.PK.class)
public class SentenceCalcType extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private String calculationType;
        private String category;
    }

    @Id
    @Column(name = "SENTENCE_CALC_TYPE")
    private String calculationType;

    @Id
    @Column(name = "SENTENCE_CATEGORY")
    private String category;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PROGRAM_METHOD")
    private String programMethod;

    @Column(name = "FUNCTION_TYPE")
    private String functionType;

    @Column(name = "LIST_SEQ")
    private Long listSequence;

    @Column(name = "REORDER_SENTENCE_SEQ")
    private Long reorderSentenceSequence;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @Column(name = "ACTIVE_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean active;

    public boolean isAFine() {
        return Objects.equals(calculationType, "A/FINE");
    }

    public boolean isRecallType(){
        return licenceRecallTypes.contains(calculationType);
    }

    private static final Set<String> licenceRecallTypes = Set.of("LR", "LR_ORA", "LR_YOI_ORA", "LR_SEC91_ORA", "LRSEC250_ORA");

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        SentenceCalcType that = (SentenceCalcType) o;
        return getCalculationType() != null && Objects.equals(getCalculationType(), that.getCalculationType())
            && getCategory() != null && Objects.equals(getCategory(), that.getCategory());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(calculationType, category);
    }
}
