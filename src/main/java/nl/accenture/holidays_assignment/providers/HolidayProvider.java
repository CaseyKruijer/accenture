package nl.accenture.holidays_assignment.providers;

import lombok.extern.log4j.Log4j2;
import nl.accenture.holidays_assignment.constants.ErrorMessages;
import nl.accenture.holidays_assignment.modals.Holiday;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Component
@Log4j2
public class HolidayProvider {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String BASEURL = "https://date.nager.at/api/v3/PublicHolidays/";

    /**
     * Retrieves all public holidays for a given country and year from the external holiday API.
     *
     * @param countryCode the country code
     * @param year the year for which the holidays should be retrieved
     * @return a list of {@link Holiday}
     */
    public List<Holiday> getHolidays(String countryCode, int year) {
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
