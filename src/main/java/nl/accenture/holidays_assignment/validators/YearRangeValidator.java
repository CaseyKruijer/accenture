package nl.accenture.holidays_assignment.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.accenture.holidays_assignment.validations.ValidYearRange;

import java.util.Calendar;

public class YearRangeValidator implements ConstraintValidator<ValidYearRange, Integer> {
    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext constraintValidatorContext) {
        if (year == null) return false;

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int min = currentYear - 50;
        int max = currentYear + 50;

        return year >= min && year <= max;
    }
}
