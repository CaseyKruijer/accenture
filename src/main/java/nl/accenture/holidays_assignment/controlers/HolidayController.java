package nl.accenture.holidays_assignment.controlers;

import lombok.RequiredArgsConstructor;
import nl.accenture.holidays_assignment.responses.CountryHolidayCountResponse;
import nl.accenture.holidays_assignment.responses.HolidayResponse;
import nl.accenture.holidays_assignment.responses.SharedHolidayResponse;
import nl.accenture.holidays_assignment.services.HolidayService;
import nl.accenture.holidays_assignment.validations.IsoCountry;
import nl.accenture.holidays_assignment.validations.ValidYearRange;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/holidays")
@RequiredArgsConstructor
public class HolidayController {
    private final HolidayService holidayService;

    @GetMapping("/{countryCode}/last3")
    public ResponseEntity<List<HolidayResponse>> getLastThreeCelebratedHolidays(
            @PathVariable @IsoCountry String countryCode
    ) {
        return ResponseEntity.ok(holidayService.getLastThreeCelebratedHolidays(countryCode));
    }

    @GetMapping("/counts")
    public ResponseEntity<List<CountryHolidayCountResponse>> getCountOfHolidaysNotOnWeekend(
            @RequestParam List<@IsoCountry String> countryCodes,
            @RequestParam @ValidYearRange int year) {
        return ResponseEntity.ok(holidayService.getHolidayCounts(countryCodes, year));
    }

    @GetMapping("/shared")
    public ResponseEntity<List<SharedHolidayResponse>> getSharedHolidays(
            @RequestParam @IsoCountry String countryCode1,
            @RequestParam @IsoCountry String countryCode2,
            @RequestParam @ValidYearRange int year
    ) {
        return ResponseEntity.ok(holidayService.getSharedHolidays(countryCode1, countryCode2, year));
    }
}
