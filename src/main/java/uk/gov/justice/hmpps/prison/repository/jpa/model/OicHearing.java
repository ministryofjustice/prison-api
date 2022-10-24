package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OIC_HEARINGS")
@ToString(of = {"oicHearingId"})
public class OicHearing extends AuditableEntity {

    @Id
    @Column(name = "OIC_HEARING_ID", nullable = false)
    @SequenceGenerator(name = "OIC_HEARING_ID", sequenceName = "OIC_HEARING_ID", allocationSize = 1)
    @GeneratedValue(generator = "OIC_HEARING_ID")
    private Long oicHearingId;

    @Column(name = "OIC_INCIDENT_ID", nullable = false)
    private Long adjudicationNumber;

    /*
        //columns to map in new entity

    OIC_HEARING_ID NUMBER(10,0),
    OIC_HEARING_TYPE VARCHAR2(12),
    OIC_INCIDENT_ID NUMBER(10,0),
    SCHEDULE_DATE DATE,
    SCHEDULE_TIME DATE,
    HEARING_DATE DATE,
    HEARING_TIME DATE,
    HEARING_STAFF_ID NUMBER(10,0),
    VISIT_JUSTICE_TEXT VARCHAR2(40),
    COMMENT_TEXT VARCHAR2(240),
    TAPE_NUMBER VARCHAR2(12),

    -- moved these from bottom section seem specific
    INTERNAL_LOCATION_ID NUMBER(10,0),
    REPRESENTATIVE_TEXT VARCHAR2(240), //seems to be around who attends hearings.  not sure we capture any of this
    EVENT_STATUS VARCHAR2(12) DEFAULT 'SCH',

    //comes from AuditableEntry

    CREATE_DATETIME TIMESTAMP DEFAULT systimestamp ,
    CREATE_USER_ID VARCHAR2(32) DEFAULT USER,
    MODIFY_DATETIME TIMESTAMP,
    MODIFY_USER_ID VARCHAR2(32),

    //discover these, mix of audit and non audit fields
    ???
    EVENT_ID NUMBER(10,0),

    ??audit related not captured above
    AUDIT_TIMESTAMP TIMESTAMP,
    AUDIT_USER_ID VARCHAR2(32),
    AUDIT_MODULE_NAME VARCHAR2(65),
    AUDIT_CLIENT_USER_ID VARCHAR2(64),
    AUDIT_CLIENT_IP_ADDRESS VARCHAR2(39),
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
    AUDIT_ADDITIONAL_INFO VARCHAR2(256),
    ??

     */
}
