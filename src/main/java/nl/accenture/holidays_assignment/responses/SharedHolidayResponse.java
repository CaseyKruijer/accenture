package nl.accenture.holidays_assignment.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class SharedHolidayResponse {
    private LocalDate date;
    private List<String> localNames;
}
