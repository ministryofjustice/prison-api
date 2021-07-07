package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(CaseNoteSubType.CASE_NOTE_SUB_TYPE)
@NoArgsConstructor
public class CaseNoteSubType extends ReferenceCode {

    static final String CASE_NOTE_SUB_TYPE = "TASK_SUBTYPE";

    public CaseNoteSubType(final String code, final String description) {
        super(CASE_NOTE_SUB_TYPE, code, description);
    }

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(CASE_NOTE_SUB_TYPE, code);
    }
}
