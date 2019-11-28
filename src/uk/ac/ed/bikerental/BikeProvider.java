package uk.ac.ed.bikerental;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ed.bikerental.Utils.EBikeType;
import uk.ac.ed.bikerental.Utils.EBookingStatus;

public class BikeProvider {

	private List<Booking> allBookings;
	private int providerID;
	private Location location;
	private List<Bike> bikes;
	private List<BikeProvider> partners;
	private PricingPolicy pPolicy;
	private ValuationPolicy vPolicy;
	private BikeRentalSystem system;
	
	private static int UNIQUE_CODE_COUNT = 0;

	///constructors
	public BikeProvider(BikeRentalSystem brs, Location pLoc, ValuationPolicy vPol, PricingPolicy pPol) {
		system = brs;
		allBookings = new ArrayList<Booking>();
		providerID = ++UNIQUE_CODE_COUNT;
		location = pLoc;
		bikes = new ArrayList<Bike>();
		partners = new ArrayList<BikeProvider>();
		pPolicy = pPol;
		vPolicy = vPol;
	}
	
	///getters setters

	public void addPartner(BikeProvider partner)
	{
		partners.add(partner);
	}
	public static int getIDCounter()
	{
		return UNIQUE_CODE_COUNT;
	}
	
	public List<BikeProvider> getPartners() {
		return partners;
	}
	
	public PricingPolicy getPricingPolicy() {
		return this.pPolicy;
	}
	
	public ValuationPolicy getValuationPolicy() {
		return this.vPolicy;
	}
	
	public Booking getBooking(int orderNo) throws Exception
	{
		for(Booking b : allBookings)
		{
			if(b.getOrderCode() == orderNo)
			{
				return b;
			}
		}

		throw new Exception("No booking with that code was found");
	}

	public List<Bike> getBikesFromBooking(int orderNo) throws Exception
	{

		Booking b;
		try{
			b = getBooking(orderNo);
		}catch(Exception e){
			throw new Exception("no such booking");
		}
		List<Bike> bikes = new ArrayList<Bike>();
		assert(b.getBikeCodes().size() > 0);
		for(int bikeCode : b.getBikeCodes())
		{
			for(Bike bike : bikes)
			{
				if(bike.getCode() == bikeCode)
				{
					bikes.add(bike);
				}
			}
		}

		return bikes;
	}
	
	public Bike getBikeWithCode(int code) throws Exception {
		
		for (Bike b : this.bikes) {
			if (b.getCode() == code) return b;
		}
		
		throw new Exception("Bike with ID not found");
		
	}

	public Location getLocation() { return location; }
	public int getId(){return providerID;}

	///public functionality
	public boolean canAccommodateRental(DateRange dr, Collection<EBikeType> expectedBikeTypes) {
		
		HashMap<EBikeType, Integer> remainingNumOfBikesPerType = new HashMap<EBikeType, Integer>();
		int totalBikesNeeded = expectedBikeTypes.size();

		//we count how many of each bike type we need to find
		for(EBikeType bike : expectedBikeTypes)
		{
			if(remainingNumOfBikesPerType.containsKey(bike))
			{
				Integer currNumber = remainingNumOfBikesPerType.get(bike);
				remainingNumOfBikesPerType.put(bike,currNumber+1);
			}
			else
			{
				remainingNumOfBikesPerType.put(bike,1);
			}
		}
		
		for(Bike b : bikes)
		{
			EBikeType currBikeEBikeType = b.getBikeType().getType();
			Integer currNumber = remainingNumOfBikesPerType.get(currBikeEBikeType);
			boolean needMoreOfThisType;
			if (currNumber != null) needMoreOfThisType = currNumber > 0;
			else needMoreOfThisType = false;
			
			if(needMoreOfThisType && b.isAvailable(dr))
			{
				//reduce the needed amount by 1
				remainingNumOfBikesPerType.put(currBikeEBikeType,currNumber - 1);

				//if we found all the bikes types we need, we can accommodate the rental
				if(--totalBikesNeeded == 0){return true;}

			}
		}

		//we haven't found all the required bikes and exhausted the list of bikes
		return false;
		
	}
	
