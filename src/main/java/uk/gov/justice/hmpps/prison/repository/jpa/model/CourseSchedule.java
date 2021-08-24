package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "COURSE_SCHEDULES")
public class CourseSchedule extends AuditableEntity {
    @Id
    @Column(name = "CRS_SCH_ID")
    private double scheduleId;

    @Column(name = "SLOT_CATEGORY_CODE")
    private String slot;
}

