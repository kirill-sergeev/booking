package org.example.booking.service;

import org.example.booking.config.AppConfig;
import org.example.booking.dto.AvailableUnitsResponse;
import org.example.booking.dto.BookingDateRangeDto;
import org.example.booking.exception.InvalidRequestException;
import org.example.booking.model.Booking;
import org.example.booking.model.Unit;
import org.example.booking.repository.BookingRepository;
import org.example.booking.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnitAvailabilityServiceTest {

    private static final String TOTAL_UNITS_COUNT_KEY = "booking-service:total-units-count";
    private static final String BOOKED_UNITS_KEY_PREFIX = "booking-service:booked-unit-ids-by-date:";

    @Mock
    private AppConfig appConfig;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private UnitAvailabilityService unitAvailabilityService;

    @Captor
    private ArgumentCaptor<RedisCallback<?>> redisCallbackCaptor;

    private Booking testBooking;
    private Unit testUnit;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<String> dateKeys;

    @BeforeEach
    void setUp() {
        // Setup mock Redis operations
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);

        // Setup test data
        testUnit = Unit.builder()
                .id(1L)
                .build();

        checkInDate = LocalDate.now();
        checkOutDate = checkInDate.plusDays(3);

        testBooking = Booking.builder()
                .id(1L)
                .unit(testUnit)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .build();

        // Generate date keys for the booking period
        dateKeys = Arrays.asList(
                BOOKED_UNITS_KEY_PREFIX + checkInDate,
                BOOKED_UNITS_KEY_PREFIX + checkInDate.plusDays(1),
                BOOKED_UNITS_KEY_PREFIX + checkInDate.plusDays(2)
        );
    }

    @Test
    void whenIncrementTotalUnitsCount_shouldCallRedisIncrement() {
        // When
        unitAvailabilityService.incrementTotalUnitsCount();

        // Then
        verify(valueOperations).increment(TOTAL_UNITS_COUNT_KEY);
    }

    @Test
    void whenAddBookedDates_shouldAddUnitIdToDateKeys() {
        // When
        unitAvailabilityService.addBookedDates(testBooking);

        // Then
        verify(stringRedisTemplate).executePipelined(redisCallbackCaptor.capture());
        
        // We can't directly test the callback execution, but we can verify it was called
        // In a real scenario, we might use a Redis test container for integration testing
    }

    @Test
    void whenRemoveBookedDates_shouldRemoveUnitIdFromDateKeys() {
        // When
        unitAvailabilityService.removeBookedDates(testBooking);

        // Then
        verify(stringRedisTemplate).executePipelined(redisCallbackCaptor.capture());
        
        // We can't directly test the callback execution, but we can verify it was called
        // In a real scenario, we might use a Redis test container for integration testing
    }

    @Test
    void whenIsUnitAvailable_withAvailableUnit_shouldReturnTrue() {
        // Given
        List<Object> results = Arrays.asList(false, false, false); // Unit not booked on any date
        
        // Mock the pipeline execution to return our results
        when(stringRedisTemplate.executePipelined(any(RedisCallback.class))).thenReturn(results);

        // When
        boolean result = unitAvailabilityService.isUnitAvailable(1L, checkInDate, checkOutDate);

        // Then
        assertTrue(result);
        verify(stringRedisTemplate).executePipelined(any(RedisCallback.class));
    }

    @Test
    void whenIsUnitAvailable_withUnavailableUnit_shouldReturnFalse() {
        // Given
        List<Object> results = Arrays.asList(false, true, false); // Unit booked on one date
        
        // Mock the pipeline execution to return our results
        when(stringRedisTemplate.executePipelined(any(RedisCallback.class))).thenReturn(results);

        // When
        boolean result = unitAvailabilityService.isUnitAvailable(1L, checkInDate, checkOutDate);

        // Then
        assertFalse(result);
        verify(stringRedisTemplate).executePipelined(any(RedisCallback.class));
    }

    @Test
    void whenGetAvailableUnitsCount_withValidDates_shouldReturnCorrectCount() {
        // Given
        when(valueOperations.get(TOTAL_UNITS_COUNT_KEY)).thenReturn("10"); // 10 total units
        Set<String> unavailableUnitIds = new HashSet<>(Arrays.asList("2", "3", "4")); // 3 unavailable units
        when(setOperations.union(any(List.class))).thenReturn(unavailableUnitIds);

        // When
        AvailableUnitsResponse result = unitAvailabilityService.getAvailableUnitsCount(checkInDate, checkOutDate);

        // Then
        assertEquals(7, result.getAvailableUnitsCount()); // 10 total - 3 unavailable = 7 available
        verify(valueOperations).get(TOTAL_UNITS_COUNT_KEY);
        verify(setOperations).union(any(List.class));
    }

    @Test
    void whenGetAvailableUnitsCount_withNoTotalUnitsCount_shouldReturnZero() {
        // Given
        when(valueOperations.get(TOTAL_UNITS_COUNT_KEY)).thenReturn(null); // No total units count

        // When
        AvailableUnitsResponse result = unitAvailabilityService.getAvailableUnitsCount(checkInDate, checkOutDate);

        // Then
        assertEquals(0, result.getAvailableUnitsCount());
        verify(valueOperations).get(TOTAL_UNITS_COUNT_KEY);
        verify(setOperations, never()).union(any(List.class));
    }

    @Test
    void whenGetAvailableUnitsCount_withNoUnavailableUnits_shouldReturnTotalCount() {
        // Given
        when(valueOperations.get(TOTAL_UNITS_COUNT_KEY)).thenReturn("10"); // 10 total units
        when(setOperations.union(any(List.class))).thenReturn(Collections.emptySet()); // No unavailable units

        // When
        AvailableUnitsResponse result = unitAvailabilityService.getAvailableUnitsCount(checkInDate, checkOutDate);

        // Then
        assertEquals(10, result.getAvailableUnitsCount()); // All 10 units are available
        verify(valueOperations).get(TOTAL_UNITS_COUNT_KEY);
        verify(setOperations).union(any(List.class));
    }

    @Test
    void whenGetAvailableUnitsCount_withInvalidDates_shouldThrowException() {
        // Given
        LocalDate sameDate = LocalDate.now();

        // When & Then
        assertThrows(InvalidRequestException.class, () -> 
                unitAvailabilityService.getAvailableUnitsCount(sameDate, sameDate));
    }

    @Test
    void whenInitializeUnitAvailabilityCache_withRefreshEnabled_shouldReinitializeCache() {
        // Given
        when(appConfig.isRefreshCacheOnStartup()).thenReturn(true);
        when(stringRedisTemplate.hasKey(TOTAL_UNITS_COUNT_KEY)).thenReturn(true);
        when(unitRepository.count()).thenReturn(10L);
        
        // Mock scan operation for deleteKeysByPrefix
        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("key1", "key2");
        when(stringRedisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        
        // Mock booking repository
        List<BookingDateRangeDto> bookings = Collections.singletonList(
                new BookingDateRangeDto(1L, checkInDate, checkOutDate));
        when(bookingRepository.findActiveBookingRanges()).thenReturn(bookings);

        // When
        unitAvailabilityService.initializeUnitAvailabilityCache();

        // Then
        verify(stringRedisTemplate).delete(TOTAL_UNITS_COUNT_KEY);
        verify(stringRedisTemplate).scan(any(ScanOptions.class));
        verify(stringRedisTemplate).unlink(any(Set.class));
        verify(bookingRepository).findActiveBookingRanges();
        verify(stringRedisTemplate).executePipelined(any(RedisCallback.class));
        verify(valueOperations).set(eq(TOTAL_UNITS_COUNT_KEY), eq("10"));
    }

    @Test
    void whenInitializeUnitAvailabilityCache_withCacheExistsAndNoRefresh_shouldDoNothing() {
        // Given
        when(appConfig.isRefreshCacheOnStartup()).thenReturn(false);
        when(stringRedisTemplate.hasKey(TOTAL_UNITS_COUNT_KEY)).thenReturn(true);

        // When
        unitAvailabilityService.initializeUnitAvailabilityCache();

        // Then
        verify(stringRedisTemplate, never()).delete(anyString());
        verify(stringRedisTemplate, never()).scan(any(ScanOptions.class));
        verify(bookingRepository, never()).findActiveBookingRanges();
        verify(unitRepository, never()).count();
    }
}