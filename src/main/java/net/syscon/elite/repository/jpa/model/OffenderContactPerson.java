package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.*;

import static net.syscon.elite.repository.jpa.model.ReferenceCode.*;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_CONTACT_PERSONS")
public class OffenderContactPerson {
    @Id
    @Column(name = "OFFENDER_CONTACT_PERSON_ID", nullable = false)
    private Long contactPersonId;

    @Column(name = "OFFENDER_BOOK_ID", nullable = false)
    private Long bookingId;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + RELATIONSHIP + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "RELATIONSHIP", referencedColumnName = "code"))
    })
    private RelationshipType relationshipType;

}
