package uk.ac.ed.bikerental;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import uk.ac.ed.bikerental.Utils.EBikeType;
import uk.ac.ed.bikerental.Utils.EBookingStatus;
import uk.ac.ed.bikerental.Utils.QuoteInformation;

public class BikeProvider {

	private LinkedList<Booking> allBookings;
	private int providerID;
	private Location location;
	private LinkedList<Bike> bikes;
	private LinkedList<BikeProvider> partners;
	private float depositRate;
	private String phoneNumber;
	private String openingTimes;
	
	private HashMap<EBikeType, BigDecimal> typePrices;
	
	public BikeProvider(int pID, Location pLoc, float pRate, String pPhone, String pOpeningTimes) {
		allBookings = new LinkedList<Booking>();
		providerID = pID;
		location = pLoc;
		bikes = new LinkedList<Bike>();
		partners = new LinkedList<BikeProvider>();
		depositRate = pRate;
		phoneNumber = pPhone;
		openingTimes = pOpeningTimes;
	}
	
	public boolean canAccomodateRental(DateRange dr, Collection<EBikeType> expectedBikeTypes) {
		
		HashMap<EBikeType, Integer> remainingNumOfBikesPerType = new HashMap<EBikeType, Integer>();
		
		for (EBikeType type : EBikeType.values()) {
			remainingNumOfBikesPerType.put(type, 0);
		}
		
		////WRONG! WE NEED TO CHECK IF THE BIKE IS AVAILABLE IN THE DATE RANGE! HOW? ATTACH COLLECTION OF BOOKINGS TO BIKE OR SMTH?
		for (Bike b : bikes) {
			EBikeType type = b.getBikeType().getType();
			remainingNumOfBikesPerType.put(type, remainingNumOfBikesPerType.get(type) + 1);
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		for (EBikeType type : expectedBikeTypes) {
			remainingNumOfBikesPerType.put(type, remainingNumOfBikesPerType.get(type) - 1);
		}
		
		assert(false);
		return false;
		
	}
	
	public Quote createQuote(DateRange dr, Collection<EBikeType> pBikes) throws Exception {
		
		BigDecimal quotePrice = new BigDecimal(0);
		
		for (EBikeType bt : pBikes) {											// Polymorphism boiiiii
			if (!typePrices.containsKey(bt)) {
				throw new Exception("This provider has not set a price for the required bike type.");
			} else {
				quotePrice = quotePrice.add(typePrices.get(bt));
			}
			
		}
		
		// TODO: Compute Deposit, which should just be the depositRate times the replacement value of the type, which we will get from BikeRentalSystem
		
		BigDecimal deposit = new BigDecimal(0);
		
		assert(false);
		
		Quote q = new Quote(this, quotePrice, deposit, dr);
		
		return q;
		
	}
	
	public void setPriceForType(EBikeType type, BigDecimal price) {
		typePrices.put(type, price);
	}
	
	public Booking createBooking(Quote q, QuoteInformation qInfo) {
		
		LinkedList<Integer> bikeCodes = new LinkedList<Integer>();
		
		for (Bike b : q.getBikes()) {
			bikeCodes.addLast(b.code);
		}
		
		Booking booking = new Booking(q.getDeposit(), q.getPrice(), bikeCodes, q.getDates().getEnd(), this);
		
		return booking;
		
	}
	
	public void updateBooking(int orderCode, EBookingStatus newStatus) throws Exception{
		
		if (!allBookings.contains(orderCode)) {
			throw new Exception("This provider cannot update a booking it doesn't have.");
		} else {
			
		// To test our code, we might want to assert that every orderCode appears only once in our list. 
			
			for (Booking b : allBookings) {											
				if (b.getOrderCode() == orderCode) {
					b.setBookingStatus(newStatus);
					return;
				}
			}
		}
		
	}
	
	public LinkedList<BikeProvider> getPartners() {
		return partners;
	}
	
	public EBookingStatus findBookingStatus(int orderCode) throws Exception {
		if (!allBookings.contains(orderCode)) {
			throw new Exception("This provider cannot update a booking it doesn't have.");
		} else {
			
		// To test our code, we might want to assert that every orderCode appears only once in our list. 
			
			for (Booking b : allBookings) {											
				if (b.getOrderCode() == orderCode) {
					return b.getStatus();
				}
			}
		}
		
		assert(false);
		return null; // Our compiler doesn't understand why we don't actually have to add this. It doesn't hurt either.
	}
	
	public Location getLocation() { return location; }
	
}
