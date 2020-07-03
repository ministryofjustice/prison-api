package uk.gov.justice.hmpps.nomis.prison.datacompliance.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDERS")
public class DuplicateOffender {
    @Id
    @Column(name = "OFFENDER_ID_DISPLAY")
    private String offenderNumber;
}
