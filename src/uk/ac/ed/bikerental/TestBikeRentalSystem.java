package uk.ac.ed.bikerental;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;

import org.junit.jupiter.api.*;

import uk.ac.ed.bikerental.Utils.EBikeType;
import uk.ac.ed.bikerental.Utils.ECondition;

public class TestBikeRentalSystem
{
  
	private BikeRentalSystem brs;
	
    @BeforeAll
    void setUp()
    {
    	DeliveryServiceFactory dsf = new DeliveryServiceFactory();
    	dsf.setupMockDeliveryService();
    	
    	LocalDate date = LocalDate.now();
    	
        brs = new BikeRentalSystem(
        		dsf.getDeliveryService(),
        		date
        );
        
    }
    
    @Test
    void testRegisterBikeType() {
    	EBikeType hybrid = EBikeType.HYBRID;
    	BigDecimal hybridRepValue = new BigDecimal(200);
    	brs.registerBikeType(hybrid, hybridRepValue);
    	assertTrue("Bike type did not get added successfully" , brs.getBikeTypes().contains(hybrid));
    	
    	EBikeType alsoHybrid = EBikeType.HYBRID;
    	BigDecimal higherHybridRepValue = new BigDecimal(300);
    	assertThrows(Exception.class, () -> {
    		brs.registerBikeType(alsoHybrid, higherHybridRepValue);
    	});
    	// This doesn't really test the BikeRentalSystem, but rather my understanding of equality for Enums
    	assertTrue("Equality for types doesn't work like this" , brs.getBikeTypes().contains(alsoHybrid));
    	
    	EBikeType mountain = EBikeType.MOUNTAIN;
    	BigDecimal mountainRepValue = new BigDecimal(500);
    	brs.registerBikeType(mountain, mountainRepValue);
    	assertTrue("HYBRID is not in the List at this point" , brs.getBikeTypes().contains(hybrid));
    	assertTrue("MOUNTAIN has not been added" , brs.getBikeTypes().contains(mountain));
    }

    @Test
    void testRegisterBike()
    {
    	EBikeType hybrid = EBikeType.HYBRID;
    	BigDecimal hybridRepValue = new BigDecimal(200);
    	BikeType hybridType = new BikeType(hybrid, hybridRepValue);
    	ECondition cond = ECondition.AVERAGE;
    	LocalDate madeOn = LocalDate.EPOCH; // This is a 50 year old bike, but the condition is alright
    	
    	// This first call should return an exception because we haven't registered the bike type. 
    	assertThrows(Exception.class, () -> {
    		Bike b = brs.registerBike(hybridType, cond, madeOn);
    	});
    	
    	// This test is only valid if registerBikeType is working, sadly. 
    	brs.registerBikeType(hybrid, hybridRepValue);
    	Bike b = brs.registerBike(hybridType, cond, madeOn);
    	assertTrue("The type of the bike has changed" , b.getBikeType().getType() == hybrid);
    	assertTrue("The replacement value is different" , b.getBikeType().getReplacementValue().equals(hybridRepValue));
    	assertTrue("The condition of the bike has changed" , b.getCondition() == cond);
    }
    
    @Test
    void testRegisterProvider() {
    	Location loc = new Location("EH37QZ" , "42 Cool Town Road");
    	StandardValuationPolicy vp = new StandardValuationPolicy((float) 0.3);
    	StandardPricingPolicy pp = new StandardPricingPolicy();
    	brs.registerProvider(loc, "911", "all day every day", vp, pp);
    	
    	boolean containsID = false;
    	for (BikeProvider bp: brs.getProviders()) {
    		if (bp.getId() == 1) containsID = true;
    	}
    	assertTrue("BikeProvider has not been added or ID hasn't been set properly" , containsID);
    	
    	Location loc2 = new Location("XD3LOL" , "69 Old Town Road");
    	StandardValuationPolicy vp2 = new StandardValuationPolicy((float) 0.7);
    	StandardPricingPolicy pp2 = new StandardPricingPolicy();
    	brs.registerProvider(loc2, "10110", "sometimes", vp2, pp2);
    	
    	containsID = true;
    	for (BikeProvider bp: brs.getProviders()) {
    		if (bp.getId() == 2) {
    			containsID = true;
    			assertTrue("ID is set to the wrong provider, or address was changed" , bp.getLocation().getAddress().equalsIgnoreCase("69 Old Town Road"));
    		}
    	}
    	assertTrue("second BikeProvider has not been added or ID hasn't been set properly" , containsID);
    }
    
    @Test
    void testStepDateForward() {
    	LocalDate ini = brs.getDate();
    	brs.stepDateForward();
    	LocalDate post = brs.getDate();
    	assertTrue("Date has not been advanced properly", ini.datesUntil(post).toArray().length==1);
    }
}