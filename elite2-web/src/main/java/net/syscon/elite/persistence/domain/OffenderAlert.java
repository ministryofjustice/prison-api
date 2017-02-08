package net.syscon.elite.persistence.domain;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="offender_alert")
@SuppressWarnings("serial")
public class OffenderAlert {

	@EmbeddedId
	private OffenderAlertPK primaryKey;

	@Column(name="alert_type")
	private String alertType;

	@Column(name="alert_status")
	private String alertStatus;

	public OffenderAlertPK getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(OffenderAlertPK primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public String getAlertStatus() {
		return alertStatus;
	}

	public void setAlertStatus(String alertStatus) {
		this.alertStatus = alertStatus;
	}
}

