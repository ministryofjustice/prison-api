package net.syscon.elite.service.validation;

import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.ReferenceDomainService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.Optional;

@Component
public class ReferenceCodesValidator implements ConstraintValidator<ReferenceCodesValid, NewCaseNote> {

    @Autowired
    private ReferenceDomainService referenceDomainService;

    @Override
    public void initialize(ReferenceCodesValid constraintAnnotation) {
        Assert.notNull(referenceDomainService, "Spring injection failed for referenceDomainService");
    }

    @Override
    public boolean isValid(NewCaseNote value, ConstraintValidatorContext context) {

        boolean valid = true;
        // This should be ok as it is cached:
        final Page<ReferenceCode> allTypes = referenceDomainService.getCaseNoteTypeByCurrentCaseLoad(null, null, null,
                0, 1000, true);
        final Optional<ReferenceCode> type = allTypes.getItems().stream().filter(x -> {
            return x.getCode().equals(value.getType());
        }).findFirst();
        if (!type.isPresent()) {
            valid = false;
        } else {
            final Optional<ReferenceCode> subType = type.get().getSubCodes().stream().filter(x -> {
                return x.getCode().equals(value.getSubType());
            }).findFirst();
            valid = subType.isPresent();
        }
        if (!valid) {
            context.disableDefaultConstraintViolation();
            final String message = "Reference (type,subtype)=(" + value.getType() + ',' + value.getSubType()
                    + ") does not exist";
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }
        return valid;
    }
}
