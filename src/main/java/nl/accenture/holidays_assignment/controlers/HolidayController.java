package nl.accenture.holidays_assignment.controlers;

import lombok.RequiredArgsConstructor;
import nl.accenture.holidays_assignment.response.CountryHolidayCountResponse;
import nl.accenture.holidays_assignment.response.HolidayResponse;
import nl.accenture.holidays_assignment.response.SharedHolidayResponse;
import org.springframework.web.bind.annotation.*;
import nl.accenture.holidays_assignment.services.HolidayService;

import java.util.List;

@RestController
@RequestMapping("/holidays")
@RequiredArgsConstructor
public class HolidayController {
    private final HolidayService holidayService;

    @GetMapping("/{countryCode}/last3")
    public List<HolidayResponse> getLastThreeCelebratedHolidays(@PathVariable String countryCode) throws Exception {
        return holidayService.getLastThreeCelebratedHolidays(countryCode);
    }

    @GetMapping("/{counts}")
    public List<CountryHolidayCountResponse> getCountOfHolidaysNotOnWeekend(
            @RequestParam List<String> countryCode,
            @RequestParam int year) throws Exception {
        return holidayService.getHolidayCounts(countryCode, year);
    }

    @GetMapping("/shared")
    public List<SharedHolidayResponse> getSharedHolidays(
            @RequestParam String countryCode1,
            @RequestParam String countryCode2,
            @RequestParam int year
    ) throws Exception {
        return holidayService.getSharedHolidays(countryCode1, countryCode2, year);
    }
}
