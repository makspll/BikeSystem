package uk.ac.ed.bikerental;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import uk.ac.ed.bikerental.Utils.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SystemTests {

	// TODO : include table that tells the marker where to find the tests for which use case
	
    //TODO tests for the use cases
	BikeRentalSystem brs;
	int bpr1ID,bpr2ID;
	int b1,b2,b3,b4,b5;
	Location cloc,loc,loc2,locFar;
	Customer c, c2;
	
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
		Location c2Loc = new Location("BQ36ZU", "123 Number Street");
		c2 = new Customer(brs, "Rob", "Beasley" , "0123/456789", c2Loc);
		
		Location loc = new Location("EH89QX", "5 Main Street");
		StandardPricingPolicy spp = new StandardPricingPolicy();
		StandardValuationPolicy svp = new StandardValuationPolicy(1f);
		bpr1ID = brs.registerProvider(loc, svp, spp);
		
		Location loc2 = new Location("EH89BL", "12 Side Street");
		bpr2ID = brs.registerProvider(loc2, svp, spp);
		
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
    void returningBike()
    {
        
    }
    
}
