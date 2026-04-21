package nl.accenture.holidays_assignment.controlers;

import lombok.RequiredArgsConstructor;
import nl.accenture.holidays_assignment.modals.Holiday;
import org.springframework.web.bind.annotation.*;
import nl.accenture.holidays_assignment.services.HolidayService;

@RestController
@RequestMapping("/holidays")
@RequiredArgsConstructor
public class HolidayController {
    private final HolidayService holidayService;

    @GetMapping("/{country}")
    public Holiday[] getHolidays(
            @PathVariable String country,
            @RequestParam(required = false) Integer year
    ) throws Exception {
        int resolvedYear = (year != null) ? year : java.time.Year.now().getValue();

        return holidayService.getHolidays(resolvedYear, country);
    }
}
