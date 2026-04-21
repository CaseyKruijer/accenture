package nl.accenture.holidays_assignment.controlers;

import lombok.RequiredArgsConstructor;
import nl.accenture.holidays_assignment.response.HolidayResponse;
import org.springframework.web.bind.annotation.*;
import nl.accenture.holidays_assignment.services.HolidayService;

@RestController
@RequestMapping("/holidays")
@RequiredArgsConstructor
public class HolidayController {
    private final HolidayService holidayService;

    @GetMapping("/{country}")
    public HolidayResponse[] getLastThreeCelebratedHolidays(@PathVariable String country) throws Exception {
        return holidayService.getLastThreeCelebratedHolidays(country);
    }


}
