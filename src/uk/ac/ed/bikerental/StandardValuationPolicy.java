package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class StandardValuationPolicy implements ValuationPolicy {

	public float depositRate;

	public StandardValuationPolicy() {
		depositRate = (float) 1;
	}
	
	public StandardValuationPolicy(float dr)
	{
		depositRate = dr;
	}

	@Override
	public BigDecimal calculateValue(Bike bike, LocalDate date) {
		//full replacement value * deposit rate, rounded appropriately
		return bike.getBikeType().getReplacementValue().multiply(new BigDecimal(depositRate)).setScale(2,RoundingMode.HALF_EVEN);
	}
	
}
