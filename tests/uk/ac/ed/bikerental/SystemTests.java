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
    void findingAQuoteSimpleScenarios()
    {
		//test with all available bikes, we should have all dates free

		//testing quote for mountain and Hybrid bike over 4 days at location with 2 bike providers
		List<EBikeType> bikes = new ArrayList<EBikeType>();
		bikes.add(EBikeType.MOUNTAIN);
		bikes.add(EBikeType.HYBRID);
		DateRange dateRange =new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,2)); 

		List<Quote> quotes = null;
		//we shoold get a single quote for our simple order since only one suplier has a hybrid and mountain bike
		try{
			quotes = c.findQuotes(dateRange, bikes, loc);
		}catch (Exception e)
		{
			assertTrue(false,"system throws an exception when it shouldn't");
		}

		assertEquals(1,quotes.size(),"system doesn't return any quotes through customer");

		//now check the quote is what we expect
		//we should get a quote from provider 1, with the 2 cheapest bikes so b1/b2 and b3
		assertEquals(quotes.get(0).getProvider().getId(),bpr1ID,"quote returned is from wrong provider");

		boolean b1Found = false;
		boolean b3Found = false;
		boolean nonRequestedTypeReturned = false;
		EBikeType nonRequested = null;
 		for(Bike bike : quotes.get(0).getBikes())
		{
			if(bike.getCode() == b1 || bike.getCode() == b2)
			{
				b1Found = true;
			}
			else if(bike.getCode() == b3)
			{
				b3Found = true;
			}
			EBikeType currBikeType = bike.getBikeType().getType();
			if(currBikeType != EBikeType.MOUNTAIN && currBikeType != EBikeType.HYBRID)
			{
				nonRequestedTypeReturned = true;
				nonRequested = currBikeType;
			}
		}
		

		assertFalse(nonRequestedTypeReturned,"a bike with different type than requested was in the quote:" + nonRequested);
		assertTrue(b1Found,"bike 1 was not in the returned quote");
		assertTrue(b3Found,"bike 3 was not in the returned quote: " + quotes.get(0).getBikes().get(0).getBikeType().getType() +  quotes.get(0).getBikes().get(1).getBikeType().getType());
	}

	@Test
	void testFindingAQuoteFail()
	{
		/*
			tests what happens when all the bikes required are booked or unavailable at given dates
		*/

		//let's try to make an order for 3 hybrid bikes, which should not give us any quotes
		List<EBikeType> order = new ArrayList<EBikeType>();
		order.add(EBikeType.HYBRID);
		order.add(EBikeType.HYBRID);
		order.add(EBikeType.HYBRID);

		//attempt to get a quote
		List<Quote> quotes = null;
		try{
			quotes = c.findQuotes(new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,2)),order, loc);
		}catch (Exception e)
		{
			assertTrue(false,"exception when looking for quotes");
		}

		//we should not receive any quotes!
		assertEquals(0,quotes.size(),"quotes returned when they shouldn't be");

		//let's try another order, this time for one bike on a range that is booked already
		//let's book all our mountain bikes

		//we create a booking with 2 mountain bikes b1 and b2
		DateRange dates = new DateRange(LocalDate.of(2019, 1, 1),LocalDate.of(2019,1,2));
		List<Integer> inOrder = new ArrayList<Integer>();
		inOrder.add(b1);
		inOrder.add(b2);
		Booking booking1 = new Booking(BigDecimal.ZERO, BigDecimal.ZERO,inOrder, dates, bpr1ID,ECollectionMode.DELIVERY);
		try{
			brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).addBooking(booking1);
			brs.getProviderWithID(bpr1ID).getBikeWithCode(b2).addBooking(booking1);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider by id");
		}
		
		//now create an order for a mountain bike
		List<EBikeType> mountainBikeOrder = new ArrayList<EBikeType>();
		mountainBikeOrder.add(EBikeType.MOUNTAIN);

		//attempt to get a quote for the order
		try{
			quotes = c.findQuotes(dates,mountainBikeOrder, loc);
		}catch (Exception e)
		{
			assertTrue(false,"exception when looking for quotes");
		}

		//we should not get any since all of our mountain bikes are booked already at the given dates
		assertEquals(0,quotes.size(),"found quotes in second order when it shouldn't have");
		
	}

	@Test
	void testFindingQuoteNoProviders()
	{
		/*
			this tests what happens when we try to find a quote at a location without any registered bike providers
		*/

		//form an order for a mountain bike
		List<EBikeType> order = new ArrayList<EBikeType>();
		order.add(EBikeType.MOUNTAIN);
		//at a location without any provider
		Location noBikeZone = new Location("JAB123","42 wise st");

		//attempt to get a quote
		List<Quote> quotes = null;
		try{
			quotes = c.findQuotes(new DateRange(LocalDate.of(2019,1,1),LocalDate.of(2019,1,2)),order, noBikeZone);
		}catch (Exception e)
		{
			assertTrue(false,"exception when looking for quotes");
		}

		//should not receive any quotes
		assertEquals(0,quotes.size(),"found quotes even though providers arent near enough");
	}
    
    ////////////////////START TESTS FOR BOOKING A QUOTE//////////////////////////
    @Test
	void testSimpleOrderGoesThrough() {
		////////////////////////Setting up the quote/////////////////////////////
		LinkedList<Bike> oneBike = new LinkedList<Bike>();

		try {
			oneBike.add(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		BikeProvider bpr1 = null;
		try{
			bpr1 = brs.getProviderWithID(bpr1ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		
		BigDecimal price   = bpr1.getPricingPolicy().calculatePrice(oneBike, dr);
		BigDecimal deposit = null;
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1), 
																		  brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}

		Quote q = new Quote(bpr1 , price, deposit, oneBike, dr);
		////////////////////////Setting up the quote/////////////////////////////
		
		boolean success = c.orderQuote(q, ECollectionMode.PICKUP);
		
		assertTrue(success , "The booking was not successful");
	}
	
	@Test
	void testSimpleOrderProducesBooking() {
		////////////////////////Setting up the quote/////////////////////////////
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		BikeProvider bpr1 = null;
		try{
			bpr1 = brs.getProviderWithID(bpr1ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		
		BigDecimal price   = bpr1.getPricingPolicy().calculatePrice(oneBike, dr);
		BigDecimal deposit = null;
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1), 
																		  brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}
		
		Quote q = null;
		try {
			q = new Quote(brs.getProviderWithID(bpr1ID) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		////////////////////////Setting up the quote/////////////////////////////

		c.orderQuote(q, ECollectionMode.DELIVERY);

		int bookingNo = c.getCurrentInvoices().get(0).getOrderCode();
		
		try {
			// Checks if Booking is booked
			assertEquals(EBookingStatus.BOOKED , brs.getProviderWithID(bpr1ID).getBooking(bookingNo).getStatus());
		} catch(Exception e) {
			assertTrue(false, "Exception occurred when getting provider or booking");

		try {
			// Checks if start date is as expected
			assertTrue(brs.getProviderWithID(bpr1ID).getBooking(bookingNo).getDates().getStart().equals(today));
		} catch (Exception e2) {
			assertTrue(false, "Exception occurred when getting provider or booking");
		}
		}	
	}
	
	@Test
	void testSimpleOrderAddsInvoice() {
		////////////////////////Setting up the quote/////////////////////////////
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		BikeProvider bpr1 = null;
		try{
			bpr1 = brs.getProviderWithID(bpr1ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		
		BigDecimal price   = bpr1.getPricingPolicy().calculatePrice(oneBike, dr);
		BigDecimal deposit = null;
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1), 
																		  brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}
		
		Quote q = null;
		try {
			q = new Quote(brs.getProviderWithID(bpr1ID) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		////////////////////////Setting up the quote/////////////////////////////
		
		assertTrue(c.orderQuote(q, ECollectionMode.DELIVERY) , "Ordering the quote failed.");
		
		System.out.println(c.getCurrentInvoices().size());
		try {
			assertEquals(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).getCode() , 
							c.getCurrentInvoices().get(0).getBikeCodes().get(0));
		} catch (Exception e) {
			assertTrue(false , "An error occurred when getting the bike/provider with the appropriate ID");
		}
	}
	
	@Test
	void testDeliveryIsScheduled() {
		////////////////////////Setting up the quote/////////////////////////////
		LinkedList<Bike> oneBike = new LinkedList<Bike>();

		try {
			oneBike.add(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		BikeProvider bpr1 = null;
		try{
			bpr1 = brs.getProviderWithID(bpr1ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		
		BigDecimal price   = bpr1.getPricingPolicy().calculatePrice(oneBike, dr);
		BigDecimal deposit = null;
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1), 
																		  brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}

		Quote q = new Quote(bpr1 , price, deposit, oneBike, dr);
		////////////////////////Setting up the quote/////////////////////////////
		
		assertTrue(c.orderQuote(q, ECollectionMode.DELIVERY) , "Ordering the quote failed.");
		
		assertEquals(oneBike.get(0).getCode(),
						((Bike)  ((MockDeliveryService) DeliveryServiceFactory.getDeliveryService())
								.getPickupsOn(q.getDates().getStart()).toArray()[0])
									.getCode() , "Delivery failed to be added");
		
	}
	
	@Test
	void testDeliveryDoesNotOccurWhenPickedUp( ) {
		////////////////////////Setting up the quote/////////////////////////////
		LinkedList<Bike> oneBike = new LinkedList<Bike>();

		try {
			oneBike.add(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		BikeProvider bpr1 = null;
		try{
			bpr1 = brs.getProviderWithID(bpr1ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		
		BigDecimal price   = bpr1.getPricingPolicy().calculatePrice(oneBike, dr);
		BigDecimal deposit = null;
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1), 
																		  brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}

		Quote q = new Quote(bpr1 , price, deposit, oneBike, dr);
		////////////////////////Setting up the quote/////////////////////////////
		
		assertTrue(c.orderQuote(q, ECollectionMode.PICKUP) , "Ordering the quote failed.");
		
		assertEquals(0 , ((MockDeliveryService) DeliveryServiceFactory.getDeliveryService()).pickups.keySet().size() , "Delivery was falsely added");
		
	}
	
	// We also want a valid order to go through if it contains more than one bike of the same provider. 
	@Test
	void testDoubleOrderGoesThrough() {
		////////////////////////Setting up the quote/////////////////////////////
		LinkedList<Bike> twoBikes = new LinkedList<Bike>();

		try {
			twoBikes.add(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1));
			twoBikes.add(brs.getProviderWithID(bpr1ID).getBikeWithCode(b3));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(twoBikes.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		BikeProvider bpr1 = null;
		try{
			bpr1 = brs.getProviderWithID(bpr1ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		
		BigDecimal price   = bpr1.getPricingPolicy().calculatePrice(twoBikes, dr);
		BigDecimal deposit = null;
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1), 
					  brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).getManufactureDate());
			deposit = deposit.add(bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr1ID).getBikeWithCode(b2), 
					  brs.getProviderWithID(bpr1ID).getBikeWithCode(b2).getManufactureDate()));
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}

		Quote q = new Quote(bpr1 , price, deposit, twoBikes, dr);
		////////////////////////Setting up the quote/////////////////////////////
		
		boolean success = c.orderQuote(q, ECollectionMode.PICKUP);
		
		assertTrue(success , "The booking was not successful");
	}
	
	@Test
	void testTwoCustomersCanOrderQuotes() {
		////////////////////////Setting up the first quote/////////////////////////////
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		BikeProvider bpr1 = null;
		try{
			bpr1 = brs.getProviderWithID(bpr1ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		
		BigDecimal price   = bpr1.getPricingPolicy().calculatePrice(oneBike, dr);
		BigDecimal deposit = null;
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1), 
																		  brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}
		
		Quote q1 = null;
		try {
			q1 = new Quote(brs.getProviderWithID(bpr1ID) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		////////////////////////Setting up the first quote/////////////////////////////
		
		boolean success1 = c.orderQuote(q1, ECollectionMode.PICKUP);
		assertTrue(success1, "The first quote could not be ordered");
		
		////////////////////////Setting up the second quote/////////////////////////////
		LinkedList<Bike> otherBike = new LinkedList<Bike>();
		try {
			otherBike.add(brs.getProviderWithID(bpr2ID).getBikeWithCode(b5));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		
		BikeProvider bpr2 = null;
		try {
			bpr2 = brs.getProviderWithID(bpr2ID);
		} catch (Exception e1) {
			assertTrue(false, "Failed retrieving the second bike provider");
		}
		
		price = bpr2.getPricingPolicy().calculatePrice(otherBike, dr);
		try {
			deposit = bpr2.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr2ID).getBikeWithCode(b5), 
							 brs.getProviderWithID(bpr2ID).getBikeWithCode(b5).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}
		
		Quote q2 = null;
		try {
			q2 = new Quote(bpr2 , price, deposit, otherBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		////////////////////////Setting up the second quote/////////////////////////////
		
		boolean success2 = c2.orderQuote(q2, ECollectionMode.DELIVERY);
		assertTrue(success2, "Quote of second customer could not be booked");
		
	}
	
	// Here, we have two quotes involving the same bike. The quote should go through 
	// since the 1st provider has 2 equivalent bikes of the same type / age / condition.
	@Test
	void testTwoEquivalentOrdersGoThrough() {
		////////////////////////Setting up the first quote/////////////////////////////
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		BikeProvider bpr1 = null;
		try{
			bpr1 = brs.getProviderWithID(bpr1ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		
		BigDecimal price   = bpr1.getPricingPolicy().calculatePrice(oneBike, dr);
		BigDecimal deposit = null;
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1), 
																		  brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}
		
		Quote q1 = null;
		try {
			q1 = new Quote(brs.getProviderWithID(bpr1ID) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		////////////////////////Setting up the first quote/////////////////////////////

		boolean success1 = c.orderQuote(q1, ECollectionMode.PICKUP);
		
		assertTrue(success1, "The first quote could not be ordered");
		
		////////////////////////Setting up the second quote/////////////////////////////
		Quote q2 = null;
		try {
			q2 = new Quote(brs.getProviderWithID(bpr1ID) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		////////////////////////Setting up the first quote/////////////////////////////
		
		boolean success2 = c.orderQuote(q2, ECollectionMode.PICKUP);
		
		assertTrue(success2, "The second quote could not be ordered");
	}
	
	@Test
	void testEquivalentOrdersProduceBookings() {
		////////////////////////Setting up the first quote/////////////////////////////
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		BikeProvider bpr1 = null;
		try{
			bpr1 = brs.getProviderWithID(bpr1ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		
		BigDecimal price   = bpr1.getPricingPolicy().calculatePrice(oneBike, dr);
		BigDecimal deposit = null;
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1), 
																		  brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}
		
		Quote q1 = null;
		try {
			q1 = new Quote(brs.getProviderWithID(bpr1ID) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		////////////////////////Setting up the first quote/////////////////////////////

		boolean success1 = c.orderQuote(q1, ECollectionMode.PICKUP);
		
		assertTrue(success1, "The first quote could not be ordered");
		
		////////////////////////Setting up the second quote/////////////////////////////
		Quote q2 = null;
		try {
			q2 = new Quote(brs.getProviderWithID(bpr1ID) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		////////////////////////Setting up the first quote/////////////////////////////
		
		int bookingNo = c.getCurrentInvoices().get(0).getOrderCode();
		
		try {
			assertEquals(EBookingStatus.BOOKED , brs.getProviderWithID(bpr1ID).getBooking(bookingNo).getStatus());
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider or booking");
		}	// Checks if 1st Booking is booked
		try {
			assertTrue(brs.getProviderWithID(bpr1ID).getBooking(bookingNo).getDates().getStart().equals(today) , "The bookings got mixed up, or the information for booking 1 got altered");
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider or booking");
		}
		try {
			assertEquals(EBookingStatus.BOOKED , brs.getProviderWithID(bpr1ID).getBooking(bookingNo).getStatus());
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider or booking");
		}	// Checks if 2nd Booking is booked
		try {
			assertTrue(brs.getProviderWithID(bpr1ID).getBooking(bookingNo).getDates().getStart().equals(today) , "The bookings got mixed up, or the information for booking 1 got altered");
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider or booking");
		}		
	}
	
	@Test
	void testOrderFailsIfBikeTypeIsNotAvailable() {
		////////////////////////Setting up the quote/////////////////////////////
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		BikeProvider bpr1 = null;
		try{
			bpr1 = brs.getProviderWithID(bpr1ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		
		BigDecimal price   = bpr1.getPricingPolicy().calculatePrice(oneBike, dr);
		BigDecimal deposit = null;
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr1ID).getBikeWithCode(b1), 
																		  brs.getProviderWithID(bpr1ID).getBikeWithCode(b1).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}
		////////////////////////Setting up the quote/////////////////////////////
		
		Quote q = null;
		try {
			q = new Quote(brs.getProviderWithID(bpr2ID) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		
		boolean success = c.orderQuote(q, ECollectionMode.PICKUP);
		assertFalse(success , "The order goes through even though the 2nd provider doesn't offer mountain bikes");
		
		assertTrue(c.getCurrentInvoices().size() == 0 , "An invoice was added when it shouldn't have");
	}
	
	@Test
	void testOrderFailsIfNotEnoughBikesCanBeProvided() {
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(bpr2ID).getBikeWithCode(b4));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		
		LinkedList<Bike> otherBike = new LinkedList<Bike>();
		try {
			otherBike.add(brs.getProviderWithID(bpr2ID).getBikeWithCode(b5));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		BikeProvider bpr1 = null;
		try{
			bpr1 = brs.getProviderWithID(bpr1ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		BikeProvider bpr2 = null;
		try{
			bpr2 = brs.getProviderWithID(bpr2ID);
		}catch(Exception e)
		{
			assertTrue(false,"exception when getting provider");
		}
		
		BigDecimal price   = bpr1.getPricingPolicy().calculatePrice(oneBike, dr);
		BigDecimal deposit = null;
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr2ID).getBikeWithCode(b4), 
							 brs.getProviderWithID(bpr2ID).getBikeWithCode(b4).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}
		
		Quote q1 = null;
		try {
			q1 = new Quote(brs.getProviderWithID(bpr1ID) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}

		boolean success1 = c.orderQuote(q1, ECollectionMode.PICKUP);
		
		assertTrue(success1, "The first quote could not be ordered even though the provider has one suitable bike");
		
		price = bpr1.getPricingPolicy().calculatePrice(otherBike, dr);
		try {
			deposit = bpr1.getValuationPolicy().calculateValue(brs.getProviderWithID(bpr2ID).getBikeWithCode(b5), 
							 brs.getProviderWithID(bpr2ID).getBikeWithCode(b5).getManufactureDate());
		} catch (Exception e) {
			assertTrue(false, "An error occurred when getting the bike with the appropriate ID");
		}
		
		Quote q2 = null;
		try {
			q2 = new Quote(brs.getProviderWithID(bpr1ID) , price, deposit, otherBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		
		boolean success2 = c.orderQuote(q2, ECollectionMode.DELIVERY);
		
		assertFalse(success2, "The second quote was ordered, even though the 1st provider only has one bike of the needed type");
	}
	/////////////////////////END TESTS FOR BOOKING A QUOTE/////////////////////

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
