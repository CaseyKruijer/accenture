package nl.accenture.holidays_assignment.services;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nl.accenture.holidays_assignment.modals.Holiday;
import nl.accenture.holidays_assignment.providers.HolidayProvider;
import nl.accenture.holidays_assignment.responses.CountryHolidayCountResponse;
import nl.accenture.holidays_assignment.responses.HolidayResponse;
import nl.accenture.holidays_assignment.responses.SharedHolidayResponse;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
@AllArgsConstructor
public class HolidayService {
    private HolidayProvider holidayProvider;

    /**
     * Retrieves the three most recent holidays that have already occurred in the current year
     * for the given country.
     *
     * @param countryCode the country code
     * @return a list of up to three {@link HolidayResponse}
     */
    public List<HolidayResponse> getLastThreeCelebratedHolidays(String countryCode) {
        LocalDate today = LocalDate.now();
        List<Holiday> holidays = holidayProvider.getHolidays(countryCode, today.getYear());

        return holidays.stream()
                .filter(h-> h.getDate().isBefore(today))
                .sorted(Comparator.comparing(Holiday::getDate).reversed())
                .limit(3)
                .map(h -> new HolidayResponse(h.getName(), h.getDate()))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the number of public holidays that fall on weekdays (Monday-Friday)
     * for each given country in a specific year.
     *
     * @param countryCodes list of country codes
     * @param year the year for witch the holidays should be counted
     * @return a list of {@link CountryHolidayCountResponse}
     */
    public List<CountryHolidayCountResponse> getHolidayCounts(List<String> countryCodes, int year) {
        return countryCodes.stream()
                .map(code -> {
                    try {
                        List<Holiday> holidays = holidayProvider.getHolidays(code, year);

                        long count = holidays.stream()
                                .filter(h -> {
                                    DayOfWeek day = h.getDate().getDayOfWeek();

                                    return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
                                }).count();

                        return new CountryHolidayCountResponse(code, count);
                    }catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).sorted(Comparator.comparingLong(CountryHolidayCountResponse::getCount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all holidays that occur on the same date in two given countries for a specific year.
     * The method compares the holiday calendars of both countries and returns a list of shared holidays
     *
     * @param countryCode1 the country code of the first country
     * @param countryCode2 the country code of the second country
     * @param year the year for which the holidays should be retrieved
     * @return a list of {@link SharedHolidayResponse} objects, if none occur return an empty list
     */
    public List<SharedHolidayResponse> getSharedHolidays(String countryCode1, String countryCode2, int year) {
        List<Holiday> holidays1 = holidayProvider.getHolidays(countryCode1, year);
        List<Holiday> holidays2 = holidayProvider.getHolidays(countryCode2, year);

        Map<LocalDate, Holiday> mapHoliday = holidays1.stream()
                .collect(Collectors.toMap(Holiday::getDate, h ->h));

        return holidays2.stream()
                .filter(h -> mapHoliday.containsKey(h.getDate()))
                .map(h -> {
                    Holiday holiday = mapHoliday.get(h.getDate());

                    return new SharedHolidayResponse(h.getDate(), List.of(holiday.getLocalName(), h.getLocalName()));
                }).collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                SharedHolidayResponse::getDate,
                                r -> r,
                                (r1, r2) -> r1
                        ),
                        m -> new ArrayList<>(m.values())
                ));
    }
}
