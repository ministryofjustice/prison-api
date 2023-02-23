package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PHONES")
@DiscriminatorColumn(name = "OWNER_CLASS")
@Inheritance
@EqualsAndHashCode(of = "phoneId", callSuper = false)
public abstract class Phone extends AuditableEntity {
    @Id
    @SequenceGenerator(name = "PHONE_ID", sequenceName = "PHONE_ID", allocationSize = 1)
    @GeneratedValue(generator = "PHONE_ID")
    @Column(name = "PHONE_ID", nullable = false)
    private Long phoneId;

    @Column(name = "PHONE_TYPE")
    private String phoneType;

    @Column(name = "PHONE_NO")
    private String phoneNo;

    @Column(name = "EXT_NO")
    private String extNo;

}
