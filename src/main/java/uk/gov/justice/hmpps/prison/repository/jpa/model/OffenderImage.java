package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_IMAGES")
public class OffenderImage extends AuditableEntity {
    @Id
    @Column(name = "OFFENDER_IMAGE_ID")
    @SequenceGenerator(name = "OFFENDER_IMAGE_ID", sequenceName = "OFFENDER_IMAGE_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_IMAGE_ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    @Exclude
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
    @Convert(converter = YesNoConverter.class)
    private boolean active;

    @Lob
    @Column(name = "FULL_SIZE_IMAGE", columnDefinition = "BLOB")
    @Exclude
    private byte[] fullSizeImage;

    @Lob
    @Column(name = "THUMBNAIL_IMAGE", columnDefinition = "BLOB")
    @Exclude
    private byte[] thumbnailImage;

    public ImageDetail transform() {
        return ImageDetail.builder()
            .imageId(getId())
            .captureDate(getCaptureDateTime().toLocalDate())
            .captureDateTime(getCaptureDateTime())
            .createDateTime(getCreateDatetime())
            .imageView(getViewType())
            .imageOrientation(getOrientationType())
            .imageType(getImageType())
            .objectId(getImageObjectId())
            .build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final OffenderImage that = (OffenderImage) o;

        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
