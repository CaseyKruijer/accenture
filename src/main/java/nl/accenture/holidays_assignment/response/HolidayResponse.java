package nl.accenture.holidays_assignment.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class HolidayResponse {
    private String name;
    private LocalDate date;
}
