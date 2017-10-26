package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.CaseNoteRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Transactional
@Service
public class CaseNoteServiceImpl implements CaseNoteService {
	private static final String DATE_TO_LTEQ_QUERY_TERM = "occurrenceDateTime:lteq:";
	private static final String DATE_TO_LT_QUERY_TERM = "occurrenceDateTime:lt:";

	@Value("${api.caseNote.sourceCode:AUTO}")
	private String caseNoteSource;

    private final static String AMEND_CASE_NOTE_FORMAT = "%s ...[%s updated the case notes on %s] %s";

    private final CaseNoteRepository caseNoteRepository;
    private final CaseNoteTransformer transformer;
    private final BookingService bookingService;

    @Autowired
    public CaseNoteServiceImpl(CaseNoteRepository caseNoteRepository, CaseNoteTransformer transformer,
            BookingService bookingService) {
        super();
        this.caseNoteRepository = caseNoteRepository;
        this.transformer = transformer;
        this.bookingService = bookingService;
    }

    @Transactional(readOnly = true)
	@Override
	public List<CaseNote> getCaseNotes(long bookingId, String query, String orderBy, Order order, long offset, long limit) {
        bookingService.verifyBookingAccess(bookingId);
        String colSort = orderBy;
		if (StringUtils.isBlank(orderBy)) {
			colSort = "occurrenceDateTime";
			order = Order.DESC;
		}

		String processedQuery = processQuery(query);
		final List<CaseNote> caseNotes = caseNoteRepository.getCaseNotes(bookingId, processedQuery, colSort, order, offset, limit);
		return caseNotes.stream().map(cn -> transformer.transform(cn)).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public CaseNote getCaseNote(final long bookingId, final long caseNoteId) {
        bookingService.verifyBookingAccess(bookingId);
		final CaseNote caseNote = caseNoteRepository.getCaseNote(bookingId, caseNoteId).orElseThrow(new EntityNotFoundException(String.valueOf(caseNoteId)));
		return transformer.transform(caseNote);
	}

	@Override
	public CaseNote createCaseNote(final long bookingId, final NewCaseNote caseNote) {
        bookingService.verifyBookingAccess(bookingId);
		//TODO: First - check Booking Id Sealed status. If status is not sealed then allow to add Case Note.
        final Long caseNoteId = caseNoteRepository.createCaseNote(bookingId, caseNote, caseNoteSource);
        return getCaseNote(bookingId, caseNoteId);

	}

	@Override
	public CaseNote updateCaseNote(final long bookingId, final long caseNoteId, final String newCaseNoteText) {

        bookingService.verifyBookingAccess(bookingId);
        final CaseNote caseNote = caseNoteRepository.getCaseNote(bookingId, caseNoteId).orElseThrow(new EntityNotFoundException(String.valueOf(caseNoteId)));
        final String amendedText = format(AMEND_CASE_NOTE_FORMAT,
                caseNote.getText(),
                UserSecurityUtils.getCurrentUsername(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")),
                newCaseNoteText);

        caseNoteRepository.updateCaseNote(bookingId, caseNoteId, amendedText, UserSecurityUtils.getCurrentUsername());
        return getCaseNote(bookingId, caseNoteId);
	}

	// This handles a query which includes an inclusive 'date to' element of a date range filter being used to retrieve
    // case notes based on the occurrenceDateTime (OFFENDER_CASE_NOTES.CONTACT_TIME) falling on or between two dates
    // (inclusive date from and date to elements included) or being on or before a specified date (inclusive date to
    // element only). By inclusive 'date to', we mean that the query string incorporates this pattern:
    //
    //   occurrenceDateTime:lteq:YYYY-MM-DD
    //
    // As the CONTACT_TIME field is a TIMESTAMP (i.e. includes a time component), a clause which performs a '<='
    // comparison between CONTACT_TIME and the provided 'date to' value will not evaluate to 'true' for CONTACT_TIME
    // values on the same day as the 'date to' value. Due to constraints imposed by the dynamic query building
    // implementation within the API, it is not possible to modify the query to TRUNC(CONTACT_TIME) or to append a time
    // component (e.g. 23:59:59) to the provided 'date to' value (without requiring significant rework).
    //
    // Instead, this processing step has been introduced to detect an inclusive 'date to' element within the query
    // string, extract it, add one day to the provided 'date to' value and replace it with an exclusive 'date to'
    // element. For example, if the query string included:
    //
    //   occurrenceDateTime:lteq:2017-04-11
    //
    // it will be replaced with:
    //
    //   occurrenceDateTime:lt:2017-04-12
    //
    // This approach avoids extensive rework to the dynamic query building implementation and ensures all eligible case
    // notes are returned.
    //
	private String processQuery(String query) {
		String processedQuery;

		int dateToIdx = StringUtils.indexOf(query, DATE_TO_LTEQ_QUERY_TERM);

		if (dateToIdx >= 0) {
			int posToDateStr = query.indexOf(DATE_TO_LTEQ_QUERY_TERM);
			String toDateStr = StringUtils.substringBetween(StringUtils.substringAfter(query, DATE_TO_LTEQ_QUERY_TERM), "'");
			String restOfQuery = query.substring(posToDateStr + DATE_TO_LTEQ_QUERY_TERM.length() + toDateStr.length() + 2);

			LocalDate toDate = LocalDate.parse(toDateStr);

			toDate = toDate.plusDays(1);

			toDateStr = DateTimeFormatter.ISO_LOCAL_DATE.format(toDate);

			processedQuery = String.format("%s%s'%s'%s", query.substring(0, posToDateStr), DATE_TO_LT_QUERY_TERM, toDateStr, restOfQuery);
		} else {
			processedQuery = query;
		}

		return processedQuery;
	}
}
