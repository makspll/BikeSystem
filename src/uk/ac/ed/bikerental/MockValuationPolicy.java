package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MockValuationPolicy implements ValuationPolicy {
    
    public float linearPriceDecayPerYear;
    
    public MockValuationPolicy() {
        linearPriceDecayPerYear = (float) 0.1;
    }

    @Override
    public BigDecimal calculateValue(Bike bike, LocalDate date) {
        
        LocalDate now = LocalDate.now();
        long ageInYears = date.until(now, ChronoUnit.YEARS);
        
        BigDecimal deposit = bike.getBikeType().getReplacementValue()
                                .multiply(new BigDecimal (Math.pow(1 - linearPriceDecayPerYear, ageInYears)));
        return deposit;
    }
    
    
    
}
