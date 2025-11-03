package org.example.booking.service;

import org.example.booking.dto.UnitCreateRequest;
import org.example.booking.dto.UnitDto;
import org.example.booking.dto.UnitSearchRequest;
import org.example.booking.exception.InvalidRequestException;
import org.example.booking.exception.ResourceNotFoundException;
import org.example.booking.model.AccommodationType;
import org.example.booking.model.Unit;
import org.example.booking.model.UnitEventType;
import org.example.booking.repository.UnitRepository;
import org.example.booking.repository.UnitSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UnitSpecification unitSpecification;

    @Mock
    private MarkupService markupService;

    @Mock
    private UnitEventService unitEventService;

    @Mock
    private UnitAvailabilityService unitAvailabilityService;

    @InjectMocks
    private UnitService unitService;

    @Captor
    private ArgumentCaptor<Unit> unitCaptor;

    private UnitCreateRequest createRequest;
    private Unit testUnit;
    private UnitSearchRequest validSearchRequest;
    private UnitSearchRequest invalidPriceSearchRequest;
    private UnitSearchRequest invalidDateSearchRequest;

    @BeforeEach
    void setUp() {
        // Setup create request
        createRequest = new UnitCreateRequest(
                2,
                AccommodationType.FLAT,
                3,
                new BigDecimal("100.00"),
                "Nice apartment with balcony"
        );

        // Setup test unit
        testUnit = Unit.builder()
                .id(1L)
                .numberOfRooms(2)
                .accommodationType(AccommodationType.FLAT)
                .floor(3)
                .baseCost(new BigDecimal("100.00"))
                .description("Nice apartment with balcony")
                .createdAt(Instant.now())
                .build();

        // Setup valid search request
        LocalDate today = LocalDate.now();
        validSearchRequest = new UnitSearchRequest();
        validSearchRequest.setNumberOfRooms(2);
        validSearchRequest.setAccommodationType(AccommodationType.FLAT);
        validSearchRequest.setFloor(3);
        validSearchRequest.setMinCost(new BigDecimal("50.00"));
        validSearchRequest.setMaxCost(new BigDecimal("200.00"));
        validSearchRequest.setCheckInDate(today);
        validSearchRequest.setCheckOutDate(today.plusDays(5));

        // Setup invalid price search request (minCost > maxCost)
        invalidPriceSearchRequest = new UnitSearchRequest();
        invalidPriceSearchRequest.setNumberOfRooms(2);
        invalidPriceSearchRequest.setAccommodationType(AccommodationType.FLAT);
        invalidPriceSearchRequest.setFloor(3);
        invalidPriceSearchRequest.setMinCost(new BigDecimal("200.00"));
        invalidPriceSearchRequest.setMaxCost(new BigDecimal("100.00"));
        invalidPriceSearchRequest.setCheckInDate(today);
        invalidPriceSearchRequest.setCheckOutDate(today.plusDays(5));

        // Setup invalid date search request (checkInDate = checkOutDate)
        invalidDateSearchRequest = new UnitSearchRequest();
        invalidDateSearchRequest.setNumberOfRooms(2);
        invalidDateSearchRequest.setAccommodationType(AccommodationType.FLAT);
        invalidDateSearchRequest.setFloor(3);
        invalidDateSearchRequest.setMinCost(new BigDecimal("50.00"));
        invalidDateSearchRequest.setMaxCost(new BigDecimal("200.00"));
        invalidDateSearchRequest.setCheckInDate(today);
        invalidDateSearchRequest.setCheckOutDate(today);
    }

    @Test
    void whenCreateUnit_shouldReturnUnitDto() {
        // Given
        given(unitRepository.save(any(Unit.class))).willAnswer(invocation -> {
            Unit unit = invocation.getArgument(0);
            unit.setId(1L);
            unit.setCreatedAt(Instant.now());
            return unit;
        });
        given(markupService.calculateTotalUnitCost(any(BigDecimal.class)))
                .willReturn(new BigDecimal("110.00"));

        // When
        UnitDto result = unitService.createUnit(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2, result.getNumberOfRooms());
        assertEquals(AccommodationType.FLAT, result.getAccommodationType());
        assertEquals(3, result.getFloor());
        assertEquals(new BigDecimal("110.00"), result.getTotalCost());
        assertEquals("Nice apartment with balcony", result.getDescription());
        assertNotNull(result.getCreatedAt());

        // Verify unit was saved
        verify(unitRepository).save(unitCaptor.capture());
        Unit savedUnit = unitCaptor.getValue();
        assertEquals(2, savedUnit.getNumberOfRooms());
        assertEquals(AccommodationType.FLAT, savedUnit.getAccommodationType());
        assertEquals(3, savedUnit.getFloor());
        assertEquals(new BigDecimal("100.00"), savedUnit.getBaseCost());
        assertEquals("Nice apartment with balcony", savedUnit.getDescription());

        // Verify event was logged
        verify(unitEventService).logEvent(
                savedUnit,
                null,
                UnitEventType.UNIT_CREATED,
                "Unit created via API");

        // Verify total units count was incremented
        verify(unitAvailabilityService).incrementTotalUnitsCount();
    }

    @Test
    void whenGetUnitById_withExistingId_shouldReturnUnitDto() {
        // Given
        given(unitRepository.findById(1L)).willReturn(Optional.of(testUnit));
        given(markupService.calculateTotalUnitCost(any(BigDecimal.class)))
                .willReturn(new BigDecimal("110.00"));

        // When
        UnitDto result = unitService.getUnitById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2, result.getNumberOfRooms());
        assertEquals(AccommodationType.FLAT, result.getAccommodationType());
        assertEquals(3, result.getFloor());
        assertEquals(new BigDecimal("110.00"), result.getTotalCost());
        assertEquals("Nice apartment with balcony", result.getDescription());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void whenGetUnitById_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        given(unitRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> unitService.getUnitById(999L));
        assertEquals("Unit not found with id: 999", exception.getMessage());
    }

    @Test
    void whenFindAvailableUnits_withValidCriteria_shouldReturnUnits() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Specification<Unit> spec = any();
        given(unitSpecification.findByCriteria(validSearchRequest)).willReturn(spec);
        given(unitRepository.findAllSliced(spec, pageable))
                .willReturn(new SliceImpl<>(List.of(testUnit)));
        given(markupService.calculateTotalUnitCost(any(BigDecimal.class)))
                .willReturn(new BigDecimal("110.00"));

        // When
        var result = unitService.findAvailableUnits(validSearchRequest, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        UnitDto unitDto = result.getContent().get(0);
        assertEquals(1L, unitDto.getId());
        assertEquals(2, unitDto.getNumberOfRooms());
        assertEquals(AccommodationType.FLAT, unitDto.getAccommodationType());
        assertEquals(3, unitDto.getFloor());
        assertEquals(new BigDecimal("110.00"), unitDto.getTotalCost());
        assertEquals("Nice apartment with balcony", unitDto.getDescription());
    }

    @Test
    void whenFindAvailableUnits_withInvalidPriceCriteria_shouldThrowInvalidRequestException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> unitService.findAvailableUnits(invalidPriceSearchRequest, pageable));
        assertEquals("minCost must be less than or equal to maxCost", exception.getMessage());
    }

    @Test
    void whenFindAvailableUnits_withInvalidDateCriteria_shouldThrowInvalidRequestException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> unitService.findAvailableUnits(invalidDateSearchRequest, pageable));
        assertEquals("checkOutDate must be after checkInDate", exception.getMessage());
    }
}