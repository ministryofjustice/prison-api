package uk.gov.justice.hmpps.prison.service.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.CaseLoadService;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class CaseNoteTypeSubTypeValidator implements ConstraintValidator<CaseNoteTypeSubTypeValid, NewCaseNote> {
    private final AuthenticationFacade authenticationFacade;
    private final CaseLoadService caseLoadService;
    private final CaseNoteService caseNoteService;

    public CaseNoteTypeSubTypeValidator(final AuthenticationFacade authenticationFacade,
                                        final CaseLoadService caseLoadService,
                                        final CaseNoteService caseNoteService) {
        this.authenticationFacade = authenticationFacade;
        this.caseLoadService = caseLoadService;
        this.caseNoteService = caseNoteService;
    }

    @Override
    public void initialize(final CaseNoteTypeSubTypeValid constraintAnnotation) {
        Assert.notNull(caseLoadService, "Spring injection failed for caseLoadService");
        Assert.notNull(caseNoteService, "Spring injection failed for caseNoteService");
    }

    @Override
    public boolean isValid(final NewCaseNote value, final ConstraintValidatorContext context) {
        if (value == null) return true;
        if (value.getType().equals("MOVED_CELL")) return validateMoveCellSubtype(value, context);

        // This should be ok as it is cached:
        final var caseLoad = caseLoadService.getWorkingCaseLoadForUser(authenticationFacade.getCurrentUsername());
        final var caseLoadType = caseLoad.isPresent() ? caseLoad.get().getType() : "BOTH";
        final var allTypes = caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(caseLoadType);

        final var parentType = allTypes.stream()
            .filter(type -> type.getCode().equals(value.getType()))
            .findFirst();

        if (parentType.isEmpty()) return triggerViolation(value, context);

        final var subType = parentType
            .get()
            .getSubCodes()
            .stream()
            .filter(sc -> sc.getCode().equals(value.getSubType()))
            .findFirst();

        return subType.isPresent() || triggerViolation(value, context);
    }

    private Boolean validateMoveCellSubtype(final NewCaseNote value, final ConstraintValidatorContext context) {
        try {
            MovedCell.valueOf(value.getSubType());
            return true;

        } catch (IllegalArgumentException e) {
        }

        return triggerViolation(value, context);
    }

    private Boolean triggerViolation(final NewCaseNote value, final ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        final var message = "CaseNote (type,subtype)=(" + value.getType() + ',' + value.getSubType()
            + ") does not exist";

        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();

        return false;
    }
}
