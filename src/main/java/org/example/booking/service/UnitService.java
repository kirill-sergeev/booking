package org.example.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booking.dto.UnitCreateRequest;
import org.example.booking.dto.UnitDto;
import org.example.booking.dto.UnitSearchRequest;
import org.example.booking.exception.InvalidRequestException;
import org.example.booking.exception.ResourceNotFoundException;
import org.example.booking.model.Unit;
import org.example.booking.model.UnitEventType;
import org.example.booking.repository.UnitRepository;
import org.example.booking.repository.UnitSpecification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;
    private final UnitSpecification unitSpecification;
    private final MarkupService markupService;
    private final UnitEventService unitEventService;
    private final UnitAvailabilityService unitAvailabilityService;

    @Transactional
    public UnitDto createUnit(UnitCreateRequest createRequest) {
        Unit unit = Unit.builder()
                .numberOfRooms(createRequest.getNumberOfRooms())
                .accommodationType(createRequest.getAccommodationType())
                .floor(createRequest.getFloor())
                .baseCost(createRequest.getBaseCost())
                .description(createRequest.getDescription())
                .build();

        Unit savedUnit = unitRepository.save(unit);
        unitEventService.logEvent(savedUnit, null, UnitEventType.UNIT_CREATED, "Unit created via API");
        unitAvailabilityService.incrementTotalUnitsCount();

        log.info("New unit created with id: {}", savedUnit.getId());
        return toDto(savedUnit);
    }

    @Transactional(readOnly = true)
    public UnitDto getUnitById(Long id) {
        return unitRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Slice<UnitDto> findAvailableUnits(UnitSearchRequest criteria, Pageable pageable) {
        log.debug("Searching for units with criteria: {} and pageable: {}", criteria, pageable);
        validateSearchCriteria(criteria);
        var spec = unitSpecification.findByCriteria(criteria);
        return unitRepository.findAllSliced(spec, pageable).map(this::toDto);
    }

    private void validateSearchCriteria(UnitSearchRequest criteria) {
        if (criteria.getMaxCost() != null
                && criteria.getMinCost() != null
                && criteria.getMinCost().compareTo(criteria.getMaxCost()) > 0) {
            throw new InvalidRequestException("minCost must be less than or equal to maxCost");
        }

        if (criteria.getCheckInDate().isAfter(criteria.getCheckOutDate())
                || criteria.getCheckInDate().isEqual(criteria.getCheckOutDate())) {
            throw new InvalidRequestException("checkOutDate must be after checkInDate");
        }
    }

    private UnitDto toDto(Unit unit) {
        return new UnitDto(
                unit.getId(),
                unit.getNumberOfRooms(),
                unit.getAccommodationType(),
                unit.getFloor(),
                markupService.calculateTotalUnitCost(unit.getBaseCost()),
                unit.getDescription(),
                unit.getCreatedAt()
        );
    }
}
