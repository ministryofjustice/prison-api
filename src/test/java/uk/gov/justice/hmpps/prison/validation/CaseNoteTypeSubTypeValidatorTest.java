package uk.gov.justice.hmpps.prison.validation;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.CaseLoadService;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;
import uk.gov.justice.hmpps.prison.service.validation.CaseNoteTypeSubTypeValidator;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CaseNoteTypeSubTypeValidatorTest {

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private CaseLoadService caseLoadService;

    @Mock
    private CaseNoteService caseNoteService;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintViolationBuilder constraintViolationBuilder;

    private final String VALIDATION_MESSAGE = "CaseNote (type,subtype)=(%s,%s) does not exist";
    private final String SOME_TYPE_CODE = "type1";
    private final String SOME_SUBTYPE_CODE = "subtype1";
    private final String INCORRECT_MOVED_CELL_SUBTYPE = "INCORRECT";

    @Test
    public void testDoesNothingOnNullValue() {
        final var validator = new CaseNoteTypeSubTypeValidator(authenticationFacade, caseLoadService, caseNoteService);

        final var result = validator.isValid(null, context);

        assertThat(result).isTrue();

        verify(authenticationFacade, never()).getCurrentUsername();
        verify(caseLoadService, never()).getWorkingCaseLoadForUser(any());
        verify(caseNoteService, never()).getCaseNoteTypesWithSubTypesByCaseLoadType(any());
        verify(context, never()).disableDefaultConstraintViolation();
        verify(context, never()).buildConstraintViolationWithTemplate(any());
        verify(constraintViolationBuilder, never()).addConstraintViolation();
    }

    @Test
    public void testNoCaseNotesForCaseload() {

        when(caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(any())).thenReturn(Collections.emptyList());
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);

        final var validator = new CaseNoteTypeSubTypeValidator(authenticationFacade, caseLoadService, caseNoteService);

        final var result = validator.isValid(NewCaseNote.builder().type(SOME_TYPE_CODE).subType(SOME_SUBTYPE_CODE).build(), context);

        assertThat(result).isFalse();

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(String.format(VALIDATION_MESSAGE, SOME_TYPE_CODE, SOME_SUBTYPE_CODE));
    }

    @Test
    public void testSubtypeNotFound() {
        when(caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(any())).thenReturn(
            List.of(ReferenceCode.builder()
                .code("type1")
                .subCodes(Collections.emptyList())
                .build())
        );

        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);

        final var validator = new CaseNoteTypeSubTypeValidator(authenticationFacade, caseLoadService, caseNoteService);

        final var result = validator.isValid(NewCaseNote.builder().type(SOME_TYPE_CODE).subType(SOME_SUBTYPE_CODE).build(), context);

        assertThat(result).isFalse();

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(String.format(VALIDATION_MESSAGE, SOME_TYPE_CODE, SOME_SUBTYPE_CODE));
    }

    @Test
    public void testMatchOnValidTypeAndSubType() {
        when(caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(any())).thenReturn(
            List.of(ReferenceCode.builder()
                .code(SOME_TYPE_CODE)
                .subCodes(List.of(ReferenceCode.builder().code(SOME_SUBTYPE_CODE).build()))
                .build())
        );

        final var validator = new CaseNoteTypeSubTypeValidator(authenticationFacade, caseLoadService, caseNoteService);
        final var result = validator.isValid(NewCaseNote.builder().type(SOME_TYPE_CODE).subType(SOME_SUBTYPE_CODE).build(), context);

        assertThat(result).isTrue();

        verify(context, never()).disableDefaultConstraintViolation();
        verify(context, never()).buildConstraintViolationWithTemplate(any());
        verify(constraintViolationBuilder, never()).addConstraintViolation();
    }

    @Test
    public void testInvalidMovedCellSubtype() {
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);

        final var validator = new CaseNoteTypeSubTypeValidator(authenticationFacade, caseLoadService, caseNoteService);
        final var result = validator.isValid(NewCaseNote.builder().type("MOVED_CELL").subType(INCORRECT_MOVED_CELL_SUBTYPE).build(), context);

        assertThat(result).isFalse();

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(String.format(VALIDATION_MESSAGE, "MOVED_CELL", INCORRECT_MOVED_CELL_SUBTYPE));
    }
}
