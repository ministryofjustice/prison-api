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
@Table(name = "OFFENDER_SENT_CONDITIONS")
public class OffenderSentConditions {

    @Id
    @Column(name = "OFFENDER_SENT_CONDITION_ID")
    private Long offenderSentConditionId;

    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @Column(name = "GROOMING_FLAG")
    private String groomingFlag;


    @Column(name = "NO_WORK_WITH_UNDER_AGE")
    private String noWorkWithUnderAge;

}
