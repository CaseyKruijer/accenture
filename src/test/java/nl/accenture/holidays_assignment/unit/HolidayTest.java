package nl.accenture.holidays_assignment.unit;

import nl.accenture.holidays_assignment.modals.Holiday;
import nl.accenture.holidays_assignment.providers.HolidayProvider;
import nl.accenture.holidays_assignment.responses.CountryHolidayCountResponse;
import nl.accenture.holidays_assignment.responses.HolidayResponse;
import nl.accenture.holidays_assignment.responses.SharedHolidayResponse;
import nl.accenture.holidays_assignment.services.HolidayService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HolidayTest {
    @Mock
    private HolidayProvider holidayProvider;
    @InjectMocks
    private HolidayService holidayService;

    static Stream<Arguments> holidayCases() {
        LocalDate today = LocalDate.of(2026, 4, 23);

        return Stream.of(
                Arguments.of("NL", today,
                        List.of(
                                new Holiday(today.plusDays(10), "Future Holiday", "Future Holiday", "NL"),
                                new Holiday(today.minusDays(1), "Old 1", "Old Holiday1", "NL"),
                                new Holiday(today.minusDays(5), "Old 2",  "Old Holiday2", "NL"),
                                new Holiday(today.minusDays(2), "Old 3",  "Old Holiday3", "NL"),
                                new Holiday(today.minusDays(10), "Old 4", "Old Holiday4", "NL")
                        ), 3),
                Arguments.of("NL", today, List.of(new Holiday(today.minusDays(1), "Only Holiday", "Only Holiday", "NL")), 1),
                Arguments.of("NL", today,
                        List.of(new Holiday(today.plusDays(1), "Future 1", "Future 1", "NL"),
                                new Holiday(today.plusDays(5), "Future 2", "Future 2", "NL")
                        ), 0)
        );
    }

    @ParameterizedTest
    @MethodSource("holidayCases")
    void shouldReturnCorrectLastThreeHolidays(String countryCode, LocalDate today, List<Holiday> mockHolidays, int expectedSize) {
        when(holidayProvider.getHolidays(countryCode, today.getYear()))
                .thenReturn(mockHolidays);

        List<HolidayResponse> result =
                holidayService.getLastThreeCelebratedHolidays(countryCode);

        assertEquals(expectedSize, result.size());

        assertTrue(result.stream().allMatch(r -> r.getDate().isBefore(today)));

        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(
                    result.get(i).getDate().isAfter(result.get(i + 1).getDate())
            );
        }
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> holidayCountCases() {
        int year = 2026;

        return Stream.of(
                Arguments.of(List.of("NL"), year,
                        List.of(
                                new Holiday(LocalDate.of(year, 1, 1), "NY", "NY", "NL"),
                                new Holiday(LocalDate.of(year, 1, 4), "Weekend", "W", "NL")), List.of(1L)),
                Arguments.of(List.of("NL", "BE"), year, List.of(
                        new Holiday(LocalDate.of(year, 1, 1), "NY", "NY", "NL"),
                        new Holiday(LocalDate.of(year, 1, 2), "NY2", "NY2", "BE")), List.of(1L, 1L))
        );
    }

    @ParameterizedTest
    @MethodSource("holidayCountCases")
    void shouldReturnCorrectHolidayCounts(
            List<String> countryCodes,
            int year,
            List<Holiday> holidays,
            List<Long> expectedCounts
    ) {

        for (String code : countryCodes) {
            when(holidayProvider.getHolidays(code, year))
                    .thenReturn(
                            holidays.stream()
                                    .filter(h -> h.getCountryCode().equals(code))
                                    .toList()
                    );
        }

        List<CountryHolidayCountResponse> result =
                holidayService.getHolidayCounts(countryCodes, year);

        assertEquals(countryCodes.size(), result.size());

        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getCount() >= result.get(i + 1).getCount());
        }

        List<Long> actualCounts = result.stream()
                .map(CountryHolidayCountResponse::getCount)
                .toList();

        assertTrue(actualCounts.containsAll(expectedCounts));
    }

    static Stream<Arguments> sharedHolidayCases() {
        LocalDate base = LocalDate.of(2026, 1, 1);

        return Stream.of(
                Arguments.of("NL", "BE", base.getYear(),
                        List.of(
                                new Holiday(base, "Nieuwjaar", "New Year", "NL"),
                                new Holiday(base.plusDays(1), "Not Shared", "Not Shared", "NL")
                        ),
                        List.of(new Holiday(base, "Nieuwjaar", "New Year", "BE")), 1),
                Arguments.of("NL", "BE", base.getYear(),
                        List.of(
                                new Holiday(base, "NY NL", "New Year NL", "NL"),
                                new Holiday(base.plusDays(1), "K1", "Holiday 1", "NL"),
                                new Holiday(base.plusDays(2), "K2", "Holiday 2", "NL")
                        ),
                        List.of(
                                new Holiday(base, "NY BE", "New Year BE", "BE"),
                                new Holiday(base.plusDays(2), "K2", "Holiday 2", "BE")
                        ), 2),
                Arguments.of("NL", "BE", base.getYear(),
                        List.of(new Holiday(base, "A", "A", "NL")),
                        List.of(new Holiday(base.plusDays(1), "B", "B", "BE")), 0)
        );
    }

    @ParameterizedTest
    @MethodSource("sharedHolidayCases")
    void shouldReturnSharedHolidays(String country1, String country2, int year, List<Holiday> holidays1, List<Holiday> holidays2, int expectedSize) {
        when(holidayProvider.getHolidays(country1, year))
                .thenReturn(holidays1);

        when(holidayProvider.getHolidays(country2, year))
                .thenReturn(holidays2);

        List<SharedHolidayResponse> result =
                holidayService.getSharedHolidays(country1, country2, year);

        assertEquals(expectedSize, result.size());

        for (SharedHolidayResponse r : result) {
            assertNotNull(r.getDate());
            assertEquals(2, r.getLocalNames().size());
        }
    }
}
