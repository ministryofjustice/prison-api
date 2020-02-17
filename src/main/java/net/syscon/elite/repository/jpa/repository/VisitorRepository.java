package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.Visitor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitorRepository extends PagingAndSortingRepository<Visitor, String> {
    @Query(value =
            "SELECT * FROM " +
                    "( SELECT P.PERSON_ID," +
                    "VISITOR.OFFENDER_VISIT_ID VISIT_ID," +
                    "P.FIRST_NAME," +
                    "P.LAST_NAME," +
                    "P.BIRTHDATE," +
                    "RC1.DESCRIPTION RELATIONSHIP," +
                    "VISITOR.GROUP_LEADER_FLAG LEAD_VISITOR " +
                    "FROM OFFENDER_VISIT_VISITORS VISITOR " +
                    "LEFT JOIN OFFENDER_CONTACT_PERSONS OCP ON VISITOR.PERSON_ID = OCP.PERSON_ID AND OCP.OFFENDER_BOOK_ID = :bookingId " +
                    "LEFT JOIN REFERENCE_CODES RC1 ON RC1.DOMAIN = 'RELATIONSHIP' AND RC1.CODE = OCP.RELATIONSHIP_TYPE " +
                    "LEFT JOIN PERSONS P ON P.PERSON_ID = VISITOR.PERSON_ID " +
                    "WHERE VISITOR.OFFENDER_VISIT_ID = :visitId)",
            nativeQuery = true)
    List<Visitor> getVisitorsForVisitAndBooking(@Param("visitId") final Long visitId, @Param("bookingId") final Long bookingId);;
}
