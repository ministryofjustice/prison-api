package uk.gov.justice.hmpps.prison.repository.jpa.model;

import io.swagger.annotations.ApiModelProperty;
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

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PROGRAM_SERVICES")
@ToString(of = {"programId"})
public class ProgramService {

    @Id
    @Column(name = "PROGRAM_ID")
    private Long programId;

    @Column(name = "DESCRIPTION")
    @ApiModelProperty(value = "Activity name")
    private String activity;
}
