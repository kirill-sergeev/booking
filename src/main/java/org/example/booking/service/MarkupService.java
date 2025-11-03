package org.example.booking.service;

import lombok.AllArgsConstructor;
import org.example.booking.config.AppConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@AllArgsConstructor
public class MarkupService {

    private final AppConfig appConfig;

    public BigDecimal calculateTotalUnitCost(BigDecimal baseCost) {
        BigDecimal markup = BigDecimal.valueOf(appConfig.getMarkupPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal markupAmount = baseCost.multiply(markup);
        return baseCost.add(markupAmount).setScale(2, RoundingMode.HALF_UP);
    }
}
