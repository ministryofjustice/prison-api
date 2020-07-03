package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_IMAGES")
public class OffenderImage {
    @Id
    @Column(name = "OFFENDER_IMAGE_ID")
    private Long offenderImageId;

    @Column(name = "CAPTURE_DATETIME")
    private LocalDateTime captureDateTime;

    @Column(name = "IMAGE_VIEW_TYPE")
    private String imageViewType;

    @Column(name = "ORIENTATION_TYPE")
    private String orientationType;

    @Column(name = "IMAGE_OBJECT_TYPE")
    private String imageObjectType;

    @Column(name = "IMAGE_OBJECT_ID")
    private Long imageObjectId;
}
