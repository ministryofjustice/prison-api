package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@EqualsAndHashCode(of = "staffId", callSuper = false)
@Entity
@Table(name = "STAFF_MEMBERS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Staff extends AuditableEntity {

    @Id()
    @Column(name = "STAFF_ID", nullable = false)
    private Long staffId;

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "STATUS")
    private String status;


    public String getFullName() {
        return lastName + ", " + firstName;
    }
}
