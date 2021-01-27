package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

import static org.hibernate.annotations.NotFoundAction.IGNORE;


@Entity
@Table(name = "OFFENDER_LANGUAGES")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@IdClass(OffenderLanguage.PK.class)
public class OffenderLanguage {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        @Column(insertable = false, updatable = false)
        private Long offenderBookId;

        @Column(name = "LANGUAGE_TYPE")
        private String type;

        @Column(insertable = false, updatable = false, name = "LANGUAGE_CODE")
        private String code;
    }

    @Id
    private Long offenderBookId;

    @Id
    private String type;

    @Id
    private String code;

    private String readSkill;
    private String writeSkill;
    private String speakSkill;
    private String interpreterRequestedFlag;

    @Column(name = "PREFERED_WRITE_FLAG")
    private String preferredWriteFlag;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + LanguageReferenceCode.DOMAIN + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "LANGUAGE_CODE", referencedColumnName = "code"))
    })
    private LanguageReferenceCode referenceCode;
}
