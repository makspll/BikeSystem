package uk.ac.ed.bikerental;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import uk.ac.ed.bikerental.Utils.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SystemTests {

    //TODO tests for the use cases
	BikeRentalSystem brs;
	BikeProvider bpr1,bpr2;
	Location cloc,loc,loc2,locFar;
	Customer c;
	
    @BeforeEach
    void setUp() throws Exception {
		/*
		setting up a system with 2 providers, at the customers location, 3 bike types, and 5 bikes
		*/
        DeliveryServiceFactory.setupMockDeliveryService();
		DeliveryService ds = DeliveryServiceFactory.getDeliveryService();
		brs = new BikeRentalSystem(ds, LocalDate.now());
		
		Location cLoc = new Location("EH12FJ", "79 Street Street");
		c = new Customer(brs, "Bob", "Bobertson" , "911" , cLoc);
		
		Location loc = new Location("EH89QX", "5 Main Street");
		StandardPricingPolicy spp = new StandardPricingPolicy();
		StandardValuationPolicy svp = new StandardValuationPolicy(1f);
		bpr1 = brs.getProviderWithID(brs.registerProvider(loc, svp, spp));
		
		Location loc2 = new Location("EH89BL", "12 Side Street");
		bpr2 = brs.getProviderWithID(brs.registerProvider(loc2, svp, spp));
		
		locFar = new Location("AB11B12","21 asd st");

		try{
		    brs.registerBikeType(EBikeType.MOUNTAIN, new BigDecimal(500));
		    brs.registerBikeType(EBikeType.HYBRID, new BigDecimal(700));
		    brs.registerBikeType(EBikeType.ROAD, new BigDecimal(300));
		
		    brs.registerBike(brs.getType(EBikeType.MOUNTAIN),ECondition.BAD,  LocalDate.MIN, 1);
		    brs.registerBike(brs.getType(EBikeType.MOUNTAIN), ECondition.AVERAGE, LocalDate.now(), 1);
		    brs.registerBike(brs.getType(EBikeType.HYBRID), ECondition.GOOD, LocalDate.now(), 1);
		    brs.registerBike(brs.getType(EBikeType.HYBRID), ECondition.NEW, LocalDate.now(), 2);
		    brs.registerBike(brs.getType(EBikeType.ROAD), ECondition.NEW, LocalDate.MAX, 2);
		}catch(Exception e)
		{
			throw new Exception("bikes couldnt be registered to test");
		}

		//test we set up correctly
		if(brs.getProviders().size() != 2)
		{
			throw new Exception("the providers weren't set up correctly");
		}
		if(bpr1.getBikes().size() != 3)
		{
			throw new Exception("provider 1 didn't have its bikes registered correctly");
		}
		if(bpr2.getBikes().size() != 2)
		{
			throw new Exception("provider 2 didn't have its bikes registered correctly");
		}
    }
    
    @Test
    void bookingAQuote()
    {
    }

    @Test
    void findingAQuote()
    {
		//test with all available bikes, we should have all dates free
		List<EBikeType> bikes = new ArrayList<EBikeType>();
		
		bikes.add(EBikeType.MOUNTAIN);
		//quote for mountain and hybrid bike over 4 days at location with 2 bike providers
		List<Quote> quotes = null;
		try{
			quotes = c.findQuotes(new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,4)),bikes, loc);
		}catch (Exception e)
		{
			assertTrue(false,"findQuotes threw an exception");
		}
		assertNotNull(quotes,"bikerental system getQuotes threw an exception");
		//only one provider should be able to accomodate, so we should be able to get only one quote
		//but we should get a single quote
		assertTrue(quotes.size() > 0, "not enough quotes");
		assertEquals(1,quotes.size(), "too many quotes");

    }

    @Test
    void returningBike()
    {
        
    }

}
