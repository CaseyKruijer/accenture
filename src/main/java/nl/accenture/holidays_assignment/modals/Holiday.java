package nl.accenture.holidays_assignment.modals;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Holiday {
    private LocalDate date;
    private String localName;
    private String name;
    private String countryCode;
}
