package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Subselect;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Subselect(
        "SELECT * FROM " +
                "( SELECT P.PERSON_ID," +
                "VISITOR.OFFENDER_VISIT_ID VISIT_ID," +
                "P.FIRST_NAME," +
                "P.LAST_NAME," +
                "P.BIRTHDATE," +
                "VISITOR.GROUP_LEADER_FLAG LEAD_VISITOR " +
                "FROM OFFENDER_VISIT_VISITORS VISITOR " +
                "LEFT JOIN PERSONS P ON P.PERSON_ID = VISITOR.PERSON_ID " +
                "ORDER BY P.BIRTHDATE DESC)")
@Immutable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class VisitorInformation {
    @Id
    private Long personId;
    private Long visitId;
    private String lastName;
    private String firstName;
    private LocalDate birthdate;
    private String leadVisitor;
}
