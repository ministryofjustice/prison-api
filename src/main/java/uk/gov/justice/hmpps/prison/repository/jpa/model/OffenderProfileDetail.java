package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderProfileDetail.PK.class)
@Table(name = "OFFENDER_PROFILE_DETAILS")
public class OffenderProfileDetail extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {

        @Column(name = "OFFENDER_BOOK_ID", nullable = false, updatable = false, insertable = false)
        private Long bookingId;

        @Column(name = "PROFILE_TYPE", nullable = false, updatable = false, insertable = false)
        private String type;

        @Column(name = "PROFILE_SEQ", nullable = false, updatable = false, insertable = false)
        private Integer sequence;

    }

    @Id
    private Long bookingId;

    @Id
    private String type;

    @Id
    private Integer sequence;


    @Column(name = "PROFILE_CODE")
    private String code;

    @Column(name = "CASELOAD_TYPE")
    private String caseloadType;

    @Column(name = "COMMENT_TEXT")
    private String comment;

    @Column(name = "LIST_SEQ", nullable = false)
    @Builder.Default
    private Integer listSequence = 99;

}
