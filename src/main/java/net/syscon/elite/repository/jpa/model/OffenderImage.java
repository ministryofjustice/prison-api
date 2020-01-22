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

    @Column(name = "IMAGE_OBJECT_ID")
    private Long imageObjectId;

    @Column(name = "IMAGE_OBJECT_SEQ")
    private Long imageObjectSeq;

    @Column(name = "ACTIVE_FLAG")
    private String activeFlag;

    @Column(name = "CREATE_DATETIME")
    private Timestamp createDatetime;

    @Column(name = "CREATE_USER_ID")
    private String createUserId;

    @Column(name = "MODIFY_DATETIME")
    private Timestamp modifyDatetime;

    @Column(name = "MODIFY_USER_ID")
    private String modifyUserId;

    @Column(name = "AUDIT_TIMESTAMP")
    private Timestamp auditTimestamp;

    @Column(name = "AUDIT_USER_ID")
    private String auditUserId;

    @Column(name = "AUDIT_MODULE_NAME")
    private String auditModuleName;

    @Column(name = "AUDIT_CLIENT_USER_ID")
    private String auditClientUserId;

    @Column(name = "AUDIT_CLIENT_IP_ADDRESS")
    private String auditClientIpAddress;

    @Column(name = "AUDIT_CLIENT_WORKSTATION_NAME")
    private String auditClientWorkstationName;

    @Column(name = "AUDIT_ADDITIONAL_INFO")
    private String auditAdditionalInfo;

    @Column(name = "IMAGE_SOURCE_CODE")
    private String imageSourceCode;
}
