package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StandardValuationPolicy implements ValuationPolicy {

	@Override
	public BigDecimal calculateValue(Bike bike, LocalDate date) {
		
		return bike.getBikeType().getReplacementValue();
		
	}
	
}
