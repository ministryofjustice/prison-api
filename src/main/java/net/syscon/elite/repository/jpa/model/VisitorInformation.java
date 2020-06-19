package net.syscon.elite.repository.jpa.model;

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
                "OCP.OFFENDER_BOOK_ID BOOKING_ID," +
                "P.FIRST_NAME," +
                "P.LAST_NAME," +
                "P.BIRTHDATE," +
                "RC1.DESCRIPTION RELATIONSHIP," +
                "VISITOR.GROUP_LEADER_FLAG LEAD_VISITOR " +
                "FROM OFFENDER_VISIT_VISITORS VISITOR " +
                "LEFT JOIN OFFENDER_CONTACT_PERSONS OCP ON VISITOR.PERSON_ID = OCP.PERSON_ID " +
                "LEFT JOIN REFERENCE_CODES RC1 ON RC1.DOMAIN = 'RELATIONSHIP' AND RC1.CODE = OCP.RELATIONSHIP_TYPE " +
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
    private Long bookingId;
    private String lastName;
    private String firstName;
    private LocalDate birthdate;
    private String leadVisitor;
    private String relationship;
}
