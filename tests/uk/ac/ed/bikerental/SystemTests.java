package uk.ac.ed.bikerental;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import uk.ac.ed.bikerental.Utils.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;

public class SystemTests {

	// TODO : include table that tells the marker where to find the tests for which use case
	
	BikeRentalSystem brs;
	int bpr1ID,bpr2ID;
	int b1,b2,b3,b4,b5;
	Location cloc,c2loc,loc,loc2;
	Customer c, c2;

    @BeforeEach
    void setUp() throws Exception {
		/*
			setting up a system with 2 providers, at the customers location, 3 bike types, and 5 bikes
			we believe this is enough data to test most variations in code flow
		*/
		DeliveryServiceFactory.setupMockDeliveryService();
		resetDeliveryService();
		StandardPricingPolicy spp = new StandardPricingPolicy();
		StandardValuationPolicy svp = new StandardValuationPolicy(1f);

		brs = new BikeRentalSystem(LocalDate.now());
		
		cloc = new Location("EH12FJ", "79 Street Street");
		c = new Customer(brs, "Bob", "Bobertson" , "911" , cloc);
		c2loc = new Location("BQ36ZU", "123 Number Street");
		c2 = new Customer(brs, "Rob", "Beasley" , "0123/456789", c2loc);
		
		loc = new Location("EH89QX", "5 Main Street");
		bpr1ID = brs.registerProvider(loc, svp, spp);
		
		loc2 = new Location("EH89BL", "12 Side Street");
		bpr2ID = brs.registerProvider(loc2, svp, spp);
		
		//register bikes and bike types, this requires the registering to work correctly
		try {
			BikeType bt1 = brs.registerBikeType(EBikeType.MOUNTAIN, new BigDecimal(500));
			spp.setDailyRentalPrice(bt1, new BigDecimal(10));

			BikeType bt2 = brs.registerBikeType(EBikeType.HYBRID, new BigDecimal(700));
			spp.setDailyRentalPrice(bt2, new BigDecimal(10));
		} catch (Exception e) {
			assertTrue(false, "SETUP:Exception occurred when registering bike type");
		}
		
		try {
			b1 = brs.registerBike(brs.getType(EBikeType.MOUNTAIN),ECondition.BAD,  LocalDate.now().minusYears(5), bpr1ID);
			b2 = brs.registerBike(brs.getType(EBikeType.MOUNTAIN), ECondition.BAD, LocalDate.now().minusYears(5), bpr1ID);
			b3 = brs.registerBike(brs.getType(EBikeType.HYBRID), ECondition.NEW, LocalDate.now().minusYears(1), bpr1ID);
			b4 = brs.registerBike(brs.getType(EBikeType.HYBRID), ECondition.NEW, LocalDate.now().minusDays(1), bpr2ID);
			b5 = brs.registerBike(brs.getType(EBikeType.HYBRID), ECondition.NEW, LocalDate.now().minusDays(1), bpr2ID);
		} catch (Exception e) {
			assertTrue(false, "SETUP:Exception occurred when registering bikes");
		}

		//testing that we set up correctly:
		if(brs.getProviders().size() != 2)
		{
			throw new Exception("the providers weren't set up correctly");
		}
		if(brs.getProviderWithID(bpr1ID).getBikes().size() != 3)
		{
			throw new Exception("provider 1 didn't have its bikes registered correctly");
		}
		if(brs.getProviderWithID(bpr2ID).getBikes().size() != 2)
		{
			throw new Exception("provider 2 didn't have its bikes registered correctly");
		}
    }

