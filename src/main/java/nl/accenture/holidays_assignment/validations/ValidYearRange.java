package nl.accenture.holidays_assignment.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import nl.accenture.holidays_assignment.constants.ErrorMessages;
import nl.accenture.holidays_assignment.validators.YearRangeValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = YearRangeValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidYearRange {
    String message() default ErrorMessages.YEAR_IS_INVALID;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
