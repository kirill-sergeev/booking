package org.example.booking.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "booking")
public class AppConfig {

    private int markupPercent;
    private int cancellationMinutes;
    private int generateUnitsCount;
    private boolean generateData;
    private boolean refreshCacheOnStartup;
}

