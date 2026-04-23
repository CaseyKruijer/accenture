package nl.accenture.holidays_assignment.services;

import lombok.extern.log4j.Log4j2;
import nl.accenture.holidays_assignment.constants.ErrorMessages;
import nl.accenture.holidays_assignment.modals.Holiday;
import nl.accenture.holidays_assignment.response.CountryHolidayCountResponse;
import nl.accenture.holidays_assignment.response.HolidayResponse;
import nl.accenture.holidays_assignment.response.SharedHolidayResponse;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class HolidayService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String BASEURL = "https://date.nager.at/api/v3/PublicHolidays/";

    /**
     * Retrieves the three most recent holidays that have already occurred in the current year
     * for the given country.
     *
     * @param countryCode the country code
     * @return a list of up to three {@link HolidayResponse}
     * @throws Exception if an error occurs while retrieving holiday data
     */
    public List<HolidayResponse> getLastThreeCelebratedHolidays(String countryCode) {
        LocalDate today = LocalDate.now();
        List<Holiday> holidays = getHolidays(countryCode, today.getYear());

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
     * @throws Exception if an error occurs while retrieving holiday data
     */
    public List<CountryHolidayCountResponse> getHolidayCounts(List<String> countryCodes, int year) {
        return countryCodes.stream()
                .map(code -> {
                    try {
                        List<Holiday> holidays = getHolidays(code, year);

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
     * @throws Exception if an error occurs while retrieving holiday data
     */
    public List<SharedHolidayResponse> getSharedHolidays(String countryCode1, String countryCode2, int year) {
        List<Holiday> holidays1 = getHolidays(countryCode1, year);
        List<Holiday> holidays2 = getHolidays(countryCode2, year);

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

    /**
     * Retrieves all public holidays for a given country and year from the external holiday API.
     *
     * @param countryCode the country code
     * @param year the year for which the holidays should be retrieved
     * @return a list of {@link Holiday}
     * @throws Exception if an error occurs while retrieving holiday data
     */
    private List<Holiday> getHolidays(String countryCode, int year) {
        try {
            String url =  BASEURL + year + "/" + countryCode;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Holiday[] holidays = mapper.readValue(response.body(), Holiday[].class);

            return Arrays.asList(holidays);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new RuntimeException(ErrorMessages.API_FAILED);
        }
    }
}
