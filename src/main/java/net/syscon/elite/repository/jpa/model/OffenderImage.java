package net.syscon.elite.repository.jpa.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@Table(name = "OFFENDER_IMAGES")
public class OffenderImage {
    @Id
    @Column(name = "OFFENDER_IMAGE_ID")
    private Long offenderImageId;

    @Column(name = "OFFENDER_BOOK_ID")
    private Long offenderBookingId;

    @Column(name = "CAPTURE_DATETIME")
    private Timestamp captureDateTime;

    @Column(name = "ORIENTATION_TYPE")
    private String orientationType;

    @Column(name = "FULL_SIZE_IMAGE")
    private byte[] fullSizeImage;

    @Column(name = "THUMBNAIL_IMAGE")
    private byte[] thumbnailImage;

    @Column(name = "IMAGE_OBJECT_TYPE")
    private String imageObjectType;

    @Column(name = "IMAGE_VIEW_TYPE")
    private String imageViewType;
}
