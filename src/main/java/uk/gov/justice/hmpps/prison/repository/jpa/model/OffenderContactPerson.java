package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.ContactType.CONTACTS;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.RelationshipType.RELATIONSHIP;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "OFFENDER_CONTACT_PERSONS")
@ToString(exclude = {"offenderBooking"})
public class OffenderContactPerson extends AuditableEntity {

    private static final String ACTIVE = "Y";

    @Id
    @Column(name = "OFFENDER_CONTACT_PERSON_ID", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "PERSON_ID", nullable = false)
    private Long personId;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + RELATIONSHIP + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "RELATIONSHIP_TYPE", referencedColumnName = "code"))
    })
    private RelationshipType relationshipType;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CONTACTS + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "CONTACT_TYPE", referencedColumnName = "code"))
    })
    private ContactType contactType;

    @Column(name = "ACTIVE_FLAG")
    @Type(type="yes_no")
    private boolean active;

    @Column(name = "NEXT_OF_KIN_FLAG")
    private String nextOfKinFlag;

    @Column(name = "EMERGENCY_CONTACT_FLAG")
    private String emergencyContactFlag;

    @Column(name = "COMMENT_TEXT")
    private String comment;

    @Column(name = "MODIFY_DATETIME", insertable = false, updatable = false)
    private LocalDateTime modifyDateTime;

    @Column(name = "CREATE_DATETIME", insertable = false, updatable = false)
    private LocalDateTime createDateTime;

    public LocalDateTime lastUpdatedDateTime() {
        return modifyDateTime != null ? modifyDateTime : createDateTime;
    }
}