	//////////////////////////////////////TESTS FOR FINDING QUOTE USE CASE START HERE//////////////////////////////////////////
    @Test
    void returningBikeToOriginalProvider()
    {
        /*
            this test tests the use case of a customer returning a bike to its original provider
        */

        //we first set up a booking, and set it to the status BIKES_AWAY
        DateRange dates = new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,3));
        //attempt to set the bikes' state to be out of the shop
        Bike bikeB1 = null;
        Bike bikeB2 = null;
        try{
            bikeB1 = brs.getProviderWithID(bpr1ID).getBikeWithCode(b1);
            bikeB2 = brs.getProviderWithID(bpr1ID).getBikeWithCode(b2);
        }catch(Exception e)
        {
            assertTrue(false,"cannot find the provider something, setup fail");
        }
        bikeB1.setInStore(false);
        bikeB2.setInStore(false);

        //form an order
        List<EBikeType> bikesInvolved = new ArrayList<EBikeType>();
        bikesInvolved.add(EBikeType.MOUNTAIN);
        bikesInvolved.add(EBikeType.MOUNTAIN);

        //attempt to get a quote
        Quote q = null;
        try{
            q = brs.getProviderWithID(bpr1ID).createQuote(dates, bikesInvolved);
        }catch (Exception e)
        {
            assertTrue(false,"error in setup");
        }
        //we book the quote
        c.orderQuote(q, ECollectionMode.PICKUP);

        //we now find the reference to the booking
        Booking exampleBooking = null;
        try{
            exampleBooking = brs.getProviderWithID(bpr1ID).getBooking( c.getCurrentInvoices().get(0).orderCode );
        } catch(Exception e)
        {
            assertTrue(false,"error in setup");
        }

        //set the state of it to BIKES_AWAY
        exampleBooking.setBookingStatus(EBookingStatus.BIKES_AWAY);

        //start of the use case

        //the customer returns the bike to the system
        try{
            c.returnBikeToOriginalProvider(exampleBooking.getOrderCode());
        }catch (Exception e)
        {
            assertTrue(false,e.toString());
        }

        //check booking state
        assertEquals(EBookingStatus.RETURNED,exampleBooking.getStatus(),"booking status wasn't updated correctly");
        assertEquals(true,bikeB1.inStore(),"bike 1 was not changed correctly");
        assertEquals(true,bikeB1.inStore(),"bike 2 was not changed correctly");

    }
    
    @Test
    void returningBikeToPartnerProvider()
    {
        /*
            this test tests the use case of a customer returning a bike to its partner provider
        */

        //we set up the system first, and get booking to the BIKES_AWAY state

        //we attempt to find the providers in order to set up the partnership
        BikeProvider prov1 = null;
        BikeProvider prov2 = null;
        try{
            prov1 = brs.getProviderWithID(bpr1ID);
            prov2 = brs.getProviderWithID(bpr2ID);
        }catch(Exception e)
        {
            assertTrue(false,"error in setup");
        }

        //set up partnership
        prov1.addPartner(prov2);
        prov2.addPartner(prov1);

        //set up order
        DateRange dates = new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,3));
        //we set up the date to be the start day of the booking
        brs.setDate(LocalDate.of(2019,1,1));

        //bikes are away, so let's set their state appropriately
        Bike bikeB1 = null;
        Bike bikeB2 = null;
        try{
            bikeB1 = brs.getProviderWithID(bpr1ID).getBikeWithCode(b1);
            bikeB2 = brs.getProviderWithID(bpr1ID).getBikeWithCode(b2);
        }catch(Exception e)
        {
            assertTrue(false,"cannot find the provider something, setup fail");
        }
        bikeB1.setInStore(false);
        bikeB2.setInStore(false);

        //form an order
        List<EBikeType> bikesInvolved = new ArrayList<EBikeType>();
        bikesInvolved.add(EBikeType.MOUNTAIN);
        bikesInvolved.add(EBikeType.MOUNTAIN);

        //attempt to get a quote for it
        Quote q = null;
        try{
            q = brs.getProviderWithID(bpr1ID).createQuote(dates, bikesInvolved);
        }catch (Exception e)
        {
            assertTrue(false,"error in setup");
        }
        
        //book the quote
        c.orderQuote(q, ECollectionMode.PICKUP);

        //we now retrieve the booking
        Booking exampleBooking = null;
        try{
            exampleBooking = brs.getProviderWithID(bpr1ID).getBooking( c.getCurrentInvoices().get(0).orderCode );
        } catch(Exception e)
        {
            assertTrue(false,"error in setup");
        }

        //set the state to the expiry date of the booking
        exampleBooking.setBookingStatus(EBookingStatus.BIKES_AWAY);
        brs.setDate(LocalDate.of(2019,1,2));
        //reset deliveries
        resetDeliveryService();

        //Beggining of the use case

        //customer returns the bike
        try{
            c.returnBikeToPartnerProvider(exampleBooking.getOrderCode(), bpr2ID);
        } catch (Exception e)
        {
            assertTrue(false,e.toString());
        }

        //check state didn't change yet
        assertEquals(EBookingStatus.BIKES_AWAY,exampleBooking.getStatus(),"booking status wasn't updated correctly");
        assertEquals(false,bikeB1.inStore(),"bike 1 was not changed correctly");
        assertEquals(false,bikeB2.inStore(),"bike 2 was not changed correctly");

        //shop closes, and the delivery service does its job

        //perform pickups
        MockDeliveryService mds = (MockDeliveryService)DeliveryServiceFactory.getDeliveryService();
        mds.carryOutPickups(brs.getDate());

        //check state changed correctly
        assertEquals(EBookingStatus.DELIVERY_TO_PROVIDER,exampleBooking.getStatus(),"booking status wasn't updated correctly");
        assertEquals(false,bikeB1.inStore(),"bike 1 was not changed correctly");
        assertEquals(false,bikeB2.inStore(),"bike 2 was not changed correctly");
        //carry out dropoffs
        mds.carryOutDropoffs();

        //check state changed correctly
        assertEquals(EBookingStatus.RETURNED,exampleBooking.getStatus(),"booking status wasn't updated correctly");
        assertEquals(true,bikeB1.inStore(),"bike 1 was not changed correctly");
        assertEquals(true,bikeB2.inStore(),"bike 2 was not changed correctly");


    }
    ////////////////////////SIMULATION WITH DELIVERY SERVICE ALL USE CASES ////////////////////
    @Test
    void TestExtensionSubmodule()
    {
        /*
            we see if our extension submodule works correctly with our system
        */

        //change pricing policy to extension set up as in the coursework example

        PricingPolicy extensionPolicy = new MultidayRateDiscountPolicy(2, 6, 13, 5);

        BikeProvider bpr1 = null;
        try{
            bpr1 = brs.getProviderWithID(bpr1ID);
            bpr1.changePricingPolicy(extensionPolicy);

            extensionPolicy.setDailyRentalPrice(brs.getType(EBikeType.MOUNTAIN), new BigDecimal(10));
            extensionPolicy.setDailyRentalPrice(brs.getType(EBikeType.HYBRID), new BigDecimal(10));
        }catch (Exception e)
        {
            assertTrue(false, "exception when setting up");
        }

        //form simple order, which bike provider 1 can accomodate
        List<EBikeType> order = new ArrayList<EBikeType>();
        order.add(EBikeType.MOUNTAIN);	//daily price = 10
        order.add(EBikeType.MOUNTAIN);	//daily price = 10
        order.add(EBikeType.HYBRID);	//daily price = 10

        //set up different date ranges
        DateRange noDiscount = new DateRange(LocalDate.of(2019,1,1), LocalDate.of(2019,1,2)); //2 day  			-> T=>(2 * 30) 	Discount 	=> 0 * T	 Price=> 60
        DateRange smallDiscount = new DateRange(LocalDate.of(2019,1,1), LocalDate.of(2019,1,6)); //6 days 		-> T=>(6 * 30) 	Discount 	=> 0.05 * T	 Price=> 171
        DateRange mediumDiscount = new DateRange(LocalDate.of(2019,1,1), LocalDate.of(2019,1,13)); //13 days 	-> T=>(13 * 30) Discount 	=> 0.10 * T	 Price=> 351
        DateRange maxDiscount = new DateRange(LocalDate.of(2019,1,1), LocalDate.of(2019,1,14));	//14 days 		-> T=>(14 * 30)	Discount	=> 0.15 * T	 Price=> 357

        BigDecimal noDPrice = new BigDecimal(60.00).setScale(2,RoundingMode.HALF_UP).stripTrailingZeros();
        BigDecimal smallDPrice = new BigDecimal(171.00).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        BigDecimal mediumDPrice = new BigDecimal(351.00).setScale(2,RoundingMode.HALF_UP).stripTrailingZeros();
        BigDecimal maxDPrice = new BigDecimal(357.00).setScale(2,RoundingMode.HALF_UP).stripTrailingZeros();

        //check the quotes give correct values
        assertEquals(noDPrice,bpr1.createQuote(noDiscount, order).getPrice().stripTrailingZeros());
        assertEquals(smallDPrice,bpr1.createQuote(smallDiscount, order).getPrice().stripTrailingZeros());
        assertEquals(mediumDPrice,bpr1.createQuote(mediumDiscount, order).getPrice().stripTrailingZeros());
        assertEquals(maxDPrice,bpr1.createQuote(maxDiscount, order).getPrice().stripTrailingZeros());

    }
    @Test
    void allUseCasesWithDelivery()
    {
        /*
            this is the final test, we simulate a full operation on a single order, with all the use cases required
        */

        //we begin by setting up the partnership 
        BikeProvider bpr1 = null;
        BikeProvider bpr2 = null;
        try{
            bpr1 = brs.getProviderWithID(bpr1ID);
            bpr2 = brs.getProviderWithID(bpr2ID);
        }catch (Exception e)
        {
            assertTrue(false,"exception in setting up partnership");
        }
        bpr1.addPartner(bpr2);
        bpr2.addPartner(bpr1);

        //and set the day to the following date
        brs.setDate(LocalDate.of(2019,1,1));

        //BEGIN SIMULATION/////////////////
        //day 2019 1 1 -------------

        //form 1 mountain bike order
        List<EBikeType> order = new ArrayList<EBikeType>();
        order.add(EBikeType.MOUNTAIN);

        //the order will be delivered tomorrow, and expire the next day
        DateRange dates = new DateRange(LocalDate.of(2019,1,2),LocalDate.of(2019,1,3));

        //the customer asks for quotes for his order
        List<Quote> quotes = null;
        try{
            quotes = c.findQuotes(dates, order, cloc);
        }catch(Exception e)
        {
            assertTrue(false,"exception in finding quotes");
        }

        //there is only one bike in the quote
        Bike bike = quotes.get(0).getBikes().get(0);

        //we should expect a single offer from provider 1 for bike 1
        assertTrue(quotes.size() == 1,"too many quotes");

        //the customer decides to book the quote
        c.orderQuote(quotes.get(0),ECollectionMode.DELIVERY);

        //find the booking from the customers invoice
        Invoice invoice = c.getCurrentInvoices().get(0);
        Booking booking = null;
        try{
            booking = brs.findBooking(invoice.getOrderCode());
        }catch (Exception e)
        {
            assertTrue(false,"exception when finding booking: " + e.toString());
        }

        //check the initial status is correct
        assertEquals(EBookingStatus.BOOKED,booking.getStatus());

        //check bike is still in store state-wise
        assertEquals(true,bike.inStore(),"bike was not correctly updated after delivery");

        //now after the closing hours, the mock delivery driver picks up the bikes
        //there should be nothing to pickup or drop off
        MockDeliveryService ds = (MockDeliveryService)DeliveryServiceFactory.getDeliveryService();
        ds.carryOutPickups(brs.getDate());
        //then it drops off the bikes before the end of the day
        ds.carryOutDropoffs();
        // the day passes
        brs.stepDateForward();

        //day 2019 1 2 ------------- PICKUP DAY/delivery

        //check the date is correctly incremented
        assertTrue(brs.getDate().equals(LocalDate.of(2019,1,2)),"date wasn't incremented properly");

        //check booking is still the same status
        assertEquals(EBookingStatus.BOOKED,booking.getStatus());


        //no new customers


        //closing hours
        //delivery driver goes again

        //pickups happen
        ds.carryOutPickups(brs.getDate());

        //our mountain bike should be picked up
        //check the status
        assertEquals(EBookingStatus.DELIVERY_TO_CLIENT,booking.getStatus(),"booking wasn't updated properly after delivery");

        //then it drops off the bikes before the end of the day
        ds.carryOutDropoffs();

        //check our bike was delivered
        assertEquals(EBookingStatus.BIKES_AWAY,booking.getStatus(),"booking wasn't updated properly after delivery");
        assertEquals(false,bike.inStore(),"bike was not correctly updated after delivery");

        //the day passes

        //day 2019 1 3 ------------- EXPIRY DAY/ delivery from partner to provider

        brs.stepDateForward();

        //check date was incremented properly
        assertTrue(brs.getDate().equals(LocalDate.of(2019,1,3)),"date wasn't incremented properly");

        //the customer returns the bikes to partner
        try{
            c.returnBikeToPartnerProvider(invoice.orderCode,bpr2ID);
        } catch (Exception e)
        {
            assertTrue(false,"exception when returning bike");
        }

        //check the state has not changed, since the delivery hasnt come yet
        assertEquals(EBookingStatus.BIKES_AWAY,booking.getStatus(),"booking wasn't updated properly after delivery");
        //bikes are still not in the store
        assertEquals(false,bike.inStore(),"bike was not correctly updated after delivery");

        //closing hours

        //delivery happens
        ds.carryOutPickups(brs.getDate());
        //check status changed correctly
        //bike is picked up and now on way to original provider
        assertEquals(EBookingStatus.DELIVERY_TO_PROVIDER,booking.getStatus(),"booking wasn't updated properly after delivery");
        assertEquals(false,bike.inStore(),"bike was not correctly updated after delivery");

        //drop offs happen
        ds.carryOutDropoffs();
        //check the bike is returned to original provider and in store
        assertEquals(EBookingStatus.RETURNED,booking.getStatus(),"booking wasn't updated properly after delivery");
        assertEquals(true,bike.inStore(),"bike was not correctly updated after delivery");

    }

    void resetDeliveryService()
    {
        MockDeliveryService	msf = (MockDeliveryService)DeliveryServiceFactory.getDeliveryService();
        msf.pickups = new HashMap<LocalDate,Collection<Deliverable>>(); 
        msf.dropoffs = new ArrayDeque<Deliverable>();
    }

}