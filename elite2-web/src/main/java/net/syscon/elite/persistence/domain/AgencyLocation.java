package net.syscon.elite.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name="agency_locations")
@SuppressWarnings("serial")
public class AgencyLocation implements Serializable {
	
	@Id
	@Column(name="agy_loc_id")
	private String agencyId;
	
	@Column(name="description")
	private String description;
	
	@Column(name="agency_location_type")
	private String agencyType;


	public String getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(final String agencyId) {
		this.agencyId = agencyId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(final String description) {
		this.description = description;
	}
	public String getAgencyType() {
		return agencyType;
	}
	public void setAgencyType(final String agencyType) {
		this.agencyType = agencyType;
	}

}

