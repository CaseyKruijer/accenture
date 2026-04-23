package nl.accenture.holidays_assignment.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.accenture.holidays_assignment.constants.ErrorMessages;
import nl.accenture.holidays_assignment.validation.IsoCountry;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class IsoCountryValidator implements ConstraintValidator<IsoCountry, String> {
    private Set<String> validCountries;
    @Override
    public void initialize(IsoCountry constraintAnnotation) {
        validCountries = Arrays.stream(Locale.getISOCountries())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String countryCode, ConstraintValidatorContext constraintValidatorContext) {
        if (countryCode == null) return false;

        boolean isValid = validCountries.contains(countryCode.toUpperCase());

        if (!isValid) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    ErrorMessages.NO_ISO_COUNTRY_CODE
            ).addConstraintViolation();
        }

        return isValid;
    }
}
