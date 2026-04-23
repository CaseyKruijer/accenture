package nl.accenture.holidays_assignment.integration;

import nl.accenture.holidays_assignment.constants.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class HolidayTest {
    @Autowired
    private MockMvc mockMvc;

    static Stream<Arguments> lastThreeCelebratedHolidaysCases() {
        return Stream.of(
                Arguments.of("NL", 200, ""),
                Arguments.of("N", 400, ErrorMessages.NO_ISO_COUNTRY_CODE)
        );
    }

    @ParameterizedTest
    @MethodSource("lastThreeCelebratedHolidaysCases")
    void get_last_three_celebrated_holidays(String countryCode, int expectedStatus, String expectedMessage) throws Exception {
        ResultActions result = mockMvc.perform(get("/holidays/" + countryCode + "/last3"));

        result.andExpect(status().is(expectedStatus));
        if (expectedStatus == 400 && expectedMessage != null) {
            result.andExpect(content().string(org.hamcrest.Matchers.containsString(expectedMessage)));
        }
    }

    @Test
    void should_return404_for_empty_country_code() throws Exception {
        mockMvc.perform(get("/holidays//last3"))
                .andExpect(status().isNotFound());
    }

    static Stream<Arguments> countOfHolidaysNotOnWeekendCases() {
        return Stream.of(
                Arguments.of(List.of("NL"), 2026, 200, ""),
                Arguments.of(List.of("NL", "BE"), 2026, 200, ""),
                Arguments.of(List.of("NL", "BE"), 2000, 200, ""),
                Arguments.of(List.of("NL", "BE"), 2050, 200, ""),
                Arguments.of(List.of("N", "BE"), 2026, 400, ErrorMessages.NO_ISO_COUNTRY_CODE),
                Arguments.of(List.of("NL", "B"), 2026, 400, ErrorMessages.NO_ISO_COUNTRY_CODE),
                Arguments.of(List.of("NL", "BE"), 1800, 400, ErrorMessages.YEAR_IS_INVALID),
                Arguments.of(List.of("NL", "BE"), 2200, 400, ErrorMessages.YEAR_IS_INVALID)
        );
    }

    @ParameterizedTest
    @MethodSource("countOfHolidaysNotOnWeekendCases")
    void get_count_of_holidays_not_on_weekends(List<String> countryCodes, int year, int expectedStatus, String expectedMessage) throws Exception {
        ResultActions result = mockMvc.perform(get("/holidays/counts")
                .param("countryCodes", countryCodes.toArray(new String[0]))
                .param("year", String.valueOf(year)));

        result.andExpect(status().is(expectedStatus));
        if (expectedStatus == 400 && expectedMessage != null) {
            result.andExpect(content().string(org.hamcrest.Matchers.containsString(expectedMessage)));
        }
    }

    static Stream<Arguments> duplicateHolidaysCases() {
        return Stream.of(
                Arguments.of("NL", "BE", 2026, 200, ""),
                Arguments.of("NL", "BE", 2020, 200, ""),
                Arguments.of("NL", "BE", 2030, 200, ""),
                Arguments.of("N", "BE", 2026, 400, ErrorMessages.NO_ISO_COUNTRY_CODE),
                Arguments.of("NL", "B", 2026, 400, ErrorMessages.NO_ISO_COUNTRY_CODE),
                Arguments.of("NL", "BE", 1800, 400, ErrorMessages.YEAR_IS_INVALID),
                Arguments.of("NL", "BE", 2200, 400, ErrorMessages.YEAR_IS_INVALID)
        );
    }

    @ParameterizedTest
    @MethodSource("duplicateHolidaysCases")
    void get_duplicated_holidays(String countryCode1, String countryCode2, int year, int expectedStatus, String expectedMessage) throws Exception {
        ResultActions result = mockMvc.perform(get("/holidays/counts")
                .param("countryCodes", countryCode1)
                .param("countryCodes", countryCode2)
                .param("year", String.valueOf(year)));

        result.andExpect(status().is(expectedStatus));
        if (expectedStatus == 400 && expectedMessage != null) {
            result.andExpect(content().string(org.hamcrest.Matchers.containsString(expectedMessage)));
        }
    }
}
