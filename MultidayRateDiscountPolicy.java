package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.HashMap;
import java.time.temporal.ChronoUnit;

public class MultidayRateDiscountPolicy implements PricingPolicy {
	
	private int maxDaysNoDiscount;
	private int maxDaysSmallDiscount;
	private int maxDaysMediumDiscount;
	private int smallDiscountPercentage;
	private int mediumDiscountPercentage;
	private int maxDiscountPercentage;
	
	private HashMap<Utils.EBikeType , BigDecimal> prices = new HashMap<Utils.EBikeType , BigDecimal>();
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
	
		BigDecimal roundedPrice = dailyPrice.setScale(2, RoundingMode.HALF_UP);
		
		prices.put(bikeType.getType(), roundedPrice);
		
	}

	@Override
	public BigDecimal calculatePrice(Collection<Bike> bikes, DateRange duration) {
		
		BigDecimal price = new BigDecimal(0);

		for (Bike b : bikes) {
			assert(prices.containsKey(b.getBikeType().getType()));
			
			price = price.add(prices.get(b.getBikeType().getType()));		
		}
		
		int daysInt = computeNumDays(duration);
		BigDecimal numDays = new BigDecimal(daysInt);

		price = price.multiply(numDays);

		assert(numDays.compareTo(BigDecimal.ZERO) >= 0);                                                    // It would be very surprising if this test failed. But better safe than sorry. 
		
		BigDecimal multiplicand;
		if (daysInt <= maxDaysNoDiscount) {
			multiplicand = new BigDecimal(1);
		} else if (daysInt <= maxDaysSmallDiscount) {
			multiplicand = new BigDecimal(1.0d - ((double) smallDiscountPercentage) / 100.0d);
		} else if(daysInt <= maxDaysMediumDiscount) {
			multiplicand = new BigDecimal(1.0d - ((double) mediumDiscountPercentage) / 100.0d);
		} else {
			multiplicand = new BigDecimal(1.0d - ((double) maxDiscountPercentage) / 100.0d);
		}
		
		price = price.multiply(multiplicand);
		price = price.setScale(2,RoundingMode.HALF_UP);
		
		assert(price.compareTo(new BigDecimal(0)) >= 0);
		return price;
	}

	public int computeNumDays(DateRange duration) {
		int difference = (int)duration.toDays();
		
		assert(difference >= 0);
		return difference + 1;                                                  // We add 1 to the difference to get the number of days. 
		                                                                        // Otherwise one could book a bike from 8 a.m. to 8 p.m. for free
	}
	
}