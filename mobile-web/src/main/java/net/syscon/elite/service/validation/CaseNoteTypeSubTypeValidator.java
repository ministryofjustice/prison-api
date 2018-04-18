package net.syscon.elite.service.validation;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.CaseNoteService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Optional;

@Component
public class CaseNoteTypeSubTypeValidator implements ConstraintValidator<CaseNoteTypeSubTypeValid, NewCaseNote> {
    private final AuthenticationFacade authenticationFacade;
    private final CaseLoadService caseLoadService;
    private final CaseNoteService caseNoteService;

    public CaseNoteTypeSubTypeValidator(AuthenticationFacade authenticationFacade,
                                        CaseLoadService caseLoadService, CaseNoteService caseNoteService) {
        this.authenticationFacade = authenticationFacade;
        this.caseLoadService = caseLoadService;
        this.caseNoteService = caseNoteService;
    }

    @Override
    public void initialize(CaseNoteTypeSubTypeValid constraintAnnotation) {
        Assert.notNull(caseLoadService, "Spring injection failed for caseLoadService");
        Assert.notNull(caseNoteService, "Spring injection failed for caseNoteService");
    }

    @Override
    public boolean isValid(NewCaseNote value, ConstraintValidatorContext context) {
        boolean valid = true;

        // This should be ok as it is cached:
        Optional<CaseLoad> caseLoad = caseLoadService.getWorkingCaseLoadForUser(authenticationFacade.getCurrentUsername());
        String caseLoadType = caseLoad.isPresent() ? caseLoad.get().getType() : "BOTH";
        List<ReferenceCode> allTypes = caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(caseLoadType);

        Optional<ReferenceCode> type =
                allTypes.stream().filter(x -> x.getCode().equals(value.getType())).findFirst();

        if (!type.isPresent()) {
            valid = false;
        } else {
            Optional<ReferenceCode> subType =
                    type.get().getSubCodes().stream().filter(x -> x.getCode().equals(value.getSubType())).findFirst();

            valid = subType.isPresent();
        }

        if (!valid) {
            context.disableDefaultConstraintViolation();

            String message = "CaseNote (type,subtype)=(" + value.getType() + ',' + value.getSubType()
                    + ") does not exist";

            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }

        return valid;
    }
}
