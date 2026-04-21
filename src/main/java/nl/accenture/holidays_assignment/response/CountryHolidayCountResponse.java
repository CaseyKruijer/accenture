package nl.accenture.holidays_assignment.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CountryHolidayCountResponse {
    private String countryCode;
    private long count;
}
