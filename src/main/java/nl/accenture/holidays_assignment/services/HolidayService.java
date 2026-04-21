package nl.accenture.holidays_assignment.services;

import nl.accenture.holidays_assignment.modals.Holiday;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class HolidayService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String BASEURL = "https://date.nager.at/api/v3/PublicHolidays/";

    /**
     * Get all the holidays from the given country in the given year
     *
     * @param year The year to get the holidays from
     * @param countryCode Code of a land from what holidays you want to know
     * @return an array of {@link Holiday}s
     * @throws Exception
     */
    public Holiday[] getHolidays(int year, String countryCode) throws Exception {
        String url =  BASEURL + year + "/" + countryCode;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(response.body(), Holiday[].class);
    }

}
