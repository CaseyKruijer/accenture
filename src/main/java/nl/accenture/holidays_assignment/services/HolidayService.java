package nl.accenture.holidays_assignment.services;

import nl.accenture.holidays_assignment.modals.Holiday;
import nl.accenture.holidays_assignment.response.CountryHolidayCountResponse;
import nl.accenture.holidays_assignment.response.HolidayResponse;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HolidayService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String BASEURL = "https://date.nager.at/api/v3/PublicHolidays/";

    /**
     * Based on the given landcode, you will get an array of the last 3 celebrated holidays
     *
     * @param countryCode the land code
     * @return list of the last 3 holidays celebrated
     * @throws Exception
     */
    public List<HolidayResponse> getLastThreeCelebratedHolidays(String countryCode) throws Exception {
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
     * search for every country code, all the public holidays where the date is not on a weekend saturday or sunday
     *
     * @param countryCodes list with all the countrycodes to search in
     * @param year year to search
     * @return list of {@link CountryHolidayCountResponse}
     * @throws Exception
     */
    public List<CountryHolidayCountResponse> getHolidayCounts(List<String> countryCodes, int year) throws Exception {
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
     * Get all the holidays from the given country in the given year
     *
     * @param countryCode Code of a land from what holidays you want to know
     * @return a list of {@link Holiday}s
     * @throws Exception
     */
    private List<Holiday> getHolidays(String countryCode, int year) throws Exception {
        String url =  BASEURL + year + "/" + countryCode;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Holiday[] holidays = mapper.readValue(response.body(), Holiday[].class);

        return Arrays.asList(holidays);
    }
}
