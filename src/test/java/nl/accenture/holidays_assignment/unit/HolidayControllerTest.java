package nl.accenture.holidays_assignment.unit;

import nl.accenture.holidays_assignment.constants.ErrorMessages;
import nl.accenture.holidays_assignment.controlers.HolidayController;
import nl.accenture.holidays_assignment.responses.CountryHolidayCountResponse;
import nl.accenture.holidays_assignment.responses.HolidayResponse;
import nl.accenture.holidays_assignment.responses.SharedHolidayResponse;
import nl.accenture.holidays_assignment.services.HolidayService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HolidayController.class)
public class HolidayControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HolidayService holidayService;

    static Stream<Arguments> countryCodeProviderCases() {
        return Stream.of(
                Arguments.of("NL", true, 200, ""),
                Arguments.of("USA", false, 400, ErrorMessages.NO_ISO_COUNTRY_CODE)
        );
    }

    @ParameterizedTest
    @MethodSource("countryCodeProviderCases")
    void get_correct_last_three_holidays(String countryCode, boolean isValid, int expectedStatus, String expectedMessage) throws Exception {
        if (isValid) {
            LocalDate today = LocalDate.of(2026, 4, 23);
            List<HolidayResponse> mockResponse = List.of(
                    new HolidayResponse("Holiday1", today),
                    new HolidayResponse("Holiday2", today.minusDays(5)),
                    new HolidayResponse("Holiday3",  today.plusDays(5))
            );
            given(holidayService.getLastThreeCelebratedHolidays(countryCode)).willReturn(mockResponse);
        }

        ResultActions result = mockMvc.perform(get("/holidays/{countryCode}/last3", countryCode));

        result.andExpect(status().is(expectedStatus));
        if (expectedStatus == 400 && expectedMessage != null) {
            result.andExpect(content().string(org.hamcrest.Matchers.containsString(expectedMessage)));
        }
    }

    static Stream<Arguments> holidaysNotOnWeekendsCases() {
        return Stream.of(
                Arguments.of(List.of("NL"), 2026, 200, ""),
                Arguments.of(List.of("NL", "BE"), 2026, 200, ""),
                Arguments.of(List.of("NL", "BE"), 2000, 200, ""),
                Arguments.of(List.of("NL", "BE"), 2030, 200, ""),
                Arguments.of(List.of("L"), 2030, 400, ErrorMessages.NO_ISO_COUNTRY_CODE),
                Arguments.of(List.of("NL", "B"), 2030, 400, ErrorMessages.NO_ISO_COUNTRY_CODE),
                Arguments.of(List.of("NL", "BE"), 1800, 400, ErrorMessages.YEAR_IS_INVALID),
                Arguments.of(List.of("NL", "BE"), 2100, 400, ErrorMessages.YEAR_IS_INVALID)
        );
    }

    @ParameterizedTest
    @MethodSource("holidaysNotOnWeekendsCases")
    void get_count_of_holidays_not_on_weekends(List<String> countryCodes, int year, int expectedStatus, String expectedMessage) throws Exception {
        if (expectedStatus == 200) {
            List<CountryHolidayCountResponse> mockResponse = List.of(
                    new CountryHolidayCountResponse("NL", 5),
                    new CountryHolidayCountResponse("BE", 6)
            );

            given(holidayService.getHolidayCounts(countryCodes, year)).willReturn(mockResponse);
        }

        ResultActions result = mockMvc.perform(get("/holidays/counts")
                        .param("countryCodes", countryCodes.toArray(new String[0]))
                        .param("year", String.valueOf(year)));

        result.andExpect(status().is(expectedStatus));
        if (expectedStatus == 400 && expectedMessage != null) {
            result.andExpect(content().string(org.hamcrest.Matchers.containsString(expectedMessage)));
        }
    }

    static Stream<Arguments> sharedHolidayCases() {
        return Stream.of(
                Arguments.of("NL", "BE", 2026, 200, null),
                Arguments.of("INVALID", "BE", 2026, 400, ErrorMessages.NO_ISO_COUNTRY_CODE),
                Arguments.of("NL", "INVALID", 2026, 400, ErrorMessages.NO_ISO_COUNTRY_CODE),
                Arguments.of("NL", "BE", 1800, 400, ErrorMessages.YEAR_IS_INVALID),
                Arguments.of("NL", "BE", 2100, 400, ErrorMessages.YEAR_IS_INVALID));
    }

    @ParameterizedTest
    @MethodSource("sharedHolidayCases")
    void get_shared_holidays(String countryCode1, String countryCode2, int year, int expectedStatus, String expectedMessage) throws Exception {

        if (expectedStatus == 200) {
            LocalDate today = LocalDate.of(2026, 4, 23);
            List<SharedHolidayResponse> mockResponse = List.of(
                    new SharedHolidayResponse(today, List.of("Holiday1")),
                    new SharedHolidayResponse(today.plusDays(20), List.of("Holiday2"))
            );

            given(holidayService.getSharedHolidays(countryCode1, countryCode2, year))
                    .willReturn(mockResponse);
        }

        ResultActions result = mockMvc.perform(get("/holidays/shared")
                .param("countryCode1", countryCode1)
                .param("countryCode2", countryCode2)
                .param("year", String.valueOf(year)));

        result.andExpect(status().is(expectedStatus));

        if (expectedStatus == 400 && expectedMessage != null) {
            result.andExpect(content().string(
                    org.hamcrest.Matchers.containsString(expectedMessage)
            ));
        }
    }
}
