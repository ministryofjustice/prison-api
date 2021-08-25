package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Embeddable
@Builder
@AllArgsConstructor
public class AgencyLocationCaseloadId implements Serializable {

    @Column(name = "AGY_LOC_ID")
    private String id;

    @Column(name = "CASELOAD_ID")
    private String caseload;

}
