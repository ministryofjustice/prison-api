package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
@ToString
public class OffenderContactPerson extends AuditableEntity {

    private static final String ACTIVE = "Y";

    @Id
    @Column(name = "OFFENDER_CONTACT_PERSON_ID", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    @Exclude
    private OffenderBooking offenderBooking;

    @Column(name = "PERSON_ID", nullable = false)
    private Long personId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID", updatable = false, insertable = false)
    @Exclude
    private Person person;

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


    @OneToMany
    @JoinColumn(name = "OFFENDER_CONTACT_PERSON_ID", referencedColumnName = "OFFENDER_CONTACT_PERSON_ID")
    @Exclude
    @Default
    private List<VisitorRestriction> visitorRestrictions = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "PERSON_ID", referencedColumnName = "PERSON_ID")
    @Exclude
    @Default
    private List<GlobalVisitorRestriction> globalVisitorRestrictions = new ArrayList<>();

    @Column(name = "ACTIVE_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean active;

    @Column(name = "NEXT_OF_KIN_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean nextOfKin;

    @Column(name = "EMERGENCY_CONTACT_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean emergencyContact;

    @Column(name = "APPROVED_VISITOR_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean approvedVisitor;

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
