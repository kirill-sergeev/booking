package org.example.booking.service;

import org.example.booking.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MarkupServiceTest {

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private MarkupService markupService;

    @Test
    void whenCalculateTotalUnitCost_withStandardMarkup_shouldReturnCorrectTotal() {
        // Given
        BigDecimal baseCost = new BigDecimal("100.00");
        given(appConfig.getMarkupPercent()).willReturn(10); // 10% markup

        // When
        BigDecimal result = markupService.calculateTotalUnitCost(baseCost);

        // Then
        BigDecimal expected = new BigDecimal("110.00");
        assertEquals(expected, result);
    }

    @Test
    void whenCalculateTotalUnitCost_withZeroMarkup_shouldReturnBaseCost() {
        // Given
        BigDecimal baseCost = new BigDecimal("100.00");
        given(appConfig.getMarkupPercent()).willReturn(0); // 0% markup

        // When
        BigDecimal result = markupService.calculateTotalUnitCost(baseCost);

        // Then
        assertEquals(baseCost, result);
    }

    @Test
    void whenCalculateTotalUnitCost_withZeroBaseCost_shouldReturnZero() {
        // Given
        BigDecimal baseCost = BigDecimal.ZERO;
        given(appConfig.getMarkupPercent()).willReturn(10); // 10% markup

        // When
        BigDecimal result = markupService.calculateTotalUnitCost(baseCost);

        // Then
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result);
    }
}