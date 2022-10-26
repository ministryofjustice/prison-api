package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;



@Data
@EqualsAndHashCode(callSuper=false)
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OIC_HEARINGS")
@ToString(of = {"oicHearingId"})
public class OicHearing extends AuditableEntity {

    public enum OicHearingStatus {
        SCH,EXP,CAN
    }

    public enum OicHearingType {
        INAD_YOI, INAD_ADULT, GOV_ADULT, GOV_YOI, GOV
    }

    @Id
    @Column(name = "OIC_HEARING_ID", nullable = false)
    @SequenceGenerator(name = "OIC_HEARING_ID", sequenceName = "OIC_HEARING_ID", allocationSize = 1)
    @GeneratedValue(generator = "OIC_HEARING_ID")
    private Long oicHearingId;

    @Column(name = "OIC_INCIDENT_ID", nullable = false)
    private Long adjudicationNumber;

    @Column(name = "HEARING_DATE", nullable = false)
    private LocalDate hearingDate;

    @Column(name = "HEARING_TIME", nullable = false)
    private LocalDateTime hearingTime;

    @Column(name = "INTERNAL_LOCATION_ID", nullable = false)
    private Long internalLocationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "OIC_HEARING_TYPE", nullable = false, length = 12)
    private OicHearingType oicHearingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "EVENT_STATUS", nullable = false, length = 12)
    private OicHearingStatus eventStatus;

}
