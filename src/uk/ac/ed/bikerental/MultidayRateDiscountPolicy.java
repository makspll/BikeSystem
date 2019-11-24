package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.HashMap;

public class MultidayRateDiscountPolicy implements PricingPolicy {
	
	private int maxDaysNoDiscount;
	private int maxDaysSmallDiscount;
	private int maxDaysMediumDiscount;
	private int smallDiscountPercentage;
	private int mediumDiscountPercentage;
	private int maxDiscountPercentage;
	
	HashMap<BikeType , BigDecimal> prices = new HashMap<BikeType , BigDecimal>();
	// This class doesn't need to know who the provider is. The provider just needs to know that this is their pricing policy. 

	public MultidayRateDiscountPolicy(int maxNo, int maxSmall, int maxMed, int discount) {
		maxDaysNoDiscount = maxNo;
		maxDaysSmallDiscount = maxSmall;
		maxDaysMediumDiscount = maxMed;
		smallDiscountPercentage = discount;
		mediumDiscountPercentage = 2*discount;
		maxDiscountPercentage = 3*discount;
	}
	
	@Override
	public void setDailyRentalPrice(BikeType bikeType, BigDecimal dailyPrice) {
	
		prices.put(bikeType, dailyPrice);
		
	}

	@Override
	public BigDecimal calculatePrice(Collection<Bike> bikes, DateRange duration) {
		
		BigDecimal price = new BigDecimal(0);
		
		for (Bike b : bikes) {
			price = price.add(prices.get(b.getType()));		
		}
		
		int numDays = computeNumDays(duration);
		
		assert(numDays > 0);                                                    // It would be very surprising if this test failed. But better safe than sorry. 
		
		BigDecimal multiplicand;
		if (numDays <= maxDaysNoDiscount) {
			multiplicand = new BigDecimal(1);
		} else if (numDays <= maxDaysSmallDiscount) {
			multiplicand = new BigDecimal(1.0 - ((double) smallDiscountPercentage) / 100.0);
		} else if(numDays <= maxDaysMediumDiscount) {
			multiplicand = new BigDecimal(1.0 - ((double) mediumDiscountPercentage) / 100.0);
		} else {
			multiplicand = new BigDecimal(1.0 - ((double) maxDiscountPercentage) / 100.0);
		}
		
		price = price.multiply(multiplicand);
		return price;
	}

	public int computeNumDays(DateRange duration) {
		LocalDate start = duration.getStart();
		LocalDate end = duration.getEnd();
		
		int startDay = start.get(ChronoField.EPOCH_DAY);
		int endDay = end.get(ChronoField.EPOCH_DAY);
		
		int difference = endDay - startDay;
		
		assert(difference >= 0);
		
		return difference + 1;                                                  // We add 1 to the difference to get the number of days. 
		                                                                        // Otherwise one could book a bike from 8 a.m. to 8 p.m. for free
	}
	
}
