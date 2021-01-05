package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "IMPRISONMENT_STATUSES")
public class ImprisonmentStatus extends AuditableEntity {

    @Id
    @Column(name = "IMPRISONMENT_STATUS_ID")
    private Long id;

    @Column(name = "IMPRISONMENT_STATUS", nullable = false)
    private String status;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "BAND_CODE", nullable = false)
    private String bandCode;

    @Column(name = "RANK_VALUE", nullable = false)
    private String rankValue;

    @Column(name = "IMPRISONMENT_STATUS_SEQ")
    private String sequence;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    private String activeFlag;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    public boolean isActive() {
        return "Y".equals(activeFlag);
    }

}