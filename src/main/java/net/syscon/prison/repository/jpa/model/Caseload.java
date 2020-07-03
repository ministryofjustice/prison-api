package net.syscon.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CASELOADS")
@Data()
@NoArgsConstructor
@AllArgsConstructor
public class Caseload {

    @Id()
    @Column(name = "CASELOAD_ID", nullable = false)
    private String id;

    @Column(name = "DESCRIPTION", nullable = false)
    private String name;

}
