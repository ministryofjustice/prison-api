package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@EqualsAndHashCode(of = {"eventId"}, callSuper = true)
@Data
@Entity
@Table(name = "API_OFFENDER_EVENTS", schema = "API_OWNER")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OffenderEvent extends ExtendedAuditableEntity {

    @Id
    @Column(name = "API_EVENT_ID")
    private Long eventId;
    @Column(name = "EVENT_TYPE")
    private String eventType;
    @Column(name = "EVENT_TIMESTAMP")
    private Timestamp eventTimestamp;

    @Column(name = "ROOT_OFFENDER_ID")
    private Long rootOffenderId;
    @Column(name = "NOMS_ID")
    private String offenderIdDisplay;

    @Column(name = "AGY_LOC_ID")
    private String agencyLocId;

    @Column(name = "EVENT_DATA_1", length = 4000)
    private String eventData1;
    @Column(name = "EVENT_DATA_2", length = 4000)
    private String eventData2;
    @Column(name = "EVENT_DATA_3", length = 4000)
    private String eventData3;

    public String getEventData() {
        return eventData1 + StringUtils.defaultString(eventData2) + StringUtils.defaultString(eventData3);
    }

}
