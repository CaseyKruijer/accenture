package nl.accenture.holidays_assignment.services;

import nl.accenture.holidays_assignment.modals.Holiday;
import nl.accenture.holidays_assignment.response.HolidayResponse;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;

@Service
public class HolidayService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String BASEURL = "https://date.nager.at/api/v3/PublicHolidays/";

    /**
     * Based on the given landcode, you will get an array of the last 3 celebrated holidays
     *
     * @param landCode the land code
     * @return array of the last 3 holidays celebrated
     * @throws Exception
     */
    public HolidayResponse[] getLastThreeCelebratedHolidays(String landCode) throws Exception {
        Holiday[] holidays = getHolidays(landCode);
        LocalDate today = LocalDate.now();

        return Arrays.stream(holidays)
                .filter(h-> h.getDate().isBefore(today))
                .sorted(Comparator.comparing(Holiday::getDate).reversed())
                .limit(3)
                .map(h -> new HolidayResponse(h.getName(), h.getDate()))
                .toArray(HolidayResponse[]::new);
    }

    /**
     * Get all the holidays from the given country in the given year
     *
     * @param countryCode Code of a land from what holidays you want to know
     * @return an array of {@link Holiday}s
     * @throws Exception
     */
    private Holiday[] getHolidays(String countryCode) throws Exception {
        int currentYear = java.time.Year.now().getValue();
        String url =  BASEURL + currentYear + "/" + countryCode;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(response.body(), Holiday[].class);
    }
}
