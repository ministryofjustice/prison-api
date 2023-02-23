package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "COURSE_ACTIVITIES")
public class CourseActivity extends AuditableEntity {
    @Id
    @Column(name = "CRS_ACTY_ID")
    private Long activityId;

    @Column(name = "AGY_LOC_ID")
    private String prisonId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CODE")
    private String code;

    @Column(name = "SCHEDULE_START_DATE")
    private LocalDate scheduleStartDate;

    @Column(name = "SCHEDULE_END_DATE")
    private LocalDate scheduleEndDate;
}

