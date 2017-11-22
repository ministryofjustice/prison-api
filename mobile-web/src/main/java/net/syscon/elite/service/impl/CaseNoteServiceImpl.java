package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.CaseNoteCount;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.CaseNoteRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.validation.CaseNoteTypeSubTypeValid;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Transactional
@Service
@Validated
public class CaseNoteServiceImpl implements CaseNoteService {
	private static final String AMEND_CASE_NOTE_FORMAT = "%s ...[%s updated the case notes on %s] %s";

	@Value("${api.caseNote.sourceCode:AUTO}")
	private String caseNoteSource;

    private final CaseNoteRepository caseNoteRepository;
    private final CaseNoteTransformer transformer;
    private final BookingService bookingService;
	private final TelemetryClient telemetryClient;

    public CaseNoteServiceImpl(CaseNoteRepository caseNoteRepository, CaseNoteTransformer transformer,
							   BookingService bookingService, TelemetryClient telemetryClient) {
        this.caseNoteRepository = caseNoteRepository;
        this.transformer = transformer;
        this.bookingService = bookingService;
        this.telemetryClient = telemetryClient;
    }

    @Transactional(readOnly = true)
	@Override
	public Page<CaseNote> getCaseNotes(long bookingId, String query, LocalDate from, LocalDate to, String orderBy, Order order, long offset, long limit) {
        bookingService.verifyBookingAccess(bookingId);

		final boolean orderByBlank = StringUtils.isBlank(orderBy);
        Page<CaseNote> caseNotePage = caseNoteRepository.getCaseNotes(
				bookingId,
				query,
				from,
				to,
				orderByBlank ? "occurrenceDateTime" : orderBy,
				orderByBlank ? Order.DESC : order,
				offset,
				limit);

		List<CaseNote> transformedCaseNotes =
				caseNotePage.getItems().stream().map(transformer::transform).collect(Collectors.toList());

		return new Page<>(transformedCaseNotes, caseNotePage.getTotalRecords(), caseNotePage.getPageOffset(), caseNotePage.getPageLimit());
	}

	@Override
	@Transactional(readOnly = true)
	public CaseNote getCaseNote(final long bookingId, final long caseNoteId) {
        bookingService.verifyBookingAccess(bookingId);

		CaseNote caseNote = caseNoteRepository.getCaseNote(bookingId, caseNoteId).orElseThrow(EntityNotFoundException.withId(caseNoteId));

		return transformer.transform(caseNote);
	}

	@Override
    public CaseNote createCaseNote(long bookingId, @Valid @CaseNoteTypeSubTypeValid NewCaseNote caseNote) {
        bookingService.verifyBookingAccess(bookingId);

		//TODO: First - check Booking Id Sealed status. If status is not sealed then allow to add Case Note.
        Long caseNoteId = caseNoteRepository.createCaseNote(bookingId, caseNote, caseNoteSource);

		final CaseNote caseNoteCreated = getCaseNote(bookingId, caseNoteId);

		// Log event
		telemetryClient.trackEvent("CaseNoteCreated", ImmutableMap.of("type", caseNoteCreated.getType(), "subType", caseNoteCreated.getSubType()), null);
		return caseNoteCreated;
    }

	@Override
	public CaseNote updateCaseNote(final long bookingId, final long caseNoteId, @NotBlank(message="{caseNoteTextBlank}") @Length(max=4000, message="{caseNoteTextTooLong}") final String newCaseNoteText) {
        bookingService.verifyBookingAccess(bookingId);

        CaseNote caseNote = caseNoteRepository.getCaseNote(bookingId, caseNoteId).orElseThrow(EntityNotFoundException.withId(caseNoteId));

        String amendedText = format(AMEND_CASE_NOTE_FORMAT,
                caseNote.getText(),
                UserSecurityUtils.getCurrentUsername(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")),
                newCaseNoteText);

        caseNoteRepository.updateCaseNote(bookingId, caseNoteId, amendedText, UserSecurityUtils.getCurrentUsername());

        return getCaseNote(bookingId, caseNoteId);
	}

	@Override
	public CaseNoteCount getCaseNoteCount(long bookingId, String type, String subType, LocalDate fromDate, LocalDate toDate) {
		// Validate date range
		if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && toDate.isBefore(fromDate)) {
			throw new BadRequestException("Invalid date range: toDate is before fromDate.");
		}

		bookingService.verifyBookingAccess(bookingId);

		Long count = caseNoteRepository.getCaseNoteCount(bookingId, type, subType, fromDate, toDate);

        return CaseNoteCount.builder()
				.bookingId(bookingId)
				.type(type)
				.subType(subType)
				.fromDate(fromDate)
				.toDate(toDate)
				.count(count)
				.build();
	}

	@Override
	public List<ReferenceCode> getCaseNoteTypesByCaseLoadType(String caseLoadType) {
		return caseNoteRepository.getCaseNoteTypesByCaseLoadType(caseLoadType);
	}

	@Override
	public List<ReferenceCode> getCaseNoteTypesWithSubTypesByCaseLoadType(String caseLoadType) {
		return caseNoteRepository.getCaseNoteTypesWithSubTypesByCaseLoadType(caseLoadType);
	}
}
