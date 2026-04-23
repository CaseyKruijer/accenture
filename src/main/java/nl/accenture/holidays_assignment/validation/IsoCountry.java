package nl.accenture.holidays_assignment.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import nl.accenture.holidays_assignment.constants.ErrorMessages;
import nl.accenture.holidays_assignment.validator.IsoCountryValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IsoCountryValidator.class)
@Target({
        ElementType.PARAMETER,
        ElementType.FIELD,
        ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsoCountry {
    String message() default ErrorMessages.NO_ISO_COUNTRY_CODE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
