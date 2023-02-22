package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(CaseNoteType.CASE_NOTE_TYPE)
@NoArgsConstructor
public class CaseNoteType extends ReferenceCode {

    static final String CASE_NOTE_TYPE = "TASK_TYPE";

    public CaseNoteType(final String code, final String description) {
        super(CASE_NOTE_TYPE, code, description);
    }

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(CASE_NOTE_TYPE, code);
    }
}
