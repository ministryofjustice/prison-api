package uk.gov.justice.hmpps.prison.service.transformers;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteAmendment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.util.regex.Pattern;

@Component
public class CaseNoteTransformer {

    private static final String AMEND_REGEX = "\\.\\.\\.\\[(\\w+) updated the case notes* on ([0-9-/ :]*)\\]";
    private final static Pattern AMEND_CASE_NOTE_REGEX = Pattern.compile(AMEND_REGEX);

    private final String caseNoteDateFormat;

    private final StaffUserAccountRepository staffUserAccountRepository;

    @Autowired
    public CaseNoteTransformer(final StaffUserAccountRepository staffUserAccountRepository, @Value("${caseNote.dateFormat:yyyy/MM/dd HH:mm:ss}") final String caseNoteDateFormat) {
        this.staffUserAccountRepository = staffUserAccountRepository;
        this.caseNoteDateFormat = caseNoteDateFormat;
    }

    public CaseNote transform(final CaseNote in) {
        CaseNote out = null;

        if (in != null && in.getText() != null) {
            final var caseNote = CaseNote.builder()
                    .caseNoteId(in.getCaseNoteId())
                    .bookingId(in.getBookingId())
                    .type(in.getType())
                    .subType(in.getSubType())
                    .typeDescription(in.getTypeDescription())
                    .subTypeDescription(in.getSubTypeDescription())
                    .creationDateTime(in.getCreationDateTime())
                    .occurrenceDateTime(in.getOccurrenceDateTime())
                    .source(in.getSource())
                    .text(in.getText())
                    .staffId(in.getStaffId())
                    .agencyId(in.getAgencyId())
                    .authorName(WordUtils.capitalize(StringUtils.lowerCase(in.getAuthorName())))
                    .build();

            // Now create matcher object.
            out = splitOutAmendments(in.getText(), caseNote);
        }

        return out;
    }

    public CaseNote transform(final OffenderCaseNote in) {
        CaseNote out = null;

        if (in != null && in.getCaseNoteText() != null) {
            final var caseNote = CaseNote.builder()
                .caseNoteId(in.getId())
                .bookingId(in.getOffenderBooking().getBookingId())
                .type(in.getType().getCode())
                .subType(in.getSubType().getCode())
                .typeDescription(in.getType().getDescription())
                .subTypeDescription(in.getSubType().getDescription())
                .creationDateTime(in.getCreateDatetime())
                .occurrenceDateTime(in.getOccurrenceDateTime())
                .source(in.getNoteSourceCode())
                .text(in.getCaseNoteText())
                .staffId(in.getAuthor().getStaffId())
                .agencyId(in.getAgencyLocation() != null ? in.getAgencyLocation().getId() : null)
                .authorName(WordUtils.capitalize(StringUtils.lowerCase(in.getAuthor().getFullName())))
                .build();

            // Now create matcher object.
            out = splitOutAmendments(in.getCaseNoteText(), caseNote);
        }

        return out;
    }

    private CaseNote splitOutAmendments(final String originalText, final CaseNote caseNote) {

        final var breakUp = originalText.split(AMEND_REGEX);
        var workingText = originalText;

        for (var amendmentCount = 0; amendmentCount < breakUp.length; amendmentCount++) {
            final var caseNoteText = breakUp[amendmentCount];

            if (amendmentCount == 0) {
                caseNote.setOriginalNoteText(caseNoteText.trim());
                // Careful not to use a util which treats caseNoteText as a pattern
                workingText = StringUtils.replace(workingText, caseNoteText, StringUtils.EMPTY, 1);
            } else {
                // split up string
                final var firstOcc = StringUtils.indexOf(workingText, caseNoteText);

                final var amendmentDetails = StringUtils.substring(workingText, 0, firstOcc);
                final var m = AMEND_CASE_NOTE_REGEX.matcher(amendmentDetails);
                if (m.find()) {
                    final var dateTimeOfAmendment = m.group(2);
                    final var firstAttemptDateTime = DateTimeConverter.fromStringToLocalDateTime(dateTimeOfAmendment, caseNoteDateFormat);
                    final var amendmentDateTime = firstAttemptDateTime != null ? firstAttemptDateTime : DateTimeConverter.fromStringToLocalDateTime(dateTimeOfAmendment, "dd-MM-yyyy HH:mm:ss");
                    final var caseNoteAmendment = CaseNoteAmendment.builder()
                            .additionalNoteText(caseNoteText.trim())
                            .authorName(getFullNameFromUsername(m.group(1)))
                            .authorUserId(m.group(1))
                            .creationDateTime(amendmentDateTime)
                            .build();
                    caseNote.getAmendments().add(caseNoteAmendment);
                }

                workingText = StringUtils.substring(workingText, firstOcc);
            }
        }
        return caseNote;
    }

    private String getFullNameFromUsername(final String username) {
        var authorUsername = username;
        if (StringUtils.isNotBlank(username)) {
            authorUsername = staffUserAccountRepository.findById(username).map(user ->
                WordUtils.capitalize(StringUtils.lowerCase(user.getStaff().getFullName()))).orElse(username);
        }
        return authorUsername;
    }
}
