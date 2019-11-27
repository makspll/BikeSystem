package uk.ac.ed.bikerental;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.jupiter.api.*;

import uk.ac.ed.bikerental.Utils.EBikeType;
import uk.ac.ed.bikerental.Utils.ECondition;

public class TestBikeRentalSystem
{
  
	private BikeRentalSystem brs;
	
    @BeforeEach
    void setUp()
    {
    	DeliveryServiceFactory.setupMockDeliveryService();
    	LocalDate date = LocalDate.now();
    	
        brs = new BikeRentalSystem(
        		DeliveryServiceFactory.getDeliveryService(),
        		date
        );
        
    }
    
    @Test
    void testRegisterBikeType() {
    	EBikeType hybrid = EBikeType.HYBRID;
		BigDecimal hybridRepValue = new BigDecimal(200);
		try{
			brs.registerBikeType(hybrid, hybridRepValue);
		} catch(Exception e)
		{
			assertTrue(false,"exception when registering first bike type");
		}

    	assertTrue(brs.getEBikeTypes().contains(hybrid),"Bike type did not get added successfully");
    	
    	EBikeType alsoHybrid = EBikeType.HYBRID;
    	BigDecimal higherHybridRepValue = new BigDecimal(300);
    	assertThrows(Exception.class, () -> {
    		brs.registerBikeType(alsoHybrid, higherHybridRepValue);
    	});
    	// This doesn't really test the BikeRentalSystem, but rather my understanding of equality for Enums
    	assertTrue(brs.getEBikeTypes().contains(alsoHybrid),"Equality for types doesn't work like this" );
    	
    	EBikeType mountain = EBikeType.MOUNTAIN;
		BigDecimal mountainRepValue = new BigDecimal(500);
		try{
		brs.registerBikeType(mountain, mountainRepValue);
		} catch(Exception e)
		{
			assertTrue(false,"exception when registering second bike type");
		}
    	assertTrue(brs.getEBikeTypes().contains(hybrid),"HYBRID is not in the List at this point" );
    	assertTrue(brs.getEBikeTypes().contains(mountain),"MOUNTAIN has not been added" );
    }

    @Test
    void testRegisterBike()
    {
    	EBikeType hybrid = EBikeType.HYBRID;
    	BigDecimal hybridRepValue = new BigDecimal(200);
    	BikeType hybridType = new BikeType(hybrid, hybridRepValue);
    	ECondition cond = ECondition.AVERAGE;
    	LocalDate madeOn = LocalDate.ofEpochDay(0); // This is a 50 year old bike, but the condition is alright
    	BikeProvider bpr = new BikeProvider(brs,new Location("aaaaaa","aaaaaa"),new StandardValuationPolicy(1),new StandardPricingPolicy());
		
		// This first call should return an exception because we haven't registered the bike type or provider. 
    	assertThrows(Exception.class, () -> {
    		brs.registerBike(hybridType, cond, madeOn,1);
    	});
    	
		// This test is only valid if registerBikeType and registerBikeProvider are working, sadly. 
		try{
			Location loc = new Location("EH37QZ" , "42 Cool Town Road");
	    	StandardValuationPolicy vp = new StandardValuationPolicy((float) 0.3);
	    	StandardPricingPolicy pp = new StandardPricingPolicy();
	    	brs.registerProvider(loc, vp, pp);
	    	
    		brs.registerBikeType(hybrid, hybridRepValue);
    		
    		brs.registerBike(hybridType, cond, madeOn,0);
    		
    		assertTrue(bpr.getBikeWithCode(1).getBikeType().getType() == hybrid,"The type of the bike has changed" );
    		assertTrue(bpr.getBikeWithCode(1).getBikeType().getReplacementValue().equals(hybridRepValue),"The replacement value is different" );
			assertTrue(bpr.getBikeWithCode(1).getCondition() == cond,"The condition of the bike has changed" );
		}catch(Exception e)
		{
			assertTrue(false,"exception when registering bike");
		}
    }
    
    @Test
    void testRegisterProvider() {
    	Location loc = new Location("EH37QZ" , "42 Cool Town Road");
    	StandardValuationPolicy vp = new StandardValuationPolicy();
    	StandardPricingPolicy pp = new StandardPricingPolicy();
    	brs.registerProvider(loc, vp, pp);
    	
    	boolean containsID = false;
    	for (BikeProvider bp: brs.getProviders()) {
    		if (bp.getId() == 1) containsID = true;
    	}
    	assertTrue(containsID,"BikeProvider has not been added or ID hasn't been set properly");
    	
    	Location loc2 = new Location("XD3LOL" , "69 Old Town Road");
    	StandardValuationPolicy vp2 = new StandardValuationPolicy((float) 0.7);
    	StandardPricingPolicy pp2 = new StandardPricingPolicy();
    	brs.registerProvider(loc2, vp2, pp2);
    	
    	containsID = true;
    	for (BikeProvider bp: brs.getProviders()) {
    		if (bp.getId() == 2) {
    			containsID = true;
    			assertTrue(bp.getLocation().getAddress().equalsIgnoreCase("69 Old Town Road"),"ID is set to the wrong provider, or address was changed" );
    		}
    	}
    	assertTrue(containsID,"second BikeProvider has not been added or ID hasn't been set properly");
    }
    
    @Test
    void testStepDateForward() {
    	LocalDate ini = brs.getDate();
    	brs.stepDateForward(); 
    	LocalDate post = brs.getDate();
    	assertTrue(ini.until(post).getDays()==1,"Date has not been advanced properly");
    }
}