package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class FreeTextMatch {

    @Id
    @Column(name = "TABLE_NAME")
    private String tableName;
}
