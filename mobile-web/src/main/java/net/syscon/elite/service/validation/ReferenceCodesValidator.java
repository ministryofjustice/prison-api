package net.syscon.elite.service.validation;

import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.ReferenceDomainService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class ReferenceCodesValidator implements ConstraintValidator<ReferenceCodesValid, NewCaseNote> {

    @Autowired
    ReferenceDomainService referenceDomainService;

    @Override
    public void initialize(ReferenceCodesValid constraintAnnotation) {
        Assert.notNull(referenceDomainService, "Spring injection failed for referenceDomainService");
    }

    @Override
    public boolean isValid(NewCaseNote value, ConstraintValidatorContext context) {
        try {
            referenceDomainService.getCaseNoteSubType(value.getType(), value.getSubType());
        } catch (EntityNotFoundException e) {
            context.disableDefaultConstraintViolation();
            final String message = "Reference type/subtype=" + value.getType() + '/' + value.getSubType()
                    + " does not exist";
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }
        return true;
    }
}
