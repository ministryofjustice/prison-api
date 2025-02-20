package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMark;
import uk.gov.justice.hmpps.prison.api.model.DistinguishingMarkImageDetail;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static jakarta.persistence.FetchType.EAGER;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@IdClass(OffenderIdentifyingMark.PK.class)
@Entity
@Table(name = "OFFENDER_IDENTIFYING_MARKS")
public class OffenderIdentifyingMark extends AuditableEntity {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        @Column(name = "OFFENDER_BOOK_ID", updatable = false, insertable = false, nullable = false)
        private Long bookingId;
        @Column(name = "ID_MARK_SEQ", updatable = false, insertable = false, nullable = false)
        private Integer sequenceId;
    }

    @Id
    private Long bookingId;
    @Id
    private Integer sequenceId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", insertable = false, updatable = false, nullable = false)
    @Exclude
    private OffenderBooking offenderBooking;

    @Column(name = "BODY_PART_CODE")
    private String bodyPart;

    @Column(name = "MARK_TYPE")
    private String markType;

    @Column(name = "SIDE_CODE")
    private String side;

    @Column(name = "PART_ORIENTATION_CODE")
    private String partOrientation;

    @Column(name = "COMMENT_TEXT", length = 240)
    private String commentText;

    @OneToMany(mappedBy = "mark", fetch = EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Default
    private List<OffenderImage> images = new ArrayList<>();

    public DistinguishingMark transform() {
        var latestImageId = images.stream()
            .filter(OffenderImage::isActive)
            .max(Comparator.comparingLong(OffenderImage::getId))
            .map(OffenderImage::getId)
            .orElse(-1L);
        var imageInfo = images.stream()
            .filter(OffenderImage::isActive)
            .map(it -> new DistinguishingMarkImageDetail(it.getId(), latestImageId.equals(it.getId())))
            .toList();

        return new DistinguishingMark(
            sequenceId,
            bookingId,
            offenderBooking.getOffender().getNomsId(),
            bodyPart,
            markType,
            side,
            partOrientation,
            commentText,
            getCreateDatetime(),
            getCreateUserId(),
            imageInfo
        );
    }
}
