package org.example.booking.service.startup;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.example.booking.service.UnitAvailabilityService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class AppStartupCoordinator implements CommandLineRunner {

    private final DataGenerator dataGenerator;
    private final UnitAvailabilityService unitAvailabilityService;

    @Override
    @SchedulerLock(name = "application-startup-lock")
    public void run(String... args) {
        log.info("Starting application initialization...");
        try {
            dataGenerator.generateData();
            unitAvailabilityService.initializeUnitAvailabilityCache();
            log.info("Application initialization completed successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Failed during application initialization", e);
        }
    }
}
