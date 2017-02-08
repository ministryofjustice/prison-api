package net.syscon.elite.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("serial")
@Embeddable
public class OffenderAlertPK implements Serializable {


	@Column(name="offender_book_id")
    protected Long offenderBookId;

	@Column(name="alert_seq")
	protected Long alertSequency;

    public OffenderAlertPK() {}

    public OffenderAlertPK(Long offenderBookId, Long alertSequency) {
		this.offenderBookId = offenderBookId;
        this.alertSequency = alertSequency;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OffenderAlertPK that = (OffenderAlertPK) o;
		return Objects.equals(offenderBookId, that.offenderBookId) &&
				Objects.equals(alertSequency, that.alertSequency);
	}

	@Override
	public int hashCode() {
		return Objects.hash(offenderBookId, alertSequency);
	}
}

