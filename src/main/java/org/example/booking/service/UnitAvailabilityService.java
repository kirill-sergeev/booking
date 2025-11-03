package org.example.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booking.config.AppConfig;
import org.example.booking.dto.AvailableUnitsResponse;
import org.example.booking.dto.BookingDateRangeDto;
import org.example.booking.exception.InvalidRequestException;
import org.example.booking.model.Booking;
import org.example.booking.repository.BookingRepository;
import org.example.booking.repository.UnitRepository;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class UnitAvailabilityService {

    private static final String TOTAL_UNITS_COUNT_KEY = "booking-service:total-units-count";
    private static final String BOOKED_UNITS_KEY_PREFIX = "booking-service:booked-unit-ids-by-date:";

    private final AppConfig appConfig;
    private final UnitRepository unitRepository;
    private final BookingRepository bookingRepository;
    private final StringRedisTemplate stringRedisTemplate;

    public void incrementTotalUnitsCount() {
        stringRedisTemplate.opsForValue().increment(TOTAL_UNITS_COUNT_KEY);
    }

    public void addBookedDates(Booking booking) {
        String unitIdStr = String.valueOf(booking.getUnit().getId());
        List<String> dateKeys = getDateKeys(booking.getCheckInDate(), booking.getCheckOutDate());
        stringRedisTemplate.executePipelined((RedisCallback<?>) connection -> {
            dateKeys.forEach(key -> connection.setCommands().sAdd(key.getBytes(), unitIdStr.getBytes()));
            return null;
        });
    }

    public void removeBookedDates(Booking booking) {
        String unitIdStr = String.valueOf(booking.getUnit().getId());
        List<String> dateKeys = getDateKeys(booking.getCheckInDate(), booking.getCheckOutDate());
        stringRedisTemplate.executePipelined((RedisCallback<?>) connection -> {
            dateKeys.forEach(key -> connection.setCommands().sRem(key.getBytes(), unitIdStr.getBytes()));
            return null;
        });
    }

    public boolean isUnitAvailable(Long unitId, LocalDate checkInDate, LocalDate checkOutDate) {
        String unitIdStr = String.valueOf(unitId);
        List<String> dateKeys = getDateKeys(checkInDate, checkOutDate);

        List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<?>) connection -> {
            dateKeys.forEach(key -> connection.setCommands().sIsMember(key.getBytes(), unitIdStr.getBytes()));
            return null;
        });
        boolean isUnavailable = results.stream().anyMatch(result -> (result instanceof Boolean && (Boolean) result));
        return !isUnavailable;
    }

    public AvailableUnitsResponse getAvailableUnitsCount(LocalDate checkInDate, LocalDate checkOutDate) {
        validateSearchCriteria(checkInDate, checkOutDate);

        String allUnitsCountStr = stringRedisTemplate.opsForValue().get(TOTAL_UNITS_COUNT_KEY);
        if (allUnitsCountStr == null) {
            return new AvailableUnitsResponse(0);
        }

        int allUnitsCount = Integer.parseInt(allUnitsCountStr);
        List<String> dateKeys = getDateKeys(checkInDate, checkOutDate);
        Set<String> unavailableUnitIds = stringRedisTemplate.opsForSet().union(dateKeys);
        if (unavailableUnitIds == null || unavailableUnitIds.isEmpty()) {
            return new AvailableUnitsResponse(allUnitsCount);
        }

        return new AvailableUnitsResponse(allUnitsCount - unavailableUnitIds.size());
    }

    @Transactional(readOnly = true)
    public void initializeUnitAvailabilityCache() {
        if (stringRedisTemplate.hasKey(TOTAL_UNITS_COUNT_KEY) && !appConfig.isRefreshCacheOnStartup()) {
            log.info("Unit availability cache is already initialized.");
            return;
        }

        log.info("Starting unit availability cache initialization...");

        stringRedisTemplate.delete(TOTAL_UNITS_COUNT_KEY);
        deleteExistingBookedUnits();
        log.info("Cleared old cache keys.");

        List<BookingDateRangeDto> bookings = bookingRepository.findActiveBookingRanges();
        stringRedisTemplate.executePipelined((RedisCallback<?>) (connection) -> {
            for (BookingDateRangeDto booking : bookings) {
                String value = String.valueOf(booking.getUnitId());
                List<String> dateKeys = getDateKeys(booking.getCheckInDate(), booking.getCheckOutDate());
                dateKeys.forEach(key -> connection.setCommands().sAdd(key.getBytes(), value.getBytes()));
            }
            return null;
        });

        long allUnitIds = unitRepository.count();
        stringRedisTemplate.opsForValue().set(TOTAL_UNITS_COUNT_KEY, String.valueOf(allUnitIds));

        log.info("Unit availability cache initialization completed. Processed {} active bookings.", bookings.size());
    }

    private void deleteExistingBookedUnits() {
        Set<String> keysToDelete = new HashSet<>();
        try (Cursor<String> cursor = stringRedisTemplate.scan(
                ScanOptions.scanOptions().match(BOOKED_UNITS_KEY_PREFIX + "*").count(1000).build())) {
            while (cursor.hasNext()) {
                keysToDelete.add(cursor.next());
            }
        }
        if (!keysToDelete.isEmpty()) {
            stringRedisTemplate.unlink(keysToDelete);
        }
    }

    private List<String> getDateKeys(LocalDate checkInDate, LocalDate checkOutDate) {
        return checkInDate.datesUntil(checkOutDate)
                .map(date -> BOOKED_UNITS_KEY_PREFIX + date)
                .toList();
    }

    private void validateSearchCriteria(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate.isAfter(checkOutDate)
                || checkInDate.isEqual(checkOutDate)) {
            throw new InvalidRequestException("checkOutDate must be after checkInDate");
        }
    }
}