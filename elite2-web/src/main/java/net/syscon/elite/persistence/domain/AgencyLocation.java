package net.syscon.elite.persistence.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="AGENCY_LOCATION")
@SuppressWarnings("serial")
public class AgencyLocation implements Serializable {
	
	@Id
	@Column(name="AGY_LOC_ID")
	private String agencyId;
	
	@Column(name="DESCRIPTION")
	private String description;
	
	@Column(name="AGENCY_LOCATION_TYPE")
	private String agencyType;
	
	public String getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAgencyType() {
		return agencyType;
	}
	public void setAgencyType(String agencyType) {
		this.agencyType = agencyType;
	}

}

