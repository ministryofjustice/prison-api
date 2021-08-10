package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_IMAGES")
public class OffenderImage extends AuditableEntity {
    @Id
    @Column(name = "OFFENDER_IMAGE_ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "CAPTURE_DATETIME")
    private LocalDateTime captureDateTime;

    @Column(name = "IMAGE_VIEW_TYPE")
    private String viewType;

    @Column(name = "ORIENTATION_TYPE")
    private String orientationType;

    @Column(name = "IMAGE_OBJECT_TYPE")
    private String imageType;

    @Column(name = "IMAGE_OBJECT_ID")
    private Long imageObjectId;

    @Column(name = "IMAGE_SOURCE_CODE")
    private String sourceCode;

    @Column(name = "IMAGE_OBJECT_SEQ")
    private Long sequence;

    @Column(name = "ACTIVE_FLAG")
    @Enumerated(EnumType.STRING)
    private ActiveFlag activeFlag;

    @Column(name = "FULL_SIZE_IMAGE")
    private byte[] fullSizeImage;

    @Column(name = "THUMBNAIL_IMAGE")
    private byte[] thumbnailImage;

    public ImageDetail transform() {
        return ImageDetail.builder()
            .imageId(getId())
            .captureDate(getCaptureDateTime().toLocalDate())
            .imageView(getViewType())
            .imageOrientation(getOrientationType())
            .imageType(getImageType())
            .objectId(getImageObjectId())
            .build();
    }
}
