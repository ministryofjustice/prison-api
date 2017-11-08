package net.syscon.elite.service.validation;

import net.syscon.elite.service.BookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class BookingAccessValidator implements ConstraintValidator<BookingAccessValid, Long> {

    @Autowired
    private BookingService bookingService;
    
    @Override
    public void initialize(BookingAccessValid constraintAnnotation) {
        Assert.notNull(bookingService, "Spring injection failed for bookingService");
    }

    @Override
    public boolean isValid(Long bookingId, ConstraintValidatorContext context) {
       // try {
            bookingService.verifyBookingAccess(bookingId);
       /* } catch (EntityNotFoundException e) {
            final String message = "Reference (type,subtype)=(" + value.getType() + ',' + value.getSubType()
                    + ") does not exist";
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }*/
        return true;
    }
}
