package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.CaseNoteRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.UserService;
import net.syscon.elite.service.validation.CaseNoteTypeSubTypeValid;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
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

@Service
@Validated
public class CaseNoteServiceImpl implements CaseNoteService {
	private static final String AMEND_CASE_NOTE_FORMAT = "%s ...[%s updated the case notes on %s] %s";

	@Value("${api.caseNote.sourceCode:AUTO}")
	private String caseNoteSource;

    private final CaseNoteRepository caseNoteRepository;
    private final CaseNoteTransformer transformer;
    private final UserService userService;
	private final TelemetryClient telemetryClient;

    public CaseNoteServiceImpl(CaseNoteRepository caseNoteRepository, CaseNoteTransformer transformer,
							   UserService userService, TelemetryClient telemetryClient) {
        this.caseNoteRepository = caseNoteRepository;
        this.transformer = transformer;
        this.userService = userService;
        this.telemetryClient = telemetryClient;
    }

	@Override
    @Transactional(readOnly = true)
	@VerifyBookingAccess
	public Page<CaseNote> getCaseNotes(Long bookingId, String query, LocalDate from, LocalDate to, String orderBy, Order order, long offset, long limit) {
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
	@VerifyBookingAccess
	public CaseNote getCaseNote(Long bookingId, Long caseNoteId) {
		CaseNote caseNote = caseNoteRepository.getCaseNote(bookingId, caseNoteId)
				.orElseThrow(EntityNotFoundException.withId(caseNoteId));

		return transformer.transform(caseNote);
	}

	@Override
	@Transactional
	@VerifyBookingAccess
    public CaseNote createCaseNote(Long bookingId, @Valid @CaseNoteTypeSubTypeValid NewCaseNote caseNote, String username) {
		// TODO: For Elite - check Booking Id Sealed status. If status is not sealed then allow to add Case Note.
        Long caseNoteId = caseNoteRepository.createCaseNote(bookingId, caseNote, caseNoteSource, username);

		final CaseNote caseNoteCreated = getCaseNote(bookingId, caseNoteId);

		// Log event
		telemetryClient.trackEvent("CaseNoteCreated", ImmutableMap.of("type", caseNoteCreated.getType(), "subType", caseNoteCreated.getSubType()), null);

		return caseNoteCreated;
    }

	@Override
	@Transactional
	@VerifyBookingAccess
	public CaseNote updateCaseNote(Long bookingId, Long caseNoteId, String username, @NotBlank(message="{caseNoteTextBlank}") String newCaseNoteText) {
        CaseNote caseNote = caseNoteRepository.getCaseNote(bookingId, caseNoteId)
				.orElseThrow(EntityNotFoundException.withId(caseNoteId));

        // Verify that user attempting to amend case note is same one who created it.
        UserDetail userDetail = userService.getUserByUsername(username);

		if (!caseNote.getStaffId().equals(userDetail.getStaffId())) {
            throw new AccessDeniedException("User not authorised to amend case note.");
        }

        String amendedText = format(AMEND_CASE_NOTE_FORMAT,
                caseNote.getText(),
                username,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")),
                newCaseNoteText);

        caseNoteRepository.updateCaseNote(bookingId, caseNoteId, amendedText, username);

        return getCaseNote(bookingId, caseNoteId);
	}

	@Override
	@Transactional(readOnly = true)
	@VerifyBookingAccess
	public CaseNoteCount getCaseNoteCount(Long bookingId, String type, String subType, LocalDate fromDate, LocalDate toDate) {
		// Validate date range
		if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && toDate.isBefore(fromDate)) {
			throw new BadRequestException("Invalid date range: toDate is before fromDate.");
		}

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
	@Transactional(readOnly = true)
	public List<ReferenceCode> getCaseNoteTypesByCaseLoadType(String caseLoadType) {
		return caseNoteRepository.getCaseNoteTypesByCaseLoadType(caseLoadType);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReferenceCode> getCaseNoteTypesWithSubTypesByCaseLoadType(String caseLoadType) {
		return caseNoteRepository.getCaseNoteTypesWithSubTypesByCaseLoadType(caseLoadType);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReferenceCode> getUsedCaseNoteTypesWithSubTypes() {
		return caseNoteRepository.getUsedCaseNoteTypesWithSubTypes();
	}
}