	public Quote createQuote(DateRange dr, Collection<EBikeType> pBikes) {
		
		// yes, we assume that we can accommodate the rental, as the bike system checks that before
		assertTrue(this.canAccommodateRental(dr, pBikes) , "Rental cannot be accommodated");

		//we are doing this on a first come first served basis, first bikes that appear in the list are preferred over the later ones
		//since the list is sorted by bike price, then this will yield the cheapest set of bikes (individually and not as a group)

		//so let's choose our finest bikes 
		ArrayList<Bike> bikesInTheQuote = new ArrayList<Bike>();
		BigDecimal deposit = new BigDecimal(0);
		
		//for each bike type requested
		outerloop:
		for (EBikeType type : pBikes) {
			//test if for some type we can't supply
			boolean haveThisType = false;
			//go through all bikes
			for (Bike bike : bikes) {
				if(bike.getBikeType().getType().equals(type))
				{
					haveThisType = true;
				}
				//pick the available ones with the type we need, and which we haven't already picked
				if (bike.isAvailable(dr) && bike.getBikeType().getType().equals(type) && !bikesInTheQuote.contains(bike)) {
					bikesInTheQuote.add(bike);
					deposit = deposit.add(vPolicy.calculateValue(bike, bike.getManufactureDate()));
	
					//stop when we found enough
					if(bikesInTheQuote.size() == pBikes.size()){break outerloop;}
					//also stop looking for bikes for each type when we found one already
					break;
				}
			}
			assertTrue(haveThisType,"error in creating quote, we were supposed to have this type of bike");
		}

		//we better have found enough, this is a double sanity check
		assert(pBikes.size() == bikesInTheQuote.size());
		
		//complete the rest of the quote
		BigDecimal quotePrice = pPolicy.calculatePrice(bikesInTheQuote, dr);
		
		Quote q = new Quote(this, quotePrice, deposit, bikesInTheQuote, dr);
		
		return q;
		
	}
	
	public Booking createBooking(Quote q, QuoteInformation qInfo) {
		
		//just read off the given values to create the booking, then register it onto the system
		List<Integer> bikeCodes = new ArrayList<Integer>();
		
		for (Bike b : q.getBikes()) {
			bikeCodes.add(b.getCode());
		}
		
		Booking booking = new Booking(q.getDeposit(), q.getPrice(), bikeCodes, q.getDates(), providerID, qInfo.collectionMode);
		
		//we now add the booking to the main list
		allBookings.add(booking);

		//we also carry the references to the bikes involved in the booking, so they have a reference to it, and can update it on deliveries
		for(Bike b : q.getBikes())
		{
			b.addBooking(booking);
		}

		return booking;
	}
	
	public void updateBooking(int orderCode, EBookingStatus newStatus) throws Exception{
		
		for (Booking b : allBookings) {											
			if (b.getOrderCode() == orderCode) {
				b.setBookingStatus(newStatus);

				//we retain bookings which are now complete, and whose bikes are returned
				//however we have to update the bikes which have those bookings, so the state of the system remains valid
				if(newStatus == EBookingStatus.RETURNED)
				{
					for(Bike bike : bikes)
					{
						if(bike.containsBooking(b))
						{
							bike.removeBooking(b);
							bike.setInStore(true);
						}
					}
				}
				return;
			}
		}

		//if we haven't found this booking, we cannot update it
		throw new Exception("This provider cannot update a booking it doesn't have.");
	}
	
	//the bike must be registered with the system first
	public void addBike(Bike b)
	{	
		bikes.add(b);
		
		// We sort the bikes in our bike collection by price. This needs to be done every time a bike is added, 
		// in order to keep our sorting up-to-date.
		LinkedList<Bike> bike1 = new LinkedList<Bike>();
		LinkedList<Bike> bike2 = new LinkedList<Bike>();
		PricingPolicy pp = this.pPolicy;
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		DateRange dr = new DateRange(today, tomorrow);
		
		Collections.sort(this.bikes, new Comparator<Bike>() {
			  @Override
			  public int compare(Bike b1, Bike b2) {
				  bike1.add(b1);
				  bike2.add(b2);
				  BigDecimal price1 = pp.calculatePrice(bike1, dr);
				  BigDecimal price2 = pp.calculatePrice(bike2, dr);
				  return price1.compareTo(price2);
			  }
			});
		
	}

	public List<Bike> getBikes()
	{
		return bikes;
	}
	
	public EBookingStatus findBookingStatus(int orderCode) throws Exception {

		// To test our code, we might want to assert that every orderCode appears only once in our list. 
			
		for (Booking b : allBookings) {											
			if (b.getOrderCode() == orderCode) {
				return b.getStatus();
			}
		}
	
		throw new Exception("This provider cannot update a booking it doesn't have.");
		//ya gotta throw an exception, only then the compiler knows this shouldn't be reached
	}
	
	boolean containsBooking(Booking b)
	{
		return allBookings.contains(b);
	}

	boolean containsBooking(int orderNo)
	{
		for(Booking b : allBookings)
		{
			if(b.getOrderCode() == orderNo){
				return true;
			}
		}
		return false;
	}

	void changePricingPolicy(PricingPolicy newPolicy)
	{
		pPolicy = newPolicy;
	}
	void changeValuationPolicy(ValuationPolicy newPolicy)
	{
		vPolicy = newPolicy;
	}
	
	///private parts

}