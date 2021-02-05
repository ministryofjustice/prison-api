package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_RESTRICTIONS")
public class OffenderRestrictions {

    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long offenderBookId;

    @Column(name = "OFFENDER_RESTRICTION_ID")
    private Long offenderRestrictionId;

    @Column(name = "RESTRICTION_TYPE")
    private String restrictionType;

    @Column(name = "COMMENT_TEXT")
    private String commentText;
}
