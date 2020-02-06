package net.syscon.elite.repository.jpa.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDERS")
public class OffenderToDelete {
    @Id
    @Column(name = "OFFENDER_ID_DISPLAY")
    private String offenderNumber;
}
