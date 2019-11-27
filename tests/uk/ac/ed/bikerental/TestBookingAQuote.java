package uk.ac.ed.bikerental;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;

import uk.ac.ed.bikerental.Utils.EBikeType;
import uk.ac.ed.bikerental.Utils.EBookingStatus;
import uk.ac.ed.bikerental.Utils.ECollectionMode;
import uk.ac.ed.bikerental.Utils.ECondition;

public class TestBookingAQuote {

	BikeRentalSystem brs;
	Customer c;
	
	/*
	 * Using this file, we shall test the following use case:
	 * A customer wants to book a quote. Price/Deposit of the quote have
	 * already been computed, i.e. the quote has been generated. 
	 * Our tests will pass if the payment happens as it should,
	 * if the invoice for the quote is passed to the customer accordingly
	 * and if the requested bikes are available iff we would expect them to. 
	 */
  
	@BeforeEach
	void setUp() {
		DeliveryServiceFactory.setupMockDeliveryService();
		DeliveryService ds = DeliveryServiceFactory.getDeliveryService();
		brs = new BikeRentalSystem(ds, LocalDate.now());
		
		Location cLoc = new Location("EH12FJ", "79 Street Street");
		c = new Customer(brs, "Bob", "Bobertson" , "911" , cLoc);
		
		Location loc = new Location("EH89QX", "5 Main Street");
		StandardPricingPolicy spp = new StandardPricingPolicy();
		StandardValuationPolicy svp = new StandardValuationPolicy(1f);
		brs.registerProvider(loc, svp, spp);
		
		Location loc2 = new Location("EH89BL", "12 Side Street");
		brs.registerProvider(loc2, svp, spp);
		
		try {
			brs.registerBikeType(EBikeType.MOUNTAIN, new BigDecimal(500));
			brs.registerBikeType(EBikeType.HYBRID, new BigDecimal(700));
			brs.registerBikeType(EBikeType.ROAD, new BigDecimal(300));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when registering bike type");
		}
		
		try {
			brs.registerBike(brs.getType(EBikeType.MOUNTAIN),ECondition.BAD,  LocalDate.now().minusYears(5), 1);
			brs.registerBike(brs.getType(EBikeType.MOUNTAIN), ECondition.BAD, LocalDate.now().minusYears(5), 1);
			brs.registerBike(brs.getType(EBikeType.HYBRID), ECondition.GOOD, LocalDate.now().minusYears(1), 1);
			brs.registerBike(brs.getType(EBikeType.HYBRID), ECondition.NEW, LocalDate.now().minusDays(1), 2);
			brs.registerBike(brs.getType(EBikeType.ROAD), ECondition.NEW, LocalDate.now().minusDays(1), 2);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when registering bikes");
		}
		
	}
	
	@Test
	void testSimpleOrderGoesThrough() {
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(1).getBikeWithCode(1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		BigDecimal price = new BigDecimal(30);
		BigDecimal deposit = new BigDecimal(100);
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		Quote q = null;
		try {
			q = new Quote(brs.getProviderWithID(1) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		
		boolean success = c.orderQuote(q, ECollectionMode.PICKUP);
		
		assertTrue(success);
	}
	
	@Test
	void testSimpleOrderProducesBooking() {
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(1).getBikeWithCode(1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		BigDecimal price = new BigDecimal(30);
		BigDecimal deposit = new BigDecimal(100);
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		try {
			Quote q = new Quote(brs.getProviderWithID(1) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		
		try {
			assertEquals(EBookingStatus.BOOKED , brs.getProviderWithID(1).getBooking(1).getStatus());
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider or booking");
		}	// Checks if Booking is booked
		try {
			assertTrue(brs.getProviderWithID(1).getBooking(1).getDates().getStart().equals(today));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider or booking");
		}		// Checks if start date is as expected
	}
	
	@Test
	void testSimpleOrderAddsInvoice() {
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(1).getBikeWithCode(1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		BigDecimal price = new BigDecimal(30);
		BigDecimal deposit = new BigDecimal(100);
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		try {
			Quote q = new Quote(brs.getProviderWithID(1) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		
		assertEquals(1 , c.getCurrentInvoices().get(0).getOrderCode());
	}
	
	// Here, we have two quotes involving the same bike
	@Test
	void testTwoEquivalentOrdersGoThrough() {
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(1).getBikeWithCode(1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.now().minusYears(5)));
		
		BigDecimal price = new BigDecimal(30);
		BigDecimal deposit = new BigDecimal(100);
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);
		
		Quote q1 = null;
		try {
			q1 = new Quote(brs.getProviderWithID(1) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}

		boolean success1 = c.orderQuote(q1, ECollectionMode.PICKUP);
		
		assertTrue(success1, "The first quote could not be ordered");
		
		Quote q2 = null;
		try {
			q2 = new Quote(brs.getProviderWithID(1) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		
		boolean success2 = c.orderQuote(q2, ECollectionMode.PICKUP);
		
		assertTrue(success2, "The second quote could not be ordered");
	}
	
	@Test
	void testEquivalentOrdersProduceBookings() {
		LinkedList<Bike> oneBike = new LinkedList<Bike>();
		try {
			oneBike.add(brs.getProviderWithID(1).getBikeWithCode(1));
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when adding bike to collection");
		}
		assert(oneBike.get(0).getManufactureDate().equals(LocalDate.MIN));
		
		BigDecimal price = new BigDecimal(30);
		BigDecimal deposit = new BigDecimal(100);
		
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(3);
		DateRange dr = new DateRange(today, soon);

		Quote q1 = null;
		try {
			q1 = new Quote(brs.getProviderWithID(1) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}

		boolean success1 = c.orderQuote(q1, ECollectionMode.PICKUP);
		
		assertTrue(success1, "The first quote could not be ordered");
		
		Quote q2 = null;
		try {
			q2 = new Quote(brs.getProviderWithID(1) , price, deposit, oneBike, dr);
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider");
		}
		
		try {
			assertEquals(EBookingStatus.BOOKED , brs.getProviderWithID(1).getBooking(1).getStatus());
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider or booking");
		}	// Checks if 1st Booking is booked
		try {
			assertTrue(brs.getProviderWithID(1).getBooking(1).getDates().getStart().equals(today) , "The bookings got mixed up, or the information for booking 1 got altered");
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider or booking");
		}
		try {
			assertEquals(EBookingStatus.BOOKED , brs.getProviderWithID(1).getBooking(2).getStatus());
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider or booking");
		}	// Checks if 2nd Booking is booked
		try {
			assertTrue(brs.getProviderWithID(1).getBooking(2).getDates().getStart().equals(today) , "The bookings got mixed up, or the information for booking 1 got altered");
		} catch (Exception e) {
			assertTrue(false, "Exception occurred when getting provider or booking");
		}		
	}
	
}
