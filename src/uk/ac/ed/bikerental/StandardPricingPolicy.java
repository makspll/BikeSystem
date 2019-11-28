package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;

public class StandardPricingPolicy implements PricingPolicy {
    
    private HashMap<Utils.EBikeType , BigDecimal> prices = new HashMap<Utils.EBikeType , BigDecimal>();
    
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
