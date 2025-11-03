package org.example.booking.service.startup;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booking.config.AppConfig;
import org.example.booking.model.AccommodationType;
import org.example.booking.model.Unit;
import org.example.booking.model.UnitEventType;
import org.example.booking.repository.UnitRepository;
import org.example.booking.service.UnitEventService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@AllArgsConstructor
public class DataGenerator {

    private static final String DESCRIPTION = "Randomly generated unit";

    private final AppConfig appConfig;
    private final UnitRepository unitRepository;
    private final UnitEventService unitEventService;

    public void generateData() {
        if (!appConfig.isGenerateData()) {
            log.info("Data generation disabled.");
            return;
        }

        int generateUnitsCount = appConfig.getGenerateUnitsCount();
        int alreadyGeneratedUnitsCount = unitRepository.countByDescriptionEquals(DESCRIPTION);
        log.info("Already generated units count: {}. Target is {}", alreadyGeneratedUnitsCount, generateUnitsCount);

        if (alreadyGeneratedUnitsCount >= generateUnitsCount) {
            log.info("Sufficient generated units already exist. Skipping data initialization.");
            return;
        }

        int unitsToCreate = generateUnitsCount - alreadyGeneratedUnitsCount;
        log.info("Generating {} new random units...", unitsToCreate);

        Random rand = ThreadLocalRandom.current();
        List<Unit> newUnits = new ArrayList<>(unitsToCreate);
        AccommodationType[] accommodationTypes = AccommodationType.values();

        for (int i = 0; i < unitsToCreate; i++) {
            Unit unit = Unit.builder()
                    .numberOfRooms(rand.nextInt(1, 5))
                    .accommodationType(accommodationTypes[rand.nextInt(accommodationTypes.length)])
                    .floor(rand.nextInt(0, 21))
                    .description(DESCRIPTION)
                    .baseCost(BigDecimal.valueOf(rand.nextDouble(50.0, 500.0)).setScale(2, RoundingMode.HALF_UP))
                    .createdAt(Instant.now())
                    .build();
            newUnits.add(unit);
        }

        List<Unit> savedUnits = unitRepository.saveAll(newUnits);
        for (Unit savedUnit : savedUnits) {
            unitEventService.logEvent(savedUnit, null, UnitEventType.UNIT_CREATED, "Unit randomly generated");
        }
        log.info("Successfully created {} new units.", savedUnits.size());
    }
}

